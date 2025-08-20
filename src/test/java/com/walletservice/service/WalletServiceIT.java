package com.walletservice.service;

import com.walletservice.dto.OperationRequest;
import com.walletservice.dto.OperationType;
import com.walletservice.it.AbstractPostgresTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class WalletServiceIT extends AbstractPostgresTest {

    @Autowired WalletService service;
    @Autowired JdbcTemplate jdbc;

    static final UUID ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @BeforeEach
    void ensureWallet() {
        jdbc.update("""
        INSERT INTO wallets(id, balance_cents, created_at, updated_at)
        VALUES (?,?, now(), now())
        ON CONFLICT (id) DO NOTHING
        """, ID, 0L);
    }

    @Test
    void deposit_and_withdraw() {
        service.apply(new OperationRequest(ID, OperationType.DEPOSIT, 500));
        var r = service.apply(new OperationRequest(ID, OperationType.WITHDRAW, 200));
        assertEquals(300, r.balance());
    }
}