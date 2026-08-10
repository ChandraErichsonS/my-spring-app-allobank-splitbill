package com.allobank.splitbill.dto;

import java.math.BigDecimal;
import java.util.List;

public record SettlementResponse(
    Long groupId,
    String groupName,
    BigDecimal totalExpense,
    int serviceChargePct,
    BigDecimal serviceChargeAmount,
    BigDecimal totalWithServiceCharge,
    List<Settlement> settlements,
    List<Balance> balances
) {
    public record Settlement(String from, String to, BigDecimal amount) {}
    public record Balance(String participant, BigDecimal amount) {}
}
