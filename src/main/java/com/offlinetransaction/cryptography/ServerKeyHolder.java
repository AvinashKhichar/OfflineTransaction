package com.offlinetransaction.cryptography;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

@Component
public class ServerKeyHolder {

    private static final Logger log = LoggerFactory.getLogger(ServerKeyHolder.class);

    private KeyPair keyPair;

    //whenever beans will be created the system will generate these two keys out of which public will be shared to devices
    //and the public one will be stored on server only
    @PostConstruct
    public void init() throws Exception{

        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        this.keyPair = gen.generateKeyPair();
        log.info("Server RSA keypair generated (2048-bit). Public key fingerprint: {}",
                getPublicKeyBase64().substring(0, 32) + "...");

    }

    public PublicKey getPublicKey(){
        return keyPair.getPublic();
    }

    public PrivateKey getPrivateKey(){
        return keyPair.getPrivate();
    }

    public String getPublicKeyBase64(){
        return Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
    }
}
