package model.rules

import model.entities.AccountCreationRequest
import model.entities.Violation
import model.repository.AccountRepository

typealias AccountRule = (AccountCreationRequest, AccountRepository) -> Violation?

val accountCreationRule: AccountRule = fun(_, accountRepository): Violation? {

    val account = accountRepository.getAccount()
    if (account != null) {
        return Violation("account-already-initialized")
    }
    return null
}