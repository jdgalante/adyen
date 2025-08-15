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

        val registerChange = change
        //adding each bill or coin count to register
        for (element in amountPaid.getElements()) {
            registerChange.add(element, amountPaid.getCount(element))
        }

        val changeAmount = paid - price
        if (changeAmount == 0L) return Change.none()

        val resultChange = Change()
        var remainingAmount = changeAmount
        // loops through the bills/coins starting with largest denomination
        for (element in registerChange.getElements().sortedByDescending { it.minorValue }) {
            val availableChange = registerChange.getCount(element)
            if (availableChange <= 0) {
                continue
            }
            // computes how many bills/coins of this denomination can be used
            val changeNeeded = (remainingAmount / element.minorValue).toInt()
            // if availableChange > changeNeeded, use changeNeeded count, else use availableChange count
            val changeToUse = minOf(changeNeeded, availableChange)
            if (changeToUse > 0) {
                resultChange.add(element, changeToUse)
                registerChange.remove(element, changeToUse)
                remainingAmount -= element.minorValue * changeToUse
            }
            if (remainingAmount == 0L) {
                break
            }
        }

        // if remainingChange is > 0 exact change is not possible given the available bills/coins in the register.
        // Remove the paid amount from the register and add the change back to the register.
        if (remainingAmount > 0L) {
            for (element in amountPaid.getElements()) {
                registerChange.remove(element, amountPaid.getCount(element))
            }
            for (element in resultChange.getElements()) {
                registerChange.add(element, resultChange.getCount(element))
            }
            throw TransactionException("exact change not possible")
        }

        return resultChange
    }

    class TransactionException(message: String, cause: Throwable? = null) :
        Exception(message, cause)
}
