import org.jsoup.Jsoup;

import java.io.InputStreamReader;
import java.net.*;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
public class LinkParser {

    public static String parseLink(URL pLink,String childLink)
    {

        String completeURL;

        if(childLink.startsWith("http")||childLink.startsWith("https"))
            completeURL=childLink;
        else if(childLink.startsWith("./"))
        {
            completeURL= pLink.getProtocol()+"://"+pLink.getHost()+childLink.substring(1);
        }
        else if(childLink.startsWith("//"))
        {
            completeURL=pLink.getProtocol()+":"+childLink;
        }
        else if(childLink.startsWith("../"))
        {
            completeURL=pLink.getProtocol()+"://"+pLink.getHost()+"/"+childLink;
        }
        else
            completeURL="";


        return completeURL;



    }

    public static String getDocumentType(HttpURLConnection urlC)
    {
        return urlC.getContentType().toString();
    }


    public static String hashLink(String url)
    {
        return UUID.nameUUIDFromBytes(url.getBytes()).toString();
    }


    public static boolean isBaseURL(String url)
    {
        return url.matches("https?:\\/\\/.+\\.[a-zA-Z]+\\/?");
    }

    public static boolean linksToDirectory(String url,String dir)
    {
        if(dir.contains("*")||dir.contains("$"))
        {
            //construct the pattern
            dir=dir.replace("*",".*");
            StringBuilder s1=new StringBuilder(dir);
            if(s1.indexOf(".*")!=0)
                s1.insert(0,".*");


            Pattern p1=Pattern.compile("https?://.+.[a-zA-Z]+/"+s1.toString());
            //  System.out.println(p1.pattern());

            Matcher m=p1.matcher(url);

            return m.matches();
        }
        else

            return url.contains(dir);


    }


    public static boolean isValid(String url)
    {
        /* Try creating a valid URL */
        try {


            Document d= Jsoup.connect(url).get();
            return true;
        }

        // If there was an Exception
        // while creating URL object
        catch (Exception e) {
            return false;
        }
    }


    public static String getBaseUrl(String url)
    {
        String baseURL="";
        try {
            URL myUrl=new URL(url);
            baseURL="";
            String authority=myUrl.getHost();
            String protocol=myUrl.getProtocol();
            baseURL+=protocol;
            baseURL+="://";
            baseURL+=authority;
          //  System.out.println(baseURL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return baseURL;

    }

    public static String normalize(String myURL)
    {

        try {

            URL myURI=new URL(myURL);

            String scheme=myURI.getProtocol();
            String host=myURI.getHost();
            //Step 1: Convert the scheme and host to lowercase
            myURL.replace(scheme,scheme.toLowerCase());
            myURL.replace(host,host.toLowerCase());

            //Step2: Remove the default ports
            if(myURL.contains(":80")) //default port for http
                myURL.replace(":80","");
            else {
                if (myURL.contains(":443")) //Default port for https
                    myURL.replace(":443", "");
            }

            //Step3: Removing trailing slashes
            if(myURL.lastIndexOf('/')==myURL.length()-1)
            {
                myURL=myURL.substring(0,myURL.length()-1);
            }

            //Step4: Remove directory index


            //Step5: Remove fragments
            int fragpos=myURL.indexOf('#');

            if(!(fragpos==-1))
            {
                myURL=myURL.substring(0,fragpos);
            }





        }

        catch (MalformedURLException e) {
            e.printStackTrace();
            return "";
        }


        return myURL;

    }

}
