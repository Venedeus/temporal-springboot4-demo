package dev.shvetsov.temporal.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequest {
  private String fromAccountId;
  private String toAccountId;
  private double amount;
  private String referenceId;
  private String correlationId;
}
