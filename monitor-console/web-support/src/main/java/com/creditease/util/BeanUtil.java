package com.creditease.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import java.util.*;

/**
 * 属性拷贝工具类
 */
public final class BeanUtil{
	private static Logger log =  LoggerFactory.getLogger(BeanUtil.class);
	/**拷贝对象**/
	public static <T> T copyObject(Object src, Class<T> destClass){
		if(src==null){
			return null;
		}
		try{
			T destObj = destClass.newInstance();
			BeanUtils.copyProperties(src,destObj);
			return destObj;
		}catch(Exception e){
			log.error("convertObject ERROR", e);
			return null;
		}
	}
	
	/**拷贝列表List**/
	@SuppressWarnings("rawtypes")
	public static <T> List<T> copyList(List srcList, Class<T> destClass){
		if(srcList==null){
			return null;
		}
		try{
			List<T> destList = new ArrayList<T>( srcList.size() );
			for(int i=0;i<srcList.size();i++ ){
				Object srcObj = srcList.get(i);
				T destObj = destClass.newInstance();
				BeanUtils.copyProperties(srcObj,destObj);
				destList.add(destObj);
			}
			return destList;
		}catch(Exception e){
			log.error("copyList error", e);
			return null;
		}
	}
	
	/**拷贝集合Set**/
	@SuppressWarnings("rawtypes")
	public static <T> Set<T> copySet(Set srcSet, Class<T> destClass ){
		if(srcSet==null){
			return null;
		}
		try{
			HashSet<T> destSet = new HashSet<T>( (srcSet.size()+1)*2 );
			Iterator iterator = srcSet.iterator();
			while(iterator.hasNext()){
				Object srcObj = iterator.next();
				T destObj = destClass.newInstance();
				BeanUtils.copyProperties(srcObj,destObj);
				destSet.add(destObj);
			}
			return destSet;
		}catch(Exception e){
			log.error("copySet error", e);
			return null;
		}
	}
}