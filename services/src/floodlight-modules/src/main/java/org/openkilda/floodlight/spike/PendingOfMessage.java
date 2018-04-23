package org.openkilda.floodlight.spike;

import net.floodlightcontroller.core.IOFSwitch;
import org.openkilda.floodlight.SwitchUtils;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.types.DatapathId;

public class PendingOfMessage {
    private final int MAX_WRITE_ERROR = 5;

    private final DatapathId dpId;
    private final OFMessage request;
    private OFMessage response = null;
    private final long xid;
    private boolean isInstalled = false;
    private int writeErrors = 0;

    public PendingOfMessage(DatapathId dpId, OFMessage request) {
        this.dpId = dpId;
        this.request = request;
        this.xid = request.getXid();
    }

    public void install(SwitchUtils switchUtils) throws SwitchWriteError {
        if (isInstalled) {
            return;
        }

        IOFSwitch sw = switchUtils.lookupSwitch(getDpId());
        isInstalled = sw.write(request);

        if (!isInstalled) {
            writeErrors += 1;
            if (MAX_WRITE_ERROR <= writeErrors) {
                throw new SwitchWriteError(getDpId(), getRequest());
            } else {
                throw new SwitchWriteRepeatableError(getDpId(), getRequest());
            }
        }
    }

    public boolean response(OFMessage payload) {
        if (payload.getXid() != getXid()) {
            return false;
        }
        response = payload;
        return true;
    }

    public DatapathId getDpId() {
        return dpId;
    }

    public OFMessage getRequest() {
        return request;
    }

    public OFMessage getResponse() {
        return response;
    }

    public long getXid() {
        return xid;
    }

    public int getWriteErrors() {
        return writeErrors;
    }
}
