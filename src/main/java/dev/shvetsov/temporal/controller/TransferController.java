package dev.shvetsov.temporal.controller;

import dev.shvetsov.temporal.model.TransferRequest;
import dev.shvetsov.temporal.workflow.MoneyTransferWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {

  private static final Logger log = LoggerFactory.getLogger(TransferController.class);
  private static final String TASK_QUEUE = "BANKING_TASK_QUEUE";

  @Autowired
  private WorkflowClient workflowClient;

  @PostMapping
  public ResponseEntity<Map<String, String>> initiateTransfer(
      @RequestBody TransferRequest request) {
    String workflowId = "transfer-" + UUID.randomUUID().toString();
    String correlationId = UUID.randomUUID().toString();

    TransferRequest enrichedRequest = TransferRequest.builder()
        .fromAccountId(request.getFromAccountId())
        .toAccountId(request.getToAccountId())
        .amount(request.getAmount())
        .referenceId(workflowId)
        .correlationId(correlationId)
        .build();

    WorkflowOptions options = WorkflowOptions.newBuilder()
        .setWorkflowId(workflowId)
        .setTaskQueue(TASK_QUEUE)
        .build();

    MoneyTransferWorkflow workflow = workflowClient.newWorkflowStub(
        MoneyTransferWorkflow.class,
        options
    );

    WorkflowClient.start(workflow::transferMoney, enrichedRequest);

    log.info("The workflow is started: {}, correlation: {}", workflowId, correlationId);

    Map<String, String> response = new HashMap<>();
    response.put("workflowId", workflowId);
    response.put("correlationId", correlationId);
    response.put("status", "STARTED");
    return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
  }

  @GetMapping("/{workflowId}/status")
  public ResponseEntity<Map<String, Object>> getTransferStatus(@PathVariable String workflowId) {
    MoneyTransferWorkflow workflow = workflowClient.newWorkflowStub(
        MoneyTransferWorkflow.class,
        workflowId
    );
    String status = workflow.getTransferStatus();
    TransferRequest details = workflow.getTransferDetails();
    Map<String, Object> response = new HashMap<>();
    response.put("workflowId", workflowId);
    response.put("status", status);
    if (details != null) {
      response.put("fromAccount", details.getFromAccountId());
      response.put("toAccount", details.getToAccountId());
      response.put("amount", details.getAmount());
    }
    return ResponseEntity.ok(response);
  }

  @PostMapping("/{workflowId}/cancel")
  public ResponseEntity<Map<String, String>> cancelTransfer(@PathVariable String workflowId) {
    MoneyTransferWorkflow workflow = workflowClient.newWorkflowStub(
        MoneyTransferWorkflow.class,
        workflowId
    );
    workflow.cancelTransfer();
    log.info("The workflow cancellation has been sent: {}", workflowId);
    Map<String, String> response = new HashMap<>();
    response.put("workflowId", workflowId);
    response.put("message", "Cancel signal has been sent");
    return ResponseEntity.ok(response);
  }
}
