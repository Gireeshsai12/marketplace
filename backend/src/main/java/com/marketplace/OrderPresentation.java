package com.marketplace;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Converts stored order data into the public order-details contract. */
final class OrderPresentation {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final List<String> ADDRESS_FIELDS = List.of("name", "line1", "city", "state", "zip");

    private OrderPresentation() {}

    static Map<String, String> shippingAddress(String snapshot) {
        try {
            JsonNode address = JSON.readTree(snapshot);
            if (address == null || !address.isObject()) {
                throw new IllegalStateException("Invalid stored shipping address");
            }
            Map<String, String> result = new LinkedHashMap<>();
            for (String field : ADDRESS_FIELDS) {
                JsonNode value = address.get(field);
                if (value == null || !value.isTextual() || value.textValue().isBlank()) {
                    throw new IllegalStateException("Incomplete stored shipping address");
                }
                result.put(field, value.textValue());
            }
            return result;
        } catch (JsonProcessingException | IllegalArgumentException exception) {
            // Do not include the address or parser input in logged error messages.
            throw new IllegalStateException("Invalid stored shipping address");
        }
    }

    static boolean canCancel(String paymentState, List<Map<String, Object>> portions) {
        return Set.of("PAID", "PENDING").contains(paymentState)
                && !portions.isEmpty()
                && portions.stream().allMatch(portion -> "PROCESSING".equals(portion.get("fulfillment")));
    }
}
