package org.openkilda.northbound.dto.flows;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class VerificationOutput {
    @JsonProperty("flow_id")
    private String flowId;

    @JsonProperty("forward")
    private UniFlowVerificationOutput forward;

    @JsonProperty("reverser")
    private UniFlowVerificationOutput reverse;

    @JsonCreator
    public VerificationOutput(
            @JsonProperty("flow_id") String flowId,
            @JsonProperty("forward") UniFlowVerificationOutput forward,
            @JsonProperty("reverse") UniFlowVerificationOutput reverse) {
        this.flowId = flowId;
        this.forward = forward;
        this.reverse = reverse;
    }
}
