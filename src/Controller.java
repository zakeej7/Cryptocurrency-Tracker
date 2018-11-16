import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTabPane;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Path;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;
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
    private String curCrypto;
    private Crypto selectedCrypto;
    private int curCryptoIndex;

    // Currency Symbols
    private static final String GBP = "\u00A3";
    private static final String EUR = "\u20AC";
    private static final String USD = "$";
    private static final String AUD = "A$";
    private static final String JPY = "\u00A5";
    private static final String SAR = "SR ";


    private String[] currencies = {"USD", "GBP", "EUR", "AUD", "JPY", "CNY", "SAR"};

    private ObservableList<String> cryptos = FXCollections.observableArrayList(
            "BTC - Bitcoin", "LTC - Litecoin", "BCH - Bitcoin Cash", "ETH - Ethereum",
            "XRP - Ripple", "XMR - Monero", "ZEC - ZCash");

    private ObservableList<String> options = FXCollections.observableArrayList("USD", "GBP", "EUR", "AUD", "JPY", "CNY", "SAR");

    private FilteredList<String> filteredList = new FilteredList<>(cryptos);

    // Graph Done
    private boolean dayDone = false;
    private boolean monthDone = false;
    private boolean allTimeDone = false;

    // Listview
    @FXML private ListView myListView;
    @FXML private TextField searchCrypto;

    // Currency box
    @FXML private ChoiceBox currencyChoice;

    // Main tabs
    @FXML private JFXTabPane mainTabPane;
    @FXML private JFXTabPane graphTabPane;

    // Top bar
    @FXML private Label nameTicker;
    @FXML private Label currentPrice;
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

                curCryptoIndex = myListView.getSelectionModel().getSelectedIndex();
                curCrypto = newValue.substring(0,3);
                nameTicker.setText(newValue);
                String crypto = curCrypto + selectedCurrency;
                selectedCrypto = new Crypto(crypto);

                dayDone = false;
                monthDone = false;
                allTimeDone = false;

                updateApplication();
            }
        });

        currencyChoice.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

            public void changed(ObservableValue<? extends String> ov, String oldValue, String newValue) {

                if(newValue == null) {
                    return;
                }

                selectedCurrency = newValue;

                String crypto = curCrypto.substring(0,3) + selectedCurrency;
                selectedCrypto = new Crypto(crypto);

                dayDone = false;
                monthDone = false;
                allTimeDone = false;

                updateApplication();
            }
        });

        // Set Listview and Options Values
        myListView.setItems(filteredList);
        currencyChoice.setItems(options);

        PersistentData p = new PersistentData();

        if(p.getCrypto() != null) {
            curCrypto = p.getCrypto();
            selectedCurrency = p.getCurrency();
            curCryptoIndex = p.getIndex();

            myListView.getSelectionModel().select(curCryptoIndex);
            currencyChoice.setValue(selectedCurrency);
            graphTabPane.getSelectionModel().select(p.getTimePeriod());
        }
        else {
            curCrypto = "BTC";
            selectedCurrency = "USD";
            curCryptoIndex = 0;

            myListView.getSelectionModel().select(0);
            currencyChoice.setValue("USD");

        }

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
        volume.setText(df.format(volumeVal));
        bid.setText(Double.toString(bidVal));
        ask.setText(Double.toString(askVal));
        if(valChange <= 0) {
            currentPrice.setTextFill(Paint.valueOf("Red"));
            currentPrice.setText(getCurrencySymbol() + Double.toString(current) + "  " + Double.toString(valChange) + " (" + Double.toString(percentChange) + " %)");
        }
        else {
            currentPrice.setTextFill(Paint.valueOf("Green"));
            currentPrice.setText(getCurrencySymbol() + Double.toString(current) + "  +" + Double.toString(valChange) + " (+" + Double.toString(percentChange) + " %)");
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
        String coinType = curCrypto;  // Current selected Crypto for which we pulls news articles on
        String coinName = cryptos.get(curCryptoIndex).replaceAll("\\s","").split("-")[1];

        if(coinType.equals("ZEC") || coinType.equals("LTC") || coinType.equals("ETH")) {
            List<NewsData> nameList = temp.relevantArticles(coinName);
            newsList = new ArrayList<>(nameList);
        }
        else {
            List<NewsData> typeList  = temp.relevantArticles(coinType);
            newsList = new ArrayList<>(typeList);
        }

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
    public void openNewsArticle(ActionEvent event) throws MalformedURLException {
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

//        Parent root;
//        try {
//            root = FXMLLoader.load(getClass().getClassLoader().getResource("mainWindow.fxml"));
//            Stage stage = new Stage();
//            stage.setTitle("My New Stage Title");
//            stage.setScene(new Scene(root, 450, 450));
//            stage.show();
//        }
//        catch (IOException e) {
//            e.printStackTrace();
//        }

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
            dayFirstLabel.setText("From: " + historicalData.get(0).getTime());
            daySecondLabel.setText("To: " + historicalData.get(historicalData.size() - 1).getTime());
            dayDone = true;
        }
        else if(tabSelected == 1) {
            monthY.setUpperBound(max * 1.10);
            monthY.setLowerBound(min * .9);
            monthY.setTickUnit(max/25);
            monthX.setLowerBound(0);
            monthX.setUpperBound(historicalData.size() + 20);
            monthX.setTickUnit((historicalData.size() + 20)/25.0);
            monthFirstLabel.setText("From: " + historicalData.get(0).getTime());
            monthSecondLabel.setText("To: " + historicalData.get(historicalData.size() - 1).getTime());
            monthDone = true;

        }
        else {
            allTimeY.setUpperBound(max * 1.10);
            allTimeY.setLowerBound(min * .9);
            allTimeY.setTickUnit(max/25);
            allTimeX.setLowerBound(0);
            allTimeX.setUpperBound(historicalData.size() + 20);
            allTimeX.setTickUnit((historicalData.size() + 20)/25.0);
            allTimeFirstLabel.setText("From: " + historicalData.get(0).getTime());
            allTimeSecondLabel.setText("To: " + historicalData.get(historicalData.size() - 1).getTime());
            allTimeDone = true;
        }




        for(int i = 0; i < historicalData.size(); i++) {
            seriesCrypto.getData().add(new XYChart.Data(i, yAxis.get(i)));
        }

        cur.getData().addAll(seriesCrypto);
        cur.setCreateSymbols(false);
        cur.setLegendVisible(false);
    }

    private String getCurrencySymbol() {
        if(selectedCurrency.equals("USD")) {
            return USD;
        }
        else if(selectedCurrency.equals("JPY") || selectedCurrency.equals("CNY")) {
            return JPY;
        }
        else if(selectedCurrency.equals("EUR")) {
            return EUR;
        }
        else if(selectedCurrency.equals("GBP")) {
            return GBP;
        }
        else if(selectedCurrency.equals("SAR")) {
            return SAR;
        }
        else {
            return AUD;
        }
    }

    public void saveChanges() {
        PersistentData p = new PersistentData(curCrypto, selectedCurrency, graphTabPane.getSelectionModel().getSelectedIndex(), curCryptoIndex);
    }

    public void exitHandler() {
        Platform.exit();

    }

}
