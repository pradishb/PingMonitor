package pingmonitor;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.net.URL;
import java.util.ResourceBundle;

public class PreferencesDialogController implements Initializable {

    @FXML
    TextField hostText;

    @FXML
    TextField timeoutText;

    @FXML
    ColorPicker graphColorPicker;

    @FXML
    private void onSaveClick(final ActionEvent e) {
        try {
            PreferencesUtils.prefs.setTimeout(Integer.parseInt(timeoutText.getText()));
            PreferencesUtils.prefs.setHost(hostText.getText());
            PreferencesUtils.prefs.setGraphColor(graphColorPicker.getValue());
            PreferencesUtils.savePersonDataToFile();
            ((Stage)((Node)(e.getSource())).getScene().getWindow()).close();
        }
        catch (NumberFormatException ex){
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setContentText("Invalid Values");
            error.showAndWait();
        }

    }

    @FXML
    private void onCancelClick(final ActionEvent e) {
        ((Stage)((Node)(e.getSource())).getScene().getWindow()).close();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        hostText.setText(PreferencesUtils.prefs.getHost());
        timeoutText.setText(Integer.toString(PreferencesUtils.prefs.getTimeout()));
        graphColorPicker.setValue(PreferencesUtils.prefs.getGraphColor());
    }
}
