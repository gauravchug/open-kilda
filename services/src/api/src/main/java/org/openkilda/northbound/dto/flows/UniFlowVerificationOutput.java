package org.openkilda.northbound.dto.flows;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonSerialize
public class UniFlowVerificationOutput {
    @JsonProperty("ping_success")
    private boolean pingSuccess;

    @JsonProperty("error")
    private String error;

    @JsonCreator
    public UniFlowVerificationOutput(
            @JsonProperty("ping_success") boolean pingSuccess,
            @JsonProperty("error") String error) {
        this.pingSuccess = pingSuccess;
        this.error = error;
    }
}
