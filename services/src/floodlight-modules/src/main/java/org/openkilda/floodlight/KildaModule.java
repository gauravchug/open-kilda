package org.openkilda.floodlight;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;

import java.util.Collection;
import java.util.Map;

public class KildaModule implements IFloodlightModule {
    IoService transactionalIoService = new IoService();
    PacketInService packetInService = new PacketInService();

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleServices() {
        return ImmutableList.of(
                IoService.class,
                PacketInService.class);
    }

    @Override
    public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
        return ImmutableMap.of(
                IoService.class, transactionalIoService,
                PacketInService.class, packetInService);
    }

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
        return ImmutableList.of(
                IFloodlightProviderService.class,
                IOFSwitchService.class);
    }

    @Override
    public void init(FloodlightModuleContext context) throws FloodlightModuleException {

    }

    @Override
    public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
        transactionalIoService.init(context);
    }
}
