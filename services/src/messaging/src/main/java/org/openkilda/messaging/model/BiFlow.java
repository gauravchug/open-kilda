package org.openkilda.messaging.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BiFlow {
    private String flowId;

    // FIXME(surabujin): String field is worse possible representation of time
    // private String lastUpdated;

    private int bandwidth;
    private boolean ignoreBandwidth;

    private long cookie;

    private String description;

    private Flow forward;
    private Flow reverse;

    public BiFlow(ImmutablePair<Flow, Flow> flowPair) {
        Flow primary = flowPair.getLeft();

        flowId = primary.getFlowId();
        bandwidth = primary.getBandwidth();
        ignoreBandwidth = primary.isIgnoreBandwidth();
        cookie = primary.getFlagglessCookie();
        description = primary.getDescription();

        forward = flowPair.getLeft();
        reverse = flowPair.getRight();
    }
}
