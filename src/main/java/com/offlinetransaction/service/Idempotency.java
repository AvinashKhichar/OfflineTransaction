package com.offlinetransaction.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class Idempotency {
    //map banega packer:time type se
    private final Map<String, Instant> seen = new ConcurrentHashMap<>();

    @Value("${upi.mesh.idempotency-ttl-seconds}")
    private long ttlseconds;

    //claim karenge if pahle se exist nhi kar rha true if accepted otherwise no
    public boolean claim(String packetHash){
        Instant now = Instant.now();
        Instant prev = seen.putIfAbsent(packetHash, now);
        return prev == null;
    }

    public int size(){
        return seen.size();
    }

    //purane walo ko hata denge jo ttl se bahar ho gaye
    @Scheduled(fixedDelay = 60_000)
    public void evictExxpired(){
        Instant cutoff = Instant.now().minusSeconds(ttlseconds);
        seen.entrySet().removeIf(entry -> entry.getValue().isBefore(cutoff));
    }

    public void clear(){
        seen.clear();
    }
}
