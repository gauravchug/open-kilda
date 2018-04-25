package org.openkilda.floodlight;

import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.MacAddress;

import java.util.Arrays;

public class SwitchUtils {
    private final IOFSwitchService switchService;

    public SwitchUtils(IOFSwitchService switchService) {
        this.switchService = switchService;
    }

    public IOFSwitch lookupSwitch(DatapathId dpId) {
        IOFSwitch swInfo = switchService.getSwitch(dpId);
        if (swInfo == null) {
            throw new IllegalArgumentException(String.format("Switch %s not found", dpId));
        }
        return swInfo;
    }

    public MacAddress dpIdToMac(final IOFSwitch sw) {
        return this.dpIdToMac(sw.getId());
    }

    public MacAddress dpIdToMac(final DatapathId dpId) {
        return MacAddress.of(Arrays.copyOfRange(dpId.getBytes(), 2, 8));
    }
}
