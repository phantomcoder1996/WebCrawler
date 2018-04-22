
import javafx.util.Pair;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

public class MyFileManager {

    String url;
    StringBuilder fileContent=new StringBuilder();
    String downloadPath;
    String fileName;
//    HashMap<String,Integer> KeyMap=new HashMap<>();
//    String simHash="";
    String fileHeaders="";
    String fileBody="";
    String fileTitle="";
    int outlinks=0;
    Document document;
    String line;
    String myHash="";
    String description="";
    String keywords="";

    MyFileManager(String url,String downloadPath)
    {
        this.url=url;
        this.downloadPath=downloadPath;
    }


    public boolean parseFile()
    {
        System.out.println("Downloading file at link : "+url);
        boolean success=true;
        URL myUrl = null;
        try {
            myUrl = new URL(url);

            String fileUUID = UUID.nameUUIDFromBytes(url.getBytes()).toString();
            fileName=fileUUID;

            line="";
            document=Jsoup.connect(url).timeout(5000).get();


            if(document==null) return false;
            fileTitle=document.title().toString();
            line+=fileTitle;


            fileBody=document.body().text();
            line+=fileBody;

            Elements elem=document.select("h1,h2,h3,h4,h5,h6");

            for(Element e:elem)
            {
                fileHeaders+=e.text()+'\n';

            }
            line+=fileHeaders;

            //Get description from document object.

              Elements des= document.select("meta[name=description]");
              if(des!=null&&des.get(0)!=null)   description=des .get(0).attr("content");

           //   System.out.println(description);


            //Get keywords from document object.
            Elements keys =
                    document.select("meta[name=keywords]");
                  if(keys!=null&&keys.first()!=null)         keywords=keys.first().attr("content");


            String docString=document.toString();
            if(line.equals(""))
            {
                System.out.println("doc is empty");


            }
            else {

                BufferedWriter writer = new BufferedWriter(new FileWriter(downloadPath + "/"+fileName+".txt"));
                writer.write(url);
                writer.newLine();
                writer.write(docString);

                writer.close();

            }

        }

        catch (MalformedURLException e) {

            if(myUrl!=null)
                System.out.println(myUrl.toString()+ "is invalid");

            success=false;

        }

        catch (IOException e) {
            e.printStackTrace();
            System.out.println("time out from file manager");
            success=false;
        }
        return success;
    }


    public ArrayList<String> extractLinks()
    {

        ArrayList<String>links=new ArrayList<>();
        //Now Extract all the links from the page

        Elements anchors = document.select("a[href]");
        System.out.println("outs="+anchors.size());
        outlinks=anchors.size();
        if(outlinks==0) System.out.println("0 outs for "+url);
        for (Element anchor : anchors) {
            {
                String link = anchor.attr("abs:href");
                String normalizedLink=link;

                if(!normalizedLink.equals("")) links.add(normalizedLink);

            }


            //System.out.println(fullUrl);
        }
        return links;
    }

//    public void createHashMapOfKeywords()
//    {
//        Stemmer stemmer=new Stemmer();
//        ArrayList<String> stemmedKeywords=stemmer.stem(line);
//        for(int i=0;i<stemmedKeywords.size();i++)
//        {
//
//            String word=stemmedKeywords.get(i);
//
//            if(!KeyMap.containsKey(word))
//            {
//                KeyMap.put(word,1);
//            }
//            else  KeyMap.put(word,KeyMap.get(word)+1);
//
//        }
//
//        //Just for checking
////        Iterator it=KeyMap.entrySet().iterator();
////        while(it.hasNext())
////        {
////            HashMap.Entry pair=(HashMap.Entry)it.next();
////            System.out.printf("%s : %d \n",pair.getKey(),pair.getValue());
////           // it.remove();
////        }
//
//    }

//
//    public void createSimHash()
//    {
//        createHashMapOfKeywords();
//        int[] sim=new int[32];
//        Iterator it=KeyMap.entrySet().iterator();
//        while(it.hasNext())
//        {
//            HashMap.Entry pair=(HashMap.Entry)it.next();
//            String word=(String)pair.getKey();
//            Integer freq=(Integer)pair.getValue();
//
//            int hashVal32=word.hashCode();
//
//            //Now calculate the hash value for the 32 bit code
//            for(int i=0;i<32;i++)
//            {
//                int bit=(hashVal32>>i)&1;
//                if(bit==1) sim[i]+=freq;
//                else sim[i]-=freq;
//            }
//
//        }

//        for(int i=0;i<32;i++)
//        {
//            if(sim[i]>0) simHash+='1';
//            else simHash+='0';
//        }
//
//    }

    public void createHash()
    {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] array = md.digest(line.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
            }
            myHash=sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
        }
    }
    public FileInfo getFileInfo() {
        return new FileInfo(url, outlinks, myHash,fileName,fileTitle,fileHeaders,fileBody,description,keywords);

        //  return  new FileInfo(url,fileTitle,fileBody,fileHeaders,simHash,outlinks,myHash);}

    }

    public String getHashValue()
    {
        return myHash;
    }


    public String getFileName() {

        return fileName;
    }
}
