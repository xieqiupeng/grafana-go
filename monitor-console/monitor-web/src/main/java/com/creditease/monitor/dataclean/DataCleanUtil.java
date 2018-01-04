package com.creditease.monitor.dataclean;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;

public class DataCleanUtil {
    private static final Logger logger = LoggerFactory.getLogger(DataCleanUtil.class);
    public static boolean isTag(Integer tagOrField){
        return tagOrField !=null && tagOrField == 0;
    }

    public static Object getValueByType(String value , String type,String format){
        try {
            logger.info("getValueByType 类型转换开始 value={},type={},format={},errorMsg={}",value,type,format);
            if(type != null){
                type = type.trim();
                if("boolean".equals(type)){
                    return Boolean.parseBoolean(value);
                }else if("long".equals(type)){
                    return Long.parseLong(value);
                }else if("double".equals(type)){
                    return Double.parseDouble(value);
                }else if("date".equals(type)){
                    if(StringUtils.isNotBlank(format)){
                        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
                        return dateFormat.parse(value).getTime();
                    }else{
                        logger.info("no format 无法转换");
                    }
                }
            }
        }catch (Exception e){
            logger.error("getValueByType 类型转换异常 value={},type={},format={},errorMsg={}",value,type,format,e.getMessage());
        }
        return value;
    }

    public static IDataCleanRule getDataCleanRule(DataCleanRuleEntity cleanRuleEntity){
        if(cleanRuleEntity != null){
            String template = cleanRuleEntity.getTemplate();
            if("0".equals(template)){
                return new CommonTextDataCleanRule(cleanRuleEntity);
            }else if("1".equals(template)){
                return new JsonTextDataCleanRule(cleanRuleEntity);
            }else if("2".equals(template)){
                return new KVtextDataCleanRule(cleanRuleEntity);
            }
        }
        return  null;
    }
}
