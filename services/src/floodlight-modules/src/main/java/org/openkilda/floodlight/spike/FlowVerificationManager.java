package org.openkilda.floodlight.spike;

import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.util.OFMessageUtils;
import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.OFPacketOut;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.protocol.oxm.OFOxms;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.OFVlanVidMatch;
import org.projectfloodlight.openflow.types.U64;
import org.projectfloodlight.openflow.types.VlanVid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FlowVerificationManager {
    private static final Logger logger = LoggerFactory.getLogger(FlowVerificationManager.class);

    private long COOKIE_MARKER = 0x1100_0000_0000_0000L;
    private int FLOW_IN_PORT = 1;
    private int FLOW_IN_VLAN = 96;
    private int FLOW_OUT_PORT = 1;
    private int FLOW_OUT_VLAN = 128;
    private int FLOW_TRANSIT_VLAN = 256;

    private final SwitchUtils switchUtils;

    private final LinkedList<InSwitchFlow> flowChain = new LinkedList<>();
    private final Set<DatapathId> pendingSwitches = new HashSet<>();
    private final Set<DatapathId> switchesOfInterest = new HashSet<>();

    private final List<PendingOfMessage> pendingOfMod = new LinkedList<>();

    State state = State.INIT;
    private final LinkedList<State> stateRecovery = new LinkedList<>();
    private Map<Long, DatapathId> switchTransaction = new HashMap<>();

    public FlowVerificationManager(SwitchUtils switchUtils) {
        this.switchUtils = switchUtils;

        flowChain.addLast(
                new InSwitchFlow(
                        DatapathId.of(0x00000000_00000001),
                        new FlowEndpoint(FLOW_IN_PORT, VlanVid.ofVlan(FLOW_IN_VLAN)),
                        new FlowEndpoint(2, VlanVid.ofVlan(FLOW_TRANSIT_VLAN))));
        flowChain.addLast(
                new InSwitchFlow(
                        DatapathId.of(0x00000000_00000002),
                        new FlowEndpoint(3, VlanVid.ofVlan(FLOW_TRANSIT_VLAN)),
                        new FlowEndpoint(2, VlanVid.ofVlan(FLOW_TRANSIT_VLAN))));
        flowChain.addLast(
                new InSwitchFlow(
                        DatapathId.of(0x00000000_00000003),
                        new FlowEndpoint(2, VlanVid.ofVlan(FLOW_TRANSIT_VLAN)),
                        new FlowEndpoint(FLOW_OUT_PORT, VlanVid.ofVlan(FLOW_OUT_VLAN))));

        for (InSwitchFlow link : flowChain) {
            switchesOfInterest.add(link.getDpId());
            pendingSwitches.add(link.getDpId());
        }

        stateRecovery.addFirst(State.PREPARE_OF_RULES);
    }

    public synchronized void signalHandler(Signal signal) {
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
                    stateTransition();
                }
                break;

            case WAIT_RULES_INSTALLATION:
                if (pendingOfMod.size() == 0) {
                    stateTransition();
                } else {
                    logger.debug("wait for response for {} rules", pendingOfMod.size());
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

    public synchronized boolean signalOFMessage(OFMessageSignal signal) {
        boolean haveMatch = false;

        switch (signal.getType()) {
            case ERROR:
                haveMatch = recordErrorResponse(signal);
                break;
            case BARRIER_REPLY:
                haveMatch = closeTransaction(signal);
                break;
            case PACKET_IN:
                haveMatch = true;
        }

        if (haveMatch) {
            signalHandler(signal);
        }

        return haveMatch;
    }

    private boolean recordErrorResponse(OFMessageSignal signal) {
        OFMessage payload = signal.getPayload();
        long xid = payload.getXid();

        for (PendingOfMessage pending : pendingOfMod) {
            if (pending.getXid() != xid) {
                continue;
            }

            pending.response(payload);
            return true;
        }

        return false;
    }

    private boolean closeTransaction(OFMessageSignal signal) {
        long xid = signal.getPayload().getXid();
        if (! switchTransaction.containsKey(xid)) {
            return false;
        }

        DatapathId dpId = switchTransaction.get(xid);
        boolean haveErrors = false;

        Iterator<PendingOfMessage> iter = pendingOfMod.iterator();
        while (iter.hasNext()) {
            PendingOfMessage pending = iter.next();
            if (pending.getDpId() != dpId) {
                continue;
            }
            if (pending.getResponse() != null) {
                haveErrors = true;
                continue;
            }

            iter.remove();
        }

        if (haveErrors) {
            stateTransition(State.STOP);
        }

        return true;
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
            stateRecovery.addFirst(state);
            stateTransition(State.SWITCH_COMMUNICATION_PROBLEM);
        }
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
        for (InSwitchFlow link: flowChain) {
            DatapathId dpId = link.getDpId();
            IOFSwitch sw = switchUtils.lookupSwitch(dpId);

            pendingOfMod.add(new PendingOfMessage(dpId, makeTableMissRule(sw)));
            pendingOfMod.add(new PendingOfMessage(dpId, makeCatchAllRule(sw)));
            pendingOfMod.add(new PendingOfMessage(dpId, makeCatchOwnRule(sw)));
        }

        for (InSwitchFlow link : flowChain) {
            DatapathId dpId = link.getDpId();
            IOFSwitch sw = switchUtils.lookupSwitch(dpId);

            pendingOfMod.add(
                    new PendingOfMessage(dpId, makePortLinkRule(sw, link)));
            pendingOfMod.add(
                    new PendingOfMessage(dpId, makePortLinkRule(sw, link.reverse())));
        }

        stateRecovery.addFirst(State.SEND_PACKAGE);
        stateTransition(State.INSTALL_OF_RULES);
    }

    private void stateEnterInstallRules(State source) {
        makeTransactionMarkers();

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

            stateRecovery.addFirst(state);
            stateTransition(State.SWITCH_COMMUNICATION_PROBLEM);
        }
    }

    private void stateEnterSendPackage(State source) {
        InSwitchFlow sourceLink = flowChain.getFirst();
        InSwitchFlow destLink = flowChain.getLast();

        VerificationPackageAdapter verification = new VerificationPackageAdapter(
                switchUtils.lookupSwitch(sourceLink.getDpId()), OFPort.of(FLOW_IN_PORT),
                switchUtils.lookupSwitch(destLink.getDpId()), (short) FLOW_IN_VLAN,
                new VerificationPackageSign("secret"));

        OFMessage payload = makeVerificationInjection(
                switchUtils.lookupSwitch(sourceLink.getDpId()), verification.getData(),
                new FlowEndpoint(FLOW_IN_PORT, VlanVid.ofVlan(FLOW_IN_VLAN)));
        PendingOfMessage pendingMessage = new PendingOfMessage(sourceLink.getDpId(), payload);
        pendingOfMod.add(pendingMessage);

        stateRecovery.addFirst(State.RECEIVE_PACKAGE);
        stateTransition(State.INSTALL_OF_RULES);
    }

    private void stateEnterStop(State source) {
        logger.error("Unrecoverable error, stop any activity (state: {})", source);
    }

    private void makeTransactionMarkers() {
        Set<DatapathId> affectedSwitches = new HashSet<>();

        for (PendingOfMessage pending : pendingOfMod) {
            affectedSwitches.add(pending.getDpId());
        }

        for (DatapathId dpId : affectedSwitches) {
            IOFSwitch sw = switchUtils.lookupSwitch(dpId);
            OFMessage barrier = makeBarrierMessage(sw);

            pendingOfMod.add(new PendingOfMessage(dpId, barrier));
            switchTransaction.put(barrier.getXid(), dpId);
        }
    }

    private OFMessage makeTableMissRule(IOFSwitch sw) {
        OFFlowMod.Builder flowAdd = sw.getOFFactory().buildFlowAdd();
        flowAdd.setCookie(U64.of(COOKIE_MARKER | 1L));
        flowAdd.setPriority(0);
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
        actions.add(ofFactory.actions().buildOutput().setPort(OFPort.CONTROLLER).setMaxLen(512).build());

        flowAdd.setActions(actions);

        return flowAdd.build();
    }

    private OFMessage makePortLinkRule(IOFSwitch sw, InSwitchFlow flow) {
        OFFactory ofFactory = sw.getOFFactory();
        int cookie = 0xff;

        OFFlowMod.Builder flowAdd = ofFactory.buildFlowAdd();
        flowAdd.setCookie(U64.of(COOKIE_MARKER | cookie));
        flowAdd.setPriority(2500 + cookie);

        FlowEndpoint left = flow.getLeft();
        FlowEndpoint right = flow.getRight();

        Match.Builder match = ofFactory.buildMatch();
        match.setExact(MatchField.IN_PORT, OFPort.of(left.getPortNumber()));
        match.setExact(MatchField.VLAN_VID, OFVlanVidMatch.ofVlanVid(left.getVlan()));
        flowAdd.setMatch(match.build());

        List<OFAction> action = new ArrayList<>(2);
        if (!left.getVlan().equals(right.getVlan())) {
            OFOxms oxms = ofFactory.oxms();
            action.add(
                    ofFactory.actions().setField(
                            oxms.buildVlanVid()
                                    .setValue(OFVlanVidMatch.ofVlanVid(right.getVlan()))
                                    .build()));
        }
        action.add(ofFactory.actions().buildOutput().setPort(OFPort.of(right.getPortNumber())).build());
        flowAdd.setActions(action);

        return flowAdd.build();
    }

    private OFMessage makeBarrierMessage(IOFSwitch sw) {
        return sw.getOFFactory().barrierRequest();
    }

    private OFMessage makeVerificationInjection(IOFSwitch sw, byte[] data, FlowEndpoint endpoint) {
        OFFactory ofFactory = sw.getOFFactory();
        OFPacketOut.Builder portOut = ofFactory.buildPacketOut();

        portOut.setData(data);

        List<OFAction> actions = new ArrayList<>(2);
        actions.add(ofFactory.actions().buildOutput().setPort(OFPort.TABLE).build());
        portOut.setActions(actions);

        OFMessageUtils.setInPort(portOut, OFPort.of(endpoint.getPortNumber()));

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
