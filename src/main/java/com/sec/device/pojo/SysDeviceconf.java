package com.sec.device.pojo;

import java.util.Date;

public class SysDeviceconf{

    private Integer id;

    private String mid;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    private Integer status;

    private String simccid;

    private String swver;

    private String ip;

    private Integer port;

    private Integer infoupdatecycle;

    private Integer tickcycle;

    private Byte bastimes;

    private Byte gpstimes;

    private Byte ledenable;

    private Byte temporaryflag;

    private Date temporarygmt;

    private Byte clearerr;

    private Byte factory;

    private Byte uimodifyflag;

    private Byte hardmodifyflag;

    private Date updatetime;

    public SysDeviceconf(Integer id, String mid, Integer status, String simccid, String swver, String ip, Integer port, Integer infoupdatecycle, Integer tickcycle,Byte bastimes, Byte gpstimes, Byte ledenable, Byte temporaryflag, Date temporarygmt, Byte clearerr, Byte factory, Byte uimodifyflag, Byte hardmodifyflag, Date updatetime) {
        this.status = status;
        this.simccid = simccid;
        this.swver = swver;
        this.ip = ip;
        this.port = port;
        this.infoupdatecycle = infoupdatecycle;
        this.tickcycle = tickcycle;
        this.bastimes = bastimes;
        this.gpstimes = gpstimes;
        this.ledenable = ledenable;
        this.temporaryflag = temporaryflag;
        this.temporarygmt = temporarygmt;
        this.clearerr = clearerr;
        this.factory = factory;
        this.uimodifyflag = uimodifyflag;
        this.hardmodifyflag = hardmodifyflag;
        this.updatetime = updatetime;
    }


    public SysDeviceconf() {
        super();
    }

    public Byte getBastimes() {
        return bastimes;
    }

    public void setBastimes(Byte bastimes) {
        this.bastimes = bastimes;
    }

    public Byte getGpstimes() {
        return gpstimes;
    }

    public void setGpstimes(Byte gpstimes) {
        this.gpstimes = gpstimes;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getSimccid() {
        return simccid;
    }

    public void setSimccid(String simccid) {
        this.simccid = simccid == null ? null : simccid.trim();
    }

    public String getSwver() {
        return swver;
    }

    public void setSwver(String swver) {
        this.swver = swver == null ? null : swver.trim();
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip == null ? null : ip.trim();
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Integer getInfoupdatecycle() {
        return infoupdatecycle;
    }

    public void setInfoupdatecycle(Integer infoupdatecycle) {
        this.infoupdatecycle = infoupdatecycle;
    }

    public Integer getTickcycle() {
        return tickcycle;
    }

    public void setTickcycle(Integer tickcycle) {
        this.tickcycle = tickcycle;
    }

    public Byte getLedenable() {
        return ledenable;
    }

    public void setLedenable(Byte ledenable) {
        this.ledenable = ledenable;
    }

    public Byte getTemporaryflag() {
        return temporaryflag;
    }

    public void setTemporaryflag(Byte temporaryflag) {
        this.temporaryflag = temporaryflag;
    }

    public Date getTemporarygmt() {
        return temporarygmt;
    }

    public void setTemporarygmt(Date temporarygmt) {
        this.temporarygmt = temporarygmt;
    }

    public Byte getClearerr() {
        return clearerr;
    }

    public void setClearerr(Byte clearerr) {
        this.clearerr = clearerr;
    }

    public Byte getFactory() {
        return factory;
    }

    public void setFactory(Byte factory) {
        this.factory = factory;
    }

    public Byte getUimodifyflag() {
        return uimodifyflag;
    }

    public void setUimodifyflag(Byte uimodifyflag) {
        this.uimodifyflag = uimodifyflag;
    }

    public Byte getHardmodifyflag() {
        return hardmodifyflag;
    }

    public void setHardmodifyflag(Byte hardmodifyflag) {
        this.hardmodifyflag = hardmodifyflag;
    }

    public Date getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(Date updatetime) {
        this.updatetime = updatetime;
    }
}