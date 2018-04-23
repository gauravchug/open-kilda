package org.openkilda.floodlight.spike;

import org.projectfloodlight.openflow.types.DatapathId;

public class InSwitchFlow {
    private final DatapathId dpId;
    private final FlowEndpoint left;
    private final FlowEndpoint right;

    public InSwitchFlow(DatapathId dpId, FlowEndpoint left, FlowEndpoint right) {
        this.dpId = dpId;
        this.left = left;
        this.right = right;
    }

    public InSwitchFlow reverse() {
        return new InSwitchFlow(getDpId(), getRight(), getLeft());
    }


    public DatapathId getDpId() {
        return dpId;
    }

    public FlowEndpoint getLeft() {
        return left;
    }

    public FlowEndpoint getRight() {
        return right;
    }
}
