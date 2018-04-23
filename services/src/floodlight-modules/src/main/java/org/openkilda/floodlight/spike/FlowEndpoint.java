package org.openkilda.floodlight.spike;

import org.projectfloodlight.openflow.types.VlanVid;

public class FlowEndpoint {
    private final int portNumber;
    private final VlanVid vlan;

    public FlowEndpoint(int portNumber, VlanVid vlan) {
        this.portNumber = portNumber;
        this.vlan = vlan;
    }

    public int getPortNumber() {
        return portNumber;
    }

    public VlanVid getVlan() {
        return vlan;
    }
}
