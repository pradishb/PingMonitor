package pingmonitor;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.*;

public class MainViewController implements Initializable, PreferencesUtils.PreferencesChangeListener {
    private XYChart.Series dataSeries;
    private Timer t;

    private final int X_COUNT = 200;

    private File myStyleClass = new File("style.css");

    private int sum;
    private int max;
    private int min;
    private int loss;

    private MyAreaChart chart;

    @FXML
    private GridPane gridPane;

    @FXML
    private Label avgPingLabel;

    @FXML
    private Label highestPingLabel;

    @FXML
    private Label lowestPingLabel;

    @FXML
    private Label lossPercentLabel;

    @FXML
    private Label packetLossLabel;

    @FXML
    private Label fluctuationLabel;

    @FXML
    private void onCloseClick(final ActionEvent e){
        Platform.exit();
    }

    @FXML
    private void onAboutClick(final ActionEvent e){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Ping Monitor v2");
        alert.setContentText("Created By : Pradish Bijukchhe");
        alert.setTitle("About");
        alert.showAndWait();
    }

    @FXML
    private void onPreferencesClick(final ActionEvent e) throws IOException {
        Parent root = FXMLLoader.load(Main.class.getResource("PreferencesDialog.fxml"));

        Stage dialogStage = new Stage();
        dialogStage.setTitle("Preferences");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(Main.primaryStage);
        Scene scene = new Scene(root);
        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        t = new Timer(true);
        dataSeries = new XYChart.Series();

        NumberAxis xAxis = new NumberAxis();
        chart = new MyAreaChart(xAxis, new NumberAxis());
        chart.getStylesheets().add(myStyleClass.toURI().toString());

        PreferencesUtils.addPreferencesChangeListener(this);



        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(0);
        xAxis.setUpperBound(X_COUNT - 1);
        xAxis.setTickUnit(1);
        xAxis.setTickMarkVisible(false);
        xAxis.setMinorTickCount(0);
        xAxis.setTickLabelsVisible(false);

        chart.getData().addAll(dataSeries);
        chart.setAnimated(false);
        chart.setLegendVisible(false);
        chart.setTitle("Live Ping Data");
        chart.setVerticalGridLinesVisible(false);
        chart.setCreateSymbols(false);

        gridPane.add(chart, 0, 1);
        PreferencesUtils.loadPersonDataFromFile();
    }

    @Override
    public void onPreferencesChange(MyPreferences prefs){
        t.cancel();
        t = new Timer(true);
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                update();
            }
        }, prefs.getTimeout() + 1000, prefs.getTimeout());

        Color color = prefs.getGraphColor();

        String rgb = String.format("%d, %d, %d",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));

        try {
            try (PrintWriter printWriter = new PrintWriter(myStyleClass)) {
                printWriter.println(".default-color0.chart-series-area-line { -fx-stroke: rgba(" + rgb + ", 1); }");
                printWriter.println(".default-color0.chart-series-area-fill { -fx-fill: rgba(" + rgb + ", .2)}");

                chart.getStylesheets().clear();
                chart.getStylesheets().add(myStyleClass.toURI().toString());
            }

        }
        catch (IOException e){
            new ExceptionDialog(e);
        }
    }

    private void update() {
        boolean isLoss = false;
        int ping;
        try {
            ping = PseudoPing.ping(PreferencesUtils.prefs.getHost(), PreferencesUtils.prefs.getTimeout());
        } catch (IOException e) {
            isLoss = true;
            try {
                ping = ((Data<Integer, Integer>) dataSeries.getData().get(0)).getYValue();
            }
            catch (IndexOutOfBoundsException ex){
                ping = 0;
            }
        }

        final Data<Integer, Integer> myData = new Data<>(0, ping, isLoss);
        Platform.runLater(() -> {
            ((ObservableList<Data<Integer, Integer>>) dataSeries.getData()).forEach(
                    (myItem) -> {
                        int oldXValue = myItem.getXValue();
                        myItem.setXValue(oldXValue + 1);
                    }
            );
            if (dataSeries.getData().size() > X_COUNT - 1) {
                dataSeries.getData().remove(X_COUNT - 1);
            }

            dataSeries.getData().add(0, myData);

            updateAnalytics();
        });
    }

    private void updateAnalytics(){
        sum = 0;
        max = Integer.MIN_VALUE;
        min = Integer.MAX_VALUE;
        loss = 0;

        ((ObservableList<Data<Integer, Integer>>)dataSeries.getData()).forEach(
            (data) -> {
                if ((boolean)data.getExtraValue()){
                    loss++;
                }
                else{
                    sum += data.getYValue();
                    if(data.getYValue() < min){
                        min = data.getYValue();
                    }
                    if(data.getYValue() > max){
                        max = data.getYValue();
                    }
                }
            }
        );
        if(min == Integer.MAX_VALUE){
            min = 0;
        }
        if(max == Integer.MIN_VALUE){
            max = 0;
        }


        int avg = ((dataSeries.getData().size() - loss == 0 ? 0 : sum / (dataSeries.getData().size() - loss)));
        int lossPer = (dataSeries.getData().size() == 0 ? 0 : loss * 100 / dataSeries.getData().size());

        avgPingLabel.setText("Average Ping: " + avg + "ms");
        highestPingLabel.setText("Highest Ping: " + max + "ms");
        lowestPingLabel.setText("Lowest Ping: " + min + "ms");
        lossPercentLabel.setText("Loss: " + lossPer + "%");
        packetLossLabel.setText("Packet Loss: " + loss + " / " + dataSeries.getData().size());
        fluctuationLabel.setText("Fluctuation: " + (max - min) + "ms");

    }
}
