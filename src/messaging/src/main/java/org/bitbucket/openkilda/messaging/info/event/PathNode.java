package org.bitbucket.openkilda.messaging.info.event;

import static com.google.common.base.Objects.toStringHelper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.Serializable;
import java.util.Objects;

/**
 * Defines the payload payload of a Message representing a path node info.
 */
@JsonSerialize
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "switch_id",
        "port_no",
        "seq_id",
        "segment_latency"})
public class PathNode implements Serializable {
    /**
     * Serialization version number constant.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Switch id.
     */
    @JsonProperty("switch_id")
    private String switchId;

    /**
     * Port number.
     */
    @JsonProperty("port_no")
    private int portNo;

    /**
     * Sequence id.
     */
    @JsonProperty("seq_id")
    private int seqId;

    /**
     * Segment latency.
     */
    @JsonInclude(JsonInclude.Include.NON_DEFAULT) // Needed to exclude when not set
    @JsonProperty("segment_latency")
    private Long segmentLatency;

    /**
     * Default constructor.
     */
    public PathNode() {
    }

    /**
     * Instance creator without segment latency value.
     *
     * @param   switchId  switch id
     * @param   portNo    port number
     * @param   seqId     sequence id
     */
    public PathNode(final String switchId, final int portNo, final int seqId) {
        this.switchId = switchId;
        this.portNo = portNo;
        this.seqId = seqId;
    }

    /**
     * Instance creator.
     *
     * @param   switchId        switch id
     * @param   portNo          port number
     * @param   seqId           sequence id
     * @param   segmentLatency  segment latency
     */
    @JsonCreator
    public PathNode(@JsonProperty("switch_id") final String switchId,
                    @JsonProperty("port_no") final int portNo,
                    @JsonProperty("seq_id") final int seqId,
                    @JsonProperty("segment_latency") final Long segmentLatency) {
        this.switchId = switchId;
        this.portNo = portNo;
        this.seqId = seqId;
        this.segmentLatency = segmentLatency;
    }

    /**
     * Returns switch id.
     *
     * @return  switch id
     */
    @JsonProperty("switch_id")
    public String getSwitchId() {
        return switchId;
    }

    /**
     * Sets switch id.
     *
     * @param   switchId  switch id to set
     */
    @JsonProperty("switch_id")
    public void setSwitchId(final String switchId) {
        this.switchId = switchId;
    }

    /**
     * Returns port number.
     *
     * @return  port number
     */
    @JsonProperty("port_no")
    public int getPortNo() {
        return portNo;
    }

    /**
     * Sets port number.
     *
     * @param   portNo  port number to set
     */
    @JsonProperty("port_no")
    public void setPortNo(final int portNo) {
        this.portNo = portNo;
    }

    /**
     * Returns sequence id.
     *
     * @return  sequence id
     */
    @JsonProperty("seq_id")
    public int getSeqId() {
        return seqId;
    }

    /**
     * Sets sequence id.
     *
     * @param   seqId  sequence id to set
     */
    @JsonProperty("seq_id")
    public void setSeqId(final int seqId) {
        this.seqId = seqId;
    }

    /**
     * Returns segment latency.
     *
     * @return  segment latency
     */
    @JsonProperty("segment_latency")
    public Long getSegLatency() {
        return segmentLatency;
    }

    /**
     * Sets segment latency.
     *
     * @param   latency  segment latency to set
     */
    @JsonProperty("segment_latency")
    public void setSegLatency(final long latency) {
        this.segmentLatency = latency;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return toStringHelper(this)
                .add("switch_id", switchId)
                .add("port_no", portNo)
                .add("seq_id", seqId)
                .add("segment_latency", segmentLatency)
                .toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(switchId, portNo, seqId, segmentLatency);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        PathNode that = (PathNode) object;
        return Objects.equals(getSwitchId(), that.getSwitchId())
                && Objects.equals(getPortNo(), that.getPortNo())
                && Objects.equals(getSeqId(), that.getSeqId())
                && Objects.equals(getSegLatency(), that.getSegLatency());
    }
}
