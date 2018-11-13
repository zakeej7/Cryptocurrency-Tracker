/*
 * DateData.java
 * Object class to model all possible date data values received by an API
 */

import org.json.JSONException;
import org.json.JSONObject;

public class DateData {
    // member variables
    private double hour;
    private double day;
    private double week;
    private double month;
    private double month_3;
    private double month_6;
    private double year;
    private boolean isInDepth;

    // constructors
    public DateData() {
        isInDepth = false;
        hour = day = week = month = month_3 = month_6 = year = -1;
    }

    // getters
    public double getHour() {
        return hour;
    }

    public double getDay() {
        return day;
    }

    public double getWeek() {
        return week;
    }

    public double getMonth() {
        return month;
    }

    public double getMonth_3() {
        return month_3;
    }

    public double getMonth_6() {
        return month_6;
    }

    public double getYear() {
        return year;
    }

    public boolean isInDepth() {
        return isInDepth;
    }

    /*
     * A DateData object can either be in-depth or not in-depth.
     *
     * A non in-depth DateData object ONLY has: day, week, month
     * An in-depth DateData object contains at least one additional data member, i.e. hour, month_3, month6, year
     */
    public void updateData(JSONObject obj) {
        try {
            this.isInDepth = false;

            if (obj.has("day") && obj.get("day") instanceof Double) {
                this.day   = obj.getDouble("day");
                this.week  = obj.getDouble("week");
                this.month = obj.getDouble("month");
            }

            if (obj.has("hour") && obj.get("hour") instanceof Double) {
                this.isInDepth = true;

                this.hour    = obj.getDouble("hour");
                this.month_3 = obj.getDouble("month_3");
                this.month_6 = obj.getDouble("month_6");
                this.year    = obj.getDouble("year");

            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        if (isInDepth()) {
            sb.append("Hour: " + getHour());
            sb.append("\n");
            sb.append("Day: " + getDay());
            sb.append("\n");
            sb.append("Week: " + getWeek());
            sb.append("\n");
            sb.append("Month: " + getMonth());
            sb.append("\n");
            sb.append("Month_3: " + getMonth_3());
            sb.append("\n");
            sb.append("Month_6: " + getMonth_6());
            sb.append("\n");
            sb.append("Year: " + getYear());
        }
        else {
            sb.append("Day: " + getDay());
            sb.append("\n");
            sb.append("Week: " + getWeek());
            sb.append("\n");
            sb.append("Month: " + getMonth());
        }

        return sb.toString();
    }
}
