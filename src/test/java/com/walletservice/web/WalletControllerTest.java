package com.walletservice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.walletservice.dto.OperationRequest;
import com.walletservice.dto.OperationType;
import com.walletservice.it.AbstractPostgresTest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class WalletControllerTest extends AbstractPostgresTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @Autowired JdbcTemplate jdbc;

    static final UUID ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @BeforeEach
    void ensureWallet() {
        // создаем тестовый кошелек, если его нет
        jdbc.update("""
        INSERT INTO wallets(id, balance_cents, created_at, updated_at)
        VALUES (?,?, now(), now())
        ON CONFLICT (id) DO NOTHING
        """, ID, 0L);
    }

    @Test
    void deposit_then_get_ok() throws Exception {
        var body = om.writeValueAsString(new OperationRequest(ID, OperationType.DEPOSIT, 1000));

        mvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.walletId").value(ID.toString()))
                .andExpect(jsonPath("$.balance").value(1000));

        mvc.perform(get("/api/v1/wallets/{id}", ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.walletId").value(ID.toString()))
                .andExpect(jsonPath("$.balance").value(1000));
    }

    @Test
    void withdraw_notEnough_conflict409() throws Exception {
        var body = om.writeValueAsString(new OperationRequest(ID, OperationType.WITHDRAW, 2_000_000));
        mvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict()) // 409
                .andExpect(header().string("Content-Type",
                        Matchers.containsString("application/problem+json")));
    }

    @Test
    void invalidJson_badRequest400() throws Exception {
        mvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{bad json"))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type",
                        Matchers.containsString("application/problem+json")));
    }

    @Test
    void get_unknownWallet_404() throws Exception {
        UUID other = UUID.fromString("22222222-2222-2222-2222-222222222222");
        mvc.perform(get("/api/v1/wallets/{id}", other))
                .andExpect(status().isNotFound())
                .andExpect(header().string("Content-Type",
                        Matchers.containsString("application/problem+json")));
    }
}