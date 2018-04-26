package org.openkilda.wfm.topology.flow.bolts;

import org.openkilda.messaging.Utils;
import org.openkilda.messaging.model.Flow;
import org.openkilda.messaging.model.ImmutablePair;
import org.openkilda.wfm.topology.AbstractTopology;
import org.openkilda.wfm.topology.flow.ComponentType;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import javax.ws.rs.core.GenericType;

public class VerificationBolt implements IRichBolt {
    public static final String FIELD_ID_PAYLOAD = "payload";

    public static final String STREAM_ID_REQUEST = "request";
    public static final String STREAM_ID_OUTPUT = "output";

    public static final Fields FIELDS_SET_REQUEST = AbstractTopology.fieldMessage;
    public static final Fields FIELDS_SET_OUTPUT = new Fields(Utils.FLOW_ID, FIELD_ID_PAYLOAD);

    private static final Logger logger = LoggerFactory.getLogger(VerificationBolt.class);

    private OutputCollector output = null;

    @Override
    public void execute(Tuple input) {
        try {
            handleInput(input);
        } catch (Exception e) {
            logger.error(String.format("Unhandled exception in %s", getClass().getName()), e);
        } finally {
            output.ack(input);
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputManager) {
        outputManager.declareStream(STREAM_ID_REQUEST, FIELDS_SET_REQUEST);
        outputManager.declareStream(STREAM_ID_OUTPUT, FIELDS_SET_OUTPUT);
    }

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.output = collector;
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }

    @Override
    public void cleanup() {

    }

    private void handleInput(Tuple input) {
        String source = input.getSourceComponent();

        if (source.equals(ComponentType.CRUD_BOLT.toString())) {
            producePings(input);
        } else if (source.equals(ComponentType.SPEAKER_SPOUT.toString())) {
            consumePingReply(input);
        } else {
            logger.warn("Unexpected input from {} - is topology changes without code change?", source);
        }
    }

    private void producePings(Tuple input) {
        String flowId = input.getStringByField(CrudBolt.FIELD_ID_FLOW_ID);
        GenericType<ImmutablePair<Flow, Flow>> biflowType = new GenericType<ImmutablePair<Flow, Flow>>() {};
        // TODO
        ImmutablePair<Flow, Flow> biFlow = fetchInput(input, CrudBolt.FIELD_ID_BIFLOW, ImmutablePair.class);
        biFlow.getLeft().getFlowId();
    }

    private void consumePingReply(Tuple input) {

    }

    private <T> T fetchInput(Tuple input, String field, Class<T> type) {
        T value;
        try {
            value = type.cast(input.getValueByField(field));
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(String.format("Can't cast field %s: %s", field, e));
        }
        return value;
    }
}
