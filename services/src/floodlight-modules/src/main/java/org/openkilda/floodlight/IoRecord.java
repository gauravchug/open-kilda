package org.openkilda.floodlight;

import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.types.DatapathId;

public class IoRecord {
    private final DatapathId dpId;
    private final OFMessage request;
    private long xid;
    private OFMessage response = null;
    private boolean pending = true;

    public IoRecord(DatapathId dpId, OFMessage request) {
        this.dpId = dpId;
        this.request = request;
        this.xid = request.getXid();
    }

    public boolean isPending() {
        return pending;
    }

    public long getXid() {
        return xid;
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

    public void setResponse(OFMessage response) {
        pending = false;
        this.response = response;
    }
}
