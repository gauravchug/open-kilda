package org.openkilda.floodlight;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.IFloodlightService;
import org.openkilda.floodlight.operation.Operation;
import org.openkilda.floodlight.switchmanager.OFInstallException;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFType;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class IoService implements IFloodlightService, IOFMessageListener {
    private final LinkedList<Task> operations = new LinkedList<>();

    private SwitchUtils switchUtils;

    public synchronized void push(Operation initiator, List<IoRecord> payload) throws OFInstallException {
        IoBatch batch = new IoBatch(switchUtils, payload);
        operations.addLast(new Task(initiator, batch));

        try {
            batch.write();
        } catch (OFInstallException e) {
            operations.removeLast();
            throw e;
        }
    }

    public void init(FloodlightModuleContext flContext) {
        switchUtils = new SwitchUtils(flContext.getServiceImpl(IOFSwitchService.class));

        IFloodlightProviderService flProviderService = flContext.getServiceImpl(IFloodlightProviderService.class);

        flProviderService.addOFMessageListener(OFType.ERROR, this);
        flProviderService.addOFMessageListener(OFType.PACKET_IN, this);
    }

    @Override
    public Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
        boolean match = false;

        Task completed = null;
        synchronized (this) {
            for (ListIterator<Task> iterator = operations.listIterator(); iterator.hasNext(); ) {
                Task task = iterator.next();

                if (!task.batch.handleResponse(msg)) {
                    continue;
                }

                match = true;
                if (task.batch.isComplete()) {
                    iterator.remove();
                    completed = task;
                }
                break;
            }
        }

        if (completed != null) {
            completed.operation.ioComplete(completed.batch.getBatch(), completed.batch.isErrors());
        }

        if (match) {
            return Command.STOP;
        }
        return Command.CONTINUE;
    }

    @Override
    public String getName() {
        return getClass().getName();
    }

    @Override
    public boolean isCallbackOrderingPrereq(OFType type, String name) {
        return false;
    }

    @Override
    public boolean isCallbackOrderingPostreq(OFType type, String name) {
        return false;
    }

    private class Task {
        final Operation operation;
        final IoBatch batch;

        Task(Operation operation, IoBatch batch) {
            this.operation = operation;
            this.batch = batch;
        }
    }
}
