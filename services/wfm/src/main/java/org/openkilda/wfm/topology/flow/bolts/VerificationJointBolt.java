package org.openkilda.wfm.topology.flow.bolts;

import org.apache.storm.topology.base.BaseRichBolt;
import org.openkilda.messaging.command.flow.UniflowVerificationRequest;
import org.openkilda.messaging.info.flow.UniFlowVerificationResponse;
import org.openkilda.wfm.AbstractBolt;
import org.openkilda.wfm.topology.AbstractTopology;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class VerificationJointBolt extends AbstractBolt {
    public static final String STREAM_ID_REQUEST = "request";
    public static final String STREAM_ID_RESPONSE = "response";
    public static final Fields FIELDS_SET_REQUEST = AbstractTopology.fieldMessage;
    public static final Fields FIELDS_SET_RESPONSE = AbstractTopology.fieldMessage;

    private static final Logger logger = LoggerFactory.getLogger(VerificationJointBolt.class);

    @Override
    protected void handleInput(Tuple input) {
        Object unclassified = input.getValueByField(VerificationBolt.FIELD_ID_PAYLOAD);

        if (unclassified instanceof UniflowVerificationRequest) {
            handleRequest(input, (UniflowVerificationRequest) unclassified);
        } else if (unclassified instanceof UniFlowVerificationResponse) {
            handleResponse(input, (UniFlowVerificationResponse) unclassified);
        } else {
            logger.warn(
                    "Unexpected input {} - is topology changes without code change?",
                    unclassified.getClass().getName());
        }
    }

    private void handleRequest(Tuple input, UniflowVerificationRequest request) {
        // TODO
    }

    private void handleResponse(Tuple input, UniFlowVerificationResponse response) {
        // TODO
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputManager) {
        outputManager.declareStream(STREAM_ID_REQUEST, FIELDS_SET_REQUEST);
        outputManager.declareStream(STREAM_ID_RESPONSE, FIELDS_SET_RESPONSE);
    }
}
