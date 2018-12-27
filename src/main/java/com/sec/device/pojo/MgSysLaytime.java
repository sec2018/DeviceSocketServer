package com.sec.device.pojo;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

@Document(collection ="sys_laytime")
public class MgSysLaytime implements Serializable {

    private static final long serialVersionUID = -3258839839160856613L;

    @Id
    private String _id;

    @Indexed(unique = true)
    private String id;

    private String mid;

    private String latitude;

    private String longitude;

    private Date grantgmt;

    private String err;

    private Double voltage;

    private Byte temperature;

    private Byte type;

    private Date timegmt;

    private Byte islay;

    private Byte signallevel;

    private Date updatetime;

    @PersistenceConstructor
    public MgSysLaytime(String id, String mid, String latitude, String longitude, Date grantgmt, String err, Double voltage, Byte temperature, Byte type, Date timegmt, Byte islay, Byte signallevel, Date updatetime) {
        this.id = id;
        this.mid = mid;
        this.latitude = latitude;
        this.longitude = longitude;
        this.grantgmt = grantgmt;
        this.err = err;
        this.voltage = voltage;
        this.temperature = temperature;
        this.type = type;
        this.timegmt = timegmt;
        this.islay = islay;
        this.signallevel = signallevel;
        this.updatetime = updatetime;
    }

    public MgSysLaytime() {
        super();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid == null ? null : mid.trim();
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude == null ? null : latitude.trim();
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude == null ? null : longitude.trim();
    }

    public Date getGrantgmt() {
        return grantgmt;
    }

    public void setGrantgmt(Date grantgmt) {
        this.grantgmt = grantgmt;
    }

    public String getErr() {
        return err;
    }

    public void setErr(String err) {
        this.err = err == null ? null : err.trim();
    }

    public Double getVoltage() {
        return voltage;
    }

    public void setVoltage(Double voltage) {
        this.voltage = voltage;
    }

    public Byte getTemperature() {
        return temperature;
    }

    public void setTemperature(Byte temperature) {
        this.temperature = temperature;
    }

    public Byte getType() {
        return type;
    }

    public void setType(Byte type) {
        this.type = type;
    }

    public Date getTimegmt() {
        return timegmt;
    }

    public void setTimegmt(Date timegmt) {
        this.timegmt = timegmt;
    }

    public Byte getIslay() {
        return islay;
    }

    public void setIslay(Byte islay) {
        this.islay = islay;
    }

    public Byte getSignallevel() {
        return signallevel;
    }

    public void setSignallevel(Byte signallevel) {
        this.signallevel = signallevel;
    }

    public Date getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(Date updatetime) {
        this.updatetime = updatetime;
    }
}