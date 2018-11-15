import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

public class PersistentData {
    private String filename;
    private String crypto;
    private String currency;
    private String timePeriod;
    private int index;

    // constructor to load data
    public PersistentData() {
        // load data
        filename = "data.csv";
        loadData();
    }

    // constructor to save data
    public PersistentData(String crypto, String currency, String timePeriod, int index) {
        filename = "data.csv";
        this.crypto = crypto;
        this.currency = currency;
        this.timePeriod = timePeriod;
        this.index = index;
        saveData();
    }

    // getters
    public String getCrypto() {
        return crypto;
    }

    public String getCurrency() {
        return currency;
    }

    public String getTimePeriod() {
        return timePeriod;
    }

    public int getIndex() {
        return index;
    }


    // checks if the given file exists
    private boolean fileExists(String fileName) {
        File f = new File(fileName);
        return (f.exists() && !f.isDirectory());
    }

    // saves all data to the file
    public void saveData() {
        // before opening the file check to see if it already exists
        boolean alreadyExists = new File(filename).exists();

        try {
            // use FileWriter constructor that specifies open for appending
            CsvWriter csvOutput = new CsvWriter(new FileWriter(filename, true), ',');

            // if the file didn't already exist then need to write out the header line
            if (!alreadyExists) {
                csvOutput.write("crypto");
                csvOutput.write("currency");
                csvOutput.write("time");
                csvOutput.write("index");
                csvOutput.endRecord();
            }

            csvOutput.write(crypto);
            csvOutput.write(currency);
            csvOutput.write(timePeriod);
            csvOutput.write(Integer.toString(index));
            csvOutput.endRecord();

            csvOutput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // loads the data from the file
    private void loadData() {
        if (!fileExists(filename)) {
            return;
        }

        // read from the file
        try {
            CsvReader data = new CsvReader(filename);
            data.readHeaders();

            // parse the data
            while (data.readRecord()) {
                crypto = data.get("crypto");
                currency = data.get("currency");
                timePeriod = data.get("time");
                index = Integer.parseInt(data.get("index"));
            }
        } catch (FileNotFoundException exception) {
            exception.printStackTrace();
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}