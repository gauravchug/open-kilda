package org.bitbucket.openkilda.messaging.payload.response;

import static com.google.common.base.Objects.toStringHelper;

import org.bitbucket.openkilda.messaging.payload.FlowPayload;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * Represents all flows northbound response.
 */
@JsonSerialize
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"flow-list"})
public class FlowsResponsePayload implements Serializable {
    /**
     * Serialization version number constant.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The name of the flow.
     */
    @JsonProperty("flow-list")
    protected List<FlowPayload> flowList;

    /**
     * Default constructor.
     */
    public FlowsResponsePayload() {
    }

    /**
     * Constructs instance.
     *
     * @param   flowList  flow list
     */
    @JsonCreator
    public FlowsResponsePayload(@JsonProperty("flow-list") final List<FlowPayload> flowList) {
        setFlowList(flowList);
    }

    /**
     * Returns list of flows.
     *
     * @return  flows list.
     */
    public List<FlowPayload> getFlowList() {
        return flowList;
    }

    /**
     * Sets list of flows.
     *
     * @param   flowList  flows list.
     */
    public void setFlowList(final List<FlowPayload> flowList) {
        this.flowList = flowList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return toStringHelper(this)
                .add("flow-list", flowList)
                .toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(flowList);
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

        FlowsResponsePayload that = (FlowsResponsePayload) object;
        return Objects.equals(getFlowList(), that.getFlowList());
    }
}
