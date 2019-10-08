package app

import com.beust.klaxon.Klaxon
import infrastructure.AccountRepositoryImpl
import model.entities.Operation
import model.entities.OperationResult
import model.service.process
import infrastructure.TransactionRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.Clock

fun main() = runBlocking<Unit> {

    val clock = Clock.systemUTC()

    val operationChannel = Channel<Operation>(Channel.UNLIMITED)
    val resultChannel = Channel<OperationResult>(Channel.UNLIMITED)

    val accountRepository = AccountRepositoryImpl(clock)
    val transactionRepository = TransactionRepositoryImpl()

    launch (Dispatchers.Default) {
        handleInput(operationChannel)
    }

    launch(Dispatchers.Default) {
        process(accountRepository, transactionRepository, operationChannel, resultChannel)
    }

    launch(Dispatchers.Default) {
        handleOutput(resultChannel)
    }
}

private suspend fun handleInput(operationChannel: Channel<Operation>) {
    var line: String? = readLine()
    while (line != null) {
        val jsonData = StringBuilder(line)
        println(jsonData)
        val operation = parseOperation(jsonData)
        operationChannel.send(operation)
        line = readLine()
    }
    operationChannel.close()
}

private suspend fun handleOutput(resultChannel: Channel<OperationResult>) {
    for (result in resultChannel) {
        println(Klaxon().toJsonString(result))
    }
}
