package org.openkilda.floodlight;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.packet.Ethernet;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFType;

public class PacketInService implements IFloodlightService, IOFMessageListener {
    public void init(FloodlightModuleContext flContext) {
        IFloodlightProviderService flProviderService = flContext.getServiceImpl(IFloodlightProviderService.class);

        flProviderService.addOFMessageListener(OFType.PACKET_IN, this);
    }

    @Override
    public Command receive(IOFSwitch sw, OFMessage pkt, FloodlightContext context) {
        Ethernet eth = IFloodlightProviderService.bcStore.get(context, IFloodlightProviderService.CONTEXT_PI_PAYLOAD);

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
}
