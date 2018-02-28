package pingmonitor;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        Parent root = FXMLLoader.load(getClass().getResource("MainView.fxml"));
        primaryStage.setTitle("Ping Monitor");
        primaryStage.setScene(new Scene(root, 800, 500));
        primaryStage.show();

        showPreferencesDialog();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        System.exit(0);
    }

    private void showPreferencesDialog() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        Parent root = FXMLLoader.load(getClass().getResource("PreferencesDialog.fxml"));

        Stage dialogStage = new Stage();
        dialogStage.setTitle("Preferences");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(primaryStage);
        Scene scene = new Scene(root);
        dialogStage.setScene(scene);
        dialogStage.showAndWait();

    }

    public static void main(String[] args) {
        launch(args);
    }
}
