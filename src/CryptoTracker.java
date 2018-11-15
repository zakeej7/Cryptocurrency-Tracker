import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class CryptoTracker extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("mainWindow.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Crypto Tracker");
        primaryStage.setScene(new Scene(root, 1200, 700));
        Controller c = loader.getController();
        primaryStage.show();

        primaryStage.setOnHidden(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                c.saveChanges();
            }
        });
    }


    public static void main(String[] args) {
        launch(args);
    }
}
