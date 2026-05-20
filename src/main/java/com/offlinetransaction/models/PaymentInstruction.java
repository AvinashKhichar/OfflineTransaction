package com.offlinetransaction.models;

import java.math.BigDecimal;

public class PaymentInstruction {


    private String senderVpa;
    private String recipientVpa;
    private BigDecimal amount;
    private String pinHash;
    private String nonce; //UUid unique per payment
    private Long signedAt; //when sender signed

    public PaymentInstruction(){}
    public PaymentInstruction(String senderVpa, String recipientVpa, BigDecimal amount, String pinHash, String nonce, Long signedAt) {
        this.senderVpa = senderVpa;
        this.recipientVpa = recipientVpa;
        this.amount = amount;
        this.pinHash = pinHash;
        this.nonce = nonce;
        this.signedAt = signedAt;
    }


    public String getSenderVpa() {
        return senderVpa;
    }

    public void setSenderVpa(String senderVpa) {
        this.senderVpa = senderVpa;
    }

    public Long getSignedAt() {
        return signedAt;
    }

    public void setSignedAt(Long signedAt) {
        this.signedAt = signedAt;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getPinHash() {
        return pinHash;
    }

    public void setPinHash(String pinHash) {
        this.pinHash = pinHash;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getRecipientVpa() {
        return recipientVpa;
    }

    public void setRecipientVpa(String recipientVpa) {
        this.recipientVpa = recipientVpa;
    }


}
