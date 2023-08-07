package com.app.freya.utils;

import java.math.BigDecimal;

public class CalculateUtils {

    /**
     * 除法运算
     *
     * @param v1
     * @param v2
     * @param scale
     * @return
     */
    public static double div(double v1, double v2, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException(
                    "The scale must be a positive integer or zero");
        }
        if (v2 <= 0) {
            return 0;
        } else {
            BigDecimal b1 = new BigDecimal(Double.toString(v1).replace(",", ""));
            BigDecimal b2 = new BigDecimal(Double.toString(v2).replace(",", ""));
            return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
        }
    }

    /**
     * 两个double相乘
     *
     * @param v1
     * @param v2
     * @return
     */
    public static Double mul(Double v1, Double v2) {
        BigDecimal b1 = new BigDecimal(v1.toString());
        BigDecimal b2 = new BigDecimal(v2.toString());
        return b1.multiply(b2).doubleValue();
    }
}
