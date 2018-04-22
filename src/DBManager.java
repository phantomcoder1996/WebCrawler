import com.mongodb.*;
import javafx.util.Pair;


import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.bson.*;
import org.omg.Messaging.SYNC_WITH_TRANSPORT;


public class DBManager
{

    static MongoClient mongoClient;
    static String host;
    static int port;
    static DB db;
    static BulkWriteOperation writer;

    public static void connect(String host,int port,String dbName )
    {
        host=host;
        port=port;
        try {
            mongoClient=new MongoClient(host,port);
            db=mongoClient.getDB(dbName);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }


    }




    public static void retreiveURLs() {

        String prevCrawlerRun="";
        DBCursor crawlerState=db.getCollection("State").find();
        BasicDBObject where = new BasicDBObject("rank",new BasicDBObject("$gt", 0.7));
        BasicDBObject field = new BasicDBObject();
        field.put("url", 1);
       // where.put("rank", new BasicDBObject("$gt", 10));

        //GET state variables
//
//        if(crawlerState.hasNext()) {
//             prevCrawlerRun = crawlerState.next().get("lastRunTime").toString();
//           where=new BasicDBObject("rank",new BasicDBObject("$gt", 10));
//           where.append("lastModified",new BasicDBObject("$lt",new Date(prevCrawlerRun)));
//        }

     //   db.getCollection("urls").agg

        DBCursor cursor = db.getCollection("urls").find(where, field);
        while (cursor.hasNext()) {
            String url=cursor.next().get("url").toString();
            System.out.println("url retreived"+url);
            Resources.noRevisit.add(url);
        }

        //We have to retreive previos simhashes as well
        field.put("myHash",1);
        DBCursor cursor2 = db.getCollection("urls").find(new BasicDBObject("myHash",new BasicDBObject("$exists",true)),field);
        List<DBObject>list=cursor2.toArray();
        for(int i=0;i<list.size();i++)
        {
            String url=list.get(i).get("url").toString();
            String simHash=list.get(i).get("myHash").toString();
            System.out.println("simhash retreived"+simHash);
            Resources.simHash.put(url,simHash);
        }


    }


    public static void addFilesToDB(ArrayList<FileInfo>files)
    {
        System.out.println("DB files from db manager : "+files.size());
        BulkWriteOperation bulk = db.getCollection("urls").initializeUnorderedBulkOperation();
        BulkWriteOperation bulk2 = db.getCollection("downloadedDocuments").initializeUnorderedBulkOperation();
        int size=files.size();
        System.out.println("Adding files "+ size);

        for (int i = 0; i < size; ++i) {
            try {
                FileInfo currentFile = files.get(i);
                BasicDBObject url = new BasicDBObject("url", currentFile.url);
                ArrayList<String> temp = new ArrayList<>();

                bulk.find(url).upsert().update(new BasicDBObject("$set",
                        new BasicDBObject("outLinks", currentFile.outlinks).append("myHash", currentFile.myHash).append("rank", Math.random()).append("downloaded", 1).append("inLinks", temp.toArray()).append("fileName",currentFile.fileName)
                ));
                bulk2.find(url).upsert().update(new BasicDBObject("$set",
                        new BasicDBObject("title", currentFile.title).append("description", currentFile.description).append("keywords", currentFile.keyWords).append("headers", currentFile.headers).append("body", currentFile.body)
                ));
            }catch(BulkWriteException e)
            {
                System.out.println("url must be unique");
            }

        }

        try {
            if(size>0)    bulk.execute();
        }
        catch (BulkWriteException e) {
            e.printStackTrace();

        }
        catch(IllegalStateException e)
        {
            System.out.println("Nothing to write in db");
        }
    }


    public static void updateInLinks()
    {
        System.out.println("updating links");

        BulkWriteOperation bulk = db.getCollection("urls").initializeUnorderedBulkOperation();
        for(String page:DownloadedMonitor.downloaded)
        {
            BasicDBObject q = new BasicDBObject("fileName", page);
            System.out.println("Checking downloads");
            Set<String> inLinks=InLinksMonitor.inLinks.get(page);
            if(inLinks!=null) {
                System.out.println("inlinks: "+inLinks.size());


                BasicDBObject arrayUpdate=new BasicDBObject("$addToSet",new BasicDBObject("inLinks",new BasicDBObject("$each", inLinks.toArray())));
                bulk.find(q).upsert().update(new BasicDBObject(arrayUpdate));
            }

        }
         try {
             bulk.execute();
         }catch(IllegalStateException e)
         {
             System.out.println("Nothing to write in db");
         }
         catch(BulkWriteException e)
         {
             e.printStackTrace();
         }
    }









    public static void updateCrawlerState(String cState)
    {
        DBCollection state=db.getCollection("State");
        state.update(new BasicDBObject(),new BasicDBObject("$set",new BasicDBObject("crawlerState",cState)),true,true);


    }


    public void addInLinkToDB(String url,String inlink)
    {

        DBCollection col = db.getCollection("urls");
        BasicDBObject which=new BasicDBObject("url",url);
        BasicDBObject what = new BasicDBObject("$addToSet", new BasicDBObject("inLinks",inlink));

        col.update(which,what,true,true);
    }


    public static String getCrawlerState()
    {
        String s="";

        Cursor crawlerState=db.getCollection("State").find();

        if(crawlerState.hasNext())
            s=crawlerState.next().get("crawlerState").toString();

        return s;
    }
}
