package org.openkilda.floodlight.operation.flow;

import com.auth0.jwt.JWT;
import com.google.common.collect.ImmutableList;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.packet.Data;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.packet.UDP;
import net.floodlightcontroller.util.OFMessageUtils;
import org.openkilda.floodlight.IoRecord;
import org.openkilda.floodlight.IoService;
import org.openkilda.floodlight.SwitchUtils;
import org.openkilda.floodlight.model.flow.VerificationData;
import org.openkilda.floodlight.operation.OperationContext;
import org.openkilda.floodlight.pathverification.PathVerificationService;
import org.openkilda.floodlight.service.FlowVerificationService;
import org.openkilda.floodlight.switchmanager.OFInstallException;
import org.openkilda.floodlight.utils.DataSignature;
import org.openkilda.messaging.command.flow.FlowVerificationRequest;

import net.floodlightcontroller.core.module.FloodlightModuleContext;
import org.openkilda.messaging.info.flow.FlowVerificationErrorCode;
import org.openkilda.messaging.info.flow.UniFlowVerificationResponse;
import org.openkilda.messaging.model.Flow;
import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPacketOut;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.TransportPort;

import java.util.ArrayList;
import java.util.List;

public class VerificationSendOperation extends VerificationOperationCommon {
    private final FlowVerificationRequest verificationRequest;

    private final IoService ioService;
    private final SwitchUtils switchUtils;
    private final DataSignature signature;

    public VerificationSendOperation(OperationContext context, FlowVerificationRequest verificationRequest) {
        super(context);
        this.verificationRequest = verificationRequest;

        FloodlightModuleContext moduleContext = getContext().getModuleContext();
        this.ioService = moduleContext.getServiceImpl(IoService.class);
        this.switchUtils = new SwitchUtils(moduleContext.getServiceImpl(IOFSwitchService.class));
        this.signature = moduleContext.getServiceImpl(FlowVerificationService.class).getSignature();
    }

    @Override
    public void run() {
        Flow flow = verificationRequest.getFlow();
        DatapathId sourceDpId = DatapathId.of(flow.getSourceSwitch());
        IOFSwitch sw = switchUtils.lookupSwitch(sourceDpId);

        VerificationData data = VerificationData.of(verificationRequest);
        Ethernet netPacket = wrapData(data);
        OFMessage message = makePacketOut(sw, netPacket.serialize());

        try {
            ioService.push(this, ImmutableList.of(new IoRecord(sourceDpId, message)));
        } catch (OFInstallException e) {
            UniFlowVerificationResponse response = new UniFlowVerificationResponse(
                    flow, FlowVerificationErrorCode.WRITE_FAILURE);
            sendResponse(response);
        }
    }

    @Override
    public void ioComplete(List<IoRecord> payload, boolean isError) {
        if (! isError) {
            return;
        }

        UniFlowVerificationResponse response = new UniFlowVerificationResponse(
                verificationRequest.getFlow(), FlowVerificationErrorCode.WRITE_FAILURE);
        sendResponse(response);
    }

    private Ethernet wrapData(VerificationData data) {
        Flow flow = verificationRequest.getFlow();

        Data l7 = new Data(signature.sign(data.toJWT(JWT.create())));

        UDP l4 = new UDP();
        l4.setPayload(l7);
        l4.setSourcePort(TransportPort.of(PathVerificationService.VERIFICATION_PACKET_UDP_PORT));
        l4.setDestinationPort(TransportPort.of(PathVerificationService.VERIFICATION_PACKET_UDP_PORT));

        IPv4 l3 = new IPv4();
        l3.setPayload(l4);
        l3.setSourceAddress("127.0.0.1");
        l3.setDestinationAddress("127.0.0.1");

        Ethernet l2 = new Ethernet();
        l2.setPayload(l3);
        l2.setSourceMACAddress(switchUtils.dpIdToMac(DatapathId.of(flow.getSourceSwitch())));
        l2.setDestinationMACAddress(switchUtils.dpIdToMac(DatapathId.of(flow.getDestinationSwitch())));
        if (0 != flow.getSourceVlan()) {
            l2.setVlanID((short) flow.getSourceVlan());
        }

        return l2;
    }

    private OFMessage makePacketOut(IOFSwitch sw, byte[] data) {
        OFFactory ofFactory = sw.getOFFactory();
        OFPacketOut.Builder pktOut = ofFactory.buildPacketOut();

        pktOut.setData(data);

        List<OFAction> actions = new ArrayList<>(2);
        actions.add(ofFactory.actions().buildOutput().setPort(OFPort.TABLE).build());
        pktOut.setActions(actions);

        OFMessageUtils.setInPort(pktOut, OFPort.of(verificationRequest.getFlow().getSourcePort()));

        return pktOut.build();
    }
}
