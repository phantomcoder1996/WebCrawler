import javafx.util.Pair;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;

public class Resources implements Serializable {


    public static ConcurrentHashMap<String, String> simHash = new ConcurrentHashMap<>();
    public static ArrayList<String> noRevisit = new ArrayList<>();
    static ConcurrentLinkedQueue<String> linkQueue2 = new ConcurrentLinkedQueue<>();


    public static boolean isQueueEmpty2() {

        return linkQueue2.isEmpty();

    }

    /*........................*/

    public static void addLinkToQueue2(String url) {
        linkQueue2.add(url);
    }

    /*........................*/
    public static String getLink2() {
        String front = null;
        if (!linkQueue2.isEmpty()) {
            front = linkQueue2.poll();
        }
        return front;
    }


    public static void serializeResources(ObjectOutputStream os) {

        try {

            os.writeObject(linkQueue2);

            os.writeObject(simHash);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ConcurrentModificationException e) {
            e.printStackTrace();
        }
    }

    public static void deSerializeResources(ObjectInputStream is) {

        try {
            try {

                linkQueue2 = (ConcurrentLinkedQueue<String>) is.readObject();

                simHash = (ConcurrentHashMap<String, String>) is.readObject();

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
