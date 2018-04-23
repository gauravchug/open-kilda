package org.openkilda.floodlight.operation;

import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFType;

import java.util.Set;

public abstract class Operation {
    public abstract Set<OFType> getHandlingOfTypes();
    public abstract boolean ofInput(OFMessage input);
    public abstract void ioComplete();
}
