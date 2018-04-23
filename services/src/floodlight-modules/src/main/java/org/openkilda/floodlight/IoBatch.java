package org.openkilda.floodlight;

import net.floodlightcontroller.core.IOFSwitch;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.types.DatapathId;

import javax.print.attribute.HashDocAttributeSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class IoBatch {
    private final SwitchUtils switchUtils;

    private final List<IoRecord> batch;
    private final List<IoRecord> barriers;
    private boolean errors = false;
    private boolean complete;

    public IoBatch(SwitchUtils switchUtils, List<IoRecord> batch) {
        this.switchUtils = switchUtils;

        HashSet<DatapathId> targets = new HashSet<>();
        this.batch = new ArrayList<>(batch.size() + 1);
        for (IoRecord record : batch) {
            targets.add(record.getDpId());
            this.batch.add(record);
        }

        this.barriers = new ArrayList<>(targets.size());
        for (DatapathId dpId : targets) {
            IOFSwitch sw = switchUtils.lookupSwitch(dpId);
            IoRecord record = new IoRecord(dpId, sw.getOFFactory().barrierRequest());
            barriers.add(record);
        }

        complete = 0 == barriers.size();
    }

    public void write() {
        HashMap<DatapathId, IOFSwitch> swMap = new HashMap<>();
        for (IoRecord record : barriers) {
            DatapathId dpId = record.getDpId();
            swMap.put(dpId, switchUtils.lookupSwitch(dpId));
        }

        // TODO
    }

    public boolean handleResponse(OFMessage response) {
        boolean match = true;

        if (saveResponse(barriers, response)) {
            updateBarriers();
        } else if (saveResponse(batch, response)) {
            errors = OFType.ERROR == response.getType();
        } else {
            match = false;
        }

        return match;
    }

    private boolean saveResponse(List<IoRecord> pending, OFMessage response) {
        long xid = response.getXid();
        for (IoRecord record : pending) {
            if (record.getXid() != xid) {
                continue;
            }

            record.setResponse(response);

            return true;
        }

        return false;
    }

    private void updateBarriers() {
        boolean allDone = true;

        for (IoRecord record : barriers) {
            if (record.isPending()) {
                allDone = false;
                break;
            }
        }

        if (allDone) {
            removePendingState();
            complete = true;
        }
    }

    private void removePendingState() {
        for (IoRecord record : batch) {
            if (record.isPending()) {
                record.setResponse(null);
            }
        }
    }

    public boolean isComplete() {
        return complete;
    }

    public boolean isErrors() {
        return errors;
    }

    public List<IoRecord> getBatch() {
        return batch;
    }
}
