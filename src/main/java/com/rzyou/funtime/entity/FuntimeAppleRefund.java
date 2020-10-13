package com.rzyou.funtime.entity;

import lombok.Data;

import java.io.Serializable;
@Data
public class FuntimeAppleRefund implements Serializable {
    
    private String transactionId;

    private String productId;

    private Integer quantity;

    private String purchaseDate;

    private String purchaseDateMs;

    private String purchaseDatePst;

    private String originalPurchaseDate;

    private String originalPurchaseDateMs;

    private String originalPurchaseDatePst;

    private String isTrialPeriod;

    private String originalTransactionId;

    private String cancellationDate;

    private String cancellationDateMs;

    private String cancellationDatePst;

    private String cancellationReason;

    
}