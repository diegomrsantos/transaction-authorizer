package model.entities

import java.time.Instant

data class Transaction(val merchant:String, val amount: Int, val instant: Instant, val violations: List<Violation> = listOf())