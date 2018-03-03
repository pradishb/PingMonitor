package pingmonitor;

import javafx.scene.Group;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.Axis;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

public class MyAreaChart extends AreaChart<Number, Number> {
    MyAreaChart(Axis<Number> xAxis, Axis<Number> yAxis) {
        super(xAxis, yAxis);
    }

    @Override
    protected void layoutPlotChildren() {
        super.layoutPlotChildren();
        Path p = (Path) ((Group) getData().get(0).getNode()).getChildren().get(1);

        getData().get(0).getData().forEach(i -> {
                    if ((boolean) i.getExtraValue()) {
                        p.getElements().add(new MoveTo(getXAxis().getDisplayPosition(i.getXValue()), 0));
                        p.getElements().add(new LineTo(getXAxis().getDisplayPosition(i.getXValue()), getYAxis().getHeight()));
                    }
                }
        );

    }
}