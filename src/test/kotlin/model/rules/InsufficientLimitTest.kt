package model.rules

import model.entities.Violation
import infrastructure.AccountRepositoryImpl
import infrastructure.TransactionRepositoryImpl
import io.kotlintest.shouldBe
import io.kotlintest.specs.BehaviorSpec
import model.entities.TransactionRequest
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class InsufficientLimitTest :BehaviorSpec({

    val clock = Clock.fixed(
        Instant.parse("2019-02-13T10:01:00.000Z"),
        ZoneOffset.UTC)

    Given("an account with an available limit") {

        val accountRepository = AccountRepositoryImpl(clock)
        val transactionRepository = TransactionRepositoryImpl()

        accountRepository.createAccount(true, 50)

        When("a transaction request with a greater amount is checked by insufficient-limit rule") {

            val transaction = TransactionRequest("M1",
                80,
                Instant.parse("2019-02-13T10:01:20.000Z")
            )

            val violation = transactionAmountShouldNotExceedAvailableLimitRule(accountRepository,
                transactionRepository,
                transaction
            )

            Then("an insufficient-limit violation SHOULD be created") {

                violation shouldBe Violation("insufficient-limit")
            }
        }

        When("a transaction request with a smaller amount is checked by insufficient-limit rule") {

            val transaction = TransactionRequest("M1",
                10,
                Instant.parse("2019-02-13T10:01:10.000Z")
            )

            val violation = transactionAmountShouldNotExceedAvailableLimitRule(accountRepository,
                transactionRepository,
                transaction
            )

            Then("an insufficient-limit violation SHOULD NOT be created") {

                violation shouldBe null
            }
        }
    }
})