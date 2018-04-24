package org.openkilda.floodlight.operation;

import org.openkilda.floodlight.IoRecord;
import org.projectfloodlight.openflow.protocol.OFMessage;

import java.util.List;

// TODO
public class FlowVerificationSendOperation extends Operation {

    @Override
    public boolean ofInput(OFMessage input) {
        return false;
    }

    @Override
    public void ioComplete(List<IoRecord> payload, boolean isError) {

    }
}
