package org.openkilda.wfm.topology.flow.model;

import org.openkilda.messaging.command.flow.FlowDirection;
import org.openkilda.messaging.command.flow.UniFlowVerificationRequest;
import org.openkilda.messaging.info.flow.FlowVerificationErrorCode;
import org.openkilda.messaging.info.flow.FlowVerificationResponse;
import org.openkilda.messaging.info.flow.UniFlowVerificationResponse;
import org.openkilda.messaging.model.BiFlow;
import org.openkilda.messaging.model.Flow;
import org.openkilda.wfm.topology.flow.Constants;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class VerificationWaitRecord {
    private final Constants constants = Constants.instance;

    private final long createTime;
    private final String correlationId;
    private final FlowVerificationResponse.FlowVerificationResponseBuilder response;

    private final HashMap<UUID, PendingRecord> pendingRequests = new HashMap<>();
    private final HashSet<FlowDirection> needResponsesFor = new HashSet<>();

    public VerificationWaitRecord(BiFlow request, String correlationId) {
        this.createTime = System.currentTimeMillis();
        this.correlationId = correlationId;

        this.response = FlowVerificationResponse.builder();
        this.response.flowId(request.getFlowId());

        addPending(FlowDirection.FORWARD, request.getForward());
        addPending(FlowDirection.REVERSE, request.getReverse());
    }

    public boolean consumeResponse(UniFlowVerificationResponse payload) {
        PendingRecord pending = pendingRequests.remove(payload.getPacketId());
        if (pending == null) {
            return false;
        }

        saveUniResponse(pending, payload);
        return true;
    }

    public FlowVerificationResponse produce() {
        return response.build();
    }

    public void fillPendingWithError(FlowVerificationErrorCode errorCode) {
        UniFlowVerificationResponse errorResponse;
        for (UUID packetId : pendingRequests.keySet()) {
            PendingRecord pending = pendingRequests.get(packetId);

            errorResponse = new UniFlowVerificationResponse(packetId, pending.request.getFlow(), errorCode);
            saveUniResponse(pending, errorResponse);
        }
        pendingRequests.clear();
    }

    public boolean isFilled() {
        return pendingRequests.size() == 0;
    }

    public boolean isOutdated(long currentTime) {
        long outdatedLimit = currentTime - constants.getVerificationRequestTimeoutMsec();
        if (outdatedLimit < 0) {
            outdatedLimit = 0;
        }

        return createTime < outdatedLimit;
    }

    public List<UniFlowVerificationRequest> getPendingRequests() {
        return pendingRequests.values().stream()
                .map(pending -> pending.request)
                .collect(Collectors.toList());
    }

    public String getCorrelationId() {
        return correlationId;
    }

    private void addPending(FlowDirection direction, Flow flow) {
        UniFlowVerificationRequest request = new UniFlowVerificationRequest(flow);
        PendingRecord pending = new PendingRecord(direction, request);
        pendingRequests.put(request.getPacketId(), pending);
    }

    private void saveUniResponse(PendingRecord pending, UniFlowVerificationResponse payload) {
        switch (pending.direction) {
            case FORWARD:
                response.forward(payload);
                break;
            case REVERSE:
                response.forward(payload);
                break;
            default:
                throw new IllegalArgumentException(
                        String.format("Unhandled flow direction value: %s", pending.direction));
        }
    }

    private static class PendingRecord {
        final FlowDirection direction;
        final UniFlowVerificationRequest request;

        PendingRecord(FlowDirection direction, UniFlowVerificationRequest request) {
            this.direction = direction;
            this.request = request;
        }
    }
}
