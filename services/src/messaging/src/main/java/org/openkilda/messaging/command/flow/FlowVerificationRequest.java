package org.openkilda.messaging.command.flow;

import org.openkilda.messaging.command.CommandData;
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
public class FlowVerificationRequest extends CommandData {
    @JsonProperty("flow")
    private final Flow flow;

    @JsonProperty("packet_id")
    private final UUID packetId;

    @JsonCreator
    public FlowVerificationRequest(
            @JsonProperty("flow") Flow flow,
            @JsonProperty("packet_id") UUID packetId) {
        this.flow = flow;

        if (packetId == null) {
            packetId = UUID.randomUUID();
        }
        this.packetId = packetId;
    }

    public FlowVerificationRequest(Flow flow) {
        this(flow, null);
    }
}
