package org.openkilda.floodlight.spike;

import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.util.OFMessageUtils;
import org.projectfloodlight.openflow.protocol.*;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.U64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class FlowVerificationManager {
    private static final Logger logger = LoggerFactory.getLogger(FlowVerificationManager.class);

    private long COOKIE_MARKER = 0x1100_0000_0000_0000L;
    private int FLOW_IN_PORT = 4;
    private int FLOW_OUT_PORT = 8;

    private final SwitchUtils switchUtils;

    private final LinkedList<SwitchPortLink> flowChain = new LinkedList<>();
    private final Set<DatapathId> pendingSwitches = new HashSet<>();
    private final Set<DatapathId> switchesOfInterest = new HashSet<>();

    private final List<PendingOfMessage> pendingOfMod = new LinkedList<>();

    State state = State.INIT;
    private final LinkedList<State> stateRecovery = new LinkedList<>();

    public FlowVerificationManager(IOFSwitchService switchService) {
        this.switchUtils = new SwitchUtils(switchService);

        flowChain.addLast(
                new SwitchPortLink(DatapathId.of(0x00000000_00000001), FLOW_IN_PORT, 1));
        flowChain.addLast(
                new SwitchPortLink(DatapathId.of(0x00000000_00000002), 2, 1));
        flowChain.addLast(
                new SwitchPortLink(DatapathId.of(0x00000000_00000003), 1, FLOW_OUT_PORT));

        for (SwitchPortLink link : flowChain) {
            switchesOfInterest.add(link.getDpId());
            pendingSwitches.add(link.getDpId());
        }
    }

    public void signalHandler(Signal signal) {
        logger.debug("{}: incoming signal - {}", state, signal);
        boolean unhandled = false;
        boolean silentHandling = true;

        if (signal instanceof OFMessageSignal) {
            // signalOFMessage have handled this event
        } else if (signal instanceof SwitchAddSignal) {
            pendingSwitches.remove(((SwitchAddSignal) signal).getDpId());
        } else if (signal instanceof SwitchRemoveSignal) {
            doSwitchRemove((SwitchRemoveSignal) signal);
        } else {
            silentHandling = false;
        }

        switch (state) {
            case INIT:
            case SWITCH_COMMUNICATION_PROBLEM:
                if (pendingSwitches.size() == 0) {
                    stateTransition(State.PREPARE_OF_RULES);
                }
                break;

            case WAIT_RULES_INSTALLATION:
                if (pendingOfMod.size() == 0) {
                    stateTransition();
                } else {
                    logger.debug("wait for response for %d rules");
                }
                break;

            case SEND_PACKAGE:
                if (signal instanceof SwitchRemoveSignal) {
                    doSwitchRemove((SwitchRemoveSignal) signal);
                } else {
                    unhandled = true;
                }

                if (0 < pendingSwitches.size()) {
                    stateTransition(State.STOP);
                }
                break;

            case RECEIVE_PACKAGE:
                if (signal instanceof PacketInSignal) {
                    doVerificationResponse((PacketInSignal) signal);

                } else {
                    unhandled = true;
                }
                break;

            default:
                unhandled = true;
        }

        if (unhandled && !silentHandling) {
            logger.error("{}: Unhandled signal - {}", state, signal);
        }
    }

    public boolean signalOFMessage(OFMessageSignal signal) {
        boolean haveMatch = doProcessPendingOfMods(signal);
        if (haveMatch) {
            signalHandler(signal);
        }

        return haveMatch;
    }

    private void stateTransition() {
        State target = stateRecovery.removeFirst();
        stateTransition(target);
    }

    private void stateTransition(State target) {
        logger.debug("State transition {} ==> {}", state, target);

        State source = state;
        state = target;
        switch (target) {
            case PREPARE_OF_RULES:
                stateEnterPrepareRules(source);
                break;
            case INSTALL_OF_RULES:
                stateEnterInstallRules(source);
                break;
            case SEND_PACKAGE:
                stateEnterSendPackage(source);
                break;
            case STOP:
                stateEnterStop(source);
                break;
        }
    }

    private void doSwitchRemove(SwitchRemoveSignal signal) {
        DatapathId dpId = signal.getDpId();
        if (switchesOfInterest.contains(dpId)) {
            pendingSwitches.add(dpId);
        }
    }

    private boolean doProcessPendingOfMods(OFMessageSignal signal) {
        OFMessage payload = signal.getPayload();
        long xid = payload.getXid();

        int queueSize = pendingOfMod.size();
        try {
            for (Iterator<PendingOfMessage> iter = pendingOfMod.listIterator(); iter.hasNext(); ) {
                PendingOfMessage rule = iter.next();
                if (rule.getXid() != xid) {
                    continue;
                }

                rule.response(payload);
                iter.remove();
                break;
            }
        } catch (OFModError e) {
            logger.error(e.toString());
            stateTransition(State.STOP);
        }

        return queueSize != pendingOfMod.size();
    }

    private void doVerificationResponse(PacketInSignal signal) {
        if (!OFType.PACKET_IN.equals(signal.getType()))
            return;

        OFPacketIn pkt = (OFPacketIn) signal.getPayload();
        Ethernet ethPackage = signal.getEthPacket();

        logger.info("eth type: {}", ethPackage.getEtherType());
        logger.info("eth src: {}", ethPackage.getSourceMACAddress());
        logger.info("eth dst: {}", ethPackage.getDestinationMACAddress());
    }

    private void stateEnterPrepareRules(State source) {
        for (SwitchPortLink link: flowChain) {
            DatapathId dpId = link.getDpId();
            IOFSwitch sw = switchUtils.lookupSwitch(dpId);

            pendingOfMod.add(new PendingOfMessage(dpId, makeDropAllRule(sw)));
            pendingOfMod.add(new PendingOfMessage(dpId, makeCatchAllRule(sw)));
            pendingOfMod.add(new PendingOfMessage(dpId, makeCatchOwnRule(sw)));
        }

        for (SwitchPortLink link : flowChain) {
            DatapathId dpId = link.getDpId();
            IOFSwitch sw = switchUtils.lookupSwitch(dpId);

            pendingOfMod.add(
                    new PendingOfMessage(dpId, makePortLinkRule(sw, link.getSrcPortNumber(), link.getDstPortNumber())));
            pendingOfMod.add(
                    new PendingOfMessage(dpId, makePortLinkRule(sw, link.getDstPortNumber(), link.getSrcPortNumber())));
        }

        stateRecovery.addFirst(State.SEND_PACKAGE);
        stateTransition(State.INSTALL_OF_RULES);
    }

    private void stateEnterInstallRules(State source) {
        try {
            for (PendingOfMessage rule : pendingOfMod) {
                rule.install(switchUtils);
            }
            stateTransition(State.WAIT_RULES_INSTALLATION);
        } catch (SwitchWriteRepeatableError e) {
            logger.error(String.format("Repeatable error in installing OFRule: %s", e));
            stateTransition(State.STOP);
        } catch (SwitchWriteError e) {
            logger.error(String.format("Can\'t install OFRule: %s", e));

            DatapathId dbId = e.getDpId();
            pendingSwitches.add(dbId);

            IOFSwitch sw = switchUtils.lookupSwitch(dbId);
            sw.disconnect();

            stateTransition(State.SWITCH_COMMUNICATION_PROBLEM);
        }
    }

    private void stateEnterSendPackage(State source) {
        SwitchPortLink sourceLink = flowChain.getFirst();
        SwitchPortLink destLink = flowChain.getLast();

        VerificationPackageAdapter verification = new VerificationPackageAdapter(
                switchUtils.lookupSwitch(sourceLink.getDpId()), OFPort.of(FLOW_IN_PORT),
                switchUtils.lookupSwitch(destLink.getDpId()), OFPort.of(FLOW_OUT_PORT),
                new VerificationPackageSign("secret"));

        OFMessage payload = makeVerificationInjection(
                switchUtils.lookupSwitch(sourceLink.getDpId()), verification.getData(), FLOW_IN_PORT);
        PendingOfMessage pendingMessage = new PendingOfMessage(sourceLink.getDpId(), payload);
        pendingOfMod.add(pendingMessage);

        stateRecovery.addFirst(State.RECEIVE_PACKAGE);
        stateTransition(State.WAIT_RULES_INSTALLATION);
    }

    private void stateEnterStop(State source) {
        logger.error("Unrecoverable error, stop any activity (state: {})", source);
    }

    private OFMessage makeDropAllRule(IOFSwitch sw) {
        OFFlowMod.Builder flowAdd = sw.getOFFactory().buildFlowAdd();
        flowAdd.setCookie(U64.of(COOKIE_MARKER | 1L));
        flowAdd.setPriority(1000);
        return flowAdd.build();
    }

    private OFMessage makeCatchOwnRule(IOFSwitch sw) {
        return makeCatchRule(sw, switchUtils.dpIdToMac(sw), 3);
    }

    private OFMessage makeCatchAllRule(IOFSwitch sw) {
        return makeCatchRule(sw, MacAddress.of("08:ED:02:E3:FF:FF"), 2);

    }

    private OFMessage makeCatchRule(IOFSwitch sw, MacAddress dstMac, int cookie) {
        OFFactory ofFactory = sw.getOFFactory();

        OFFlowMod.Builder flowAdd = ofFactory.buildFlowAdd();
        flowAdd.setCookie(U64.of(COOKIE_MARKER | (long)cookie));
        flowAdd.setPriority(5000 + cookie);

        Match.Builder match = ofFactory.buildMatch();
        match.setMasked(MatchField.ETH_DST, dstMac, MacAddress.NO_MASK);

        flowAdd.setMatch(match.build());

        List<OFAction> actions = new ArrayList<>(2);
        actions.add(ofFactory.actions().buildOutput().setPort(OFPort.CONTROLLER).build());

        flowAdd.setActions(actions);

        return flowAdd.build();
    }

    private OFMessage makePortLinkRule(IOFSwitch sw, int srcPort, int dstPort) {
        OFFactory ofFactory = sw.getOFFactory();
        int cookie = 0xff;

        OFFlowMod.Builder flowAdd = ofFactory.buildFlowAdd();
        flowAdd.setCookie(U64.of(COOKIE_MARKER | cookie));
        flowAdd.setPriority(2500 + cookie);

        Match.Builder match = ofFactory.buildMatch();
        match.setExact(MatchField.IN_PORT, OFPort.of(srcPort));
        flowAdd.setMatch(match.build());

        List<OFAction> action = new ArrayList<>(2);
        action.add(ofFactory.actions().buildOutput().setPort(OFPort.of(dstPort)).build());
        flowAdd.setActions(action);

        return flowAdd.build();
    }

    private OFMessage makeVerificationInjection(IOFSwitch sw, byte[] data, int srcPort) {
        OFFactory ofFactory = sw.getOFFactory();
        OFPacketOut.Builder portOut = ofFactory.buildPacketOut();

        portOut.setData(data);

        List<OFAction> actions = new ArrayList<>(2);
        actions.add(ofFactory.actions().buildOutput().setPort(OFPort.TABLE).build());
        portOut.setActions(actions);

        OFMessageUtils.setInPort(portOut, OFPort.of(srcPort));

        return portOut.build();
    }

    enum State {
        INIT,
        PREPARE_OF_RULES,
        INSTALL_OF_RULES,
        SWITCH_COMMUNICATION_PROBLEM,
        WAIT_RULES_INSTALLATION,
        SEND_PACKAGE,
        RECEIVE_PACKAGE,
        STOP
    }
}
