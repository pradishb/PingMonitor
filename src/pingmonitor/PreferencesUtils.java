package pingmonitor;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class PreferencesUtils {
    static MyPreferences prefs = new MyPreferences();
    private static final String PREFERENCES_PATH = "preferences.xml";
    private static List<PreferencesChangeListener> listeners = new ArrayList<>();

    public static void savePersonDataToFile() {
        try {
            JAXBContext contextObj = JAXBContext.newInstance(MyPreferences.class);

            Marshaller marshallerObj = contextObj.createMarshaller();
            marshallerObj.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            marshallerObj.marshal(prefs, new FileOutputStream(PREFERENCES_PATH));
            listeners.forEach((x) -> x.onPreferencesChange(prefs));
        } catch (Exception e) {
            new ExceptionDialog(e);
        }
    }

    public static void loadPersonDataFromFile() {
        try{
            File file = new File(PREFERENCES_PATH);
            JAXBContext jaxbContext = JAXBContext.newInstance(MyPreferences.class);

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            prefs = (MyPreferences) jaxbUnmarshaller.unmarshal(file);
            listeners.forEach((x) -> x.onPreferencesChange(prefs));
        }
        catch (UnmarshalException e){
            savePersonDataToFile();
            loadPersonDataFromFile();
        }
        catch (Exception e){
            new ExceptionDialog(e);
        }
    }

    interface PreferencesChangeListener{
        void onPreferencesChange(MyPreferences prefs);
    }

    public static void addPreferencesChangeListener(PreferencesChangeListener listener){
        listeners.add(listener);
    }

    public static void removePreferencesChangeListener(PreferencesChangeListener listener){
        listeners.remove(listener);
    }
}
