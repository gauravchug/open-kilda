package org.openkilda.messaging.command.flow;

import org.openkilda.messaging.command.CommandData;

import lombok.Value;

@Value
public class FlowVerificationRequest extends CommandData {
    private String flowId;
}
