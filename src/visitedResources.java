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
import java.util.concurrent.ExecutorService;

public class visitedResources implements Serializable {


    //A set for storing the UUID of already visited URLs to avoid visiting them again
   // static Set<String> visitedLinks = new TreeSet<>();

    static Set<String>visitedLinks=Collections.newSetFromMap(new ConcurrentHashMap<>());
    public static  void updateVisited(String uuid) {
        visitedLinks.add(uuid);
    }


    public static  boolean isVisited(String uuid) {
        return visitedLinks.contains(uuid);
    }


    public static Set<String> getVisited() {
        return visitedLinks;
    }


    public static void serializeResources(ObjectOutputStream os) {

        try {

            os.writeObject(visitedLinks);


        } catch (IOException e) {
            e.printStackTrace();
        } catch (ConcurrentModificationException e) {
            e.printStackTrace();
        }
    }

    public static void deSerializeResources(ObjectInputStream is) {

        try {
            try {

                visitedLinks = (Set<String>) is.readObject();

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}


