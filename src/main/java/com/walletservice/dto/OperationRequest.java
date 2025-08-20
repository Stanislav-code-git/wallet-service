package com.walletservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

public record OperationRequest(
        @NotNull UUID walletId,
        @NotNull OperationType operationType,
        @Positive long amount
) {}