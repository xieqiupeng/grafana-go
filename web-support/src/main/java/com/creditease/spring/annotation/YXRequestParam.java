package com.creditease.spring.annotation;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface YXRequestParam {

    /**
     * @Description: 用于绑定的请求参数名字
     *
     * @return String
     */
    String value() default "";

    /**
     * @Description: 参数是否必须有值。默认是，参数不可为null。若为false则参数值可为null。
     *
     * @return boolean
     * @create time 2016年1月7日 下午3:02:43
     */
    boolean required() default true;

    /**
     * @Description: 参数验证不通过时，错误提示信息
     *
     * @return String
     */
    String errmsg() default "";

    /**
     * @Description: 如果为字符串类型，需要匹配的正则表达式。默认为空。
     *
     * @return String
     */
    String regexp() default "";

    /**
     * @Description: 如果为Byte，则限制该参数的最小值。默认为java Byte最小值。
     *
     * @return byte
     */
    byte byteMin() default Byte.MIN_VALUE;

    /**
     * @Description: 如果为Byte，则限制该参数的最大值。默认为java Byte最大值。
     *
     * @return byte
     */
    byte byteMax() default Byte.MAX_VALUE;

    /**
     * @Description: 如果为Short，则限制该参数的最小值。默认为java Short最小值。
     *
     * @return short
     */
    short shortMin() default Short.MIN_VALUE;

    /**
     * @Description: 如果为Short，则限制该参数的最大值。默认为java Short最大值。
     *
     * @return short
     */
    short shortMax() default Short.MAX_VALUE;

    /**
     * @Description: 如果为整型（Integer），则限制该参数的最小值。默认为java整型最小值。
     *
     * @return int
     */
    int intMin() default Integer.MIN_VALUE;

    /**
     * @Description: 如果为整型（Integer），则限制该参数的最大值。默认为java整型最大值。
     *
     * @return int
     */
    int intMax() default Integer.MAX_VALUE;

    /**
     * @Description: 如果为Long，则限制该参数的最小值。默认为java Long最小值。
     *
     * @return long
     */
    long longMin() default Long.MIN_VALUE;

    /**
     * @Description: 如果为Long，则限制该参数的最大值。默认为java Long最大值。
     *
     * @return long
     */
    long longMax() default Long.MAX_VALUE;

    /**
     * @Description: 如果为Float，则限制该参数的最小值。默认为java Float最小值。
     *
     * @return float
     */
    float floatMin() default Float.MIN_VALUE;

    /**
     * @Description: 如果为Float，则限制该参数的最大值。默认为java Float最大值。
     *
     * @return float
     */
    float floatMax() default Float.MAX_VALUE;

    /**
     * @Description: 如果为Float，则限制该参数的最小值。默认为java Double最小值。
     *
     * @return double
     */
    double doubleMin() default Double.MIN_VALUE;

    /**
     * @Description: 如果为Double，则限制该参数的最大值。默认为java Double最大值。
     *
     * @return double
     */
    double doubleMax() default Double.MAX_VALUE;
}
