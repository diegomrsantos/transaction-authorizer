package model.rules

import infrastructure.AccountRepositoryImpl
import model.entities.TransactionRequest
import model.entities.Violation
import infrastructure.TransactionRepositoryImpl
import io.kotlintest.shouldBe
import io.kotlintest.specs.BehaviorSpec
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class CardNotActiveTest : BehaviorSpec({

    val clock = Clock.fixed(
        Instant.parse("2019-02-13T10:01:00.000Z"),
        ZoneOffset.UTC)

    Given("an account with an inactive card") {

        val accountRepository = AccountRepositoryImpl(clock)
        accountRepository.createAccount(false, 100)

        When("a transaction request is checked by the card-not-active rule") {

            val transaction = TransactionRequest("M1",
                10,
                Instant.parse("2019-02-13T09:59:20.000Z")
            )

            val violation = transactionWhenCardIsNotActiveRule(accountRepository,
                TransactionRepositoryImpl(),
                transaction
            )

            Then("a card-not-active violation SHOULD be created") {

                violation shouldBe Violation("card-not-active")
            }
        }
    }

    Given("an account with an active card") {

        val accountRepository = AccountRepositoryImpl(clock)
        accountRepository.createAccount(true, 100)

        When("a transaction request is checked by the card-not-active rule") {

            val transaction = TransactionRequest("M1",
                10,
                Instant.parse("2019-02-13T09:59:20.000Z")
            )

            val violation = transactionWhenCardIsNotActiveRule(accountRepository,
                TransactionRepositoryImpl(),
                transaction
            )

            Then("a card-not-active violation SHOULD NOT be created") {

                violation shouldBe null
            }
        }
    }
})