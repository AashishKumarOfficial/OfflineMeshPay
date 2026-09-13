package com.example.OfflineUPI_Mesh.config;

import com.example.OfflineUPI_Mesh.service.MeshSimulatorService;
import com.example.OfflineUPI_Mesh.service.VirtualDevice;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class AppConfig {

    @Bean
    public MeshSimulatorService meshSimulatorService() {

        MeshSimulatorService mesh = new MeshSimulatorService();

        // Simulated phones
        mesh.addDevice(new VirtualDevice("phone-alice", false));
        mesh.addDevice(new VirtualDevice("phone-bob", false));
        mesh.addDevice(new VirtualDevice("phone-charlie", false));

        // Internet-connected bridge nodes
        mesh.addDevice(new VirtualDevice("bridge-1", true));
        mesh.addDevice(new VirtualDevice("bridge-2", true));

        return mesh;
    }
}