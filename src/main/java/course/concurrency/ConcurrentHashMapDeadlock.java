package course.concurrency;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentHashMapDeadlock {

    public static void main(String[] args) {
        var clientName = "Ivan Deadlock";
        var greenBankAccounts = new ConcurrentHashMap<String, Integer>();
        var redBankAccounts = new ConcurrentHashMap<String, Integer>();
        greenBankAccounts.put(clientName, 1000);
        redBankAccounts.put(clientName, 1000);

        new Thread(() -> transfer(greenBankAccounts, redBankAccounts, clientName, 100), "GREEN->RED").start();
        new Thread(() -> transfer(redBankAccounts, greenBankAccounts, clientName, 50), "RED->GREEN").start();
    }

    public static void transfer(Map<String, Integer> from, Map<String, Integer> to, String clientName, Integer sum) {
        to.compute(clientName, (clientName1, accountSum1) -> {
            try { Thread.sleep(1000); } catch (Throwable ignored) {}
            from.compute(clientName, (clientName2, accountSum2) -> accountSum2 - sum);
            return accountSum1 + sum;
        });
    }
}
