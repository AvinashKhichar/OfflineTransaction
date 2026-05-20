package com.offlinetransaction.models;

import jakarta.persistence.*;

import java.math.BigDecimal;



//Bank account
@Entity
@Table(name = "accounts")
public class Account {



    @Id
    private String vpa;

    @Column(nullable = false)
    private String holderName;

    @Column(nullable = false)
    private BigDecimal balance;


    @Version //prevent duplimacy
    private Long version;

    public Account(){} //hibernate case

    public Account(String vpa, String holderName, BigDecimal balance){
        this.vpa = vpa;
        this.holderName = holderName;
        this.balance = balance;
    } // for normal object calling constructor


    public void setVersion(Long version) {
        this.version = version;
    }

    public Long getVersion() {
        return version;
    }

    public void setHolderName(String holderName) {
        this.holderName = holderName;
    }

    public void setVpa(String vpa) {
        this.vpa = vpa;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getHolderName() {
        return holderName;
    }

    public String getVpa() {
        return vpa;
    }

    public BigDecimal getBalance() {
        return balance;
    }
}
