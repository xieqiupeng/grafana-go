package com.creditease;

import org.mybatis.generator.api.MyBatisGenerator;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.xml.ConfigurationParser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class generatorTool {

    public static void main(String[] args) throws Exception{
        String generatorConfigXml = "D:/ideaWorkSpace/monitor/monitor-web/generator/localGeneratorConfig.xml";
        List<String> warnings = new ArrayList<String>();
        ConfigurationParser cp = new ConfigurationParser(warnings);
        Configuration config = cp.parseConfiguration(new File(generatorConfigXml));
        MyBatisGenerator myBatisGenerator = new MyBatisGenerator(config, null, null);
        myBatisGenerator.generate(null);
        System.out.println("-------------------GeneratorMySql OK");
    }
}
