import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTabPane;
import javafx.beans.binding.DoubleExpression;
import javafx.collections.FXCollections;
import javafx.collections.ObservableArray;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Path;
import javafx.scene.text.Text;

import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.CollationElementIterator;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

// Handles all the GUI events
public class Controller
{
    private Crypto bitCoinUS = new Crypto("BTCUSD");
    private Crypto liteCoinUS = new Crypto("LTCUSD");

    private String[] currencies = {"GBP", "EUR", "AUD", "JPY", "CNY", "SAR" };

    // Graph Done
    private boolean dayDone = false;
    private boolean monthDone = false;
    private boolean allTimeDone = false;

    // Main tabs
    @FXML private JFXTabPane mainTabPane;
    @FXML private JFXTabPane graphTabPane;

    // Top bar
    @FXML private Label nameTicker;
    @FXML private Label currentPrice;
    @FXML private Label dayChanges;
    @FXML private Label lastUpdate;
    @FXML private JFXButton refreshButton;

    // Quick Information
    @FXML private Label dailyHigh;
    @FXML private Label dailyLow;
    @FXML private Label last;
    @FXML private Label volume;
    @FXML private Label bid;
    @FXML private Label ask;

    // Converted Prices
    @FXML private Label gbp;
    @FXML private Label eur;
    @FXML private Label aud;
    @FXML private Label jpy;
    @FXML private Label cny;
    @FXML private Label sar;

    // Graph Stuff
    @FXML private NumberAxis dayX;
    @FXML private NumberAxis dayY;
    @FXML private NumberAxis monthX;
    @FXML private NumberAxis monthY;
    @FXML private NumberAxis allTimeX;
    @FXML private NumberAxis allTimeY;
    @FXML private StackedAreaChart dayGraph;
    @FXML private StackedAreaChart monthGraph;
    @FXML private StackedAreaChart allTimeGraph;
    @FXML private Label dayFirstLabel;
    @FXML private Label daySecondLabel;
    @FXML private Label monthFirstLabel;
    @FXML private Label monthSecondLabel;
    @FXML private Label allTimeFirstLabel;
    @FXML private Label allTimeSecondLabel;



    // Cryptos
    public Hyperlink btc;
    public Hyperlink ltc;

    // News
    @FXML private Hyperlink news1;
    @FXML private Hyperlink news2;
    @FXML private Hyperlink news3;
    @FXML private Hyperlink news4;
    @FXML private Hyperlink news5;
    private List<NewsData> newsList;


    // Handles which Coin is currently selected
    private String coinSelected = "Bitcoin (BTC)";


    // Load the application with data already in it
    // Setup listeners for tab switching
    public void initialize() {
        updateApplication();

        graphTabPane.getSelectionModel().selectedItemProperty().addListener((ov, oldTab, newTab) -> {
            Crypto temp;
            if(coinSelected.contentEquals("Bitcoin (BTC)")) {
                temp = bitCoinUS;
            }
            else {
                temp = liteCoinUS;
            }
            temp.updateCrypto();

            updateChart(temp, false);
        });
    }


    // Updates the GUI with new information
    public void updateApplication() {

        Crypto temp;
        if(coinSelected.contentEquals("Bitcoin (BTC)")) {
            temp = bitCoinUS;
        }
        else {
            temp = liteCoinUS;
        }
        temp.updateCrypto();

        DecimalFormat df = new DecimalFormat("#.00");

        double high = temp.getHigh();
        double low = temp.getLow();
        double lastVal = temp.getLast();
        double volumeVal = Double.parseDouble(df.format(temp.getVolume()));
        double bidVal = temp.getBid();
        double askVal = temp.getAsk();
        double current = Double.parseDouble(df.format((bidVal + askVal) / 2));
        DateData priceChanges = temp.getPriceChanges();
        DateData percentChanges = temp.getPercentChanges();
        double valChange = Double.parseDouble(df.format(priceChanges.getDay()));
        double percentChange = Double.parseDouble(df.format(percentChanges.getDay()));
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();

        StringBuilder sb = new StringBuilder();

        // All Labels updated
        dailyHigh.setText(Double.toString(high));
        dailyLow.setText(Double.toString(low));
        last.setText(Double.toString(lastVal));
        volume.setText(Double.toString(volumeVal));
        bid.setText(Double.toString(bidVal));
        ask.setText(Double.toString(askVal));
        currentPrice.setText("$" + Double.toString(current));
        if(valChange < 0) {
            dayChanges.setTextFill(Paint.valueOf("Red"));
            dayChanges.setText(Double.toString(valChange) + " (" + Double.toString(percentChange) + " %)");
        }
        else {
            dayChanges.setTextFill(Paint.valueOf("Green"));
            dayChanges.setText("+" + Double.toString(valChange) + " (+" + Double.toString(percentChange) + " %)");
        }

        lastUpdate.setText("As of " +  dtf.format(now));

        double currencyVals[] = new double[6];

        for(int i = 0; i < currencies.length; i++) {
            currencyVals[i] = Double.parseDouble(df.format(temp.convert(currencies[i], current)));
        }

        // All currencies updated
        gbp.setText(Double.toString(currencyVals[0]));
        eur.setText(Double.toString(currencyVals[1]));
        aud.setText(Double.toString(currencyVals[2]));
        jpy.setText(Double.toString(currencyVals[3]));
        cny.setText(Double.toString(currencyVals[4]));
        sar.setText(Double.toString(currencyVals[5]));

        // Update current chart
        updateChart(temp, true);

        // Reshuffle news
        updateNews(temp);



    }


