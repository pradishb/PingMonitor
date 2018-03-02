package pingmonitor;

import javafx.scene.paint.Color;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Helper class to wrap a list of persons. This is used for saving the
 * list of persons to XML.
 *
 * @author Marco Jakob
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "preferences")
public class MyPreferences {
    private String host;
    private int timeout;

    @XmlJavaTypeAdapter(ColorAdapter.class)
    private Color graphColor;

    MyPreferences() {
        host = "google.com";
        timeout = 150;
        graphColor = new Color(.2, .5, 1, 1.0);
    }

    public MyPreferences(String host, int timeout, Color graphColor) {
        this.host = host;
        this.timeout = timeout;
        this.graphColor = graphColor;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public Color getGraphColor() {
        return graphColor;
    }

    public void setGraphColor(Color graphColor) {
        this.graphColor = graphColor;
    }
}
