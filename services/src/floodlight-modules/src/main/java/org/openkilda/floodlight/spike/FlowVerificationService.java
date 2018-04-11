package org.openkilda.floodlight.spike;

import net.floodlightcontroller.core.*;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.IFloodlightService;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPortDesc;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.types.DatapathId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlowVerificationService
        implements IFloodlightService, IOFSwitchListener, IOFMessageListener {
    private static final Logger logger = LoggerFactory.getLogger(
            FlowVerificationService.class);

    private FloodlightModuleContext flContext;
    private FlowVerificationManager verify;

    public FlowVerificationService(FloodlightModuleContext flContext) {
        this.flContext = flContext;
    }

    public void init() {
        verify = new FlowVerificationManager(flContext.getServiceImpl(IOFSwitchService.class));

        flContext.getServiceImpl(IOFSwitchService.class)
                .addOFSwitchListener(this);
    }

    @Override
    public void switchAdded(DatapathId switchId) {
        logger.info("Switch added {}", switchId);
    }

    @Override
    public void switchRemoved(DatapathId switchId) {
        logger.info("Switch removed {}", switchId);
        verify.signalHandler(new SwitchRemoveSignal(switchId));
    }

    @Override
    public void switchActivated(DatapathId switchId) {
        logger.info("Switch activated {}", switchId);
        verify.signalHandler(new SwitchAddSignal(switchId));
    }

    @Override
    public void switchPortChanged(DatapathId switchId, OFPortDesc port, PortChangeType type) {
        logger.info("Switch port changed {}: port - {}, kind - {}", switchId, port, type);
    }

    @Override
    public void switchChanged(DatapathId switchId) {
        logger.info("Switch changed {}", switchId);
    }

    @Override
    public void switchDeactivated(DatapathId switchId) {
        logger.info("Switch deactivated {}", switchId);

        verify.signalHandler(new SwitchRemoveSignal(switchId));
    }

    @Override
    public Command receive(IOFSwitch sw, OFMessage payload, FloodlightContext context) {
        logger.debug(
                "OFMessage - dpId:{} type: {} xId: {}", sw.getId(), payload.getType(), payload.getXid());

        OFMessageSignal signal;
        if (OFType.PACKET_IN.equals(payload.getType())) {
            signal = new PacketInSignal(sw.getId(), payload, IFloodlightProviderService.bcStore.get(context, IFloodlightProviderService.CONTEXT_PI_PAYLOAD));
        } else {
            signal = new OFMessageSignal(sw.getId(), payload);
        }

        if (verify.signalOFMessage(signal)) {
            return Command.STOP;
        }
        return Command.CONTINUE;
    }

    @Override
    public boolean isCallbackOrderingPrereq(OFType ofType, String s) {
        return false;
    }

    @Override
    public boolean isCallbackOrderingPostreq(OFType ofType, String s) {
        return false;
    }

    @Override
    public String getName() {
        return getClass().getName();
    }
}
