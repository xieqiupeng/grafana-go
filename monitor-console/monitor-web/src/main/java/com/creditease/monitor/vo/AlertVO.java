package com.creditease.monitor.vo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author qiupengxie
 */
public class AlertVO {
    private String ruleName = "";
    private String state = "";
    private String ruleId = "";
    private String title = "";
    private String ruleUrl = "";
    private List<EvalMatch> evalMatches = new ArrayList<EvalMatch>();

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRuleUrl() {
        return ruleUrl;
    }

    public void setRuleUrl(String ruleUrl) {
        this.ruleUrl = ruleUrl;
    }

    public List<EvalMatch> getEvalMatches() {
        return evalMatches;
    }

    public void setEvalMatches(List<EvalMatch> evalMatches) {
        this.evalMatches = evalMatches;
    }

    public class EvalMatch {
        private String metric = "";
        private String value = "";

        public String getMetric() {
            return metric;
        }

        public void setMetric(String metric) {
            this.metric = metric;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
