package org.openkilda.floodlight.service;

import org.openkilda.floodlight.operation.flow.VerificationReceiveOperation;
import org.openkilda.floodlight.pathverification.PathVerificationService;
import org.openkilda.floodlight.pathverification.VerificationPacket;

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
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFType;

import java.util.LinkedList;

public class FlowVerificationRecipientService implements IFloodlightService, IOFMessageListener {
    private final LinkedList<VerificationReceiveOperation> pendingRecipients = new LinkedList<>();

    public void subscribe(VerificationReceiveOperation handler) {
        synchronized (pendingRecipients) {
            pendingRecipients.add(handler);
        }
    }

    public void unsubscribe(VerificationReceiveOperation handler) {
        synchronized (pendingRecipients) {
            pendingRecipients.remove(handler);
        }
    }

    public void init(FloodlightModuleContext flContext) {
        IFloodlightProviderService flProviderService = flContext.getServiceImpl(IFloodlightProviderService.class);

        flProviderService.addOFMessageListener(OFType.PACKET_IN, this);
    }

    @Override
    public Command receive(IOFSwitch sw, OFMessage packet, FloodlightContext context) {
        Ethernet eth = IFloodlightProviderService.bcStore.get(context, IFloodlightProviderService.CONTEXT_PI_PAYLOAD);

        VerificationPacket verificationPacket = unpack(eth);
        if (verificationPacket == null) {
            return Command.CONTINUE;
        }

        // TODO

        return null;
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
    private VerificationPacket unpack(Ethernet packet) {
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

        return new VerificationPacket((Data) udp.getPayload());
    }
}
