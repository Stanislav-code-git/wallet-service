package com.walletservice.repo;

import com.walletservice.domain.Wallet;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
        UPDATE wallets
           SET balance_cents = balance_cents + :delta,
               updated_at = now()
         WHERE id = :id
           AND (:delta >= 0 OR balance_cents >= -:delta)
         RETURNING balance_cents
        """, nativeQuery = true)
    List<Long> applyDelta(@Param("id") UUID id, @Param("delta") long delta);
}