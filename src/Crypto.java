/*
 * Crypto.java
 * Object class to model all possible crypto-currencies received by an API
 *
 * Crypto's that are supported in this application:
 *      BitCoin  - BTC
 *      LiteCoin - LTC
 *
 *
 * Currencies that are supported in this application:
 *      US Dollar         - USD
 *      Euro              - EUR
 *      British Pound     - GBP
 *      Australian Dollar - AUD
 *      Japanese Yen      - JPY
 *      Chinese Yuan      - CNY
 *      Saudi Riyal       - SAR
 */

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public class Crypto {
    private static final String BASE_URL = "https://apiv2.bitcoinaverage.com/indices/global/";
    private String rawJson;
    private String priceSymbol;
    private String timestamp;
    private String display_timestamp;
    private double ask;
    private double bid;
    private double last;
    private double high;
    private double low;
    private double volume;
    private double volumePercent;
    private DateData open;
    private DateData averages;
    private DateData priceChanges;
    private DateData percentChanges;
    private ArrayList<HistoricalData> dailyHistory;
    private ArrayList<HistoricalData> monthlyHistory;
    private ArrayList<HistoricalData> allTimeHistory;
    private ArrayList<NewsData> articles;
    private HashMap<String, Double> exchangeRates;

    // constructor
    public Crypto(String priceSymbol) {
        this.priceSymbol = priceSymbol;

        ask = bid = last = high = low = volume = volumePercent = 0;
        open           = new DateData();
        averages       = new DateData();
        priceChanges   = new DateData();
        percentChanges = new DateData();
        dailyHistory   = new ArrayList<>();
        monthlyHistory = new ArrayList<>();
        allTimeHistory = new ArrayList<>();
        articles       = new ArrayList<>();
        exchangeRates  = new HashMap<>();

        updateCrypto();
        updateExchangeRates();
    }

    // getters
    public String getPriceSymbol() {
        return priceSymbol;
    }

    public String getDisplay_timestamp() {
        return display_timestamp;
    }

    public double getAsk() {
        return ask;
    }

    public double getBid() {
        return bid;
    }

    public double getLast() {
        return last;
    }

    public double getHigh() {
        return high;
    }

    public double getLow() {
        return low;
    }

    public double getVolume() {
        return volume;
    }

    public double getVolumePercent() {
        return volumePercent;
    }

    public DateData getOpen() {
        return open;
    }

    public DateData getAverages() {
        return averages;
    }

    public DateData getPriceChanges() {
        return priceChanges;
    }

    public DateData getPercentChanges() {
        return percentChanges;
    }

    public List<HistoricalData> getDailyHistory(){
        return getHistory("daily");
    }

    public List<HistoricalData> getMonthlyHistory(){
        return getHistory("monthly");
    }

    public List<HistoricalData> getAllTimeHistory(){
        return getHistory("alltime");
    }

    // calls the api to receive relevant data for this crypto and updates respective fields
    public void updateCrypto() {
        // create the url for the endpoint and call the api using it
        String endpoint = BASE_URL + "ticker/" + getPriceSymbol();
        String apiResponse = Api.fetch(endpoint);

        // parse the JSON data received from the api
        try {
            JSONObject obj = new JSONObject(apiResponse);
            this.rawJson = apiResponse;

            // check in case the API response does not contain the fields we are expecting, and extract the data from the JSON object
            if (obj.has("ask"))
                this.ask = obj.getDouble("ask");
            if (obj.has("bid"))
                this.bid = obj.getDouble("bid");
            if (obj.has("last"))
                this.last = obj.getDouble("last");
            if (obj.has("high"))
                this.high = obj.getDouble("high");
            if (obj.has("low"))
                this.low = obj.getDouble("low");
            if (obj.has("volume"))
                this.volume = obj.getDouble("volume");
            if (obj.has("volume_percent"))
                this.volumePercent = obj.getDouble("volume_percent");
            if (obj.has("timestamp"))
                this.timestamp = Double.toString(obj.getDouble("timestamp"));
            if (obj.has("display_timestamp"))
                this.display_timestamp = obj.getString("display_timestamp");
            if (obj.has("open"))
                open.updateData(obj.getJSONObject("open"));
            if (obj.has("averages"))
                averages.updateData(obj.getJSONObject("averages"));
            if (obj.has("changes")) {
                JSONObject changes = obj.getJSONObject("changes");
                if (changes.has("price"))
                    priceChanges.updateData(changes.getJSONObject("price"));
                if (changes.has("percent"))
                    percentChanges.updateData(changes.getJSONObject("percent"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /*
     * returns an immutable list of the:
      * daily   - daily per minute daily sliding window
      * monthly - monthly per hour monthly sliding window
      * alltime - all time history (default value)
     */
    private List<HistoricalData> getHistory(String period){
        // create the url for the endpoint and call the api using it
        String endpoint = BASE_URL + "history/" + getPriceSymbol() + "?period=" + period + "&?format=json";
        String apiResponse = Api.fetch(endpoint);

        try {
            JSONArray arr = new JSONArray(apiResponse);

            if (period.equals("daily")) {
                // remove any previous data stored in the list
                dailyHistory.clear();

                for (int i = 0; i < arr.length(); ++i) {
                    JSONObject obj = arr.getJSONObject(i);

                    // check in case the API response does not contain the fields we are expecting
                    if(obj.has("time") && obj.has("average")) {

                        // extract the data from the JSON object
                        String time = obj.getString("time");
                        double avg  = obj.getDouble("average");
                        dailyHistory.add(new HistoricalData(time, avg));
                    }
                }
                Collections.reverse(dailyHistory);
                return Collections.unmodifiableList(dailyHistory);
            }
            else if (period.equals("monthly")) {
                // remove any previous data stored in the list
                monthlyHistory.clear();

                for (int i = 0; i < arr.length(); ++i) {
                    JSONObject obj = arr.getJSONObject(i);

                    // check in case the API response does not contain the fields we are expecting
                    if(obj.has("time") && obj.has("average") && obj.has("low") && obj.has("open") && obj.has("high")){

                        // extract the data from the JSON object
                        String time = obj.getString("time");
                        double avg  = obj.getDouble("average");
                        double low  = obj.getDouble("low");
                        double high = obj.getDouble("high");
                        double open = obj.getDouble("open");

                        monthlyHistory.add(new HistoricalData(time, avg, low, high, open));
                    }
                }
                Collections.reverse(monthlyHistory);
                return Collections.unmodifiableList(monthlyHistory);
            }
            else if (period.equals("alltime")) {
                // remove any previous data stored in the list
                allTimeHistory.clear();

                for (int i = 0; i < arr.length(); ++i) {
                    JSONObject obj = arr.getJSONObject(i);

                    // check in case the API response does not contain the fields we are expecting
                    if(obj.has("time") && obj.has("average") && obj.has("volume")) {

                        // extract the data from the JSON object
                        String time = obj.getString("time");
                        double avg  = obj.getDouble("average");
                        double vol  = obj.getDouble("volume");

                        allTimeHistory.add(new HistoricalData(time, avg, vol));
                    }
                }
                Collections.reverse(allTimeHistory);
                return Collections.unmodifiableList(allTimeHistory);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<NewsData> relevantArticles(String Key) {
        // https://newsapi.org/v2/everything?q=cryptocurrency&sortBy=publishedAt&apiKey=f313ec1010e145cab7730034d0c6baf5

        String base_url = "https://newsapi.org/v2/everything?";
        String tempKey  = "q=" + Key;  // This key is used for the specific coin type
        String baseKey  = "q=cryptocurrency";  // Use this key if coin has less than 5 articles
        String sortBy   = "&sortBy=publishedAt";
        String apiKey   = "&apiKey=f313ec1010e145cab7730034d0c6baf5";

        // create the url for the endpoint and call the api using it
        String endpoint = base_url + tempKey + sortBy + apiKey;
        String apiResponse = Api.fetch(endpoint);

        try {
            JSONArray arr = new JSONObject(apiResponse).getJSONArray("articles");

            articles.clear();

            // We don't have enough articles so we add addition general crypto news
            if(arr.length() < 5) {
                String endPoint = base_url + baseKey + sortBy + apiKey;
                String response = Api.fetch(endPoint);

                JSONArray baseArr = new JSONObject(response).getJSONArray("articles");

                // Append additional objects to array with our specific crypto stories
                for (int i = 0; i < baseArr.length(); i++) {
                    arr.put(baseArr.getJSONObject(i));
                }
            }

            // Get JSON Objects and parse data
            for (int i = 0; i < arr.length(); ++i) {
                JSONObject obj = arr.getJSONObject(i);

                String author, title, desc, url, image, published, content;
                author = title = desc = url = image = published = content = null;

                if (obj.has("author") && obj.get("author") instanceof String)
                    author = obj.getString("author");
                if (obj.has("title") && obj.get("title") instanceof String)
                    title = obj.getString("title");
                if (obj.has("description") && obj.get("description") instanceof String)
                    desc = obj.getString("description");
                if (obj.has("url") && obj.get("url") instanceof String)
                    url = obj.getString("url");
                if (obj.has("urlToImage") && obj.get("urlToImage") instanceof String)
                    image = obj.getString("urlToImage");
                if (obj.has("publishedAt") && obj.get("publishedAt") instanceof String)
                    published = obj.getString("publishedAt");
                if (obj.has("content") && obj.get("content") instanceof String)
                    content = obj.getString("content");

                if (author != null && title != null && desc != null && url != null && image != null && published != null && content != null) {
                    articles.add(new NewsData(author, title, desc, url, image, published, content));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return Collections.unmodifiableList(articles);
    }

    // calls the api and populates the container with the current exchange rates of all currencies
    private void updateExchangeRates() {
        // create the url for the endpoint and call the api using it
        String endpoint = "https://apiv2.bitcoinaverage.com/constants/exchangerates/global";
        String apiResponse = Api.fetch(endpoint);

        // parse the JSON data received from the api
        try {
            exchangeRates.clear();
            JSONObject rates = new JSONObject(apiResponse).getJSONObject("rates");
            Iterator<String> keys = rates.keys();

            while(keys.hasNext()) {
                String key = keys.next();
                if (rates.get(key) instanceof JSONObject) {
                    JSONObject obj = rates.getJSONObject(key);

                    if (obj.has("rate")) {
                        String currency = key;
                        double rate = Double.parseDouble(obj.getString("rate"));
                        exchangeRates.put(currency, rate);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // takes a double value in AUD and a string to convert it to, like GBP
    public double convert(String exchangeRate, double value) {
        if (!exchangeRates.containsKey(exchangeRate))
            return -1;
        String currency  = getPriceSymbol().substring(3, 6);
        double currencyValue = exchangeRates.get(currency);
        double rateValue = exchangeRates.get(exchangeRate);

        return value / currencyValue * rateValue;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Price Symbol: " + getPriceSymbol());
        sb.append("\n");
        sb.append("Timestamp: " + getDisplay_timestamp());
        sb.append("\n");
        sb.append("Ask: " + getAsk());
        sb.append("\n");
        sb.append("Bid: " + getBid());
        sb.append("\n");
        sb.append("Last: " + getLast());
        sb.append("\n");
        sb.append("High: " + getHigh());
        sb.append("\n");
        sb.append("Low: " + getLow());
        sb.append("\n");
        sb.append("Volume: " + getVolume());
        sb.append("\n");
        sb.append("Volume Percent: " + getVolumePercent());
        sb.append("\n");
        sb.append("Open:");
        sb.append("\n");
        sb.append(getOpen().toString());
        sb.append("\n");
        sb.append("Averages:");
        sb.append("\n");
        sb.append(getAverages().toString());
        sb.append("\n");
        sb.append("Price Changes:");
        sb.append("\n");
        sb.append(getPriceChanges().toString());
        sb.append("\n");
        sb.append("Percent Changes:");
        sb.append("\n");
        sb.append(getPercentChanges().toString());

        return sb.toString();
    }
}
