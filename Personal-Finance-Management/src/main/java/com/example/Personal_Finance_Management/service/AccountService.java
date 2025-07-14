package com.example.Personal_Finance_Management.service;

import com.example.Personal_Finance_Management.ExpenceRepository.*;
import com.example.Personal_Finance_Management.dto.*;
import com.example.Personal_Finance_Management.entity.*;
import com.example.Personal_Finance_Management.util.MaskingUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AccountService {
    @Autowired
    private CurrentBalanceRepo currentBalanceRepo;
    @Autowired
    private IncomeRepo incomeRepo;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private AccountTransactionRepository transactionRepository;
    @Autowired
    private TransactionRepo transactionRepo;

    public Account createAccountAndUpdateIncome(AccountCreateRequest request) {
        Optional<Account> existingAccount = accountRepository.findById(request.getAccountNumber());

        if (existingAccount.isPresent()) {
            Account account = existingAccount.get();
            BigDecimal updatedBalance = account.getBalance().add(request.getInitialBalance());
            account.setBalance(updatedBalance);

            return accountRepository.save(account);
        } else {

            Account account = new Account();
            account.setAccountNumber(request.getAccountNumber());
            account.setAccount_holder_name(request.getAccount_holder_name());
            account.setAccountType(request.getAccountType());
            account.setBalance(request.getInitialBalance());


            return accountRepository.save(account);
        }
    }


    public IncomeResponse addAccountBalanceToIncome(IncomeRequest request) {

        Account account = accountRepository.findById(request.getAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("Account not found with number: " + request.getAccountNumber()));

        BigDecimal incomeAmount = request.getAmount();


        if (account.getBalance().compareTo(incomeAmount) < 0) {
            throw new IllegalArgumentException("Insufficient balance in the account");
        }


        account.setBalance(account.getBalance().subtract(incomeAmount));
        accountRepository.save(account);


        Income income = new Income();
        income.setCategory(request.getCategory());
        income.setAccount(account);
        income.setDate(request.getDate() != null ? request.getDate() : LocalDate.now());
        income.setAmount(incomeAmount);


        updateCurrentBalanceForIncome(incomeAmount);
        recordTransaction(request.getAmount(), "income", request.getCategory());

        incomeRepo.save(income);


        IncomeResponse response = new IncomeResponse();
        response.setId(income.getId());
        response.setCategory(income.getCategory());
        response.setDate(income.getDate());
        response.setAmount(income.getAmount());
        response.setAccountNumber(MaskingUtil.maskAccountNumber(Long.valueOf(account.getAccountNumber())));

        return response;
    }


    private void updateCurrentBalanceForIncome(BigDecimal incomeAmount) {

        CurrentBalance currentBalance = currentBalanceRepo.findById(1L)
                .orElseGet(() -> {

                    CurrentBalance newBalance = new CurrentBalance();
                    newBalance.setId(1L);
                    newBalance.setCurrentBalance(BigDecimal.ZERO);
                    currentBalanceRepo.save(newBalance);
                    return newBalance;
                });
        if (currentBalance.getCurrentBalance() == null) {
            currentBalance.setCurrentBalance(BigDecimal.ZERO);
        }
        BigDecimal newBalance = currentBalance.getCurrentBalance().add(incomeAmount);
        currentBalance.setCurrentBalance(newBalance);
        currentBalanceRepo.save(currentBalance);
    }

    private void recordTransaction(BigDecimal amount, String type, String category) {

        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setType(type);
        transaction.setCategory(category);
        transaction.setDate(LocalDate.now());
        transactionRepo.save(transaction);
    }

    @Transactional
    public void transferFunds(TransferRequest transferRequest) {
        Account fromAccount = accountRepository.findById(transferRequest.getFromAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("From account not found"));
        Account toAccount = accountRepository.findById(transferRequest.getToAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("To account not found"));

        if (fromAccount.getBalance().compareTo(transferRequest.getAmount()) < 0) {
            throw new IllegalArgumentException("Insufficient balance in the from account");
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(transferRequest.getAmount()));
        toAccount.setBalance(toAccount.getBalance().add(transferRequest.getAmount()));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        AccountTransaction transaction = new AccountTransaction();
        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(toAccount);
        transaction.setAmount(transferRequest.getAmount());
        transactionRepository.save(transaction);


    }

    public AccountResponse getAccountByAccountNumber(String accountNumber) {
        Account account = accountRepository.findById(accountNumber)
                .orElseThrow(() -> new IllegalStateException("Account not found"));

        AccountResponse response = new AccountResponse();
        response.setAccountNumber(MaskingUtil.maskAccountNumber(Long.valueOf(account.getAccountNumber())));
        response.setAccount_holder_name(account.getAccount_holder_name());
        response.setAccountType(account.getAccountType());
        response.setBalance(account.getBalance());
        response.setOutgoingTransactions(
                account.getOutgoingTransactions().stream()
                        .map(this::convertToTransactionResponse)
                        .collect(Collectors.toList())
        );
        response.setIncomingTransactions(
                account.getIncomingTransactions().stream()
                        .map(this::convertToTransactionResponse)
                        .collect(Collectors.toList())
        );

        return response;
    }

    private TransactionResponse convertToTransactionResponse(AccountTransaction transaction) {
        TransactionResponse response = new TransactionResponse();
        response.setAmount(transaction.getAmount());
        response.setDescription(transaction.getDescription());
        response.setTransactionDate(transaction.getTransactionDate());
        return response;
    }
}

