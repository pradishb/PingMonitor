package pingmonitor;

import javafx.scene.paint.Color;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class ColorAdapter extends XmlAdapter<String, Color> {
    public Color unmarshal(String val) {
        return Color.web(val);
    }

    public String marshal(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }
}
