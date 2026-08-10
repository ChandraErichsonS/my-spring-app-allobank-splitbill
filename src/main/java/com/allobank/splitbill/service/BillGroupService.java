package com.allobank.splitbill.service;

import com.allobank.splitbill.dto.CreateGroupRequest;
import com.allobank.splitbill.dto.GroupResponse;
import com.allobank.splitbill.entity.BillGroup;
import com.allobank.splitbill.entity.Participant;
import com.allobank.splitbill.exception.BadRequestException;
import com.allobank.splitbill.exception.ResourceNotFoundException;
import com.allobank.splitbill.repository.BillGroupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

@Service
public class BillGroupService {

    private final BillGroupRepository groupRepository;

    public BillGroupService(BillGroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    @Transactional
    public GroupResponse create(CreateGroupRequest request) {
        var normalizedNames = new HashSet<String>();
        var group = new BillGroup(request.name().trim());

        for (var p : request.participants()) {
            var name = p.name().trim();

            if (!normalizedNames.add(name.toLowerCase())) {
                throw new BadRequestException("Duplicate participant: " + name);
            }

            group.getParticipants().add(
                new Participant(name, group)
            );
        }

        var saved = groupRepository.save(group);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public BillGroup get(Long id) {
        return groupRepository.findById(id)
            .orElseThrow(() ->
                new ResourceNotFoundException("Group not found: " + id)
            );
    }

    // TAMBAHKAN METHOD INI
    @Transactional(readOnly = true)
    public GroupResponse getResponse(Long id) {
        BillGroup group = groupRepository.findById(id)
            .orElseThrow(() ->
                new ResourceNotFoundException("Group not found: " + id)
            );

        return toResponse(group);
    }

    public GroupResponse toResponse(BillGroup group) {
        return new GroupResponse(
            group.getId(),
            group.getName(),
            group.getParticipants().stream()
                .map(p -> new GroupResponse.ParticipantResponse(
                    p.getId(),
                    p.getName()
                ))
                .toList()
        );
    }
}