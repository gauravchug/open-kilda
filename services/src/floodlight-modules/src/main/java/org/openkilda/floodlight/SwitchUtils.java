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
        return MacAddress.of(Arrays.copyOfRange(sw.getId().getBytes(), 2, 8));
    }
}
