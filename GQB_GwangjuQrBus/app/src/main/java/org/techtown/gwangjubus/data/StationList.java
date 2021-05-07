package org.techtown.gwangjubus.data;

// 버스 정류장 정보를 저장하는 객체

public class StationList {

    String busstopId;
    String busstopName;
    String updown;

    public StationList(String busstopId, String busstopName, String updown) {
        this.busstopId = busstopId;
        this.busstopName = busstopName;
        this.updown = updown;
    }

    public String getUpdown() {
        return updown;
    }

    public void setUpdown(String updown) {
        this.updown = updown;
    }

    public String getBusstopId() {
        return busstopId;
    }

    public void setBusstopId(String busstopId) {
        this.busstopId = busstopId;
    }

    public String getBusstopName() {
        return busstopName;
    }

    public void setBusstopName(String busstopName) {
        this.busstopName = busstopName;
    }
}
