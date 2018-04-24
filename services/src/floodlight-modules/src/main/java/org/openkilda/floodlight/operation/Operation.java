package org.openkilda.floodlight.operation;

import org.openkilda.floodlight.IoRecord;
import org.projectfloodlight.openflow.protocol.OFMessage;

import java.util.List;

public abstract class Operation {
    public abstract boolean ofInput(OFMessage input);
    public abstract void ioComplete(List<IoRecord> payload, boolean isError);
}
