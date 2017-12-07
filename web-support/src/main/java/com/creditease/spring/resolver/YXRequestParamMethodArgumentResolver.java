package com.creditease.spring.resolver;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.creditease.spring.annotation.YXRequestParam;
import com.creditease.spring.exception.YXRequestParamException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.annotation.AbstractNamedValueMethodArgumentResolver;

import java.lang.reflect.ParameterizedType;
import java.net.URLDecoder;
import java.util.List;
import java.util.UUID;

public class YXRequestParamMethodArgumentResolver extends AbstractNamedValueMethodArgumentResolver {

    private static Logger log = LoggerFactory.getLogger(YXRequestParamMethodArgumentResolver.class);

    /*
     * <p>Title: createNamedValueInfo</p> <p>Description: </p>
	 *
	 * @param parameter
	 *
	 * @return
	 *
	 * @see org.springframework.web.method.annotation.
	 * AbstractNamedValueMethodArgumentResolver
	 * #createNamedValueInfo(org.springframework.core.MethodParameter)
	 */
    @Override
    protected NamedValueInfo createNamedValueInfo(MethodParameter methodParameter) {
        YXRequestParam annotation = methodParameter.getParameterAnnotation(YXRequestParam.class);
        return (annotation != null ? new YXRequestParamNamedValueInfo(annotation) : new YXRequestParamNamedValueInfo());
    }

    @Override
    protected Object resolveName(String name, MethodParameter parameter, NativeWebRequest request) throws Exception {
        String[] paramValues = request.getParameterValues(name);
        Class<?> paramType = parameter.getParameterType();
        // 参数的注解
        YXRequestParam yxpAnno = parameter.getParameterAnnotation(YXRequestParam.class);
        if (null == yxpAnno) {
            String errorMsg = "No annotation YXRequestParam!" + " paramName:" + name + ", paramType:" + paramType;
            throw new YXRequestParamException(errorMsg);
        }
        String errMsgAnno = yxpAnno.errmsg();
        if (null == paramValues) {
            if (yxpAnno.required()) {// 注释中声明该参数不能为空
                String errorMsg = errMsgAnno + " Param value is null! paramValues is null!" + " paramName:" + name + ", paramType:"
                        + paramType;
                throw new YXRequestParamException(errorMsg, errMsgAnno);
            }
            return null;
        }
        try {
            if (0 == paramValues.length) {
                if (yxpAnno.required()) {// 注释中声明该参数不能为空
                    String errorMsg = errMsgAnno + " Param value is null! Length of paramValues is 0!" + " paramName:" + name
                            + ", paramType:" + paramType;
                    throw new YXRequestParamException(errorMsg, errMsgAnno);
                }
            } else if (1 == paramValues.length) {
                String text = paramValues[0];
                Object o;
                if (StringUtils.isNotBlank(text)) {
                    if (String.class.isAssignableFrom(paramType)) {
                        String param = String.valueOf(text).trim();
                        String regExp = yxpAnno.regexp();
                        if (StringUtils.isNotBlank(regExp)) {
                            boolean flag = param.matches(regExp);
                            if (!flag) {
                                String errorMsg = errMsgAnno + " Param is invalid! not match regexp" + " paramName:" + name + ", paramType:" + paramType;
                                throw new YXRequestParamException(errorMsg, errMsgAnno);
                            }
                        }
                        o = param;
                    } else if (Byte.class.isAssignableFrom(paramType) || paramType == Byte.TYPE) {
                        byte param = Byte.valueOf(text);
                        assertMinMax(param, yxpAnno.byteMin(), yxpAnno.byteMax(), name, paramType.toString(), errMsgAnno);
                        o = param;
                    } else if (Short.class.isAssignableFrom(paramType) || paramType == Short.TYPE) {
                        short param = Short.valueOf(text);
                        assertMinMax(param, yxpAnno.shortMin(), yxpAnno.shortMax(), name, paramType.toString(), errMsgAnno);
                        o = param;
                    } else if (Integer.class.isAssignableFrom(paramType) || paramType == Integer.TYPE) {
                        int param = Integer.valueOf(text);
                        assertMinMax(param, yxpAnno.intMin(), yxpAnno.intMax(), name, paramType.toString(), errMsgAnno);
                        o = param;
                    } else if (Long.class.isAssignableFrom(paramType) || paramType == Long.TYPE) {
                        long param = Long.valueOf(text);
                        assertMinMax(param, yxpAnno.longMin(), yxpAnno.longMax(), name, paramType.toString(), errMsgAnno);
                        o = param;
                    } else if (Boolean.class.isAssignableFrom(paramType) || paramType == Boolean.TYPE) {
                        o = Boolean.valueOf(text);
                    } else if (Character.class.isAssignableFrom(paramType) || paramType == Character.TYPE) {
                        o = text;
                    } else if (Float.class.isAssignableFrom(paramType) || paramType == Float.TYPE) {
                        float param = Float.valueOf(text);
                        assertMinMax(param, yxpAnno.floatMin(), yxpAnno.floatMax(), name, paramType.toString(), errMsgAnno);
                        o = param;
                    } else if (Double.class.isAssignableFrom(paramType) || paramType == Double.TYPE) {
                        double param = Double.valueOf(text);
                        assertMinMax(param, yxpAnno.doubleMin(), yxpAnno.doubleMax(), name, paramType.toString(), errMsgAnno);
                        o = param;
                    } else if (List.class.isAssignableFrom(paramType)) {
                        ParameterizedType typeImpl = (ParameterizedType) parameter.getGenericParameterType();
                        Class cls = (Class) typeImpl.getActualTypeArguments()[0];
                        String json = URLDecoder.decode(text, "utf-8");
                        o = JSONArray.parseArray(json, cls);
                    } else if (paramType.isArray()) {
                        String json = URLDecoder.decode(text, "utf-8");
                        o = JSONArray.parseArray(json, paramType.getComponentType());
                    } else {
                        String json = URLDecoder.decode(text, "utf-8");
                        o = JSON.parseObject(json, paramType);
                    }
                } else {
                    o = null;
                }
                if (o == null && yxpAnno.required()) {// 注释中声明该参数不能为空
                    String errorMsg = errMsgAnno + " Param value is null!" + " paramName:" + name + ", paramType:" + paramType;
                    throw new YXRequestParamException(errorMsg, errMsgAnno);
                }
                return o;
            } else {// 值过多
                log.error("resolveName Failure! Length of paramValues was more than 1!");
                String errorMsg = "Length of paramValues was more than 1!" + " paramName:" + name + ", paramType:" + paramType;
                throw new YXRequestParamException(errorMsg, errMsgAnno);
            }
        } catch (YXRequestParamException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failure! Http request parameter resove failed! paramName:" + name + ",paramValues:" + paramValues + ",paramType:"
                    + paramType, e);
            String errorMsg = e.getMessage();
            if (null != errorMsg && !"".equals(errorMsg.trim())) {
                errorMsg = "Exception:" + errorMsg + " paramName:" + name + ", paramValues:" + paramValues[0] + ", paramType:" + paramType;
            } else {
                errorMsg = "paramName:" + name + ", paramValues:" + paramValues[0] + ", paramType:" + paramType;
            }
            throw new YXRequestParamException(errorMsg, e, errMsgAnno);
        }

