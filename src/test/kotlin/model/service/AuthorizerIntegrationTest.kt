package model.service

import infrastructure.AccountRepositoryImpl
import infrastructure.TransactionRepositoryImpl
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldContainExactly
import io.kotlintest.shouldBe
import io.kotlintest.specs.AnnotationSpec
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import model.entities.AccountCreationRequest
import model.entities.Operation
import model.entities.OperationResult
import model.entities.TransactionRequest
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class AuthorizerIntegrationTest : AnnotationSpec() {

    private val accountRepositoryClock: Clock = Clock.fixed(
        Instant.parse("2019-02-13T10:00:00.000Z"),
        ZoneOffset.UTC)

    private val appClock: Clock = Clock.fixed(
        Instant.parse("2019-02-13T10:01:00.000Z"),
        ZoneOffset.UTC)

    @Test
    fun `account-already-initialized`() = runBlocking {

        //Given
        val operationChannel = Channel<Operation>(Channel.UNLIMITED)
        val resultChannel = Channel<OperationResult>(Channel.UNLIMITED)

        val accountRepository = AccountRepositoryImpl(accountRepositoryClock)
        val transactionRepository = TransactionRepositoryImpl()

        val accountCreationRequest1 = AccountCreationRequest(true, 100)
        val accountCreationRequest2 = AccountCreationRequest(false, 200)

        operationChannel.send(accountCreationRequest1)
        operationChannel.send(accountCreationRequest2)
        operationChannel.close()

        //When
        launch {
            process(accountRepository, transactionRepository, operationChannel, resultChannel)
        }

        //Then
        val operationResult1 = resultChannel.receive()
        val operationResult2 = resultChannel.receive()

        operationResult1.account.activeCard shouldBe true
        operationResult1.account.availableLimit shouldBe 100
        operationResult1.violations.shouldBeEmpty()

        operationResult2.account.activeCard shouldBe true
        operationResult2.account.availableLimit shouldBe 100
        operationResult2.violations shouldContainExactly listOf("account-already-initialized")
    }

    @Test
    fun `all-transactions-accepted`() = runBlocking {

        //Given
        val operationChannel = Channel<Operation>(Channel.UNLIMITED)
        val resultChannel = Channel<OperationResult>(Channel.UNLIMITED)

        val accountRepository = AccountRepositoryImpl(accountRepositoryClock)
        val transactionRepository = TransactionRepositoryImpl()

        val ac = AccountCreationRequest(true, 100)

        val t1 = TransactionRequest("M1",
            20,
            Instant.parse("2019-02-13T10:00:05.000Z")
        )

        val t2 = TransactionRequest("M2",
            20,
            Instant.parse("2019-02-13T10:00:10.000Z")
        )

        val t3 = TransactionRequest("M3",
            20,
            Instant.parse("2019-02-13T10:00:15.000Z")
        )

        operationChannel.send(ac)
        operationChannel.send(t1)
        operationChannel.send(t2)
        operationChannel.send(t3)
        operationChannel.close()

        //When
        launch {
            process(accountRepository, transactionRepository, operationChannel, resultChannel)
        }

        //Then
        val operationResult1 = resultChannel.receive()
        val operationResult2 = resultChannel.receive()
        val operationResult3 = resultChannel.receive()
        val operationResult4 = resultChannel.receive()

        operationResult1.account.activeCard shouldBe true
        operationResult1.account.availableLimit shouldBe 100
        operationResult1.violations.shouldBeEmpty()

        operationResult2.account.activeCard shouldBe true
        operationResult2.account.availableLimit shouldBe 80
        operationResult2.violations.shouldBeEmpty()

        operationResult3.account.activeCard shouldBe true
        operationResult3.account.availableLimit shouldBe 60
        operationResult3.violations.shouldBeEmpty()

        operationResult4.account.activeCard shouldBe true
        operationResult4.account.availableLimit shouldBe 40
        operationResult4.violations.shouldBeEmpty()
    }

    @Test
    fun `doubled-transaction-violation`() = runBlocking {

        //Given
        val operationChannel = Channel<Operation>(Channel.UNLIMITED)
        val resultChannel = Channel<OperationResult>(Channel.UNLIMITED)

        val accountRepository = AccountRepositoryImpl(accountRepositoryClock)
        val transactionRepository = TransactionRepositoryImpl()

        val ac = AccountCreationRequest(true, 100)

        val t1 = TransactionRequest("M1",
            20,
            Instant.parse("2019-02-13T10:00:05.000Z")
        )

        val t2 = TransactionRequest("M1",
            20,
            Instant.parse("2019-02-13T10:00:10.000Z")
        )

        val t3 = TransactionRequest("M1",
            20,
            Instant.parse("2019-02-13T10:00:15.000Z")
        )

        operationChannel.send(ac)
        operationChannel.send(t1)
        operationChannel.send(t2)
        operationChannel.send(t3)
        operationChannel.close()

        //When
        launch {
            process(accountRepository, transactionRepository, operationChannel, resultChannel)
        }

        //Then
        val operationResult1 = resultChannel.receive()
        val operationResult2 = resultChannel.receive()
        val operationResult3 = resultChannel.receive()
        val operationResult4 = resultChannel.receive()

        operationResult1.account.activeCard shouldBe true
        operationResult1.account.availableLimit shouldBe 100
        operationResult1.violations.shouldBeEmpty()

        operationResult2.account.activeCard shouldBe true
        operationResult2.account.availableLimit shouldBe 80
        operationResult2.violations.shouldBeEmpty()

        operationResult3.account.activeCard shouldBe true
        operationResult3.account.availableLimit shouldBe 60
        operationResult3.violations.shouldBeEmpty()

        operationResult4.account.activeCard shouldBe true
        operationResult4.account.availableLimit shouldBe 60
        operationResult4.violations shouldContainExactly listOf("doubled-transaction")
    }

    @Test
    fun `insufficient-limit and high-frequency-small-interval`() = runBlocking {

        //Given
        val operationChannel = Channel<Operation>(Channel.UNLIMITED)
        val resultChannel = Channel<OperationResult>(Channel.UNLIMITED)

        val accountRepository = AccountRepositoryImpl(accountRepositoryClock)
        val transactionRepository = TransactionRepositoryImpl()

        val ac = AccountCreationRequest(true, 100)

        val t1 = TransactionRequest("M1",
            20,
            Instant.parse("2019-02-13T10:00:05.000Z")
        )

        val t2 = TransactionRequest("M2",
            30,
            Instant.parse("2019-02-13T10:00:10.000Z")
        )

        val t3 = TransactionRequest("M3",
            40,
            Instant.parse("2019-02-13T10:00:15.000Z")
        )

        val t4 = TransactionRequest("M4",
            20,
            Instant.parse("2019-02-13T10:00:20.000Z")
        )

        operationChannel.send(ac)
        operationChannel.send(t1)
        operationChannel.send(t2)
        operationChannel.send(t3)
        operationChannel.send(t4)
        operationChannel.close()

        //When
        launch {
            process(accountRepository, transactionRepository, operationChannel, resultChannel)
        }

        //Then
        val operationResult1 = resultChannel.receive()
        val operationResult2 = resultChannel.receive()
        val operationResult3 = resultChannel.receive()
        val operationResult4 = resultChannel.receive()
        val operationResult5 = resultChannel.receive()

        operationResult1.account.activeCard shouldBe true
        operationResult1.account.availableLimit shouldBe 100
        operationResult1.violations.shouldBeEmpty()

        operationResult2.account.activeCard shouldBe true
        operationResult2.account.availableLimit shouldBe 80
        operationResult2.violations.shouldBeEmpty()

        operationResult3.account.activeCard shouldBe true
        operationResult3.account.availableLimit shouldBe 50
        operationResult3.violations.shouldBeEmpty()

        operationResult4.account.activeCard shouldBe true
        operationResult4.account.availableLimit shouldBe 10
        operationResult4.violations.shouldBeEmpty()

        operationResult5.account.activeCard shouldBe true
        operationResult5.account.availableLimit shouldBe 10
        operationResult5.violations shouldContainExactly listOf("insufficient-limit", "high-frequency-small-interval")
    }
}