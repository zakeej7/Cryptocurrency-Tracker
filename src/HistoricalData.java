/*
 * HistoricalData.java
 * Object class to model all possible historical data values received by an API
 */

public class HistoricalData {
    private String type;
    private String time;
    private double volume;
    private double low;
    private double high;
    private double open;
    private double average;

    // daily
    public HistoricalData(String time, double average) {
        this.type    = "DAILY";
        this.time    = time;
        this.average = average;
    }

    // monthly
    public HistoricalData(String time, double average, double low, double high, double open) {
        this.type    = "MONTHLY";
        this.low     = low;
        this.high    = high;
        this.open    = open;
        this.time    = time;
        this.average = average;
    }

    // all time
    public HistoricalData(String time, double average, double volume) {
        this.type    = "ALLTIME";
        this.time    = time;
        this.volume  = volume;
        this.average = average;
    }

    // getters
    public String getType() {
        return type;
    }

    public String getTime() {
        return time;
    }

    public double getVolume() {
        return volume;
    }

    public double getLow() {
        return low;
    }

    public double getHigh() {
        return high;
    }

    public double getOpen() {
        return open;
    }

    public double getAverage() {
        return average;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Time: " + getTime());
        sb.append("\n");
        sb.append("Average: " + getAverage());
        sb.append("\n");

        if (getType().equals("ALLTIME")) {
            sb.append("Volume: " + getVolume());
            sb.append("\n");
        }
        else if (getType().equals("MONTHLY")) {
            sb.append("Low: " + getLow());
            sb.append("\n");
            sb.append("High: " + getHigh());
            sb.append("\n");
            sb.append("Open: " + getOpen());
            sb.append("\n");
        }

        return sb.toString();
    }
}
