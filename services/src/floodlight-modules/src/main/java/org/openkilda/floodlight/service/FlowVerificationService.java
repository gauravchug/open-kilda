package org.openkilda.floodlight.service;

import com.auth0.jwt.interfaces.DecodedJWT;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import org.openkilda.floodlight.SwitchUtils;
import org.openkilda.floodlight.exc.CorruptedNetworkDataException;
import org.openkilda.floodlight.exc.InvalidSingatureConfigurationException;
import org.openkilda.floodlight.operation.flow.VerificationData;
import org.openkilda.floodlight.operation.flow.VerificationListenOperation;
import org.openkilda.floodlight.pathverification.PathVerificationService;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.packet.Data;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.packet.UDP;
import org.openkilda.floodlight.utils.DataSignature;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;

public class FlowVerificationService implements IFloodlightService, IOFMessageListener {
    private static Logger logger = LoggerFactory.getLogger(FlowVerificationService.class);

    private final LinkedList<VerificationListenOperation> pendingRecipients = new LinkedList<>();

    private DataSignature signature = null;
    private SwitchUtils switchUtils = null;

    public void subscribe(VerificationListenOperation handler) {
        synchronized (pendingRecipients) {
            pendingRecipients.add(handler);
        }
    }

    public void unsubscribe(VerificationListenOperation handler) {
        synchronized (pendingRecipients) {
            pendingRecipients.remove(handler);
        }
    }

    public void init(FloodlightModuleContext flContext) throws FloodlightModuleException {
        // FIXME(surabujin): avoid usage foreign module configuration
        Map<String, String> config = flContext.getConfigParams(PathVerificationService.class);
        try {
            signature = new DataSignature(config.get("hmac256-secret"));
        } catch (InvalidSingatureConfigurationException e) {
            throw new FloodlightModuleException(String.format("Unable to initialize %s", getClass().getName()), e);
        }

        switchUtils = new SwitchUtils(flContext.getServiceImpl(IOFSwitchService.class));

        IFloodlightProviderService flProviderService = flContext.getServiceImpl(IFloodlightProviderService.class);

        flProviderService.addOFMessageListener(OFType.PACKET_IN, this);
    }

    @Override
    public Command receive(IOFSwitch sw, OFMessage packet, FloodlightContext context) {
        Ethernet eth = IFloodlightProviderService.bcStore.get(context, IFloodlightProviderService.CONTEXT_PI_PAYLOAD);

        VerificationData data;
        try {
            byte[] payload = unpack(sw, eth);
            if (payload == null) {
                return Command.CONTINUE;
            }

            DecodedJWT token = signature.verify(payload);
            data = VerificationData.of(token);

            if (! data.getDest().equals(sw.getId())) {
                throw new CorruptedNetworkDataException(String.format(
                        "Catch flow verification package on %s while target is %s", sw.getId(), data.getDest()));
            }
        } catch (CorruptedNetworkDataException e) {
            logger.error(String.format("dpid:%s %s", sw.getId(), e));
            return Command.CONTINUE;
        }

        boolean isHandled = false;
        synchronized (pendingRecipients) {
            for (ListIterator<VerificationListenOperation> iter = pendingRecipients.listIterator(); iter.hasNext(); ) {
                VerificationListenOperation operation = iter.next();

                if (!operation.packetIn(data)) {
                    continue;
                }
                isHandled = true;
                iter.remove();
                break;
            }
        }

        if (isHandled) {
            return Command.STOP;
        }
        return Command.CONTINUE;
    }

    @Override
    public String getName() {
        return getClass().getName();
    }

    @Override
    public boolean isCallbackOrderingPrereq(OFType type, String name) {
        return false;
    }

    @Override
    public boolean isCallbackOrderingPostreq(OFType type, String name) {
        return false;
    }

    // FIXME(surabujin): move out into package related module
    private byte[] unpack(IOFSwitch sw, Ethernet packet) {
        if (!packet.getDestinationMACAddress().equals(switchUtils.dpIdToMac(sw))) {
            return null;
        }

        if (!(packet.getPayload() instanceof IPv4)) {
            return null;
        }
        IPv4 ip = (IPv4) packet.getPayload();

        if (!(ip.getPayload() instanceof UDP)) {
            return null;
        }
        UDP udp = (UDP) ip.getPayload();

        if (udp.getSourcePort().getPort() != PathVerificationService.VERIFICATION_PACKET_UDP_PORT) {
            return null;
        }
        if (udp.getDestinationPort().getPort() != PathVerificationService.VERIFICATION_PACKET_UDP_PORT) {
            return null;
        }

        return ((Data) udp.getPayload()).getData();
    }

    public DataSignature getSignature() {
        return signature;
    }
}
