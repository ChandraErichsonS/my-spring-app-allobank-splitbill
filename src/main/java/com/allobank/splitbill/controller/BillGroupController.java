package com.allobank.splitbill.controller;

import com.allobank.splitbill.dto.CreateGroupRequest;
import com.allobank.splitbill.dto.GroupResponse;
import com.allobank.splitbill.service.BillGroupService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/groups")
public class BillGroupController {

    private final BillGroupService service;

    public BillGroupController(BillGroupService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GroupResponse create(
            @Valid @RequestBody CreateGroupRequest request) {
        return service.create(request);
    }

    @GetMapping("/{id}")
    public GroupResponse get(@PathVariable Long id) {
        return service.getResponse(id);
    }
}