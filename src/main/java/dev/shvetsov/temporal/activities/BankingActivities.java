package dev.shvetsov.temporal.activities;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface BankingActivities {
  @ActivityMethod
  void withdraw(String accountId, double amount);

  @ActivityMethod
  void deposit(String accountId, double amount);

  @ActivityMethod
  double getBalance(String accountId);
}
