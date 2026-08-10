package com.allobank.splitbill.service;

import com.allobank.splitbill.dto.SettlementResponse;
import com.allobank.splitbill.entity.Expense;
import com.allobank.splitbill.entity.Participant;
import com.allobank.splitbill.repository.ExpenseRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SettlementService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2);

    private final BillGroupService groupService;
    private final ExpenseRepository expenseRepository;
    private final String githubUsername;

    public SettlementService(
            BillGroupService groupService,
            ExpenseRepository expenseRepository,
            @Value("${app.github-username}") String githubUsername
    ) {
        this.groupService = groupService;
        this.expenseRepository = expenseRepository;
        this.githubUsername = githubUsername;
    }

    @Transactional(readOnly = true)
    public SettlementResponse calculate(Long groupId) {

        var group = groupService.get(groupId);
        var expenses = expenseRepository.findByGroupId(groupId);


        BigDecimal totalExpense = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);


        int serviceChargePct = githubUsername
                .toLowerCase()
                .chars()
                .sum() % 10;

        BigDecimal serviceChargeAmount = totalExpense
                .multiply(BigDecimal.valueOf(serviceChargePct))
                .divide(HUNDRED, 2, RoundingMode.HALF_UP)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalWithServiceCharge = totalExpense
                .add(serviceChargeAmount)
                .setScale(2, RoundingMode.HALF_UP);

    
        Map<Long, BigDecimal> balances = new HashMap<>();

        for (Participant participant : group.getParticipants()) {
            balances.put(participant.getId(), ZERO);
        }


        
        BigDecimal allocatedServiceCharge = ZERO;

        for (int expenseIndex = 0;
             expenseIndex < expenses.size();
             expenseIndex++) {

            Expense expense = expenses.get(expenseIndex);

            BigDecimal amount = expense.getAmount()
                    .setScale(2, RoundingMode.HALF_UP);

            Participant payer = expense.getPaidBy();

            List<Participant> beneficiaries =
                    expense.getBeneficiaries();

            if (beneficiaries == null || beneficiaries.isEmpty()) {
                continue;
            }


            
            BigDecimal expenseServiceCharge;

            if (expenseIndex == expenses.size() - 1) {


                expenseServiceCharge = serviceChargeAmount
                        .subtract(allocatedServiceCharge)
                        .setScale(2, RoundingMode.HALF_UP);

            } else {

                expenseServiceCharge = serviceChargeAmount
                        .multiply(amount)
                        .divide(
                                totalExpense,
                                2,
                                RoundingMode.DOWN
                        )
                        .setScale(2, RoundingMode.DOWN);

                allocatedServiceCharge =
                        allocatedServiceCharge.add(expenseServiceCharge);
            }

            BigDecimal amountWithServiceCharge = amount
                    .add(expenseServiceCharge)
                    .setScale(2, RoundingMode.HALF_UP);


            balances.merge(
                    payer.getId(),
                    amountWithServiceCharge,
                    BigDecimal::add
            );


            
            int beneficiaryCount = beneficiaries.size();

            BigDecimal share = amountWithServiceCharge
                    .divide(
                            BigDecimal.valueOf(beneficiaryCount),
                            2,
                            RoundingMode.DOWN
                    )
                    .setScale(2, RoundingMode.DOWN);

            BigDecimal distributed = share
                    .multiply(BigDecimal.valueOf(beneficiaryCount))
                    .setScale(2, RoundingMode.DOWN);

            BigDecimal remainder = amountWithServiceCharge
                    .subtract(distributed)
                    .setScale(2, RoundingMode.HALF_UP);


            
            for (int i = 0; i < beneficiaries.size(); i++) {

                Participant beneficiary = beneficiaries.get(i);

                BigDecimal individualShare = share;


                if (i == 0) {
                    individualShare = individualShare
                            .add(remainder)
                            .setScale(2, RoundingMode.HALF_UP);
                }

                balances.merge(
                        beneficiary.getId(),
                        individualShare.negate(),
                        BigDecimal::add
                );
            }
        }


        List<SettlementResponse.Balance> balanceResponses =
                group.getParticipants()
                        .stream()
                        .map(participant ->
                                new SettlementResponse.Balance(
                                        participant.getName(),
                                        balances
                                                .getOrDefault(
                                                        participant.getId(),
                                                        ZERO
                                                )
                                                .setScale(
                                                        2,
                                                        RoundingMode.HALF_UP
                                                )
                                )
                        )
                        .toList();


        
        List<Debt> creditors = new ArrayList<>();
        List<Debt> debtors = new ArrayList<>();

        for (Participant participant : group.getParticipants()) {

            BigDecimal balance = balances
                    .getOrDefault(
                            participant.getId(),
                            ZERO
                    )
                    .setScale(2, RoundingMode.HALF_UP);

            if (balance.compareTo(ZERO) > 0) {

                creditors.add(
                        new Debt(
                                participant,
                                balance
                        )
                );

            } else if (balance.compareTo(ZERO) < 0) {

                debtors.add(
                        new Debt(
                                participant,
                                balance.abs()
                        )
                );
            }
        }


        creditors.sort(
                Comparator.comparing(
                        Debt::amount
                ).reversed()
        );

        debtors.sort(
                Comparator.comparing(
                        Debt::amount
                ).reversed()
        );

    
        
        List<SettlementResponse.Settlement> settlements =
                new ArrayList<>();

        int creditorIndex = 0;
        int debtorIndex = 0;

        while (
                creditorIndex < creditors.size()
                        && debtorIndex < debtors.size()
        ) {

            Debt creditor = creditors.get(creditorIndex);
            Debt debtor = debtors.get(debtorIndex);

            BigDecimal transfer = creditor
                    .amount()
                    .min(debtor.amount())
                    .setScale(2, RoundingMode.HALF_UP);

            if (transfer.compareTo(ZERO) > 0) {

                settlements.add(
                        new SettlementResponse.Settlement(
                                debtor.participant().getName(),
                                creditor.participant().getName(),
                                transfer
                        )
                );
            }

            BigDecimal remainingCreditor = creditor
                    .amount()
                    .subtract(transfer)
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal remainingDebtor = debtor
                    .amount()
                    .subtract(transfer)
                    .setScale(2, RoundingMode.HALF_UP);

            creditors.set(
                    creditorIndex,
                    new Debt(
                            creditor.participant(),
                            remainingCreditor
                    )
            );

            debtors.set(
                    debtorIndex,
                    new Debt(
                            debtor.participant(),
                            remainingDebtor
                    )
            );

            if (remainingCreditor.compareTo(ZERO) == 0) {
                creditorIndex++;
            }

            if (remainingDebtor.compareTo(ZERO) == 0) {
                debtorIndex++;
            }
        }

    
        
        return new SettlementResponse(
                group.getId(),
                group.getName(),
                totalExpense,
                serviceChargePct,
                serviceChargeAmount,
                totalWithServiceCharge,
                settlements,
                balanceResponses
        );
    }

    private record Debt(
            Participant participant,
            BigDecimal amount
    ) {
    }
}