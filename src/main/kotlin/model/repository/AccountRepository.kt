package model.repository

import model.entities.Account
import java.time.Instant

/**
 * This interface represents an immutable account repository. Operations on an account create a new version associated
 * with the current instant instead of mutating the previous account.
 */
interface AccountRepository {

    /**.
     * Creates the initial version account with the supplied parameters.
     *
     * @param[activeCard] if account card is active.
     * @param[availableLimit] account's available limit.
     *
     * @return the account that has been created.
     */
    fun createAccount(activeCard: Boolean, availableLimit: Int): Account

    /**
     *  Returns the account last version or null if the account has not been created yet.
     */
    fun getAccount(): Account?

    /**
     * Returns the account with the newest version
     * less than or equal to the given instant, or null if there
     * is no such account.
     *
     * @param[instant] the maximum point in time for an account version to be returned.
     * @return the account requested or null if it is not found.
     */
    fun getAccount(instant: Instant): Account?

    /**
     * Creates a new account version with the previous available limit minus the amount supplied.
     *
     * @return the new account version created.
     */
    fun decreaseAvailableLimit(amount: Int): Account
}