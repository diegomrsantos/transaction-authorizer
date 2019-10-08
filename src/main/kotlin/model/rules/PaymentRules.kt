package model.rules

import model.entities.TransactionRequest
import model.entities.Violation
import model.repository.AccountRepository
import model.repository.TransactionRepository
import java.time.temporal.ChronoUnit

typealias PaymentRule = (AccountRepository, TransactionRepository, TransactionRequest) -> Violation?

val transactionWhenCardIsNotActiveRule: PaymentRule = fun(accountRepository, _, _): Violation? {

    val account = accountRepository.getAccount()
    requireNotNull(account) { "Account has not been initialized yet" }
    if (!account.activeCard) {
        return Violation("card-not-active")
    }
    return null
}

val transactionAmountShouldNotExceedAvailableLimitRule: PaymentRule =  fun(accountRepository, _, transactionRequest): Violation? {

    val account = accountRepository.getAccount()
    requireNotNull(account) { "Account has not been initialized yet" }
    if (account.availableLimit < transactionRequest.amount) {
        return Violation("insufficient-limit")
    }
    return null
}

val thereShouldNotBeMoreThanThreeTransactionsInTwoMinuteInterval: PaymentRule = fun(accountRepository,
                                                                                    transactionRepository,
                                                                                    transactionRequest): Violation? {

    val account = accountRepository.getAccount()
    requireNotNull(account) { "Account has not been initialized yet" }
    val transactions = transactionRepository
        .getTransactions(transactionRequest.instant.minus(2, ChronoUnit.MINUTES), transactionRequest.instant)
        .filter { it.violations.isEmpty() }
    if (transactions.size >= 3) {
        return Violation("high-frequency-small-interval")
    }
    return null
}

val thereShouldNotBeMoreThanTwoSimilarTransactionsInATwoMinutesInterval: PaymentRule = fun(accountRepository,
                                                                                           transactionRepository,
                                                                                           transactionRequest): Violation? {

    val account = accountRepository.getAccount()
    requireNotNull(account) { "Account has not been initialized yet" }
    val transactions = transactionRepository
        .getTransactions(transactionRequest.instant.minus(2, ChronoUnit.MINUTES), transactionRequest.instant)
        .filter { it.violations.isEmpty() }
        .filter { it.amount == transactionRequest.amount && it.merchant == transactionRequest.merchant }
    if (transactions.size >= 2) {
        return Violation("doubled-transaction")
    }
    return null
}