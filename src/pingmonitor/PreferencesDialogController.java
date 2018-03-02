package pingmonitor;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class PreferencesDialogController implements Initializable {

    @FXML
    TextField hostText;

    @FXML
    TextField timeoutText;

    @FXML
    ColorPicker graphColorPicker;

    @FXML
    private void onSaveClick(final ActionEvent e) {
        PreferencesUtils.prefs.setHost(hostText.getText());
        PreferencesUtils.prefs.setGraphColor(graphColorPicker.getValue());
        try {
            PreferencesUtils.prefs.setTimeout(Integer.parseInt(timeoutText.getText()));
            PreferencesUtils.savePersonDataToFile();
        }
        catch (NumberFormatException ex){
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setContentText("Invalid Values");
            error.showAndWait();
        }
    }

    @FXML
    private void onCancelClick(final ActionEvent e) {

    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        hostText.setText(PreferencesUtils.prefs.getHost());
        timeoutText.setText(Integer.toString(PreferencesUtils.prefs.getTimeout()));
        graphColorPicker.setValue(PreferencesUtils.prefs.getGraphColor());
    }
}
