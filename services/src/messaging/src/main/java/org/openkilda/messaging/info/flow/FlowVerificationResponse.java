package org.openkilda.messaging.info.flow;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.openkilda.messaging.info.InfoData;
import org.openkilda.messaging.model.Flow;

@JsonSerialize
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FlowVerificationResponse extends InfoData {
    @JsonProperty("flow")
    private final Flow flow;

    @JsonProperty("ping_success")
    private final boolean pingSuccess;

    @JsonProperty("error")
    private final FlowVerificationErrorCode error;

    @JsonCreator
    public FlowVerificationResponse(
            @JsonProperty("flow") Flow flow,
            @JsonProperty("ping_success") boolean pingSuccess,
            @JsonProperty("error") FlowVerificationErrorCode error) {
        this.flow = flow;
        this.pingSuccess = pingSuccess;
        this.error = error;
    }

    public FlowVerificationResponse(Flow flow) {
        this(flow, true, null);
    }

    public FlowVerificationResponse(Flow flow, FlowVerificationErrorCode error) {
        this(flow, false, error);
    }

    public Flow getFlow() {
        return flow;
    }

    public boolean isPingSuccess() {
        return pingSuccess;
    }

    public FlowVerificationErrorCode getError() {
        return error;
    }
}
