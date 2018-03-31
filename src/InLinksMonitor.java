import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class InLinksMonitor {


    public static ConcurrentHashMap<String,Set<String>> inLinks=new ConcurrentHashMap<>();


    public static void serializeInlinks(ObjectOutputStream os)
    {
        try {
            os.writeObject(inLinks);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void deserializeInlinks(ObjectInputStream is)
    {
        try {
            inLinks=(ConcurrentHashMap<String,Set<String>>)is.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
