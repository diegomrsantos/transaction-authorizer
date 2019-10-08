package infrastructure

import model.entities.Transaction
import model.repository.TransactionRepository
import java.time.Instant
import java.util.concurrent.ConcurrentSkipListMap

class TransactionRepositoryImpl : TransactionRepository {

    private var txDataSource = ConcurrentSkipListMap<Instant, Transaction>()

    override fun add(tx: Transaction) {
        txDataSource[tx.instant] = tx
    }

    override fun getTransactions(fromInstant: Instant, toInstant: Instant): List<Transaction> {
        return txDataSource.subMap(fromInstant, true, toInstant, true).values.toList()
    }
}