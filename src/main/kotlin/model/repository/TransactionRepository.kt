package model.repository

import model.entities.Transaction
import java.time.Instant

/**
 * This interface represents a repository of immutable transaction values. Transactions can only be stored and retrieved but not modified.
 */
interface TransactionRepository {

    /**
     * Adds a transaction into the repository.
     */
    fun add(tx: Transaction)

    /**
     * Returns all transactions that range from fromInstant to toInstant or an empty list if there are no such transactions.
     * @param[fromInstant]
     * @return a List of transactions.
     */
    fun getTransactions(fromInstant: Instant, toInstant: Instant): List<Transaction>
}