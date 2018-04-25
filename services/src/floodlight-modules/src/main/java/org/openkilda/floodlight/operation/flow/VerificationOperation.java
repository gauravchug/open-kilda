package org.openkilda.floodlight.operation.flow;

import org.openkilda.floodlight.operation.Operation;
import org.openkilda.floodlight.operation.OperationContext;
import org.openkilda.messaging.model.Flow;

import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import org.projectfloodlight.openflow.types.DatapathId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VerificationOperation extends Operation {
    private static final Logger logger = LoggerFactory.getLogger(VerificationOperation.class);

    private final Flow flow;

    private final IOFSwitchService switchService;

    public VerificationOperation(OperationContext context, Flow flow) {
        super(context);

        this.flow = flow;

        FloodlightModuleContext moduleContext = getContext().getModuleContext();
        switchService = moduleContext.getServiceImpl(IOFSwitchService.class);
    }

    @Override
    public void run() {
        makeSendOperation();
        makeReceiveOperation();
    }

    private void makeSendOperation() {
        if (!isOwnSwitch(flow.getSourceSwitch())) {
            logger.debug("Switch {} is not under our control, do not produce flow verification send request");
            return;
        }

        startSubOperation(new VerificationSendOperation(getContext(), flow));
    }

    private void makeReceiveOperation() {
        if (!isOwnSwitch(flow.getDestinationSwitch())) {
            logger.debug("Switch {} is not under our control, do not produce flow verification receive handler");
            return;
        }

        startSubOperation(new VerificationReceiveOperation(getContext(), flow));
    }

    private boolean isOwnSwitch(String switchId) {
        DatapathId dpId = DatapathId.of(switchId);
        IOFSwitch sw = switchService.getSwitch(dpId);

        return sw != null;
    }
}
