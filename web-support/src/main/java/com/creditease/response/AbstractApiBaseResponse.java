
package com.creditease.response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;


class AbstractApiBaseResponse implements Serializable{

	/**
	* @Fields serialVersionUID : TODO
	*/
	private static final long serialVersionUID = 4980776232597106799L;

	private final static Logger log = LoggerFactory.getLogger(AbstractApiBaseResponse.class);
	/**
	 * @Fields resultCodeMsgMap : 返回码和返回提示信息
	 */
	protected static ConcurrentHashMap<Integer, String> resultCodeMsgMap = new ConcurrentHashMap<Integer, String>(10);

	static {
		try {
			Field[] fields = BaseResultCode.class.getDeclaredFields();
			for (Field field : fields) {
				field.setAccessible(true);
				int resultCode = field.getInt(null);
				if (resultCodeMsgMap.containsKey(resultCode)) {// 返回码定义冲突
					log.error("Result code definitions conflict! resultCode：" + resultCode);
					System.exit(0);
				}
				ResultMessage annotation = field.getAnnotation(ResultMessage.class);
				if (null != annotation) {
					String resultMsg = annotation.value();
					if (null != resultMsg && !"".equals(resultMsg.trim())) {
						resultCodeMsgMap.put(resultCode, resultMsg);
					}
				}
			}
		} catch (Exception e) {
			log.error("Fail to init result code info!", e);
			System.exit(0);
		}
	}

	/**
	 * @Description: 通过返回码获取返回提示信息
	 * 
	 * @param resultCode
	 * @return String
	 * @create time 2016年1月7日 下午7:30:01
	 */
	protected static String getResultMsg(int resultCode) {
		return resultCodeMsgMap.get(resultCode);
	}

	/**
	 * @Description: 添加新的返回码定义，校验冲突
	 * 
	 * @param c
	 *            -返回码类
	 * @create time 2016年1月7日 下午8:00:04
	 */
	public static void addResultCodeDefinitionClass(Class<?> c) {
		try {
			if(c == null){
				return;
			}
			Field[] fields = c.getDeclaredFields();
			for (Field field : fields) {
				field.setAccessible(true);
				int resultCode = field.getInt(null);
				if (resultCodeMsgMap.containsKey(resultCode)) {// 返回码定义冲突
					log.error("Result code definitions conflict! resultCode：" + resultCode);
				}
				ResultMessage annotation = field.getAnnotation(ResultMessage.class);
				if (null != annotation) {
					String resultMsg = annotation.value();
					if (null != resultMsg && !"".equals(resultMsg.trim())) {
						resultCodeMsgMap.put(resultCode, resultMsg);
					}
				}
			}
		} catch (Exception e) {
			log.error("Fail to add result code info!", e);
		}
	}
}
