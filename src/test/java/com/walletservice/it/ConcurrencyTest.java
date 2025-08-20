package com.walletservice.it;

import com.walletservice.dto.OperationRequest;
import com.walletservice.dto.OperationType;
import com.walletservice.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class ConcurrencyTest extends AbstractPostgresTest {

    @Autowired WalletService service;
    @Autowired JdbcTemplate jdbc;

    static final UUID ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @BeforeEach
    void resetWallet() {
        jdbc.update("DELETE FROM wallets WHERE id = ?", ID);
        jdbc.update("""
        INSERT INTO wallets(id, balance_cents, created_at, updated_at)
        VALUES (?,?, now(), now())
        """, ID, 0L);
    }

    @Test
    void thousandDeposits_consistent() throws Exception {
        int threads = 100;
        int opsPerThread = 10; // итого 1000 операций

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CyclicBarrier start = new CyclicBarrier(threads);
        CountDownLatch done = new CountDownLatch(threads);

        for (int t = 0; t < threads; t++) {
            pool.submit(() -> {
                try {
                    start.await(); // одновременный старт
                    for (int i = 0; i < opsPerThread; i++) {
                        service.apply(new OperationRequest(ID, OperationType.DEPOSIT, 1));
                    }
                } catch (Exception ignored) {
                } finally {
                    done.countDown();
                }
            });
        }

        boolean completed = done.await(90, TimeUnit.SECONDS);
        pool.shutdown();

        if (!completed) {
            throw new AssertionError("Tasks didn't finish in time");
        }

        var r = service.getBalance(ID);
        assertEquals(1000, r.balance()); // строгая консистентность, без 5xx
    }
}