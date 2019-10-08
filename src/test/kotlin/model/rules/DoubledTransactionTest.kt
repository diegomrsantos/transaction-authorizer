package model.rules

import infrastructure.AccountRepositoryImpl
import model.entities.Transaction
import model.entities.TransactionRequest
import model.entities.Violation
import infrastructure.TransactionRepositoryImpl
import io.kotlintest.shouldBe
import io.kotlintest.specs.BehaviorSpec
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class DoubledTransactionTest : BehaviorSpec({

    val clock = Clock.fixed(
        Instant.parse("2019-02-13T10:01:00.000Z"),
        ZoneOffset.UTC)

    Given("that 2 similar transactions (same amount and merchant) have not been accepted in a 2 min interval") {

        val accountRepository = AccountRepositoryImpl(clock)
        val transactionRepository = TransactionRepositoryImpl()

        accountRepository.createAccount(true, 100)

        val tx1 = Transaction("M1",
            10,
            Instant.parse("2019-02-13T10:01:10.000Z"))

        val tx2 = Transaction("M2",
            15,
            Instant.parse("2019-02-13T10:01:15.000Z"))

        transactionRepository.add(tx1)
        transactionRepository.add(tx2)

        When("a new similar transaction is requested in a 2 min interval and checked by doubled-transaction rule") {

            val transaction = TransactionRequest("M1",
                10,
                Instant.parse("2019-02-13T10:01:20.000Z"))

            val violation =
                thereShouldNotBeMoreThanTwoSimilarTransactionsInATwoMinutesInterval(accountRepository,
                    transactionRepository,
                    transaction
                )

            Then("a doubled-transaction violation SHOULD NOT be created") {

                violation shouldBe null
            }
        }
    }

    Given("that 2 similar transactions (same amount and merchant) have been accepted in a 2 min interval") {

        val accountRepository = AccountRepositoryImpl(clock)
        val transactionRepository = TransactionRepositoryImpl()

        accountRepository.createAccount(true, 100)

        val tx1 = Transaction("M1",
            10,
            Instant.parse("2019-02-13T10:01:10.000Z"))

        val tx2 = Transaction("M1",
            10,
            Instant.parse("2019-02-13T10:01:15.000Z"))

        transactionRepository.add(tx1)
        transactionRepository.add(tx2)

        When("a new similar transaction is requested in a 2 min interval and checked by doubled-transaction rule") {

            val transaction = TransactionRequest("M1",
                10,
                Instant.parse("2019-02-13T10:01:20.000Z")
            )

            val violation =
                thereShouldNotBeMoreThanTwoSimilarTransactionsInATwoMinutesInterval(accountRepository,
                    transactionRepository,
                    transaction
                )

            Then("a doubled-transaction violation SHOULD be created") {

                violation shouldBe Violation("doubled-transaction")
            }
        }

        When("a new similar transaction is requested in a exact 2 min interval and checked by doubled-transaction rule") {

            val transaction = TransactionRequest("M1",
                10,
                Instant.parse("2019-02-13T10:03:10.000Z")
            )

            val violation =
                thereShouldNotBeMoreThanTwoSimilarTransactionsInATwoMinutesInterval(accountRepository,
                    transactionRepository,
                    transaction
                )

            Then("a doubled-transaction violation SHOULD be created") {

                violation shouldBe Violation("doubled-transaction")
            }
        }
    }
})