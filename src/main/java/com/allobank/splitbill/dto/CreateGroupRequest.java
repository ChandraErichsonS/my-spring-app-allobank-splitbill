package com.allobank.splitbill.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateGroupRequest(
    @NotBlank @Size(max = 120) String name,
    @NotEmpty @Size(min = 2, max = 100) List<@Valid ParticipantRequest> participants
) {
    public record ParticipantRequest(
        @NotBlank @Size(max = 100) String name
    ) {}
}
