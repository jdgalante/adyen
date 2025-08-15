import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import kotlin.test.assertFailsWith

class CashRegisterTest {
    @Test
    fun testTransaction() {
        val drawer = Change()
            .add(Bill.TWENTY_EURO, 1)
            .add(Bill.TEN_EURO, 1)
            .add(Coin.TWENTY_CENT, 2)
            .add(Coin.TEN_CENT, 2)
        val register = CashRegister(drawer)
        val price = 169_70L
        val amountPaid = Change()
            .add(Bill.TWO_HUNDRED_EURO, 1)

        // Expect 30.30 change: 20€ + 10€ + 20c + 10c (bills + coins)
        val expectedChange = Change()
            .add(Bill.TWENTY_EURO, 1)
            .add(Bill.TEN_EURO, 1)
            .add(Coin.TWENTY_CENT, 1)
            .add(Coin.TEN_CENT, 1)
        val actualChange = register.performTransaction(price, amountPaid)

        assertEquals(expectedChange, actualChange)
    }

    @Test
    fun testInsufficientPayment() {
        val register = CashRegister(Change())
        val price = 200L
        val amountPaid = Change().add(Coin.FIFTY_CENT, 1)

        assertFailsWith<CashRegister.TransactionException> {
            register.performTransaction(price, amountPaid)
        }
    }

    @Test
    fun testPricePositive() {
        val register = CashRegister(Change())
        val price = 0L
        val amountPaid = Change().add(Coin.ONE_EURO, 1)

        assertFailsWith<CashRegister.TransactionException> {
            register.performTransaction(price, amountPaid)
        }
    }

    @Test
    fun testExactPaymentDrawerUpdates() {
        val drawer = Change()
            .add(Coin.FIFTY_CENT, 1)
            .add(Coin.TWENTY_CENT, 1)
        val register = CashRegister(drawer)
        val price = 200L
        val amountPaid = Change().add(Coin.TWO_EURO, 1)

        val actualChange = register.performTransaction(price, amountPaid)

        assertEquals(Change.none(), actualChange)

        assertEquals(
            Change()
                .add(Coin.FIFTY_CENT, 1)
                .add(Coin.TWENTY_CENT, 1)
                .add(Coin.TWO_EURO, 1),
            drawer
        )
    }

    @Test
    fun testChangeNotAvailableAndRollback() {
        val drawer = Change().add(Coin.FIFTY_CENT, 1).add(Coin.TWENTY_CENT, 1)
        val register = CashRegister(drawer)

        // Successful transaction
        val change = register.performTransaction(
            250L,
            Change().add(Coin.TWO_EURO, 1).add(Coin.ONE_EURO, 1)
        )
        assertEquals(Change().add(Coin.FIFTY_CENT, 1), change)

        assertEquals(
            Change().add(Coin.TWO_EURO, 1).add(Coin.ONE_EURO, 1).add(Coin.TWENTY_CENT, 1),
            drawer
        )

        // Insufficient change (merged from old testChangeNotAvailable)
        assertFailsWith<CashRegister.TransactionException> {
            register.performTransaction(170L, Change().add(Coin.TWO_EURO, 1))
        }
        assertEquals(
            Change().add(Coin.TWO_EURO, 1).add(Coin.ONE_EURO, 1).add(Coin.TWENTY_CENT, 1),
            drawer
        )
    }

    @Test
    fun testMinimalChange() {
        val drawer = Change()
            .add(Bill.FIFTY_EURO, 1)
            .add(Bill.TWENTY_EURO, 2)
            .add(Coin.FIFTY_CENT, 3)
            .add(Coin.TEN_CENT, 5)
        val register = CashRegister(drawer)

        val change = register.performTransaction(
            129_50L,
            Change().add(Bill.TWO_HUNDRED_EURO, 1)
        )
        assertEquals(
            Change().add(Bill.FIFTY_EURO, 1).add(Bill.TWENTY_EURO, 1).add(Coin.FIFTY_CENT, 1),
            change
        )
    }

    @Test
    fun testMultipleTransactions() {
        // Tests multiple transactions to test register keeping track of balance
        val drawer = Change()
            .add(Coin.ONE_EURO, 2)
            .add(Coin.FIFTY_CENT, 2)
            .add(Coin.TWENTY_CENT, 1)
            .add(Coin.TEN_CENT, 1)
        val register = CashRegister(drawer)

        val change1 = register.performTransaction(
            250L,
            Change().add(Coin.TWO_EURO, 1).add(Coin.FIFTY_CENT, 2)
        )
        assertEquals(Change().add(Coin.FIFTY_CENT, 1), change1)

        val change2 = register.performTransaction(
            290L,
            Change().add(Coin.TWO_EURO, 2)
        )
        assertEquals(Change().add(Coin.ONE_EURO, 1).add(Coin.TEN_CENT, 1), change2)

        assertFailsWith<CashRegister.TransactionException> {
            register.performTransaction(
                375L,
                Change().add(Coin.TWO_EURO, 2)
            )
        }
    }
}
