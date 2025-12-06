import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProductStock – JUnit 5 Test Suite")
class ProductStockTest {

    private static final String VALID_ID = "P123";
    private static final String VALID_LOC = "WH-A1";
    private ProductStock stock;

    @BeforeAll
    static void beforeAll() {
        System.out.println("Starting ProductStock test suite...");
    }

    @BeforeEach
    void setUp() {
        stock = new ProductStock(VALID_ID, VALID_LOC, 10, 5, 100);
    }

    @AfterEach
    void tearDown() {
        System.out.println("Test completed.");
    }

    @AfterAll
    static void afterAll() {
        System.out.println("All tests completed.");
    }

    // Constructor Tests

    @Test
    @Tag("sanity")
    @DisplayName("Valid constructor creates object")
    void testValidConstructor() {
        ProductStock ps = new ProductStock("A01", "LOC-1", 5, 2, 50);
        assertAll(
                () -> assertEquals("A01", ps.getProductId()),
                () -> assertEquals("LOC-1", ps.getLocation()),
                () -> assertEquals(5, ps.getOnHand()),
                () -> assertEquals(0, ps.getReserved()),
                () -> assertEquals(2, ps.getReorderThreshold()),
                () -> assertEquals(50, ps.getMaxCapacity())
        );
    }

    @Test
    @DisplayName("Invalid constructor inputs throw")
    void testInvalidConstructor() {
        assertThrows(IllegalArgumentException.class,
                () -> new ProductStock("", "L1", 0, 0, 10));

        assertThrows(IllegalArgumentException.class,
                () -> new ProductStock("X", "", 0, 0, 10));

        assertThrows(IllegalArgumentException.class,
                () -> new ProductStock("X", "L", -1, 0, 10));

        assertThrows(IllegalArgumentException.class,
                () -> new ProductStock("X", "L", 1, -1, 10));

        assertThrows(IllegalArgumentException.class,
                () -> new ProductStock("X", "L", 1, 0, 0));

        assertThrows(IllegalArgumentException.class,
                () -> new ProductStock("X", "L", 20, 0, 10));
    }


    // Location Change

    @Test
    @DisplayName("changeLocation update when valid")
    void testChangeLocation() {
        stock.changeLocation("WH-Z9");
        assertEquals("WH-Z9", stock.getLocation());
    }

    @Test
    @DisplayName("changeLocation reject null or blank")
    void testChangeLocationInvalid() {
        assertThrows(IllegalArgumentException.class, () -> stock.changeLocation(null));
        assertThrows(IllegalArgumentException.class, () -> stock.changeLocation(" "));
    }

    // addStock Tests

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 10})
    @Tag("regression")
    @DisplayName("addStock: valid amounts")
    void testAddStockValid(int amount) {
        int before = stock.getOnHand();
        stock.addStock(amount);
        assertEquals(before + amount, stock.getOnHand());
    }

    @Test
    @DisplayName("addStock: reject <= 0")
    void testAddStockInvalidAmount() {
        assertThrows(IllegalArgumentException.class, () -> stock.addStock(0));
        assertThrows(IllegalArgumentException.class, () -> stock.addStock(-5));
    }

    @Test
    @DisplayName("addStock: cannot exceed capacity")
    void testAddStockExceedsCapacity() {
        ProductStock s = new ProductStock("X", "L", 90, 5, 100);
        assertThrows(IllegalStateException.class, () -> s.addStock(20));
    }

    // removeDamaged Tests

    @Test
    @DisplayName("removeDamaged: valid removal")
    void testRemoveDamagedValid() {
        stock.removeDamaged(5);
        assertEquals(5, stock.getOnHand());
    }

    @Test
    @DisplayName("removeDamaged: too large removal")
    void testRemoveDamagedTooMuch() {
        assertThrows(IllegalStateException.class, () -> stock.removeDamaged(100));
    }

    @Test
    @DisplayName("removeDamaged: reject <= 0")
    void testRemoveDamagedInvalidAmount() {
        assertThrows(IllegalArgumentException.class, () -> stock.removeDamaged(0));
    }


    // reserve / releaseReservation / shipReserved

    @Nested
    @DisplayName("Reservation operations")
    class ReservationTests {

        @Test
        @DisplayName("reserve: valid")
        void testReserve() {
            stock.reserve(3);
            assertEquals(3, stock.getReserved());
            assertEquals(7, stock.getAvailable());
        }

        @Test
        @DisplayName("reserve: invalid amount")
        void testReserveErrors() {
            assertThrows(IllegalArgumentException.class, () -> stock.reserve(0));
            assertThrows(IllegalStateException.class, () -> stock.reserve(20));
        }

        @Test
        @DisplayName("releaseReservation: valid")
        void testReleaseReservation() {
            stock.reserve(5);
            stock.releaseReservation(3);
            assertEquals(2, stock.getReserved());
        }

        @Test
        @DisplayName("releaseReservation: too large")
        void testReleaseTooMuch() {
            stock.reserve(5);
            assertThrows(IllegalStateException.class, () -> stock.releaseReservation(10));
        }

        @Test
        @DisplayName("shipReserved: valid")
        void testShipReserved() {
            stock.reserve(5);
            stock.shipReserved(5);
            assertEquals(5, stock.getOnHand());
            assertEquals(0, stock.getReserved());
        }

        @Test
        @DisplayName("shipReserved: invalid")
        void testShipReservedErrors() {
            stock.reserve(4);
            assertThrows(IllegalArgumentException.class, () -> stock.shipReserved(0));
            assertThrows(IllegalStateException.class, () -> stock.shipReserved(10));
        }
    }

    // Reorder Logic

    @Test
    @DisplayName("reorder: needed")
    void testReorderNeeded() {
        ProductStock ps = new ProductStock("X", "L", 3, 5, 50);
        assertTrue(ps.isReorderNeeded());
    }

    @Test
    @DisplayName("reorder: not needed")
    void testReorderNotNeeded() {
        ProductStock ps = new ProductStock("X", "L", 10, 5, 50);
        assertFalse(ps.isReorderNeeded());
    }

    // updateReorderThreshold / updateMaxCapacity

    @Test
    @DisplayName("updateThreshold: valid")
    void testUpdateReorderThreshold() {
        stock.updateReorderThreshold(20);
        assertEquals(20, stock.getReorderThreshold());
    }

    @Test
    @DisplayName("updateThreshold: invalid")
    void testUpdateReorderThresholdInvalid() {
        assertThrows(IllegalArgumentException.class, () -> stock.updateReorderThreshold(-1));
        assertThrows(IllegalArgumentException.class, () -> stock.updateReorderThreshold(1000));
    }

    @Test
    @DisplayName("updateCapacity: valid")
    void testUpdateMaxCapacity() {
        stock.updateMaxCapacity(200);
        assertEquals(200, stock.getMaxCapacity());
    }

    @Test
    @DisplayName("updateCapacity: invalid")
    void testUpdateMaxCapacityInvalid() {
        assertThrows(IllegalStateException.class, () -> stock.updateMaxCapacity(5));
    }

    // Disabled future-feature test

    @Disabled("auto-balancing stock between locations") // Future feature
    @Test
    void testFutureFeature() {
        fail("Not implemented yet.");
    }

    // Timeout example

    @Test
    @Timeout(1)
    @DisplayName("Timeout example test")
    void testTimeout() {
        assertTrue(true);
    }
}