        return null;
    }

    private void assertMinMax(long param, long min, long max, String paramName, String paramType, String errMsgAnno) {
        if (param < min || param > max) {
            String errorMsg = errMsgAnno + " Param is invalid! minValue:" + min + " maxValue:" + max + " paramName:" + paramName + ", paramType:" + paramType;
            throw new YXRequestParamException(errorMsg, errMsgAnno);
        }
    }

    private void assertMinMax(double param, double min, double max, String paramName, String paramType, String errMsgAnno) {
        if (param < min || param > max) {
            String errorMsg = errMsgAnno + " Param is invalid! minValue:" + min + " maxValue:" + max + " paramName:" + paramName + ", paramType:" + paramType;
            throw new YXRequestParamException(errorMsg, errMsgAnno);
        }
    }

    /*
         * <p>Title: supportsParameter</p> <p>Description: </p>
         *
         * @param parameter
         *
         * @return
         *
         * @see
         * org.springframework.web.method.support.HandlerMethodArgumentResolver#
         * supportsParameter(org.springframework.core.MethodParameter)
         */

    public boolean supportsParameter(MethodParameter methodParameter) {
        if (methodParameter.hasParameterAnnotation(YXRequestParam.class)) {
            return true;
        }
        return false;
    }

    private class YXRequestParamNamedValueInfo extends NamedValueInfo {

        private YXRequestParamNamedValueInfo() {
            super("", false, null);
        }

        private YXRequestParamNamedValueInfo(YXRequestParam annotation) {
            super(annotation.value(), annotation.required(), null);
        }
    }
}
