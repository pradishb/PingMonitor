package pingmonitor;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileOutputStream;

public class PreferencesUtils {
    static MyPreferences prefs = new MyPreferences();
    private static final String PREFERENCES_PATH = "preferences.xml";

    public static void savePersonDataToFile() {
        try {
            JAXBContext contextObj = JAXBContext.newInstance(MyPreferences.class);

            Marshaller marshallerObj = contextObj.createMarshaller();
            marshallerObj.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            marshallerObj.marshal(prefs, new FileOutputStream(PREFERENCES_PATH));
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
        }
        catch (UnmarshalException e){
            savePersonDataToFile();
            loadPersonDataFromFile();
        }
        catch (Exception e){
            new ExceptionDialog(e);
        }
    }
}
