package org.example;

import java.util.Map;


public class SkierActivity {
    private String skierID;
    private String date;
    private String resortID;
    private Map<String, Integer> lifts;
    private int verticalTotals; // calculate as the sum of liftID(key of lifts) * 10

    // 构造函数
    public SkierActivity(String skierID, String date, String resortID, Map<String, Integer> lifts, int verticalTotals) {
        this.skierID = skierID;
        this.date = date;
        this.resortID = resortID;
        this.lifts = lifts;
        this.verticalTotals = verticalTotals;
    }

    // Getter 和 Setter 方法
    public String getSkierID() {
        return skierID;
    }

    public void setSkierID(String skierID) {
        this.skierID = skierID;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getResortID() {
        return resortID;
    }

    public void setResortID(String resortID) {
        this.resortID = resortID;
    }

    public Map<String, Integer> getLifts() {
        return lifts;
    }

    public void setLifts(Map<String, Integer> lifts) {
        this.lifts = lifts;
    }

    public int getVerticalTotals() {
        return verticalTotals;
    }

    public void setVerticalTotals(int verticalTotals) {
        this.verticalTotals = verticalTotals;
    }
}
