package com.offlinetransaction.service;

import com.offlinetransaction.models.MeshPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@Service
public class MeshSimulator {

    private static final Logger log = LoggerFactory.getLogger(MeshSimulator.class);
    private final Map<String, VirtualDevice> devices = new ConcurrentHashMap<>();


    public MeshSimulator(){
        seedDefaultDevices();
    }

    private void seedDefaultDevices() {
        devices.put("phone-alice",   new VirtualDevice("phone-alice",   false));
        devices.put("phone-stranger1", new VirtualDevice("phone-stranger1", false));
        devices.put("phone-stranger2", new VirtualDevice("phone-stranger2", false));
        devices.put("phone-stranger3", new VirtualDevice("phone-stranger3", false));
        devices.put("phone-bridge",  new VirtualDevice("phone-bridge",  true));
    }


    public Collection<VirtualDevice> getDevices(){
        return devices.values();
    }

    public VirtualDevice getDevice(String id) {
        return devices.get(id);
    }

    //sender rops packet in the mesh
    public void inject(String senderDeviceId, MeshPacket packet) {
        VirtualDevice sender = devices.get(senderDeviceId);
        if (sender == null) throw new IllegalArgumentException("Unknown device: " + senderDeviceId);
        sender.hold(packet);
        log.info("Packet {} injected at {} (TTL={})",
                packet.getPacketId().substring(0, 8), senderDeviceId, packet.getTtl());
    }

    //first gossip round will happen now
    public GossipResult gossipOnce(){
        int transfer = 0;
        List<VirtualDevice> deviceList = new ArrayList<>(devices.values());

        //har ek ddevice k paas jaega andd yaad rakhenge ki uske paas gya h ya nhi taaki kaam karna pade
        Map<String, List<MeshPacket>> snaps = new HashMap<>();
        for(VirtualDevice d : deviceList){
            snaps.put(d.getDeviceId(), new ArrayList<>(d.getHeldPackets()));
        }

        for(VirtualDevice src : deviceList){
            for(MeshPacket pkt : snaps.get(src.getDeviceId())){
                if(pkt.getTtl()<=0) continue;
                for(VirtualDevice st : deviceList){
                    if(st==src) continue;
                    if(st.holds(pkt.getPacketId())) continue;
                    MeshPacket copy = new MeshPacket();
                    copy.setPacketId(pkt.getPacketId());
                    copy.setTtl(pkt.getTtl() - 1);
                    copy.setCreatedAt(pkt.getCreatedAt());
                    copy.setCiphertext(pkt.getCiphertext());
                    st.hold(copy);
                    transfer++;
                }
            }
        }

        log.info("Gossip round complete: {} packet transfers", transfer);
        return new GossipResult(transfer, snapsMap());


    }

    private Map<String, Integer> snapsMap() {

        Map<String, Integer> m = new LinkedHashMap<>();
        for(VirtualDevice d : devices.values()){
            m.put(d.getDeviceId(), d.packetCount());
        }
        return m;
    }


    //these are the packets that are held by bridge ones and they will be shared with the backend
    //ones net access is there
    public List<BridgeUpload> collectBridgeUploads() {
        List<BridgeUpload> out = new ArrayList<>();
        for (VirtualDevice d : devices.values()) {
            if (!d.hasInternet()) continue;
            for (MeshPacket pkt : d.getHeldPackets()) {
                out.add(new BridgeUpload(d.getDeviceId(), pkt));
            }
        }
        return out;
    }

    public void resetMesh() {
        devices.values().forEach(VirtualDevice::clear);
    }


    public record GossipResult(int transfers, Map<String, Integer> deviceCounts) {}
    public record BridgeUpload(String bridgeNodeId, MeshPacket packet) {}
}
