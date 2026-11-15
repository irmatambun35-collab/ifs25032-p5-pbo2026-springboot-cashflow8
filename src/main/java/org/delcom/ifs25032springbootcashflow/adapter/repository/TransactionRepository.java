package org.delcom.ifs25032springbootcashflow.adapter.repository;

import org.delcom.ifs25032springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25032springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25032springbootcashflow.domain.repository.ITransactionRepository;
import java.time.LocalDateTime;
import java.util.List;

/** Implementasi in-memory untuk unit test. */
public class TransactionRepository extends InMemoryRepository<Transaction> implements ITransactionRepository {
    @Override protected int idOf(Transaction t) { return t.getId(); }

    @Override
    public Transaction save(int userId, String description, long amount, TransactionType type, String proofPath) {
        Transaction t = new Transaction(nextId(), userId, description, amount, type, proofPath, LocalDateTime.now());
        store.put(t.getId(), t);
        return t;
    }

    @Override public List<Transaction> findByUserId(int userId) { return findBy(t -> t.getUserId() == userId); }
}
