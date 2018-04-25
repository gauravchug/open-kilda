package org.openkilda.floodlight.operation.flow;

import org.openkilda.floodlight.operation.Operation;
import org.openkilda.floodlight.operation.OperationContext;
import org.openkilda.messaging.command.flow.FlowVerifycationRequest;
import org.openkilda.messaging.model.Flow;

import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import org.projectfloodlight.openflow.types.DatapathId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VerificationOperation extends VerificationOperationCommon {
    private static final Logger logger = LoggerFactory.getLogger(VerificationOperation.class);

    private final FlowVerifycationRequest verificationRequest;

    private final IOFSwitchService switchService;

    public VerificationOperation(OperationContext context, FlowVerifycationRequest verificationRequest) {
        super(context);

        this.verificationRequest = verificationRequest;

        FloodlightModuleContext moduleContext = getContext().getModuleContext();
        switchService = moduleContext.getServiceImpl(IOFSwitchService.class);
    }

    @Override
    public void run() {
        makeSendOperation();
        makeReceiveOperation();
    }

    private void makeSendOperation() {
        if (!isOwnSwitch(verificationRequest.getFlow().getSourceSwitch())) {
            logger.debug("Switch {} is not under our control, do not produce flow verification send request");
            return;
        }

        startSubOperation(new VerificationSendOperation(getContext(), verificationRequest));
    }

    private void makeReceiveOperation() {
        if (!isOwnSwitch(verificationRequest.getFlow().getDestinationSwitch())) {
            logger.debug("Switch {} is not under our control, do not produce flow verification receive handler");
            return;
        }

        startSubOperation(new VerificationListenOperation(getContext(), verificationRequest));
    }

    private boolean isOwnSwitch(String switchId) {
        DatapathId dpId = DatapathId.of(switchId);
        IOFSwitch sw = switchService.getSwitch(dpId);

        return sw != null;
    }
}
