package org.openkilda.floodlight.operation.flow;

import org.openkilda.floodlight.kafka.KafkaMessageProducer;
import org.openkilda.floodlight.operation.Operation;
import org.openkilda.floodlight.operation.OperationContext;
import org.openkilda.messaging.Topic;
import org.openkilda.messaging.info.InfoMessage;
import org.openkilda.messaging.info.flow.FlowVerificationResponse;

abstract class VerificationOperationCommon extends Operation {
    private final KafkaMessageProducer kafkaProducer;

    VerificationOperationCommon(OperationContext context) {
        super(context);
        kafkaProducer = getContext().getModuleContext().getServiceImpl(KafkaMessageProducer.class);
    }

    protected void sendResponse(FlowVerificationResponse response) {
        InfoMessage message = new InfoMessage(response, System.currentTimeMillis(), getContext().getCorrelationId());
        kafkaProducer.postMessage(Topic.FLOW, message);
    }
}
