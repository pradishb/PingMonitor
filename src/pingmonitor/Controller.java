package pingmonitor;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Controller implements Initializable{
    private XYChart.Series dataSeries;

    private final int X_COUNT = 30;

    @FXML
    AreaChart<Integer, Integer> chart;

    @FXML
    NumberAxis xAxis;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(0);
        xAxis.setUpperBound(X_COUNT - 1);
        xAxis.setTickUnit(1);

        dataSeries = new XYChart.Series();

        Timer t = new Timer();

        t.schedule(new TimerTask() {
            @Override
            public void run() {
                update();
            }
        }, 0, 1000);

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

        chart.getData().addAll(dataSeries);
        chart.setAnimated(false);
        chart.setLegendVisible(false);
        chart.setTitle("Ping Monitor");
    }

    private void update(){
        Platform.runLater(() -> {
            ((ObservableList<Data<Integer, Integer>>)dataSeries.getData()).forEach(
                (myItem) -> {
                    int oldXValue = myItem.getXValue();
                    myItem.setXValue(oldXValue + 1);
                }
            );
            if(dataSeries.getData().size() > X_COUNT - 1){
                dataSeries.getData().remove(X_COUNT - 1);
            }

            int ping;
            try {
                ping = PseudoPing.ping("google.com", 1000);
            }
            catch (IOException e){
                ping = 0;
            }
            dataSeries.getData().add(0, new Data<>(0, ping));
        });
    }
}
