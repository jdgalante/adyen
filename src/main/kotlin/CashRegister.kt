/**
 * The CashRegister class holds the logic for performing transactions.
 *
 * @param change The change that the CashRegister is holding.
 */
class CashRegister(private val change: Change) {
    /**
     * Performs a transaction for a product/products with a certain price and a given amount.
     *
     * @param price The price of the product(s).
     * @param amountPaid The amount paid by the shopper.
     *
     * @return The change for the transaction.
     *
     * @throws TransactionException If the transaction cannot be performed.
     */
    fun performTransaction(price: Long, amountPaid: Change): Change {
        if (price <= 0) throw TransactionException("price must be > 0")
        val paid: Long = amountPaid.total
        if (paid < price) throw TransactionException("paid must be >= price")
        return Change.none()
    }

    class TransactionException(message: String, cause: Throwable? = null) :
        Exception(message, cause)
}
