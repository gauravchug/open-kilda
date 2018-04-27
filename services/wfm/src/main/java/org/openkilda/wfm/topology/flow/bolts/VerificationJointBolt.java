package org.openkilda.wfm.topology.flow.bolts;

import static org.openkilda.messaging.Utils.MAPPER;

import org.openkilda.messaging.Message;
import org.openkilda.messaging.command.flow.UniFlowVerificationRequest;
import org.openkilda.messaging.info.flow.FlowVerificationErrorCode;
import org.openkilda.messaging.info.flow.FlowVerificationResponse;
import org.openkilda.messaging.info.flow.UniFlowVerificationResponse;
import org.openkilda.messaging.model.BiFlow;
import org.openkilda.wfm.AbstractBolt;
import org.openkilda.wfm.topology.AbstractTopology;
import org.openkilda.wfm.topology.flow.model.VerificationWaitRecord;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class VerificationJointBolt extends AbstractBolt {
    public static final String STREAM_ID_REQUEST = "request";
    public static final String STREAM_ID_RESPONSE = "response";
    public static final Fields STREAM_FIELDS_REQUEST = AbstractTopology.fieldMessage;
    public static final Fields STREAM_FIELDS_RESPONSE = AbstractTopology.fieldMessage;

    private static final Logger logger = LoggerFactory.getLogger(VerificationJointBolt.class);

    private final LinkedList<VerificationWaitRecord> ongoingVerifications = new LinkedList<>();

    @Override
    protected void handleInput(Tuple input) {
        Object unclassified = input.getValueByField(VerificationBolt.FIELD_ID_OUTPUT);

        if (unclassified instanceof BiFlow) {
            handleRequest(input, (BiFlow) unclassified);
        } else if (unclassified instanceof UniFlowVerificationResponse) {
            handleResponse(input, (UniFlowVerificationResponse) unclassified);
        } else {
            logger.warn(
                    "Unexpected input {} - is topology changes without code change?",
                    unclassified.getClass().getName());
        }
    }

    private void handleRequest(Tuple input, BiFlow request) {
        Message message = fetchInputMessage(input);
        VerificationWaitRecord waitRecord = new VerificationWaitRecord(request, message.getCorrelationId());

        List<UniFlowVerificationRequest> pendingRequests = waitRecord.getPendingRequests();
        List<String> jsonMessages = new ArrayList<>(pendingRequests.size());
        try {
            for (UniFlowVerificationRequest uniFlowVerificationRequest : pendingRequests) {
                String s = MAPPER.writeValueAsString(uniFlowVerificationRequest);
                jsonMessages.add(s);
            }
        } catch (JsonProcessingException e) {
            logger.error("Can't encode {}: {}", UniFlowVerificationRequest.class, e);
            return;
        }

        for (String json : jsonMessages) {
            getOutput().emit(STREAM_ID_REQUEST, input, new Values(json));
        }

        ongoingVerifications.addLast(waitRecord);
    }

    private void handleResponse(Tuple input, UniFlowVerificationResponse response) {
        ListIterator<VerificationWaitRecord> iter = ongoingVerifications.listIterator();

        long currentTime = System.currentTimeMillis();
        while (iter.hasNext()) {
            VerificationWaitRecord waitRecord = iter.next();

            if (waitRecord.isOutdated(currentTime)) {
                iter.remove();
                produceErrorResponse(input, waitRecord);
                continue;
            }

            if (! waitRecord.consumeResponse(response)) {
                continue;
            }
            if (! waitRecord.isFilled()) {
                continue;
            }

            iter.remove();
            produceResponse(input, waitRecord);

            break;
        }
    }

    private Message fetchInputMessage(Tuple input) {
        Object raw = input.getValueByField(VerificationBolt.FIELD_ID_INPUT);
        if (raw == null) {
            throw new IllegalArgumentException("The message field is empty in input tuple");
        }

        Message value;
        try {
            value = (Message)raw;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(String.format("Can't convert value into Message: %s", e));
        }
        return value;
    }

    private void produceResponse(Tuple input, VerificationWaitRecord waitRecord) {
        FlowVerificationResponse response = waitRecord.produce();

        String json;
        try {
            json = MAPPER.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            logger.error("Can't encode record in JSON: {}", e.toString());
            return;
        }

        getOutput().emit(STREAM_ID_RESPONSE, input, new Values(json));
    }

    private void produceErrorResponse(Tuple input, VerificationWaitRecord waitRecord) {
        waitRecord.fillPendingWithError(FlowVerificationErrorCode.NO_SPEAKER_RESPONSE);
        produceResponse(input, waitRecord);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputManager) {
        outputManager.declareStream(STREAM_ID_REQUEST, STREAM_FIELDS_REQUEST);
        outputManager.declareStream(STREAM_ID_RESPONSE, STREAM_FIELDS_RESPONSE);
    }
}
