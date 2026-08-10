package com.allobank.splitbill.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "participants",
       uniqueConstraints = @UniqueConstraint(name = "uk_participant_group_name", columnNames = {"group_id", "name"}))
public class Participant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private BillGroup group;

    public Participant() {}

    public Participant(String name, BillGroup group) {
        this.name = name;
        this.group = group;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public BillGroup getGroup() { return group; }
}
