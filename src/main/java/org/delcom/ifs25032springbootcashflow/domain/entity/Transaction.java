package org.delcom.ifs25032springbootcashflow.domain.entity;

import java.time.LocalDateTime;

public class Transaction {
    private final int id;
    private final int userId;
    private String description;
    private long amount;
    private TransactionType type;
    private String proofPath;
    private final LocalDateTime createdAt;

    public Transaction(int id, int userId, String description, long amount, TransactionType type,
                       String proofPath, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.description = description;
        this.amount = amount;
        this.type = type;
        this.proofPath = proofPath;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public long getAmount() { return amount; }
    public void setAmount(long amount) { this.amount = amount; }
    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }
    public String getProofPath() { return proofPath; }
    public void setProofPath(String proofPath) { this.proofPath = proofPath; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
