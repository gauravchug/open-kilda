package org.openkilda.wfm;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public abstract class AbstractBolt extends BaseRichBolt {
    private static final Logger logger = LoggerFactory.getLogger(AbstractBolt.class);

    private OutputCollector output;

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

    protected abstract void handleInput(Tuple input);

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.output = collector;
    }

    protected OutputCollector getOutput() {
        return output;
    }
}
