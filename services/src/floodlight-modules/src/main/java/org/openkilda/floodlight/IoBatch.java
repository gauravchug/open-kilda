package org.openkilda.floodlight;

import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFType;

import java.util.List;

public class IoBatch {
    private final long barrierXid;
    private final List<IoRecord> batch;
    private boolean errors = false;
    private boolean complete = false;

    public IoBatch(long barrierXid, List<IoRecord> batch) {
        this.barrierXid = barrierXid;
        this.batch = batch;
    }

    boolean handleResponse(OFMessage response) {
        boolean match = false;

        if (response.getType() == OFType.BARRIER_REPLY) {
            match = complete = barrierXid == response.getXid();
        } else if (response.getType() == OFType.ERROR) {
            match = handleErrorResponse(response);
        } else {
            throw new IllegalArgumentException(
                    String.format("%s can\'t handle %s type", getClass().getName(), response.getType()));
        }

        return match;
    }

    private boolean handleErrorResponse(OFMessage response) {
        boolean match = false;

        long xid = response.getXid();
        for (IoRecord request : batch) {
            if (request.getXid() != xid) {
                continue;
            }

            request.setResponse(response);
            errors = true;
            match = true;

            break;
        }

        return match;
    }

    public boolean isComplete() {
        return complete;
    }

    public boolean isErrors() {
        return errors;
    }

    public List<IoRecord> getBatch() {
        return batch;
    }
}
