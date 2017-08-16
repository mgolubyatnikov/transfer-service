package com.github.mgolubyatnikov.transferservice.util;

import java.math.BigDecimal;

public class BigDecimalUtil {

    public static BigDecimal valueOf(String value) {
        return new BigDecimal(value).setScale(2, BigDecimal.ROUND_HALF_UP);
    }
}
