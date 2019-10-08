package model.rules

import infrastructure.AccountRepositoryImpl
import model.entities.AccountCreationRequest
import model.entities.Violation
import io.kotlintest.shouldBe
import io.kotlintest.specs.BehaviorSpec
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class AccountAlreadyInitialized : BehaviorSpec({

    val clock = Clock.fixed(
        Instant.parse("2019-02-13T10:01:00.000Z"),
        ZoneOffset.UTC)

    Given("that an account has already been created") {

        val accountRepository = AccountRepositoryImpl(clock)
        accountRepository.createAccount(true, 100)

        When("a new account creation request is checked by the account-already-initialized rule") {

            val accountCreationRequest = AccountCreationRequest(false, 50)
            val violation = accountCreationRule(accountCreationRequest, accountRepository)

            Then("an account-already-initialized violation SHOULD BE created") {

                violation shouldBe Violation("account-already-initialized")
            }
        }
    }

    Given("that an account has not been created") {

        val accountRepository = AccountRepositoryImpl(clock)

        When("a new account creation is requested") {

            val accountCreationRequest = AccountCreationRequest(false, 50)
            val violation = accountCreationRule(accountCreationRequest, accountRepository)

            Then("an account-already-initialized violation SHOULD NOT BE created") {

                violation shouldBe null
            }
        }
    }
})