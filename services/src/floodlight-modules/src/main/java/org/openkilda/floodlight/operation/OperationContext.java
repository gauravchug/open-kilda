package org.openkilda.floodlight.operation;

import net.floodlightcontroller.core.module.FloodlightModuleContext;

import java.util.UUID;

public class OperationContext {
    private final FloodlightModuleContext moduleContext;
    private final long ctime;
    private final String correlationId;

    public OperationContext(FloodlightModuleContext flcontext) {
        this(flcontext, UUID.randomUUID().toString());
    }

    public OperationContext(FloodlightModuleContext moduleContext, String correlationId) {
        this.moduleContext = moduleContext;
        this.ctime = System.currentTimeMillis();
        this.correlationId = correlationId;
    }

    public FloodlightModuleContext getModuleContext() {
        return moduleContext;
    }

    public long getCtime() {
        return ctime;
    }

    public String getCorrelationId() {
        return correlationId;
    }
}
