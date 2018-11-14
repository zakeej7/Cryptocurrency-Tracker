import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTabPane;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Path;

import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

// Handles all the GUI events
public class Controller
{
    // Currencies and Cryptos
    private String selectedCurrency;
    private Crypto selectedCrypto;

    private String[] currencies = {"USD", "GBP", "EUR", "AUD", "JPY", "CNY", "SAR"};

    private ObservableList<String> cryptos = FXCollections.observableArrayList(
            "BTC - Bitcoin", "LTC - Litecoin", "BCH - Bitcoin Cash", "ETH - Ethereum",
            "XRP - Ripple", "XMR - Monero");

    private FilteredList<String> filteredList = new FilteredList<>(cryptos);

    // Graph Done
    private boolean dayDone = false;
    private boolean monthDone = false;
    private boolean allTimeDone = false;

    // Listview
    @FXML private ListView myListView;
    @FXML private TextField searchCrypto;

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
    @FXML private Label usd;

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


    // News
    @FXML private Hyperlink news1;
    @FXML private Hyperlink news2;
    @FXML private Hyperlink news3;
    @FXML private Hyperlink news4;
    @FXML private Hyperlink news5;
    private List<NewsData> newsList;


    // Load the application with data already in it
    // Setup listeners for tab switching
    public void initialize() {

        graphTabPane.getSelectionModel().selectedItemProperty().addListener((ov, oldTab, newTab) -> {

           selectedCrypto.updateCrypto();
           updateChart(selectedCrypto, false);
        });

        searchCrypto.textProperty().addListener((observable, oldValue, newValue) ->  {
            String filter = searchCrypto.getText();
            if (filter == null || filter.length() == 0) {
                filteredList.setPredicate(s -> true);
            } else {
                filteredList.setPredicate(s -> s.toLowerCase().contains(filter.toLowerCase()));
            }
        });

        myListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if(newValue == null) {
                    return;
                }
                nameTicker.setText(newValue);
                String crypto = newValue.substring(0,3) + "USD";
                selectedCrypto = new Crypto(crypto);

                dayDone = false;
                monthDone = false;
                allTimeDone = false;

                updateApplication();
            }
        });

        myListView.setItems(filteredList);
        myListView.getSelectionModel().select(3);
        myListView.getFocusModel().focus(3);
        myListView.scrollTo(3);


    }


    // Updates the GUI with new information
    public void updateApplication() {

        selectedCrypto.updateCrypto();

        DecimalFormat df = new DecimalFormat("#.00");

        double high = selectedCrypto.getHigh();
        double low = selectedCrypto.getLow();
        double lastVal = selectedCrypto.getLast();
        double volumeVal = Double.parseDouble(df.format(selectedCrypto.getVolume()));
        double bidVal = selectedCrypto.getBid();
        double askVal = selectedCrypto.getAsk();
        double current = Double.parseDouble(df.format((bidVal + askVal) / 2));
        DateData priceChanges = selectedCrypto.getPriceChanges();
        DateData percentChanges = selectedCrypto.getPercentChanges();
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

        double currencyVals[] = new double[7];

        for(int i = 0; i < currencies.length; i++) {
            currencyVals[i] = Double.parseDouble(df.format(selectedCrypto.convert(currencies[i], current)));
        }

        // All currencies updated
        usd.setText(Double.toString(currencyVals[0]));
        gbp.setText(Double.toString(currencyVals[1]));
        eur.setText(Double.toString(currencyVals[2]));
        aud.setText(Double.toString(currencyVals[3]));
        jpy.setText(Double.toString(currencyVals[4]));
        cny.setText(Double.toString(currencyVals[5]));
        sar.setText(Double.toString(currencyVals[6]));


        // Update current chart
        updateChart(selectedCrypto, true);

        // Reshuffle news
        updateNews(selectedCrypto);

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
}
