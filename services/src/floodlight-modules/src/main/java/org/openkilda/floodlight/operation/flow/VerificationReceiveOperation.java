package org.openkilda.floodlight.operation.flow;

import org.openkilda.floodlight.service.FlowVerificationRecipientService;
import org.openkilda.floodlight.operation.Operation;
import org.openkilda.floodlight.operation.OperationContext;
import org.openkilda.messaging.model.Flow;

import net.floodlightcontroller.core.module.FloodlightModuleContext;
import org.projectfloodlight.openflow.types.DatapathId;

public class VerificationReceiveOperation extends Operation {
    private final Flow flow;

    private final FlowVerificationRecipientService flowVerificationRecipientService;

    public VerificationReceiveOperation(OperationContext context, Flow flow) {
        super(context);
        this.flow = flow;

        FloodlightModuleContext moduleContext = getContext().getModuleContext();
        flowVerificationRecipientService = moduleContext.getServiceImpl(FlowVerificationRecipientService.class);
    }

    @Override
    public void run() {
        // TODO
    }

    public boolean packetIn(DatapathId sw, VerificationData payload) {

    }
}
