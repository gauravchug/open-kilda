package org.openkilda.messaging.info.flow;

import org.openkilda.messaging.info.InfoData;

import lombok.Value;

@Value
public class FlowVerificationResponse extends InfoData {
    private String flowId;
    private UniFlowVerificationResponse forward;
    private UniFlowVerificationResponse reverse;
}
