package org.openkilda.floodlight.operation.flow;

import net.floodlightcontroller.threadpool.IThreadPoolService;
import org.openkilda.floodlight.service.FlowVerificationService;
import org.openkilda.floodlight.operation.OperationContext;
import org.openkilda.messaging.command.flow.FlowVerifycationRequest;
import org.openkilda.messaging.info.flow.FlowVerificationErrorCode;
import org.openkilda.messaging.info.flow.FlowVerificationResponse;
import org.openkilda.messaging.model.Flow;

import net.floodlightcontroller.core.module.FloodlightModuleContext;
import org.projectfloodlight.openflow.types.DatapathId;

import java.util.concurrent.TimeUnit;

public class VerificationListenOperation extends VerificationOperationCommon {
    private static int TIMEOUT_SECONDS = 32;

    private final FlowVerifycationRequest verificationRequest;
    private final VerificationData verificationData;

    private final FlowVerificationService flowVerificationService;
    private final IThreadPoolService scheduler;

    public VerificationListenOperation(OperationContext context, FlowVerifycationRequest verificationRequest) {
        super(context);
        this.verificationRequest = verificationRequest;

        Flow flow = verificationRequest.getFlow();
        this.verificationData = new VerificationData(
                DatapathId.of(flow.getSourceSwitch()),
                DatapathId.of(flow.getDestinationSwitch()),
                verificationRequest.getPacketId());

        FloodlightModuleContext moduleContext = getContext().getModuleContext();
        flowVerificationService = moduleContext.getServiceImpl(FlowVerificationService.class);
        scheduler = moduleContext.getServiceImpl(IThreadPoolService.class);
    }

    @Override
    public void run() {
        flowVerificationService.subscribe(this);

        TimeoutNotification notification = new TimeoutNotification(this);
        scheduler.getScheduledExecutor().schedule(notification, TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    public boolean packetIn(VerificationData payload) {
        if (! verificationData.equals(payload)) {
            return false;
        }

        FlowVerificationResponse response = new FlowVerificationResponse(verificationRequest.getFlow());
        sendResponse(response);

        return true;
    }

    private void timeout() {
        flowVerificationService.unsubscribe(this);

        FlowVerificationResponse response = new FlowVerificationResponse(
                verificationRequest.getFlow(), FlowVerificationErrorCode.TIMEOUT);
        sendResponse(response);
    }

    private static class TimeoutNotification implements Runnable {
        private final VerificationListenOperation operation;

        TimeoutNotification(VerificationListenOperation operation) {
            this.operation = operation;
        }

        @Override
        public void run() {
            operation.timeout();
        }
    }
}
