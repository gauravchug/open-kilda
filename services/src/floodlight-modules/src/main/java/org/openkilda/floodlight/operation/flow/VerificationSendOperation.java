package org.openkilda.floodlight.operation.flow;

import org.openkilda.floodlight.IoRecord;
import org.openkilda.floodlight.IoService;
import org.openkilda.floodlight.operation.Operation;
import org.openkilda.floodlight.operation.OperationContext;
import org.openkilda.messaging.model.Flow;

import net.floodlightcontroller.core.module.FloodlightModuleContext;

import java.util.List;

public class VerificationSendOperation extends Operation {
    private final Flow flow;

    private final IoService ioService;

    public VerificationSendOperation(OperationContext context, Flow flow) {
        super(context);
        this.flow = flow;

        FloodlightModuleContext moduleContext = getContext().getModuleContext();
        this.ioService = moduleContext.getServiceImpl(IoService.class);
    }

    @Override
    public void run() {

    }

    @Override
    public void ioComplete(List<IoRecord> payload, boolean isError) {

    }
}
