package org.bitbucket.openkilda.wfm.topology.flow.bolts;

import static org.bitbucket.openkilda.messaging.Utils.MAPPER;

import org.bitbucket.openkilda.messaging.Destination;
import org.bitbucket.openkilda.messaging.Message;
import org.bitbucket.openkilda.messaging.Utils;
import org.bitbucket.openkilda.wfm.topology.AbstractTopology;
import org.bitbucket.openkilda.wfm.topology.flow.ComponentType;
import org.bitbucket.openkilda.wfm.topology.flow.StreamType;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.util.Map;

/**
 * Northbound Reply Bolt. Forms northbound replies.
 */
public class NorthboundReplyBolt extends BaseRichBolt {
    /**
     * The logger.
     */
    private static final Logger logger = LogManager.getLogger(NorthboundReplyBolt.class);

    /**
     * Output collector.
     */
    private OutputCollector outputCollector;

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(Tuple tuple) {
        ComponentType componentId = ComponentType.valueOf(tuple.getSourceComponent());
        StreamType streamId = StreamType.valueOf(tuple.getSourceStreamId());
        Message message = (Message) tuple.getValueByField(AbstractTopology.MESSAGE_FIELD);
        Values values = null;

        try {
            logger.debug("Request tuple={}", tuple);

            switch (componentId) {

                case TOPOLOGY_ENGINE_BOLT:
                case CRUD_BOLT:
                case ERROR_BOLT:
                    logger.debug("Flow response: {}={}, component={}, stream={}, message={}",
                            Utils.CORRELATION_ID, message.getCorrelationId(), componentId, streamId, message);

                    message.setDestination(Destination.NORTHBOUND);
                    values = new Values(MAPPER.writeValueAsString(message));
                    outputCollector.emit(StreamType.RESPONSE.toString(), tuple, values);

                    break;

                default:
                    logger.debug("Flow unknown response: {}={}, component={}, stream={}, message={}",
                            Utils.CORRELATION_ID, message.getCorrelationId(), componentId, streamId, message);
                    break;
            }
        } catch (JsonProcessingException exception) {
            logger.error("Could not serialize message: component={}, stream={}, message={}",
                    componentId, streamId, message);
        } finally {
            logger.debug("Northbound-Reply message ack: component={}, stream={}, tuple={}, values={}",
                    tuple.getSourceComponent(), tuple.getSourceStreamId(), tuple, values);

            outputCollector.ack(tuple);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declareStream(StreamType.RESPONSE.toString(), AbstractTopology.fieldMessage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector outputCollector) {
        this.outputCollector = outputCollector;
    }
}

