package com.walletservice.service;

import com.walletservice.dto.*;
import com.walletservice.repo.WalletRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class WalletService {

    private final WalletRepository repo;

    public WalletService(WalletRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public BalanceResponse apply(OperationRequest req) {
        long delta = req.operationType() == OperationType.DEPOSIT ? req.amount() : -req.amount();

        // Пытаемся атомарно применить изменение
        List<Long> result = repo.applyDelta(req.walletId(), delta);
        if (!result.isEmpty()) {
            return new BalanceResponse(req.walletId(), result.get(0));
        }

        // Выясняем причину отказа
        boolean exists = repo.existsById(req.walletId());
        if (!exists) {
            throw new NotFoundException("Wallet not found");
        }
        throw new BusinessConflictException("Insufficient funds");
    }

    @Transactional
    public BalanceResponse getBalance(UUID id) {
        long balance = repo.findById(id).orElseThrow(() -> new NotFoundException("Wallet not found"))
                .getBalanceCents();
        return new BalanceResponse(id, balance);
    }
}