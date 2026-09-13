package com.example.OfflineUPI_Mesh.service;

import com.example.OfflineUPI_Mesh.model.MeshPacket;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


public class MeshSimulatorService {

    private final List<VirtualDevice> devices = new ArrayList<>();

    public void addDevice(VirtualDevice device) {
        devices.add(device);
    }

    public List<VirtualDevice> getDevices() {
        return devices;
    }

    // ------------------------------------------------------------- inject

    /**
     * Inject a packet into a specific device.
     *
     * This simulates the sender phone creating a payment packet
     * and placing it into its local mesh storage.
     */
    public void inject(String deviceId, MeshPacket packet) {

        for (VirtualDevice device : devices) {

            if (device.getDeviceId().equals(deviceId)) {
                device.hold(packet);
                return;
            }
        }

        throw new IllegalArgumentException(
                "Device not found: " + deviceId
        );
    }

    // --------------------------------------------------------- broadcast

    public void broadcastPacket(MeshPacket packet, VirtualDevice sender) {

        // Packet cannot be forwarded if TTL is already 0
        if (packet.getTtl() <= 0) {
            return;
        }

        // Decrease TTL before forwarding to the next hop
        packet.setTtl(packet.getTtl() - 1);

        for (VirtualDevice device : devices) {

            // Don't send the packet back to the sender
            if (device == sender) {
                continue;
            }

            // Device already has this packet
            if (device.holds(packet.getPacketId())) {
                continue;
            }

            // Store packet on the next device
            device.hold(packet);
        }
    }

    // ------------------------------------------------------------- bridge

    public VirtualDevice findBridge() {

        for (VirtualDevice device : devices) {

            if (device.hasInternet()) {
                return device;
            }
        }

        return null;
    }

    public boolean sendToBridge(MeshPacket packet) {

        VirtualDevice bridge = findBridge();

        // No internet-connected device available
        if (bridge == null) {
            return false;
        }

        // Bridge already has this packet
        if (bridge.holds(packet.getPacketId())) {
            return true;
        }

        // Deliver packet to the bridge
        bridge.hold(packet);

        return true;
    }

    // -------------------------------------------------------------- gossip

    public void gossip(MeshPacket packet, VirtualDevice sender) {

        // Forward the packet to other devices in the mesh
        broadcastPacket(packet, sender);

        // Try to deliver the packet to an internet-connected bridge
        sendToBridge(packet);
    }

    /**
     * Perform one gossip round.
     *
     * Every device shares the packets it currently holds with
     * the other devices.
     */
    public GossipResult gossipOnce() {

        int transfers = 0;

        List<VirtualDevice> snapshot = new ArrayList<>(devices);

        for (VirtualDevice sender : snapshot) {

            List<MeshPacket> packets =
                    new ArrayList<>(sender.getHeldPackets());

            for (MeshPacket packet : packets) {

                int before = totalPacketCopies(packet.getPacketId());

                broadcastPacket(packet, sender);

                int after = totalPacketCopies(packet.getPacketId());

                transfers += Math.max(0, after - before);
            }
        }

        return new GossipResult(
                transfers,
                getDeviceCounts()
        );
    }

    // -------------------------------------------------------- bridge upload

    /**
     * Collect all packets currently held by internet-connected
     * bridge devices.
     */
    public List<BridgeUpload> collectBridgeUploads() {

        List<BridgeUpload> uploads = new ArrayList<>();

        for (VirtualDevice device : devices) {

            if (!device.hasInternet()) {
                continue;
            }

            for (MeshPacket packet : device.getHeldPackets()) {

                uploads.add(
                        new BridgeUpload(
                                packet,
                                device.getDeviceId()
                        )
                );
            }
        }

        return uploads;
    }

    // --------------------------------------------------------------- reset

    /**
     * Clear packets from every device in the mesh.
     */
    public void resetMesh() {

        for (VirtualDevice device : devices) {
            device.clear();
        }
    }

    // -------------------------------------------------------------- helper

    /**
     * Count how many devices currently hold a particular packet.
     */
    private int totalPacketCopies(String packetId) {

        int count = 0;

        for (VirtualDevice device : devices) {

            if (device.holds(packetId)) {
                count++;
            }
        }

        return count;
    }

    /**
     * Return packet count for every device.
     */
    private List<Integer> getDeviceCounts() {

        List<Integer> counts = new ArrayList<>();

        for (VirtualDevice device : devices) {
            counts.add(device.packetCount());
        }

        return counts;
    }

    // ------------------------------------------------------------- records

    /**
     * Result returned after one gossip round.
     */
    public record GossipResult(
            int transfers,
            List<Integer> deviceCounts
    ) {
    }

    /**
     * Represents one packet upload from one bridge node.
     */
    public record BridgeUpload(
            MeshPacket packet,
            String bridgeNodeId
    ) {
    }
}