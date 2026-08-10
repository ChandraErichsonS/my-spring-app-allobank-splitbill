package com.allobank.splitbill.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "expenses")
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private BillGroup group;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "paid_by_id", nullable = false)
    private Participant paidBy;

    @ManyToMany
    @JoinTable(
        name = "expense_beneficiaries",
        joinColumns = @JoinColumn(name = "expense_id"),
        inverseJoinColumns = @JoinColumn(name = "participant_id")
    )
    private List<Participant> beneficiaries = new ArrayList<>();

    public Expense() {}

    public Expense(BigDecimal amount, BillGroup group, Participant paidBy, List<Participant> beneficiaries) {
        this.amount = amount;
        this.group = group;
        this.paidBy = paidBy;
        this.beneficiaries = beneficiaries;
    }

    public Long getId() { return id; }
    public BigDecimal getAmount() { return amount; }
    public BillGroup getGroup() { return group; }
    public Participant getPaidBy() { return paidBy; }
    public List<Participant> getBeneficiaries() { return beneficiaries; }
}
