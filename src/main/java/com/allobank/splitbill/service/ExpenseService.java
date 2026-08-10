package com.allobank.splitbill.service;

import com.allobank.splitbill.dto.AddExpenseRequest;
import com.allobank.splitbill.dto.ExpenseResponse;
import com.allobank.splitbill.entity.Expense;
import com.allobank.splitbill.entity.Participant;
import com.allobank.splitbill.exception.BadRequestException;
import com.allobank.splitbill.repository.ExpenseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
public class ExpenseService {
    private final BillGroupService groupService;
    private final ExpenseRepository expenseRepository;

    public ExpenseService(BillGroupService groupService, ExpenseRepository expenseRepository) {
        this.groupService = groupService;
        this.expenseRepository = expenseRepository;
    }

    @Transactional
    public ExpenseResponse add(Long groupId, AddExpenseRequest request) {
        var group = groupService.get(groupId);
        var participants = group.getParticipants();

        Map<Long, Participant> byId = new HashMap<>();
        participants.forEach(p -> byId.put(p.getId(), p));

        var payer = byId.get(request.paidBy());
        if (payer == null) {
            throw new BadRequestException("paidBy is not a participant of this group");
        }

        var beneficiaries = new ArrayList<Participant>();
        for (Long id : request.forParticipants()) {
            var participant = byId.get(id);
            if (participant == null) {
                throw new BadRequestException("forParticipants contains participant outside this group: " + id);
            }
            if (!beneficiaries.contains(participant)) {
                beneficiaries.add(participant);
            }
        }

        if (beneficiaries.isEmpty()) {
            throw new BadRequestException("At least one beneficiary is required");
        }

        var expense = expenseRepository.save(
            new Expense(request.amount().setScale(2), group, payer, beneficiaries)
        );

        return new ExpenseResponse(
            expense.getId(),
            expense.getAmount(),
            payer.getId(),
            beneficiaries.stream().map(Participant::getId).toList()
        );
    }
}
