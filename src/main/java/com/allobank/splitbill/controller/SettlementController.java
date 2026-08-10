package com.allobank.splitbill.controller;

import com.allobank.splitbill.dto.SettlementResponse;
import com.allobank.splitbill.service.SettlementService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/groups/{groupId}/settlements")
public class SettlementController {

    private final SettlementService service;

    public SettlementController(SettlementService service) {
        this.service = service;
    }

    @GetMapping
    public SettlementResponse get(@PathVariable Long groupId) {
        return service.calculate(groupId);
    }
}