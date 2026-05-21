package com.offlinetransaction.service;



import com.offlinetransaction.cryptography.HybridCrypto;
import com.offlinetransaction.cryptography.ServerKeyHolder;
import com.offlinetransaction.models.Account;
import com.offlinetransaction.models.AccountRepository;
import com.offlinetransaction.models.MeshPacket;
import com.offlinetransaction.models.PaymentInstruction;
import jakarta.annotation.PostConstruct;
import org.aspectj.bridge.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.UUID;


@Service
public class DemoService {

    private static final Logger log = LoggerFactory.getLogger(DemoService.class);

    @Autowired private AccountRepository accounts;
    @Autowired private HybridCrypto crypto;
    @Autowired private ServerKeyHolder serverKey;


    //whenever bean will be created this post construct will addd these account in our database
    @PostConstruct
    public void seedAccounts(){
        if(accounts.count()==0){
            accounts.save(new Account("alice@demo", "Alice",   new BigDecimal("5000.00")));
            accounts.save(new Account("bob@demo",   "Bob",     new BigDecimal("1000.00")));
            accounts.save(new Account("carol@demo", "Carol",   new BigDecimal("2500.00")));
            accounts.save(new Account("dave@demo",  "Dave",    new BigDecimal("500.00")));
            log.info("Seeded 4 demo accounts");
        }
    }


    //packet that willl be sent over mesh are created over here
    public MeshPacket createPacket(String senderVpa, String receiverVpa,
                                   BigDecimal amount, String pin, int ttl) throws Exception{
        PaymentInstruction instruction = new PaymentInstruction(
                senderVpa,
                receiverVpa,
                amount,
                sha256Hex(pin),
                UUID.randomUUID().toString(), //unique rakhne k liye
                Instant.now().toEpochMilli() //for freshness taaki baar baar use na ho sake
        );

        String cipherText = crypto.encrypt(instruction, serverKey.getPublicKey());

        MeshPacket packet = new MeshPacket();
        packet.setPacketId(UUID.randomUUID().toString());
        packet.setTtl(ttl);
        packet.setCreatedAt(Instant.now().toEpochMilli());
        packet.setCiphertext(cipherText);
        return packet;
    }

    //password ko hash kar denge as a goodd habit
    private String sha256Hex(String input) throws Exception{
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(input.getBytes());
        StringBuilder hex = new StringBuilder();
        for (byte b : hash) hex.append(String.format("%02x", b));
        return hex.toString();
    }
}
