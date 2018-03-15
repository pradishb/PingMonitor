package pingmonitor;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class MainViewController implements Initializable, PreferencesUtils.PreferencesChangeListener {
    private HashMap<String, Integer> values = new HashMap<>();

    private XYChart.Series<Integer, Integer> dataSeries;
    private Timer t;

    private final int X_COUNT = 200;

    private File myStyleClass = new File("style.css");

    private int sum;
    private int max;
    private int min;
    private int loss;

    private MyAreaChart chart;

    @FXML
    private BarChart<String, Integer> barChart;

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

    //analytics tab
    @FXML
    private ChoiceBox<String> rangeChoiceBox;

    @FXML
    private ChoiceBox<String> divideChoiceBox;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private TimeSpinner startTimeSpinner;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private TimeSpinner endTimeSpinner;

    @FXML
    private Button refreshBtn;

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
        dataSeries = new XYChart.Series<>();

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

        chart.getData().addAll((XYChart.Series) dataSeries);
        chart.setAnimated(false);
        chart.setLegendVisible(false);
        chart.setTitle("Live Ping Data");
        chart.setVerticalGridLinesVisible(false);
        chart.setCreateSymbols(false);

        barChart.setLegendVisible(false);
        barChart.setAnimated(false);

        rangeChoiceBox.getItems().add("Last 5 minutes");
        rangeChoiceBox.getItems().add("Last 30 minutes");
        rangeChoiceBox.getItems().add("Today");
        rangeChoiceBox.getItems().add("Last Week");
        rangeChoiceBox.getItems().add("Last Month");
        rangeChoiceBox.getItems().add("Custom");
        rangeChoiceBox.setValue("Last 5 minutes");

        values.put("20 seconds", 20);
        values.put("1 minute", 60);
        values.put("5 minutes", 60*5);
        values.put("1 hour", 60*60);
        values.put("1 day", 60*60*24);

        changeStartAndEnd(0);
        changeDivideBy();

        //listeners
        ChangeListener<Number> divideByListener = (x, y, z) -> {
            if(z.intValue() != -1)
                changeBarChart(values.get(divideChoiceBox.getItems().get(z.intValue())));
        };

        rangeChoiceBox.getSelectionModel().selectedIndexProperty().addListener((x, y, z) -> {
            changeStartAndEnd(z);
            divideChoiceBox.getSelectionModel().selectedIndexProperty().removeListener(divideByListener);
            changeDivideBy();
            divideChoiceBox.getSelectionModel().selectedIndexProperty().addListener(divideByListener);
        });

        refreshBtn.setOnAction(x -> {
            changeStartAndEnd(rangeChoiceBox.getItems().indexOf(rangeChoiceBox.getValue()));
            divideChoiceBox.getSelectionModel().selectedIndexProperty().removeListener(divideByListener);
            changeDivideBy();
            divideChoiceBox.getSelectionModel().selectedIndexProperty().addListener(divideByListener);
        });

        divideChoiceBox.getSelectionModel().selectedIndexProperty().addListener(divideByListener);

        gridPane.add(chart, 0, 0, 2, 1);
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
                ping = (dataSeries.getData().get(0)).getYValue();
            }
            catch (IndexOutOfBoundsException ex){
                ping = 0;
            }
        }

        if(isLoss){
            SQLiteJDBCDriverConnection.insertLoss(new Timestamp(System.currentTimeMillis()));
        }
        else{
            SQLiteJDBCDriverConnection.insertPing(new Timestamp(System.currentTimeMillis()), ping);
        }



        final Data<Integer, Integer> myData = new Data<>(0, ping, isLoss);
        Platform.runLater(() -> {
            (dataSeries.getData()).forEach(
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

        (dataSeries.getData()).forEach(
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

    private void changeStartAndEnd(Number index){

        LocalDate nowDate = LocalDate.now();
        LocalTime nowTime = LocalTime.now();
        LocalDateTime nowDateTime = LocalDateTime.of(nowDate, nowTime);
        ChangeListener<LocalTime> timeListener = (x, y, z) -> changeDivideBy();
        ChangeListener<LocalDate> dateListener = (x, y, z) -> changeDivideBy();

        if(rangeChoiceBox.getItems().get(index.intValue()).equals("Custom")){
            startDatePicker.setDisable(false);
            startTimeSpinner.setDisable(false);
            endDatePicker.setDisable(false);
            endTimeSpinner.setDisable(false);
            refreshBtn.setDisable(true);

            startTimeSpinner.valueProperty().addListener(timeListener);
            endTimeSpinner.valueProperty().addListener(timeListener);
            startDatePicker.valueProperty().addListener(dateListener);
            endDatePicker.valueProperty().addListener(dateListener);
        }
        else {
            startDatePicker.setDisable(true);
            startTimeSpinner.setDisable(true);
            endDatePicker.setDisable(true);
            endTimeSpinner.setDisable(true);
            refreshBtn.setDisable(false);

            startTimeSpinner.valueProperty().removeListener(timeListener);
            endTimeSpinner.valueProperty().removeListener(timeListener);
            startDatePicker.valueProperty().removeListener(dateListener);
            endDatePicker.valueProperty().removeListener(dateListener);

            switch (rangeChoiceBox.getItems().get(index.intValue())) {
                case "Last 5 minutes":
                    startDatePicker.setValue(nowDateTime.minusMinutes(5).toLocalDate());
                    endDatePicker.setValue(nowDate);
                    startTimeSpinner.getValueFactory().setValue(nowTime.minusMinutes(5));
                    endTimeSpinner.getValueFactory().setValue(nowTime);
                    break;

                case "Last 30 minutes":
                    startDatePicker.setValue(nowDateTime.minusMinutes(30).toLocalDate());
                    endDatePicker.setValue(nowDate);
                    startTimeSpinner.getValueFactory().setValue(nowTime.minusMinutes(30));
                    endTimeSpinner.getValueFactory().setValue(nowTime);
                    break;

                case "Today":
                    startDatePicker.setValue(nowDate);
                    endDatePicker.setValue(nowDate);
                    startTimeSpinner.getValueFactory().setValue(LocalTime.of(0, 0));
                    endTimeSpinner.getValueFactory().setValue(nowTime);
                    break;

                case "Last Week":
                    startDatePicker.setValue(nowDate.minusWeeks(1));
                    endDatePicker.setValue(nowDate);
                    startTimeSpinner.getValueFactory().setValue(LocalTime.of(0, 0));
                    endTimeSpinner.getValueFactory().setValue(nowTime);
                    break;

                case "Last Month":
                    startDatePicker.setValue(nowDate.minusMonths(1));
                    endDatePicker.setValue(nowDate);
                    startTimeSpinner.getValueFactory().setValue(LocalTime.of(0, 0));
                    endTimeSpinner.getValueFactory().setValue(nowTime);
                    break;
            }
        }
    }

    private void changeDivideBy(){
        int minNumber = 1;
        int maxNumber = 50;

        LocalDateTime start = LocalDateTime.of(startDatePicker.getValue(), startTimeSpinner.getValue());
        LocalDateTime end = LocalDateTime.of(endDatePicker.getValue(), endTimeSpinner.getValue());

        Duration duration = Duration.between(start, end);

        divideChoiceBox.getItems().clear();
        for(String key: values.keySet()){
            int numberOfValues = (int)duration.getSeconds()/values.get(key);

            if(numberOfValues <= maxNumber && numberOfValues >= minNumber){
                divideChoiceBox.getItems().add(key);
            }
        }

        if(divideChoiceBox.getItems().size()== 0){
            divideChoiceBox.getItems().add("None");
        }
        divideChoiceBox.setValue(divideChoiceBox.getItems().get(0));
        changeBarChart(values.get(divideChoiceBox.getItems().get(0)));
    }

    private void changeBarChart(int divider){
        XYChart.Series<String, Integer> series = new XYChart.Series<>();

        LocalDateTime start = LocalDateTime.of(startDatePicker.getValue(), startTimeSpinner.getValue());
        LocalDateTime end = LocalDateTime.of(endDatePicker.getValue(), endTimeSpinner.getValue());

        while(start.compareTo(end) <= 0){
            int avgPing = SQLiteJDBCDriverConnection.getRangeValue(Timestamp.valueOf(start),
                    Timestamp.valueOf(start.plusSeconds(divider)));
//            System.out.print(avgPing);
            if(divider < 60){               //seconds
                series.getData().add(new Data<>(start.format(DateTimeFormatter.ofPattern("m:ss")), avgPing));
            }
            else if(divider < 60*60){      //minutes
                series.getData().add(new Data<>(start.format(DateTimeFormatter.ofPattern("h:mm")), avgPing));
            }
            else if(divider < 60*60*24){    //hour
                series.getData().add(new Data<>(start.format(DateTimeFormatter.ofPattern("MMM d, h:00")), avgPing));
            }
            else if(divider < 60*60*24*7) { //week-day
                series.getData().add(new Data<>(start.format(DateTimeFormatter.ofPattern("MMM d, EEE")), avgPing));
            }
            else if(divider < 60*60*24*7*30) {  //month-day
                series.getData().add(new Data<>(start.format(DateTimeFormatter.ofPattern("MMM d")), avgPing));
            }
            else {
                series.getData().add(new Data<>(start.toString(), avgPing));
            }


            start = start.plusSeconds(divider);
//            System.out.println("      " + start + "      " + end);
        }
        System.out.println();



        barChart.getData().clear();
        barChart.getData().addAll((XYChart.Series) series);

        for (final XYChart.Series<String, Integer> s : barChart.getData()) {
            for (final Data<String, Integer> data : s.getData()) {
                Tooltip tooltip = new Tooltip();
                tooltip.setText("Avg Ping : " + data.getYValue().toString());
                Tooltip.install(data.getNode(), tooltip);
            }
        }
    }
}
