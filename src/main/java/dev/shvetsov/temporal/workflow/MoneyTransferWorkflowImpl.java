package dev.shvetsov.temporal.workflow;

import dev.shvetsov.temporal.activities.BankingActivities;
import dev.shvetsov.temporal.model.TransferRequest;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Workflow;
import java.time.Duration;
import org.slf4j.Logger;

@WorkflowImpl(taskQueues = "BANKING_TASK_QUEUE")
public class MoneyTransferWorkflowImpl implements MoneyTransferWorkflow {
  private static final Logger log = Workflow.getLogger(MoneyTransferWorkflowImpl.class);

  private final BankingActivities activities = Workflow.newActivityStub(
    BankingActivities.class,
      ActivityOptions.newBuilder()
          .setStartToCloseTimeout(Duration.ofSeconds(10))
          .setRetryOptions(RetryOptions.newBuilder()
              .setInitialInterval(Duration.ofSeconds(1))
              .setMaximumInterval(Duration.ofSeconds(5))
              .setBackoffCoefficient(2.0)
              .setMaximumAttempts(3)
              .build()
          )
          .build()
  );

  private volatile String status = "PENDING";
  private volatile TransferRequest currentRequest;

  @Override
  public void transferMoney(TransferRequest request) {
    this.currentRequest = request;
    log.info("A new transfer is started: {} -> {} (${}) [{}]",
        request.getFromAccountId(), request.getToAccountId(),
        request.getAmount(), request.getCorrelationId());

    try {
      status = "CHECKING_BALANCE";
      double balance = activities.getBalance(request.getFromAccountId());
      if(balance < request.getAmount()) {
        throw new RuntimeException("Not enough balance: " + balance);
      }
      log.info("The balance is checked. Available: ${}", balance);

      status = "WITHDRAWING";
      activities.withdraw(request.getFromAccountId(), request.getAmount());
      log.info("The balance is withdraw.");

      status = "DEPOSITING";
      activities.deposit(request.getToAccountId(), request.getAmount());
      log.info("The balance is deposited.");

      status = "COMPLETED";
      log.info("The transfer is done: {}", request.getReferenceId());
    }
    catch (Exception e) {
      status = "FAILED";
      log.error("The transfer is failed: {}", e.getMessage());
      throw new RuntimeException("A transfer error: " + e.getMessage(), e);
    }

  }

  @Override
  public void cancelTransfer() {
    if("PENDING".equals(status) || "CHECKING_BALANCE".equals(status)) {
      status ="CANCELLED";
      log.warn("The transfer is cancelled: {}", currentRequest.getReferenceId());
    } else {
      log.warn("Cannot cancel the transfer in the status: {}", status);
    }
  }

  @Override
  public String getTransferStatus() {
    return status;
  }

  @Override
  public TransferRequest getTransferDetails() {
    return currentRequest;
  }
}
