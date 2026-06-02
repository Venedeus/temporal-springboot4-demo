package dev.shvetsov.temporal.workflow;

import dev.shvetsov.temporal.model.TransferRequest;
import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface MoneyTransferWorkflow {
  @WorkflowMethod
  void transferMoney(TransferRequest request);

  @SignalMethod
  void cancelTransfer();

  @QueryMethod
  String getTransferStatus();

  @QueryMethod
  TransferRequest getTransferDetails();
}
