package org.openkilda.messaging.command.flow;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.openkilda.messaging.command.CommandData;
import org.openkilda.messaging.model.Flow;

import java.util.UUID;

@JsonSerialize
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FlowVerifycationRequest extends CommandData {
    @JsonProperty("flow")
    private final Flow flow;

    @JsonProperty("packet_id")
    private final UUID packetId;

    @JsonCreator
    public FlowVerifycationRequest(
            @JsonProperty("flow") Flow flow,
            @JsonProperty("packet_id") UUID packetId) {
        this.flow = flow;

        if (packetId == null) {
            packetId = UUID.randomUUID();
        }
        this.packetId = packetId;
    }

    public Flow getFlow() {
        return flow;
    }

    public UUID getPacketId() {
        return packetId;
    }
}
