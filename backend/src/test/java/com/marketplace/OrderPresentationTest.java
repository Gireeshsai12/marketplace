package com.marketplace;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class OrderPresentationTest {
    private Map<String, Object> portion(String state) {
        return Map.of("fulfillment", state);
    }

    @Test void storedSnapshotBecomesPlainAddressFields() {
        var address = OrderPresentation.shippingAddress("{\"id\":1,\"user_id\":1,\"name\":\"Demo Customer\",\"line1\":\"100 University Ave\",\"city\":\"Lowell\",\"state\":\"MA\",\"zip\":\"01854\"}");
        assertEquals(Map.of("name", "Demo Customer", "line1", "100 University Ave", "city", "Lowell", "state", "MA", "zip", "01854"), address);
        assertFalse(address.containsKey("user_id"));
    }

    @Test void addressPreservesUnicodeAndLeadingZipZero() {
        var address = OrderPresentation.shippingAddress("{\"name\":\"Renée 李\",\"line1\":\"Apt 2, 10 Main St\",\"city\":\"Lowell\",\"state\":\"MA\",\"zip\":\"01854\"}");
        assertEquals("Renée 李", address.get("name"));
        assertEquals("01854", address.get("zip"));
    }

    @Test void corruptOrIncompleteSnapshotsDoNotSilentlyRenderBlank() {
        for (String value : List.of("{", "null", "[]", "{}", "{\"name\":\"Customer\"}")) {
            assertThrows(IllegalStateException.class, () -> OrderPresentation.shippingAddress(value));
        }
    }

    @Test void paidAndPendingProcessingOrdersCanBeCancelled() {
        assertTrue(OrderPresentation.canCancel("PAID", List.of(portion("PROCESSING"))));
        assertTrue(OrderPresentation.canCancel("PENDING", List.of(portion("PROCESSING"))));
    }

    @Test void anyFulfilledSellerPortionPreventsWholeOrderCancellation() {
        for (String status : List.of("SHIPPED", "DELIVERED", "RETURNED", "CANCELLED")) {
            assertFalse(OrderPresentation.canCancel("PAID", List.of(portion("PROCESSING"), portion(status))));
        }
    }

    @Test void terminalPaymentsAndEmptyOrdersCannotBeCancelled() {
        for (String status : List.of("FAILED", "EXPIRED", "CANCELLED", "REFUND_REQUIRED", "REFUNDED")) {
            assertFalse(OrderPresentation.canCancel(status, List.of(portion("PROCESSING"))));
        }
        assertFalse(OrderPresentation.canCancel("PAID", List.of()));
    }
}
