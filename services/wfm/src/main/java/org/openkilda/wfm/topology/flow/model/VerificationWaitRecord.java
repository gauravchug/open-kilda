package org.openkilda.wfm.topology.flow.model;

import org.openkilda.messaging.command.flow.FlowDirection;
import org.openkilda.messaging.command.flow.UniflowVerificationRequest;
import org.openkilda.messaging.info.flow.FlowVerificationResponse;
import org.openkilda.messaging.info.flow.UniFlowVerificationResponse;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

// TODO
public class VerificationWaitRecord {
    private final long createTime;
    private final FlowVerificationResponse.FlowVerificationResponseBuilder response;

    private final HashMap<UUID, FlowDirection> pendingRequests = new HashMap<>();
    private final HashSet<FlowDirection> haveResponsesFor = new HashSet<>();

    private static final HashSet<FlowDirection> allSet = new HashSet<>();
    static {
        allSet.add(FlowDirection.FORWARD);
        allSet.add(FlowDirection.REVERSE);
    }

    public VerificationWaitRecord(UniflowVerificationRequest request) {
        this.createTime = System.currentTimeMillis();

        this.response = FlowVerificationResponse.builder();
        this.response.flowId(request.getFlow().getFlowId());

        this.pendingRequests.add(request.getPacketId());
    }

    public boolean receive(UniFlowVerificationResponse payload) {
        if (payload.)
    }
}
