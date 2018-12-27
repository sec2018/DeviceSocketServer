package com.sec.device.pojo;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "sequence")
public class MgSeqInfo {

    @Id
    private String _id;// 主键

    public String getMgSysLaytime_id() {
        return MgSysLaytime_id;
    }

    public void setMgSysLaytime_id(String mgSysLaytime_id) {
        MgSysLaytime_id = mgSysLaytime_id;
    }

    @Field
    private String MgSysLaytime_id;// 集合id

}
