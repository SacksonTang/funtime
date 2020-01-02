package com.rzyou.funtime.common;


public enum TagColorEnmu {

    SEXANDAGE("sex","#FF0096")
    ,HEIGHT("height","#FF9500")
    ,EMOTION("emotion","#0093FF")
    ,QUALIFIVATION("qualification","#00C5FF")
    ,OCCUPATION("occupation","#00C5FF")
    ;
    private String value;
    private String desc;

    TagColorEnmu(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }
    public static String getDescByValue(String val){
        for (TagColorEnmu template:TagColorEnmu.values()){
            if (template.value.equals(val)){
                return template.desc;
            }
        }
        return null;
    }


    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
