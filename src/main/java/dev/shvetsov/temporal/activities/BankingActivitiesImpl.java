package dev.shvetsov.temporal.activities;

import io.temporal.spring.boot.ActivityImpl;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@ActivityImpl(taskQueues = "BANKING_TASK_QUEUE")
public class BankingActivitiesImpl implements BankingActivities {

  private static final Logger log = LoggerFactory.getLogger(BankingActivitiesImpl.class);

  private static final ConcurrentHashMap<String, Double> accounts = new ConcurrentHashMap<>();

  static {
    accounts.put("ACC-001", 5000.0);
    accounts.put("ACC-002", 1000.0);
    accounts.put("ACC-003", 2500.0);
  }

  @Override
  public void withdraw(String accountId, double amount) {
    log.info("Withdrawing: ${} {}", amount, accountId);
    Double balance = accounts.get(accountId);
    if (balance == null) {
      throw new RuntimeException("The account is not found: " + accountId);
    }
    if (balance < amount) {
      throw new RuntimeException("Unsufficient funds on the account: " + accountId);
    }
  }

  @Override
  public void deposit(String accountId, double amount) {
    log.info("Depositing: ${} {}", amount, accountId);
    accounts.merge(accountId, amount, Double::sum);
    log.info("Depositing is done. New balance: {}", accounts.get(accountId));
  }

  @Override
  public double getBalance(String accountId) {
    log.info("Checking the account balance: {} ${}", accountId,
        accounts.getOrDefault(accountId, 0.0));
    return accounts.getOrDefault(accountId, 0.0);
  }
}
