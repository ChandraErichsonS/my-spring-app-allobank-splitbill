package com.allobank.splitbill.service;

import com.allobank.splitbill.entity.BillGroup;
import com.allobank.splitbill.entity.Expense;
import com.allobank.splitbill.entity.Participant;
import com.allobank.splitbill.repository.ExpenseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SettlementServiceTest {

    @Mock
    private BillGroupService groupService;

    @Mock
    private ExpenseRepository expenseRepository;

    @Test
    void shouldCalculateAndOptimizeSettlement() {
        BillGroup group = new BillGroup("Lunch");
        Participant a = new Participant("A", group);
        Participant b = new Participant("B", group);
        Participant c = new Participant("C", group);
        group.getParticipants().addAll(List.of(a, b, c));

        setId(a, 1L);
        setId(b, 2L);
        setId(c, 3L);
        setId(group, 10L);

        Expense expense = new Expense(
            new BigDecimal("300.00"),
            group,
            a,
            List.of(a, b, c)
        );

        when(groupService.get(10L)).thenReturn(group);
        when(expenseRepository.findByGroupId(10L)).thenReturn(List.of(expense));

        SettlementService service = new SettlementService(
            groupService,
            expenseRepository,
            "ChandraErichsonS"
        );

        var response = service.calculate(10L);

        assertThat(response.totalExpense())
            .isEqualByComparingTo("300.00");

        assertThat(response.serviceChargePct())
            .isEqualTo(5);

        assertThat(response.serviceChargeAmount())
            .isEqualByComparingTo("15.00");

        assertThat(response.totalWithServiceCharge())
            .isEqualByComparingTo("315.00");

        assertThat(response.settlements())
            .hasSize(2);

        assertThat(response.settlements())
            .extracting("amount")
            .containsExactlyInAnyOrder(
                new BigDecimal("105.00"),
                new BigDecimal("105.00")
            );
    }
        
    private static void setId(Object entity, Long id) {
        try {
            var field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }
}
