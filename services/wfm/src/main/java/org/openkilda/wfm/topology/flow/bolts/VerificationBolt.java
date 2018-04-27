package org.openkilda.wfm.topology.flow.bolts;

import org.apache.storm.tuple.Values;
import org.openkilda.messaging.Utils;
import org.openkilda.messaging.info.flow.UniFlowVerificationResponse;
import org.openkilda.wfm.AbstractBolt;
import org.openkilda.wfm.topology.AbstractTopology;
import org.openkilda.wfm.topology.flow.ComponentType;

import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.openkilda.messaging.Utils.MAPPER;

public class VerificationBolt extends AbstractBolt {
    public static final String FIELD_ID_FLOW_ID = Utils.FLOW_ID;
    public static final String FIELD_ID_OUTPUT = "payload";
    public static final String FIELD_ID_INPUT = AbstractTopology.MESSAGE_FIELD;

    public static final String STREAM_ID_PROXY = "proxy";
    public static final Fields STREAM_FIELDS_PROXY = new Fields(FIELD_ID_FLOW_ID, FIELD_ID_OUTPUT, FIELD_ID_INPUT);

    private static final Logger logger = LoggerFactory.getLogger(VerificationBolt.class);

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputManager) {
        outputManager.declareStream(STREAM_ID_PROXY, STREAM_FIELDS_PROXY);
    }

    @Override
    protected void handleInput(Tuple input) {
        String source = input.getSourceComponent();

        if (source.equals(ComponentType.CRUD_BOLT.toString())) {
            proxyRequest(input);
        } else if (source.equals(ComponentType.SPEAKER_SPOUT.toString())) {
            consumePingReply(input);
        } else {
            logger.warn("Unexpected input from {} - is topology changes without code change?", source);
        }
    }

    private void proxyRequest(Tuple input) {
        String flowId = input.getStringByField(CrudBolt.FIELD_ID_FLOW_ID);

        Values proxyData = new Values(
                flowId,
                input.getValueByField(CrudBolt.FIELD_ID_BIFLOW),
                input.getValueByField(CrudBolt.FIELD_ID_MESSAGE));
        getOutput().emit(STREAM_ID_PROXY, input, proxyData);
    }

    private void consumePingReply(Tuple input) {
        UniFlowVerificationResponse response;
        try {
            response = fetchUniFlowResponse(input);
        } catch (IllegalArgumentException e) {
            // not our response, just some other kind of message in message bus
            return;
        }

        Values payload = new Values(response.getFlow().getFlowId(), response);
        getOutput().emit(STREAM_ID_PROXY, input, payload);
    }

    private UniFlowVerificationResponse fetchUniFlowResponse(Tuple input) {
        String json = input.getStringByField(AbstractTopology.MESSAGE_FIELD);
        UniFlowVerificationResponse value;
        try {
            value = MAPPER.readValue(json, UniFlowVerificationResponse.class);
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    String.format("Can't deserialize into %s", UniFlowVerificationResponse.class.getName()), e);
        }

        return value;
    }
}
