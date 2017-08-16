package com.github.mgolubyatnikov.transferservice.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Objects;

public class Transfer {

    @JsonProperty
    private Long id;

    @JsonProperty
    @NotNull
    private Long sourceAccountId;

    @JsonProperty
    @NotNull
    private Long destinationAccountId;

    @JsonProperty
    @NotNull
    private BigDecimal amount;

    public Transfer() {
    }

    public Transfer(Long sourceAccountId, Long destinationAccountId, BigDecimal amount) {
        this.sourceAccountId = sourceAccountId;
        this.destinationAccountId = destinationAccountId;
        this.amount = amount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSourceAccountId() {
        return sourceAccountId;
    }

    public void setSourceAccountId(Long sourceAccountId) {
        this.sourceAccountId = sourceAccountId;
    }

    public Long getDestinationAccountId() {
        return destinationAccountId;
    }

    public void setDestinationAccountId(Long destinationAccountId) {
        this.destinationAccountId = destinationAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transfer transfer = (Transfer) o;
        return Objects.equals(id, transfer.id) &&
                Objects.equals(sourceAccountId, transfer.sourceAccountId) &&
                Objects.equals(destinationAccountId, transfer.destinationAccountId) &&
                Objects.equals(amount, transfer.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, sourceAccountId, destinationAccountId, amount);
    }
}
