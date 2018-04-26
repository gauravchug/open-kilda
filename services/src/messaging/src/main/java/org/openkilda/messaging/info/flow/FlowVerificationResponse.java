package org.openkilda.messaging.info.flow;

import org.openkilda.messaging.info.InfoData;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class FlowVerificationResponse extends InfoData {
    private String flowId;
    private UniFlowVerificationResponse forward;
    private UniFlowVerificationResponse reverse;
}
