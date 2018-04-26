package org.openkilda.messaging.info.flow;

import org.openkilda.messaging.model.Flow;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Value;

@Value
@JsonSerialize
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UniFlowVerificationResponse {
    @JsonProperty("flow")
    private Flow flow;

    @JsonProperty("ping_success")
    private boolean pingSuccess;

    @JsonProperty("error")
    private FlowVerificationErrorCode error;

    @JsonCreator
    public UniFlowVerificationResponse(
            @JsonProperty("flow") Flow flow,
            @JsonProperty("ping_success") boolean pingSuccess,
            @JsonProperty("error") FlowVerificationErrorCode error) {
        this.flow = flow;
        this.pingSuccess = pingSuccess;
        this.error = error;
    }

    public UniFlowVerificationResponse(Flow flow) {
        this(flow, true, null);
    }

    public UniFlowVerificationResponse(Flow flow, FlowVerificationErrorCode error) {
        this(flow, false, error);
    }
}
