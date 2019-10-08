package model.entities

import java.time.Instant

sealed class Operation
data class AccountCreationRequest(val activeCard: Boolean, val limitAvailable: Int) : Operation()
data class TransactionRequest(val merchant: String, val amount: Int, val instant: Instant) : Operation()

data class OperationResult(val account: Account, val violations: List<String>)
