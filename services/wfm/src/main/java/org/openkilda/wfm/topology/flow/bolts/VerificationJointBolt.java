package org.openkilda.wfm.topology.flow.bolts;

import org.openkilda.messaging.info.flow.UniFlowVerificationResponse;
import org.openkilda.messaging.model.BiFlow;
import org.openkilda.wfm.AbstractBolt;
import org.openkilda.wfm.topology.AbstractTopology;

import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VerificationJointBolt extends AbstractBolt {
    public static final String STREAM_ID_REQUEST = "request";
    public static final String STREAM_ID_RESPONSE = "response";
    public static final Fields STREAM_FIELDS_REQUEST = AbstractTopology.fieldMessage;
    public static final Fields STREAM_FIELDS_RESPONSE = AbstractTopology.fieldMessage;

    private static final Logger logger = LoggerFactory.getLogger(VerificationJointBolt.class);

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
        // TODO
    }

    private void handleResponse(Tuple input, UniFlowVerificationResponse response) {
        // TODO
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputManager) {
        outputManager.declareStream(STREAM_ID_REQUEST, STREAM_FIELDS_REQUEST);
        outputManager.declareStream(STREAM_ID_RESPONSE, STREAM_FIELDS_RESPONSE);
    }
}
