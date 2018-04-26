package org.openkilda.messaging.info.flow;

import org.openkilda.messaging.model.Flow;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Value;

import java.util.UUID;

@Value
@JsonSerialize
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UniFlowVerificationResponse {
    @JsonProperty("packet_id")
    private final UUID packetId;

    @JsonProperty("flow")
    private Flow flow;

    @JsonProperty("ping_success")
    private boolean pingSuccess;

    @JsonProperty("error")
    private FlowVerificationErrorCode error;

    @JsonCreator
    public UniFlowVerificationResponse(
            @JsonProperty("packet_id") UUID packetId,
            @JsonProperty("flow") Flow flow,
            @JsonProperty("ping_success") boolean pingSuccess,
            @JsonProperty("error") FlowVerificationErrorCode error) {
        this.packetId = packetId;
        this.flow = flow;
        this.pingSuccess = pingSuccess;
        this.error = error;
    }

    public UniFlowVerificationResponse(UUID packetId, Flow flow) {
        this(packetId, flow, true, null);
    }

    public UniFlowVerificationResponse(UUID packetId, Flow flow, FlowVerificationErrorCode error) {
        this(packetId, flow, false, error);
    }
}
