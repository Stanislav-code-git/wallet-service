package com.walletservice.web;

import com.walletservice.dto.BalanceResponse;
import com.walletservice.dto.OperationRequest;
import com.walletservice.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class WalletController {

    private final WalletService service;

    public WalletController(WalletService service) {
        this.service = service;
    }

    @PostMapping("/wallet")
    public BalanceResponse post(@Valid @RequestBody OperationRequest req) {
        return service.apply(req);
    }

    @GetMapping("/wallets/{id}")
    public BalanceResponse get(@PathVariable UUID id) {
        return service.getBalance(id);
    }
}