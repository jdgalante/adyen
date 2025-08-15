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
    fun testChangeNotAvailable() {
        val register = CashRegister(Change())
        val price = 375L
        val amountPaid = Change().add(
            Coin.TWO_EURO,
            2
        )

        assertFailsWith<CashRegister.TransactionException> {
            register.performTransaction(price, amountPaid)
        }
    }
}
