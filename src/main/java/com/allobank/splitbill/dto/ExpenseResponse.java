package com.allobank.splitbill.dto;

import java.math.BigDecimal;
import java.util.List;

public record ExpenseResponse(
    Long id,
    BigDecimal amount,
    Long paidBy,
    List<Long> forParticipants
) {}
