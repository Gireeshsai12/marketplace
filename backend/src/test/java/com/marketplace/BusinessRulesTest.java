package com.marketplace;
import org.junit.jupiter.api.*;import static org.junit.jupiter.api.Assertions.*;import java.math.*;import java.util.*;
class BusinessRulesTest {
@Test void taxUsesDecimalAndHalfUp(){var t=Pricing.calculate(new BigDecimal("129.99"),BigDecimal.ZERO,"STANDARD");assertEquals(new BigDecimal("8.12"),t.tax());assertEquals(new BigDecimal("138.11"),t.total());}
@Test void percentageCouponCannotExceedSubtotal(){var t=Pricing.calculate(new BigDecimal("10.00"),new BigDecimal("50.00"),"STANDARD");assertEquals(new BigDecimal("10.00"),t.discount());assertEquals(new BigDecimal("0.00"),t.tax());}
@Test void expressShippingIsExplicit(){assertEquals(new BigDecimal("12.99"),Pricing.calculate(new BigDecimal("200"),BigDecimal.ZERO,"EXPRESS").shipping());}
@Test void standardShippingThreshold(){assertEquals(new BigDecimal("5.99"),Pricing.calculate(new BigDecimal("49.99"),BigDecimal.ZERO,"STANDARD").shipping());assertEquals(new BigDecimal("0.00"),Pricing.calculate(new BigDecimal("50"),BigDecimal.ZERO,"STANDARD").shipping());}
@Test void invalidShippingRejected(){assertThrows(IllegalArgumentException.class,()->Pricing.calculate(BigDecimal.TEN,BigDecimal.ZERO,"HACK"));}
@Test void allocationsSumExactlyWithRemainder(){var result=Pricing.allocate(new BigDecimal("1.00"),List.of(BigDecimal.ONE,BigDecimal.ONE,BigDecimal.ONE));assertEquals(List.of(new BigDecimal("0.33"),new BigDecimal("0.33"),new BigDecimal("0.34")),result);assertEquals(new BigDecimal("1.00"),result.stream().reduce(BigDecimal.ZERO,BigDecimal::add));}
@Test void csvNeutralizesFormulaInjection(){assertEquals("\"'=HYPERLINK(\"\"bad\"\")\"",Seller.csvCell("=HYPERLINK(\"bad\")"));assertTrue(Seller.csvCell(" @SUM(1)").startsWith("\"'"));assertEquals("\"Normal title\"",Seller.csvCell("Normal title"));}
}
