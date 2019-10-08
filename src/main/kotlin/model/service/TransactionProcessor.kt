package model.service
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import model.entities.*
import model.repository.AccountRepository
import model.repository.TransactionRepository
import model.rules.*

suspend fun process(accountRepository: AccountRepository,
                    transactionRepository: TransactionRepository,
                    operationChannel: ReceiveChannel<Operation>,
                    resultChannel: SendChannel<OperationResult>
) {
    try {
        for (operation in operationChannel) {
            val operationResult = when (operation) {
                is AccountCreationRequest -> {
                    processAccountCreation(operation, accountRepository)
                }
                is TransactionRequest -> {
                    processTransaction(operation, accountRepository, transactionRepository)
                }
            }
            resultChannel.send(operationResult)
        }
    } finally {
        resultChannel.close()
    }
}

fun processAccountCreation(
    accountCreationRequest: AccountCreationRequest,
    accountRepository: AccountRepository
): OperationResult {

    val violations = listOf(accountCreationRule)
        .mapNotNull { it(accountCreationRequest, accountRepository) }

    if (violations.isEmpty()) {
        accountRepository.createAccount(accountCreationRequest.activeCard, accountCreationRequest.limitAvailable)
    }
    return OperationResult(accountRepository.getAccount()!!.copy(), violations.map {it.violation}.toList())
}

fun processTransaction(
    transactionRequest: TransactionRequest,
    accountRepository: AccountRepository,
    transactionRepository: TransactionRepository
): OperationResult {

    val violations =  listOf(
        transactionWhenCardIsNotActiveRule,
        transactionAmountShouldNotExceedAvailableLimitRule,
        thereShouldNotBeMoreThanThreeTransactionsInTwoMinuteInterval,
        thereShouldNotBeMoreThanTwoSimilarTransactionsInATwoMinutesInterval
    ).mapNotNull { it(accountRepository, transactionRepository, transactionRequest) }

    if (violations.isEmpty()) {
        accountRepository.decreaseAvailableLimit(transactionRequest.amount)
    }
    transactionRepository.add(
        Transaction(transactionRequest.merchant, transactionRequest.amount, transactionRequest.instant, violations)
    )
    return OperationResult(accountRepository.getAccount()!!.copy(), violations.map {it.violation}.toList())
}

