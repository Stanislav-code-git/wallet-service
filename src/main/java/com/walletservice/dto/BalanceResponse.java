package com.walletservice.dto;

import java.util.UUID;

public record BalanceResponse(UUID walletId, long balance) {}