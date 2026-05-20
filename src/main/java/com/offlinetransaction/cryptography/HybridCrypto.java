package com.offlinetransaction.cryptography;

import com.offlinetransaction.models.PaymentInstruction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.MGF1ParameterSpec;
import java.util.Base64;

@Service
public class HybridCrypto {

    private static final String RSA_TRANSFORMATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    private static final String AES_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int AES_KEY_BITS = 256;
    private static final int RSA_KEY_BITS = 256;
    private static final int GCM_IV_BYTES = 12;
    private static final int GCM_TAG_BITS = 128;

    private final SecureRandom rng = new SecureRandom();
    private final ObjectMapper json =  new ObjectMapper();

    @Autowired
    private ServerKeyHolder serverKey;


//    all the encryption will take place here.... pahle aes key banaenge usse instructions ko encrypt karenge and RSA se
//    AES key ko encrypt karenge uske baad 1 pack banaenge jisme encrypted AES + instructions + iv daal denge


    public String encrypt(PaymentInstruction instruction, PublicKey serverPublicKey) throws Exception{
        byte[] plaintext = json.writeValueAsBytes(instruction);

        //one time AES key
        KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(AES_KEY_BITS);
        SecretKey aesKey = kg.generateKey();

        //aes-gcm encrypt
        byte[] iv = new byte[GCM_IV_BYTES];
        rng.nextBytes(iv);
        Cipher aes = Cipher.getInstance(AES_TRANSFORMATION);
        aes.init(Cipher.ENCRYPT_MODE, aesKey, new GCMParameterSpec(GCM_TAG_BITS, iv));
        byte[] aesCipherText = aes.doFinal(plaintext);

        //AES encryption with RSA
        Cipher rsa = Cipher.getInstance(RSA_TRANSFORMATION);
        OAEPParameterSpec oaep = new OAEPParameterSpec(
                "SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT
        );
        rsa.init(Cipher.ENCRYPT_MODE, serverPublicKey, oaep);
        byte[] encryptedAESKey = rsa.doFinal(aesKey.getEncoded());

        //pack karenge encryptedd AES + iv + cipherText
        ByteBuffer buf = ByteBuffer.allocate(encryptedAESKey.length + iv.length + aesCipherText.length);
        buf.put(encryptedAESKey);
        buf.put(iv);
        buf.put(aesCipherText);

        return Base64.getEncoder().encodeToString(buf.array());
    }



    //ab decrypt karna h...server se uski private key milegi + sender se instruction set milega

    public PaymentInstruction decrypt(String base64CipherText) throws Exception{
        byte[] all = Base64.getDecoder().decode(base64CipherText);

        if(all.length < RSA_KEY_BITS+GCM_IV_BYTES+GCM_TAG_BITS/8){
            throw new IllegalArgumentException("Cipher Text is too short");
        }

        byte[] encryptedAesKey = new byte[RSA_KEY_BITS];
        byte[] iv = new byte[GCM_IV_BYTES];
        byte[] aesCipherText = new byte[all.length-GCM_IV_BYTES-RSA_KEY_BITS];

        ByteBuffer buf = ByteBuffer.wrap(all);
        buf.get(encryptedAesKey);
        buf.get(iv);
        buf.get(aesCipherText);

        //RSA se AES nikalo
        Cipher rsa = Cipher.getInstance(RSA_TRANSFORMATION);
        OAEPParameterSpec oaep = new OAEPParameterSpec(
                "SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);
        rsa.init(Cipher.DECRYPT_MODE, serverKey.getPrivateKey(), oaep);
        byte[] aesKeyBytes = rsa.doFinal(encryptedAesKey);
        SecretKey aesKey = new SecretKeySpec(aesKeyBytes, "AES");


        //AES-GCM decrypt
        Cipher aes = Cipher.getInstance(AES_TRANSFORMATION);
        aes.init(Cipher.DECRYPT_MODE, aesKey, new GCMParameterSpec(GCM_TAG_BITS, iv));
        byte[] plaintext = aes.doFinal(aesCipherText);

        return json.readValue(plaintext, PaymentInstruction.class);
    }

    //SHA256 idempotency ko tackle karne k kaam aaegi
    public String hashCipherText(String base64CipherText) throws Exception{
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        byte[] hash = sha256.digest(base64CipherText.getBytes());
        StringBuilder hex = new StringBuilder();
        for(byte b : hash){
            hex.append(String.format("%02x", b));
        }

        return hex.toString();
    }
}
