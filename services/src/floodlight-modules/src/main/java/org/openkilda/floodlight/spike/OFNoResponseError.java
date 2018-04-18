package org.openkilda.floodlight.spike;

import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.types.DatapathId;

public class OFNoResponseError extends AbstractOfModError {
    public OFNoResponseError(DatapathId dpId, OFMessage payload) {
        super(dpId, payload, makeErrorMessage(dpId, payload));
    }

    private static String makeErrorMessage(DatapathId dpId, OFMessage payload) {
        return String.format("there was no response on OFMessage (%s <= %s)", dpId, payload);
    }
}