    // Click handler for refreshButton
    public void refreshApp(ActionEvent actionEvent) {
        updateApplication();
    }

    // Updated the news box
    public void updateNews(Crypto temp) {
        List<NewsData> curNewsList  = temp.relevantArticles();
        newsList = new ArrayList<>(curNewsList);
        Collections.shuffle(newsList);

        news1.setText(newsList.get(0).getTitle());
        news2.setText(newsList.get(1).getTitle());
        news3.setText(newsList.get(2).getTitle());
        news4.setText(newsList.get(3).getTitle());
        news5.setText(newsList.get(4).getTitle());

        news1.setVisited(false);
        news2.setVisited(false);
        news3.setVisited(false);
        news4.setVisited(false);
        news5.setVisited(false);

    }

    // Handles click for opening article in browser
    public boolean openNewsArticle(ActionEvent event) throws MalformedURLException {
        Hyperlink h = (Hyperlink) event.getSource();
        String id = h.getId();
        URL url;

        if(id.contentEquals("news1")) {
            url = new URL(newsList.get(0).getUrl());
        }
        else if(id.contentEquals("news2")) {
            url = new URL(newsList.get(1).getUrl());
        }
        else if(id.contentEquals("news3")) {
            url = new URL(newsList.get(2).getUrl());
        }
        else if(id.contentEquals("news4")) {
            url = new URL(newsList.get(3).getUrl());
        }
        else {
            url = new URL(newsList.get(4).getUrl());
        }

        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(url.toURI());
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;

    }

    // Updates the current chart
    public void updateChart(Crypto temp, boolean isRefresh) {
        List<HistoricalData> historicalData;

        int tabSelected = graphTabPane.getSelectionModel().getSelectedIndex();
        StackedAreaChart cur;

        if(tabSelected == 0) {
            if(dayDone && !isRefresh) {
                return;
            }
            cur = dayGraph;
            historicalData = temp.getDailyHistory();
        }
        else if(tabSelected == 1) {
            if(monthDone && !isRefresh) {
                return;
            }
            cur = monthGraph;
            historicalData = temp.getMonthlyHistory();

        }
        else {
            if(allTimeDone && !isRefresh) {
                return;
            }
            cur = allTimeGraph;
            historicalData = temp.getAllTimeHistory();
        }

        XYChart.Series<Number, Number> seriesCrypto =
                new XYChart.Series<Number, Number>();

        cur.getData().clear();
        cur.getXAxis().setTickLabelsVisible(false);
        cur.getXAxis().setTickMarkVisible(false);
        ((Path)cur.getXAxis().lookup(".axis-minor-tick-mark")).setVisible(false);

        ArrayList<Double> yAxis = new ArrayList<>();

        for(int i = 0; i < historicalData.size(); i++) {
            yAxis.add(historicalData.get(i).getAverage());
        }

        double max = Collections.max(yAxis);
        double min = Collections.min(yAxis);

        if(tabSelected == 0) {
            dayY.setUpperBound(max * 1.10);
            dayY.setLowerBound(min * .9);
            dayY.setTickUnit(max/25);
            dayX.setLowerBound(0);
            dayX.setUpperBound(historicalData.size() + 20);
            dayX.setTickUnit((historicalData.size() + 20)/25.0);
            dayFirstLabel.setText(historicalData.get(0).getTime());
            daySecondLabel.setText(historicalData.get(historicalData.size() - 1).getTime());
            dayDone = true;
        }
        else if(tabSelected == 1) {
            monthY.setUpperBound(max * 1.10);
            monthY.setLowerBound(min * .9);
            monthY.setTickUnit(max/25);
            monthX.setLowerBound(0);
            monthX.setUpperBound(historicalData.size() + 20);
            monthX.setTickUnit((historicalData.size() + 20)/25.0);
            monthFirstLabel.setText(historicalData.get(0).getTime());
            monthSecondLabel.setText(historicalData.get(historicalData.size() - 1).getTime());
            monthDone = true;

        }
        else {
            allTimeY.setUpperBound(max * 1.10);
            allTimeY.setLowerBound(min * .9);
            allTimeY.setTickUnit(max/25);
            allTimeX.setLowerBound(0);
            allTimeX.setUpperBound(historicalData.size() + 20);
            allTimeX.setTickUnit((historicalData.size() + 20)/25.0);
            allTimeFirstLabel.setText(historicalData.get(0).getTime());
            allTimeSecondLabel.setText(historicalData.get(historicalData.size() - 1).getTime());
            allTimeDone = true;
        }




        for(int i = 0; i < historicalData.size(); i++) {
            seriesCrypto.getData().add(new XYChart.Data(i, yAxis.get(i)));
        }

        cur.getData().addAll(seriesCrypto);
        cur.setCreateSymbols(false);
        cur.setLegendVisible(false);
    }

    // Handles switching between bitcoin and litecoin
    public void switchCrypto(ActionEvent actionEvent) {

        Hyperlink temp = (Hyperlink) actionEvent.getSource();

        if(temp.getText().contentEquals(coinSelected)) {
            // Do nothing
        }
        else {
            coinSelected = temp.getText();
            nameTicker.setText(coinSelected);
            dayDone = false;
            monthDone = false;
            allTimeDone = false;
            updateApplication();
        }
    }
}
