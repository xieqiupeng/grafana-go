/**
 * @Title: ResultMessage.java
 * @Package com.jrd.response
 *
 * @Description: TODO
 *
 * @Copyright: JunRongDai @ Copyright (c) 2015
 * @author JRDLQ
 * @version V1.0
 * @date   2016年1月7日 下午3:40:47
 */
package com.creditease.response;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 定义返回结果提示信息
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ResultMessage {
	public String value() default "";
}
