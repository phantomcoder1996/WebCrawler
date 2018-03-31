import com.apple.eio.FileManager;
import com.panforge.robotstxt.RobotsTxt;
import javafx.util.Pair;
import sun.awt.image.ImageWatched;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SoldierThread implements Runnable,Serializable {

    int maxWebPages;
    String baseURL;
    String baseLinkHost;
    ArrayList<String> disallowedDirectories=new ArrayList<>();
    Queue<Pair<String,String>>links;
    Queue<String>links2;
    ArrayList<FileInfo> scheduledDownloads=new ArrayList<>();
  //  ArrayList<String>downloaded;
    String downloadPath;
    int level=0;
   // int queueSize=0;
   // final int maxQueueSize=500;

    final int maxSearchLevel=500;

    RobotsTxt robot;


    SoldierThread(String url,String urlP,int maxWebPages,String downloadPath)
    {
        this.maxWebPages=maxWebPages;
     //   links=new LinkedList<>();
        links2=new LinkedList<>();
        baseURL=url;
        this.downloadPath=downloadPath;
    //    links.add(new Pair<>(url,urlP));
        links2.add(url);
        try
        {
            URL temp=new URL(baseURL);
            baseLinkHost=temp.getHost();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

      //  Resources.updateVisited(url);//TODO:Revise that line of code

    }

    private void readRobotFile()//TODO:May remove baseURL
    {
        String myBaseURL= LinkParser.getBaseUrl(baseURL);
        RobotsFileReader robotReader;
        URL myUrl = null;
        HttpURLConnection urlC=null;
        try {
            myUrl = new URL(myBaseURL+"/robots.txt");
            urlC= (HttpURLConnection)myUrl.openConnection();
            urlC.setRequestMethod("HEAD");

            urlC.setConnectTimeout(3000);
            urlC.connect();
            if(urlC.getResponseCode()==HttpURLConnection.HTTP_OK) {
                robotReader= new RobotsFileReader(myUrl);
                System.out.println("Reading robots.txt");
                disallowedDirectories=robotReader.getDisallowedDirectories();
                System.out.println("disallowed directories= "+disallowedDirectories.size());
             }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (SocketException e)
        {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void readRobots()
    {
        String myBaseURL= LinkParser.getBaseUrl(baseURL);

        URL myUrl = null;

        try {
            myUrl = new URL(myBaseURL + "/robots.txt");

            InputStream is=myUrl.openStream();
            robot=RobotsTxt.read(is);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public SoldierThread ()
    {

    }
    private boolean isLinkAllowed(String url)
    {
        int i=0;
        String disallowedDir;
        boolean stillAllowed=true;
        while(i<disallowedDirectories.size()&&stillAllowed)
        {
            disallowedDir=disallowedDirectories.get(i);
            if(LinkParser.linksToDirectory(url,disallowedDir)) {
                stillAllowed = false;
                System.out.println("Directory "+disallowedDir+ "is disallowed for url:"+url);

            }
            ++i;
        }
        return stillAllowed;
    }





    @Override
    public void run()
    {
        if(PageCounter.counter.get()>=maxWebPages)return;
        String msg=Thread.currentThread().getName()+ "has started following url";
        System.out.println(msg);

        //TODO:Read robot.txt for remainig directories and serialize resources
        //Resources.isLinkAllowed(baseURL);
        if(level==0) {
            readRobotFile();
System.out.println("level = "+0);
        }

        //readRobots();

         System.out.println("current page count at start: "+PageCounter.counter.get());
         System.out.println("level at start : "+level);
       // int level=0;
        while(level<maxSearchLevel&&PageCounter.counter.get()<maxWebPages&&!links2.isEmpty())
        {
            System.out.println(PageCounter.counter.get() +"  is from "+Thread.currentThread().getName());
            String msg2=Thread.currentThread().getName()+"is in level "+level;
            System.out.println(msg2);
            if(!links2.isEmpty())
            {
                System.out.println("links2 : "+links2.size());
                String link=links2.poll();

                String normalized="";
                normalized=LinkParser.normalize(link);
                if(!normalized.equals("")) {
                    System.out.println("normalized: "+normalized);
                    String hash=LinkParser.hashLink(normalized);
                    if (PageCounter.counter.get() < maxWebPages) {

                         System.out.println("page count from "+Thread.currentThread().getName()+" "+PageCounter.counter.get());
                        //Now we need to schedule that file for a download
                        MyFileManager fileManager = new MyFileManager(normalized, downloadPath);
                        boolean status= LinkParser.isValid(normalized);
                        boolean  status2=false;
                            if(status)  status2 = fileManager.parseFile();
                            System.out.println("status: "+status+" ,status2 : "+status2);
                        if (status2&&PageCounter.counter.get()<maxWebPages) {

                            PageCounter.counter.incrementAndGet();


                            // fileManager.createSimHash();
                            fileManager.createHash();

                            FileInfo fileInfo = fileManager.getFileInfo();
                            //If second time this link will have a simHash value,so check for update
                            //If updated the database shall know about it

                            String prevHash = Resources.simHash.get(normalized);
                            if (prevHash!=null) {

                                if (prevHash.equals(fileInfo.myHash)){System.out.println("not updated"); continue;}

                            }
                            //The page has no prev hash val so we will have to add it to database
                            // Resources.addFileObject(fileInfo);
                            fileInfo.downloaded=true;
                            scheduledDownloads.add(fileInfo);
                            System.out.println("Scheduled downloads "+scheduledDownloads.size());
                            //Declare that the file as downloaded so that we can get its in links

                                DownloadedMonitor.addDownloaded(fileInfo.fileName);
                            //Add that files inLink to the hashmap in Resources

                            //Now go through all pages and if not visited add them to queue
                            //If the pages are visited, then they may be visited by me or by someone else
                            //In this case add me as an Inlink to these pages

                                ArrayList<String> fetchedLinks = fileManager.extractLinks();
                                examineFetchedLinks(fetchedLinks,normalized);



                        }

                    }
                }
            }

            level++;
            System.out.println("level"+level);
        }

        //Add your links to db after finishing
if(scheduledDownloads.size()>0) {

    DBManager.addFilesToDB((ArrayList<FileInfo>) scheduledDownloads.clone());

    scheduledDownloads.clear();
}
    }


    void addInLinkGlobally(String hash,String normalized)
    {
        if (!InLinksMonitor.inLinks.containsKey(hash)) {
            InLinksMonitor.inLinks.put(hash, Collections.newSetFromMap(new ConcurrentHashMap<>()));
        }
        InLinksMonitor.inLinks.get(hash).add(normalized);
    }


    void examineFetchedLinks(ArrayList<String>fetchedLinks,String normalized)
    {
        for (String fetchedLink : fetchedLinks) {
            if (PageCounter.counter.get() >= maxWebPages) {
                break;
            }
            String normalizedFetchedLink = LinkParser.normalize(fetchedLink);
            String hash = LinkParser.hashLink(normalizedFetchedLink);
            addInLinkGlobally(hash,normalized);


            if (!visitedResources.isVisited(hash)) {


                visitedResources.updateVisited(hash);
//TODO:change is link allowed arguments
                // if(robot==null||robot.query(null,normalizedFetchedLink)){
                if (isLinkAllowed(normalizedFetchedLink)) {
                    if (!isSameBase(normalizedFetchedLink)) {
                        //Let the commander decide which thread will follow that link
                        Resources.addLinkToQueue2(normalizedFetchedLink);
                    } else {
                        links2.add(normalizedFetchedLink);
                    }
                }
            }


        }
    }

    public void  serializeSoldierThread(ObjectOutputStream os)
    {

        try {
            os.writeObject(baseURL);
            os.writeObject(baseLinkHost);
            os.writeObject(downloadPath);
            os.writeObject(disallowedDirectories);
            os.writeObject(links2);

            if(!(scheduledDownloads.size()==0))
            {
                DBManager.addFilesToDB((ArrayList<FileInfo>) scheduledDownloads.clone());
                scheduledDownloads.clear();
            }
            os.writeObject(new Integer(maxWebPages));
            os.writeObject(new Integer(level));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void  deSerializeSoldierThread(ObjectInputStream is)
    {

        try {
            baseURL=(String)is.readObject();
            baseLinkHost=(String)is.readObject();
            downloadPath=(String)is.readObject();
           disallowedDirectories=(ArrayList<String>)is.readObject();
           links2=(Queue<String>)is.readObject();

           maxWebPages=((Integer)is.readObject()).intValue();
           level=((Integer)is.readObject()).intValue();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }


    public  int compareSimHash(String hash1,String hash2)
    {
        long x=Long.parseLong(hash1,2);
        long y=Long.parseLong(hash2,2);
        long z=x^y;
        int dist=0;
        for(int i=0;i<32;i++)
        {
            long bit=( z>>i)&1;
            if(bit==1) dist++;
        }
        return dist;
    }

   public boolean isSameBase(String url)
   {    URL myURL=null;
        String urlHost="";
       try {
            myURL=new URL(url);
            urlHost=myURL.getHost();

       } catch (MalformedURLException e) {
           e.printStackTrace();
       }
       return baseLinkHost.equals(urlHost);
   }



}


