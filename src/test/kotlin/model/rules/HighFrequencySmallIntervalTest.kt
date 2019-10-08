package model.rules

import model.entities.Violation
import infrastructure.AccountRepositoryImpl
import infrastructure.TransactionRepositoryImpl
import io.kotlintest.shouldBe
import io.kotlintest.specs.BehaviorSpec
import model.entities.Transaction
import model.entities.TransactionRequest
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class HighFrequencySmallIntervalTest : BehaviorSpec({

    val clock = Clock.fixed(
        Instant.parse("2019-02-13T10:01:00.000Z"),
        ZoneOffset.UTC)

    Given("that 2 or less payments have been accepted in a 2 min interval") {

        val accountRepository = AccountRepositoryImpl(clock)
        val transactionRepository = TransactionRepositoryImpl()

        accountRepository.createAccount(true, 100)

        val tx1 = Transaction("M1",
            10,
            Instant.parse("2019-02-13T10:01:10.000Z"))

        val tx2 = Transaction("M2",
            15,
            Instant.parse("2019-02-13T10:01:20.000Z"))

        transactionRepository.add(tx1)
        transactionRepository.add(tx2)

        When("a new transaction is requested in a 2 min interval and checked by high-frequency-small-interval rule") {

            val transactionRequest = TransactionRequest("B", 20, Instant.parse("2019-02-13T10:01:30.000Z"))
            val violation =
                thereShouldNotBeMoreThanThreeTransactionsInTwoMinuteInterval(accountRepository,
                    transactionRepository,
                    transactionRequest
                )

            Then("a high-frequency-small-interval violation SHOULD NOT be created") {

                violation shouldBe null
            }
        }
    }

    Given("that 3 payments have been accepted in a 2 min interval") {

        val accountRepository = AccountRepositoryImpl(clock)
        val transactionRepository = TransactionRepositoryImpl()

        accountRepository.createAccount(true, 100)

        val tx1 = Transaction(
            "M1",
            10,
            Instant.parse("2019-02-13T10:01:10.000Z")
        )

        val tx2 = Transaction(
            "M2",
            15,
            Instant.parse("2019-02-13T10:01:15.000Z")
        )

        val tx3 = Transaction(
            "M2",
            15,
            Instant.parse("2019-02-13T10:01:20.000Z")
        )

        transactionRepository.add(tx1)
        transactionRepository.add(tx2)
        transactionRepository.add(tx3)

        When("a new transaction is requested in a 2 min interval and checked by high-frequency-small-interval rule") {

            val transaction = TransactionRequest("M2", 15, Instant.parse("2019-02-13T10:01:30.000Z"))

            val violation =
                thereShouldNotBeMoreThanThreeTransactionsInTwoMinuteInterval(
                    accountRepository,
                    transactionRepository,
                    transaction
                )

            Then("a high-frequency-small-interval violation SHOULD be created") {

                violation shouldBe Violation("high-frequency-small-interval")
            }
        }

        When("a new transaction is requested in a exact 2 min interval and checked by high-frequency-small-interval rule") {

            val transaction = TransactionRequest("M2", 15, Instant.parse("2019-02-13T10:03:00.000Z"))

            val violation =
                thereShouldNotBeMoreThanThreeTransactionsInTwoMinuteInterval(
                    accountRepository,
                    transactionRepository,
                    transaction
                )

            Then("a high-frequency-small-interval violation SHOULD be created") {

                violation shouldBe Violation("high-frequency-small-interval")
            }
        }
    }
})