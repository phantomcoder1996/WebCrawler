import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Vector;

public class DownloadedMonitor {


    static Vector<String> downloaded=new Vector<>();


    public static synchronized void addDownloaded(String hashLink)
    {
        downloaded.add(hashLink);
    }

    public static synchronized boolean isDownloaded(String hashLink)
    {
        return downloaded.contains(hashLink);
    }


    public static  void  serializeDownloaded(ObjectOutputStream os)
    {
        try {
            os.writeObject(downloaded);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static  void deserializeDownloaded(ObjectInputStream is)
    {
        try {
            downloaded=(Vector<String>)is.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
