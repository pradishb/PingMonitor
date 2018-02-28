package pingmonitor;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

import java.net.URL;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Controller implements Initializable{
    XYChart.Series dataSeries;

    final int X_COUNT = 30;

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
    }

    public void update(){
        ObservableList<XYChart.Data<Integer, Integer>> myData = dataSeries.getData();

        myData.forEach(
                (myItem) -> {
                    int oldXValue = myItem.getXValue();
                    myItem.setXValue(oldXValue + 1);
                }
        );

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if(dataSeries.getData().size() > X_COUNT - 1){
                    dataSeries.getData().remove(0);
                }
                dataSeries.getData().add(new XYChart.Data(0, new Random().nextInt(10)));
            }
        });
    }
}
