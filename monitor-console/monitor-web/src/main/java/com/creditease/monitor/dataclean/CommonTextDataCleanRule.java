package com.creditease.monitor.dataclean;

import com.creditease.monitor.vo.CutExampleVo;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 普通文本切割
 */
public class CommonTextDataCleanRule implements IDataCleanRule {

    private static final String escapeCharacter = "\\";

    private DataCleanRuleEntity dataCleanRuleEntity;

    public CommonTextDataCleanRule(DataCleanRuleEntity dataCleanRuleEntity) {
        this.dataCleanRuleEntity = dataCleanRuleEntity;
    }

    @Override
    public List<CutExampleVo> clean(String monitorData) {
        if(StringUtils.isNotBlank(monitorData)){
            if(dataCleanRuleEntity.getSeparator() != null){
                DataCleanRuleEntity.Separator separator = dataCleanRuleEntity.getSeparator();
                if(separator.isRegex() != null && separator.isRegex()){
                    return regexSplit(separator.isOrder(),separator.getSeparatorKeys(),monitorData,dataCleanRuleEntity.getResultColumns());
                }else{
                    return nomalSplit(separator.isOrder(),separator.getSeparatorKeys(),monitorData,dataCleanRuleEntity.getResultColumns());
                }
            }
        }
        return new ArrayList<>();
    }

    private static List<CutExampleVo> nomalSplit(Boolean isOrder, List<String> separatorKeys, String monitorData,List<DataCleanRuleEntity.DataMapping> resultColumns){
        List<String> list = new ArrayList<>();
        if(separatorKeys != null && !separatorKeys.isEmpty()){
            if(isOrder != null && isOrder){
                for(String key : separatorKeys){
                    if(StringUtils.isNotBlank(monitorData)){
                        int index = monitorData.indexOf(key);
                        if(index > -1){
                            list.add(monitorData.substring(0,index));
                            monitorData = monitorData.substring(index+1);
                        }
                    }
                }
                if(StringUtils.isNotBlank(monitorData)){
                    list.add(monitorData);
                }
            }else{
                Set<String> set = new HashSet<>();
                separatorKeys.forEach(key->{
                    if(StringUtils.isNotBlank(key)){
                        set.add(escapeCharacter.concat(key).trim());
                    }
                });
                String splitkey =  generateSplitkey(set);
                if(StringUtils.isNotBlank(splitkey)){
                    list.addAll(Arrays.asList(monitorData.split(splitkey)));
                }
            }
        }
        return  converter(list,resultColumns);
    }

    private static List<CutExampleVo> regexSplit(Boolean isOrder, List<String> separatorKeys,String monitorData,List<DataCleanRuleEntity.DataMapping> resultColumns){
        List<String> list = new ArrayList<>();
        if(separatorKeys != null && !separatorKeys.isEmpty()) {
            if (isOrder !=null && isOrder) {
                for(String key : separatorKeys){
                    Pattern pattern = Pattern.compile(key);
                    Matcher matcher = pattern.matcher(monitorData);
                    // System.out.println(matcher.matches());
                    if(matcher.find()){
                        //获取 字符串
                        list.add(monitorData.substring(0,matcher.start()));
                        //获取的字符串的首位置和末位置
                        monitorData = monitorData.substring(matcher.end());
                    }
                }
                if(StringUtils.isNotBlank(monitorData)){
                    list.add(monitorData);
                }
            }else{
                Set<String> set = new HashSet<>();
                separatorKeys.forEach(key->{
                    if(StringUtils.isNotBlank(key)){
                        set.add(key.trim());
                    }
                });
                String splitkey =  generateSplitkey(set);
                if(StringUtils.isNotBlank(splitkey)){
                    list.addAll(Arrays.asList(monitorData.split(splitkey)));
                }
            }
        }
        return  converter(list,resultColumns);
    }

    private static String generateSplitkey(Set<String> set){
        if(set != null && !set.isEmpty()){
            StringBuffer buffer = new StringBuffer();
            set.forEach(key->{
                buffer.append(key).append("|");
            });
            return buffer.substring(0,buffer.length()-1);
        }
        return StringUtils.EMPTY;
    }

    private static List<CutExampleVo> converter(List<String> list,List<DataCleanRuleEntity.DataMapping> resultColumns){
        List<CutExampleVo> dataCleanEntities = new ArrayList<>();
        if(list != null && !list.isEmpty()){
            if(resultColumns != null && !resultColumns.isEmpty()){
                Map<Integer,DataCleanRuleEntity.DataMapping> map = new HashMap<>();
                resultColumns.forEach(dataMapping -> {
                    if(dataMapping.getColumnSeq() != null){
                        map.put(dataMapping.getColumnSeq(),dataMapping);
                    }
                });

                if(list != null && !list.isEmpty()){
                    for(int i = 0; i<list.size();i++){
                        if(map.containsKey(i)){
                            DataCleanRuleEntity.DataMapping mapping = map.get(i);
                            String columnName = mapping.getColumnName();
                            if(StringUtils.isNotBlank(columnName) ){
                                CutExampleVo entity = new CutExampleVo();
                                entity.setColumnName(columnName);
                                entity.setColumnExampleValue(DataCleanUtil.getValueByType(list.get(i),mapping.getColumnType(),mapping.getFormat()));
                                entity.setColumnFormat(mapping.getColumnType());
                                dataCleanEntities.add(entity);
                            }

                        }
                    }
                }
            }
        }
        return dataCleanEntities;
    }
}
