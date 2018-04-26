package org.openkilda.wfm.topology.flow.bolts;

import org.openkilda.wfm.topology.AbstractTopology;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;

import java.util.Map;

public class VerificationJointBolt implements IRichBolt {
    public static final String STREAM_ID_RESPONSE = "response";
    public static final Fields FIELDS_SET_RESPONSE = AbstractTopology.fieldMessage;

    private OutputCollector output;

    @Override
    public void execute(Tuple input) {

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputManager) {
        outputManager.declareStream(STREAM_ID_RESPONSE, FIELDS_SET_RESPONSE);
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
}
