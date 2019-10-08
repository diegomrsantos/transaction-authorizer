package infrastructure

import model.entities.Account
import model.repository.AccountRepository
import java.time.Clock
import java.time.Instant
import java.util.concurrent.ConcurrentSkipListMap

class AccountRepositoryImpl(private val clock: Clock) : AccountRepository {

    private var accounts = ConcurrentSkipListMap<Instant, Account>()

    override fun createAccount(activeCard: Boolean, availableLimit: Int): Account {
        val account = Account(activeCard, availableLimit)
        accounts[clock.instant()] = account
        return account
    }

    override fun getAccount(): Account? {
        return accounts.lastEntry()?.value
    }

    override fun getAccount(instant: Instant): Account? {
        return accounts.floorEntry(instant)?.value
    }

    override fun decreaseAvailableLimit(amount: Int): Account {
        val account = accounts.lastEntry()?.value
        requireNotNull(account) { "Account has not been initialized yet" }
        val accountNewVersion = account.copy(
            activeCard = account.activeCard,
            availableLimit = account.availableLimit - amount
        )
        accounts[clock.instant()] = accountNewVersion
        return accountNewVersion
    }
}