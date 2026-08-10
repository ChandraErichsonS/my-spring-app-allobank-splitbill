package com.allobank.splitbill.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public record AddExpenseRequest(
    @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
    @NotNull Long paidBy,
    @NotEmpty List<Long> forParticipants
) {}
