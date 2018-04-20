package org.openkilda.floodlight.spike;

import org.openkilda.floodlight.pathverification.VerificationPacket;
import org.openkilda.floodlight.pathverification.type.PathType;

import com.auth0.jwt.JWT;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.packet.LLDPTLV;
import net.floodlightcontroller.packet.UDP;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.IpProtocol;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.TransportPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class VerificationPackageAdapter {
    private static final Logger logger = LoggerFactory.getLogger(VerificationPackageAdapter.class);

    public static final String VERIFICATION_PACKET_IP_DST = "192.168.0.255";
    public static final int VERIFICATION_PACKET_UDP_PORT = 61231;

    private byte data[];

    public VerificationPackageAdapter(IOFSwitch srcSw, OFPort srcPort, IOFSwitch dstSw, OFPort dstPort, VerificationPackageSign sign) {
        byte[] chassisId = new byte[]{4, 0, 0, 0, 0, 0, 0};
        byte[] portId = new byte[]{2, 0, 0};
        byte[] ttlValue = new byte[]{0, 0x78};
        byte[] dpidTLVValue = new byte[]{0x0, 0x26, (byte) 0xe1, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        LLDPTLV dpidTLV = new LLDPTLV()
                .setType((byte) 127)
                .setLength((short) dpidTLVValue.length)
                .setValue(dpidTLVValue);

        byte[] dpidArray = new byte[8];
        ByteBuffer dpidBB = ByteBuffer.wrap(dpidArray);
        ByteBuffer portBB = ByteBuffer.wrap(portId, 1, 2);

        DatapathId dpid = srcSw.getId();
        dpidBB.putLong(dpid.getLong());
        System.arraycopy(dpidArray, 2, chassisId, 1, 6);
        // Set the optionalTLV to the full SwitchID
        System.arraycopy(dpidArray, 0, dpidTLVValue, 4, 8);

        byte[] srcMac = new byte[6];
        System.arraycopy(dpidArray, 2, srcMac, 0, 6);

        dpidBB.rewind();
        dpidBB.putLong(dstSw.getId().getLong());
        byte[] dstMac = new byte[6];
        System.arraycopy(dpidArray, 2, dstMac, 0, 6);

        portBB.putShort(srcPort.getShortPortNumber());

        VerificationPacket vp = new VerificationPacket();
        vp.setChassisId(
                new LLDPTLV().setType((byte) 1).setLength((short) chassisId.length).setValue(chassisId));

        vp.setPortId(new LLDPTLV().setType((byte) 2).setLength((short) portId.length).setValue(portId));

        vp.setTtl(
                new LLDPTLV().setType((byte) 3).setLength((short) ttlValue.length).setValue(ttlValue));

        vp.getOptionalTLVList().add(dpidTLV);
        // Add the controller identifier to the TLV value.
        //    vp.getOptionalTLVList().add(controllerTLV);

        // Add T0 based on format from Floodlight LLDP
        long time = System.currentTimeMillis();
        long swLatency = srcSw.getLatency().getValue();
        byte[] timestampTLVValue = ByteBuffer.allocate(Long.SIZE / 8 + 4).put((byte) 0x00)
                .put((byte) 0x26).put((byte) 0xe1)
                .put((byte) 0x01) // 0x01 is what we'll use to differentiate DPID (0x00) from time (0x01)
                .putLong(time + swLatency /* account for our switch's one-way latency */)
                .array();

        LLDPTLV timestampTLV = new LLDPTLV().setType((byte) 127)
                .setLength((short) timestampTLVValue.length).setValue(timestampTLVValue);

        vp.getOptionalTLVList().add(timestampTLV);

        // Type
        byte[] typeTLVValue = ByteBuffer.allocate(Integer.SIZE / 8 + 4).put((byte) 0x00)
                .put((byte) 0x26).put((byte) 0xe1)
                .put((byte) 0x02)
                .putInt(PathType.ISL.ordinal()).array();
        LLDPTLV typeTLV = new LLDPTLV().setType((byte) 127)
                .setLength((short) typeTLVValue.length).setValue(typeTLVValue);
        vp.getOptionalTLVList().add(typeTLV);

        if (sign != null) {
            String token = JWT.create()
                    .withClaim("dpid", dpid.getLong())
                    .withClaim("ts", time + swLatency)
                    .sign(sign.getAlgorithm());

            byte[] tokenBytes = token.getBytes(Charset.forName("UTF-8"));

            byte[] tokenTLVValue = ByteBuffer.allocate(4 + tokenBytes.length).put((byte) 0x00)
                    .put((byte) 0x26).put((byte) 0xe1)
                    .put((byte) 0x03)
                    .put(tokenBytes).array();
            LLDPTLV tokenTLV = new LLDPTLV().setType((byte) 127)
                    .setLength((short) tokenTLVValue.length).setValue(tokenTLVValue);

            vp.getOptionalTLVList().add(tokenTLV);
        }

        Ethernet l2 = new Ethernet().setSourceMACAddress(MacAddress.of(srcMac))
                .setDestinationMACAddress(MacAddress.of(dstMac)).setEtherType(EthType.IPv4);

        IPv4Address dstIp = IPv4Address
                .of(((InetSocketAddress) dstSw.getInetAddress()).getAddress().getAddress());

        IPv4 l3 = new IPv4()
                .setSourceAddress(
                        IPv4Address.of(((InetSocketAddress) srcSw.getInetAddress()).getAddress().getAddress()))
                .setDestinationAddress(dstIp).setTtl((byte) 64).setProtocol(IpProtocol.UDP);

        UDP l4 = new UDP();
        l4.setSourcePort(TransportPort.of(VERIFICATION_PACKET_UDP_PORT));
        l4.setDestinationPort(TransportPort.of(VERIFICATION_PACKET_UDP_PORT));

        l2.setPayload(l3);
        l3.setPayload(l4);
        l4.setPayload(vp);

        data = l2.serialize();
    }

    public byte[] getData() {
        return data;
    }
}
