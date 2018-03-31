import org.jsoup.Jsoup;


import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Queue;
import java.util.Scanner;
import java.util.UUID;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class FileUtility {


    public static ArrayList<String> readFileToStringArray(String fileName)
    {
        ArrayList<String>lines=new ArrayList<>();

        File file=new File(fileName);
        String line;
        Scanner fileScanner= null;
        try {
            fileScanner = new Scanner(file);
            while(fileScanner.hasNext())
            {
              line=fileScanner.nextLine();
              lines.add(line);
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


       return lines;
    }






}
