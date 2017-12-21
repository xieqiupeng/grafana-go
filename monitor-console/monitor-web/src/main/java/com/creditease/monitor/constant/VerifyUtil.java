package com.creditease.monitor.constant;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VerifyUtil {

    public static boolean isIP(String addr) {
        if(StringUtils.isBlank(addr)||addr.length() < 7 || addr.length() > 15 ) {
            return false;
        }
        /**
         * 判断IP格式和范围
         */
        String rexp = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";
        Pattern pat = Pattern.compile(rexp);
        Matcher mat = pat.matcher(addr);
        boolean ipAddress = mat.find();
        return ipAddress;
    }
    public static boolean isPort(String port) {
        if(StringUtils.isBlank(port)){
            return false;
        }
        try {
            int portInt = Integer.parseInt(port);
            return portInt >0 && portInt <= 65535;
        }catch (Exception e){
        }
        return false;
    }
}
