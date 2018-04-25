package org.openkilda.floodlight.operation;

import org.openkilda.floodlight.IoRecord;

import java.util.List;

public abstract class Operation implements Runnable {
    private final OperationContext context;

    public Operation(OperationContext context) {
        this.context = context;
    }

    public void ioComplete(List<IoRecord> payload, boolean isError) {
        throw new IllegalArgumentException("Can't handle IO response, because don't send any IO requests");
    }

    protected void startSubOperation(Operation operation) {
        operation.run();
    }

    protected OperationContext getContext() {
        return context;
    }
}
