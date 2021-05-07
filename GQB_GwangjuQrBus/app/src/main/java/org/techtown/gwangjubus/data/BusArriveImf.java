package org.techtown.gwangjubus.data;

// 버스 도착 정보 저장하는 객체

public class BusArriveImf {

    String busId;
    String busName;
    String lineId;
    String busArriveTime;
    String busstopName;

    public String getLineId() {
        return lineId;
    }

    public void setLineId(String lineId) {
        this.lineId = lineId;
    }

    public BusArriveImf(String busId, String busName, String lineId, String busArriveTime, String busstopName) {
        this.busId = busId;
        this.busName = busName;
        this.lineId = lineId;
        this.busArriveTime = busArriveTime;
        this.busstopName = busstopName;
    }

    public String getBusId() {
        return busId;
    }

    public void setBusId(String busId) {
        this.busId = busId;
    }

    public String getBusName() {
        return busName;
    }

    public void setBusName(String busName) {
        this.busName = busName;
    }

    public String getBusArriveTime() {
        return busArriveTime;
    }

    public void setBusArriveTime(String busArriveTime) {
        this.busArriveTime = busArriveTime;
    }

    public String getBusstopName() {
        return busstopName;
    }

    public void setBusstopName(String busstopName) {
        this.busstopName = busstopName;
    }
}
