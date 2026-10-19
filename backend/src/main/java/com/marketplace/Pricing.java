package com.marketplace;
import java.math.*;import java.util.*;
public final class Pricing {
 public static BigDecimal money(BigDecimal v){return v.setScale(2,RoundingMode.HALF_UP);}
 public record Totals(BigDecimal subtotal,BigDecimal discount,BigDecimal shipping,BigDecimal tax,BigDecimal total){}
 public static Totals calculate(BigDecimal sub,BigDecimal discount,String method){if(!List.of("STANDARD","EXPRESS").contains(method))throw new IllegalArgumentException();BigDecimal dis=money(discount.min(sub).max(BigDecimal.ZERO));BigDecimal ship=method.equals("EXPRESS")?new BigDecimal("12.99"):sub.compareTo(new BigDecimal("50"))>=0?new BigDecimal("0.00"):new BigDecimal("5.99");BigDecimal tax=money(sub.subtract(dis).multiply(new BigDecimal("0.0625")));return new Totals(money(sub),dis,ship,tax,money(sub.subtract(dis).add(ship).add(tax)));}
 /** Largest remainder assigned to last row: allocations sum exactly, including pennies. */
 public static List<BigDecimal> allocate(BigDecimal total,List<BigDecimal> weights){BigDecimal sum=weights.stream().reduce(BigDecimal.ZERO,BigDecimal::add),used=BigDecimal.ZERO;List<BigDecimal> out=new ArrayList<>();for(int i=0;i<weights.size();i++){BigDecimal x=i==weights.size()-1?total.subtract(used):sum.signum()==0?BigDecimal.ZERO:total.multiply(weights.get(i)).divide(sum,2,RoundingMode.DOWN);out.add(x);used=used.add(x);}return out;}
}
