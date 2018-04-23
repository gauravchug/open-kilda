package org.openkilda.floodlight;

import org.projectfloodlight.openflow.protocol.OFMessage;

public class IoRecord {
    private final boolean pending = true;
    private final OFMessage request;
    private OFMessage response;
    private long xid;

    public IoRecord(OFMessage request) {
        this.request = request;
        this.xid = request.getXid();
    }

    public boolean isPending() {
        return pending;
    }

    public long getXid() {
        return xid;
    }

    public OFMessage getRequest() {
        return request;
    }

    public OFMessage getResponse() {
        return response;
    }

    public void setResponse(OFMessage response) {
        this.response = response;
    }
}
