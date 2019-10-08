package app

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import model.entities.AccountCreationRequest
import model.entities.Operation
import model.entities.TransactionRequest
import java.lang.StringBuilder
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

fun parseOperation(jsonData: StringBuilder): Operation {
    val json: JsonObject = Parser.default().parse(jsonData) as JsonObject
    return if (json["account"] != null) {
        val account = json["account"] as JsonObject
        AccountCreationRequest(account["activeCard"] as Boolean, account["availableLimit"] as Int)
    } else {
        val transaction = json["transaction"] as JsonObject
        TransactionRequest(
            transaction["merchant"] as String,
            transaction["amount"] as Int,
            LocalDateTime.parse(transaction["time"] as String, DateTimeFormatter.ISO_DATE_TIME).toInstant(ZoneOffset.UTC)

        )
    }
}