package com.example.OfflineUPI_Mesh.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository  extends JpaRepository<Transaction,Long> {


    Optional<Transaction> findByPacketHash(String packetHash);

    boolean existsByPacketHash(String packetHash);

    List<Transaction> findTop20ByOrderByIdDesc();
}
