package org.openkilda.floodlight;

import net.floodlightcontroller.core.IOFSwitch;
import org.openkilda.floodlight.switchmanager.OFInstallException;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.types.DatapathId;

import java.util.*;

class IoBatch {
    private final SwitchUtils switchUtils;

    private final List<IoRecord> batch;
    private final LinkedList<IoRecord> barriers;
    private boolean errors = false;
    private boolean complete = false;

    IoBatch(SwitchUtils switchUtils, List<IoRecord> batch) {
        this.switchUtils = switchUtils;
        this.batch = batch;
        this.barriers = new LinkedList<>();
    }

    void write() throws OFInstallException {
        HashSet<DatapathId> processedSwitched = new HashSet<>();

        boolean idle;
        do {
            idle = true;

            Iterator<IoRecord> recordIter = batch.iterator();
            while (recordIter.hasNext()) {
                IoRecord record = recordIter.next();

                if (processedSwitched.contains(record.getDpId())) {
                    continue;
                }

                processedSwitched.add(record.getDpId());
                idle = false;

                writeOneSwitch(recordIter, switchUtils.lookupSwitch(record.getDpId()));
                break;
            }
        } while (! idle);

        complete = 0 == barriers.size();
    }

    boolean handleResponse(OFMessage response) {
        boolean match = true;

        if (recordResponse(barriers, response)) {
            updateBarriers();
        } else if (recordResponse(batch, response)) {
            errors = OFType.ERROR == response.getType();
        } else {
            match = false;
        }

        return match;
    }

    private void writeOneSwitch(Iterator<IoRecord> recordIterator, IOFSwitch sw) throws OFInstallException {
        DatapathId dpId = sw.getId();

        boolean idle = true;
        while (recordIterator.hasNext()) {
            IoRecord record = recordIterator.next();

            if (!dpId.equals(record.getDpId())) {
                continue;
            }

            idle = false;
            if (!sw.write(record.getRequest())) {
                throw new OFInstallException(dpId, record.getRequest());
            }
        }

        if (! idle) {
            IoRecord barrierRecord = new IoRecord(dpId, sw.getOFFactory().barrierRequest());
            if (!sw.write(barrierRecord.getRequest())) {
                throw new OFInstallException(dpId, barrierRecord.getRequest());
            }
            barriers.addLast(barrierRecord);
        }
    }

    private boolean recordResponse(List<IoRecord> pending, OFMessage response) {
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

    boolean isComplete() {
        return complete;
    }

    boolean isErrors() {
        return errors;
    }

    List<IoRecord> getBatch() {
        return batch;
    }
}
