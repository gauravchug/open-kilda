package org.openkilda.floodlight.operation.flow;

import org.openkilda.floodlight.model.flow.VerificationData;
import org.openkilda.floodlight.operation.OperationContext;
import org.openkilda.floodlight.service.FlowVerificationService;
import org.openkilda.messaging.command.flow.FlowVerificationRequest;
import org.openkilda.messaging.info.flow.FlowVerificationErrorCode;
import org.openkilda.messaging.info.flow.UniFlowVerificationResponse;
import org.openkilda.messaging.model.Flow;

import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.threadpool.IThreadPoolService;
import org.projectfloodlight.openflow.types.DatapathId;

import java.util.concurrent.TimeUnit;

public class VerificationListenOperation extends VerificationOperationCommon {
    private static int TIMEOUT_SECONDS = 32;

    private final FlowVerificationRequest verificationRequest;
    private final VerificationData verificationData;

    private final FlowVerificationService flowVerificationService;
    private final IThreadPoolService scheduler;

    public VerificationListenOperation(OperationContext context, FlowVerificationRequest verificationRequest) {
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

        UniFlowVerificationResponse response = new UniFlowVerificationResponse(verificationRequest.getFlow());
        sendResponse(response);

        return true;
    }

    private void timeout() {
        flowVerificationService.unsubscribe(this);

        UniFlowVerificationResponse response = new UniFlowVerificationResponse(
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
