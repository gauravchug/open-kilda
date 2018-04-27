package org.openkilda.wfm.topology.flow;

import lombok.Data;

@Data
public class Constants {
    public static Constants instance;

    private long verificationRequestTimeoutMsec = 300 * 1000;
}
