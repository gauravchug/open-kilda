package org.openkilda.wfm.topology.flow.bolts;

import org.apache.storm.tuple.Values;
import org.openkilda.messaging.Utils;
import org.openkilda.messaging.command.flow.FlowDirection;
import org.openkilda.messaging.command.flow.UniflowVerificationRequest;
import org.openkilda.messaging.info.flow.UniFlowVerificationResponse;
import org.openkilda.messaging.model.Flow;
import org.openkilda.messaging.model.ImmutablePair;
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
    public static final String FIELD_ID_PAYLOAD = "payload";
    public static final String STREAM_ID_PROXY = "output";
    public static final Fields FIELDS_SET_PROXY = new Fields(Utils.FLOW_ID, FIELD_ID_PAYLOAD);

    private static final Logger logger = LoggerFactory.getLogger(VerificationBolt.class);

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputManager) {
        outputManager.declareStream(STREAM_ID_PROXY, FIELDS_SET_PROXY);
    }

    @Override
    protected void handleInput(Tuple input) {
        String source = input.getSourceComponent();

        if (source.equals(ComponentType.CRUD_BOLT.toString())) {
            producePings(input);
        } else if (source.equals(ComponentType.SPEAKER_SPOUT.toString())) {
            consumerPingReply(input);
        } else {
            logger.warn("Unexpected input from {} - is topology changes without code change?", source);
        }
    }

    private void producePings(Tuple input) {
        String flowId = input.getStringByField(CrudBolt.FIELD_ID_FLOW_ID);
        ImmutablePair<Flow, Flow> biFlow = fetchBiflow(input);

        Values paylod;

        paylod = new Values(flowId, new UniflowVerificationRequest(FlowDirection.FORWARD, biFlow.getLeft()));
        getOutput().emit(STREAM_ID_PROXY, input, paylod);
        paylod = new Values(flowId, new UniflowVerificationRequest(FlowDirection.REVERSE, biFlow.getRight()));
        getOutput().emit(STREAM_ID_PROXY, input, new Values(flowId, paylod));
    }

    private void consumerPingReply(Tuple input) {
        UniFlowVerificationResponse response;
        try {
            response = fetchUniflowResponse(input);
        } catch (IllegalArgumentException e) {
            // not our response, just some other kind of message in message bus
            return;
        }

        Values payload = new Values(response.getFlow().getFlowId(), response);
        getOutput().emit(STREAM_ID_PROXY, input, payload);
    }

    @SuppressWarnings("unchecked")
    private ImmutablePair<Flow, Flow> fetchBiflow(Tuple input) {
        return (ImmutablePair<Flow, Flow>) input.getValueByField(CrudBolt.FIELD_ID_BIFLOW);
    }

    private UniFlowVerificationResponse fetchUniflowResponse(Tuple input) {
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
