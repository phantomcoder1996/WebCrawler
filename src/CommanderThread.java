import javafx.util.Pair;
import sun.awt.image.ImageWatched;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CommanderThread extends Thread implements Serializable{

    int maxWebPages;
    ExecutorService executor;
    int threadNum;
    String downloadPath;
    String seedSetPath;
    ArrayList<SoldierThread> soldiers=new ArrayList<>();
    Integer soldiersSize=0;


    CommanderThread(int maxWebPages,int threadNum,String downloadPath,String seedSetPath)
    {
        this.maxWebPages=maxWebPages;
        this.threadNum=threadNum;
        executor= Executors.newFixedThreadPool(threadNum);
        this.downloadPath=downloadPath;
        this.seedSetPath=seedSetPath;
    }


    private void readSeedSet()
    {
        DBManager.retreiveURLs();
        if(!Resources.noRevisit.isEmpty())
        {
            for(int i=0;i<Resources.noRevisit.size();i++)
                Resources.addLinkToQueue2(Resources.noRevisit.get(i));
        }
        else {
            ArrayList<String> fileContent = FileUtility.readFileToStringArray(seedSetPath);
            DBManager.updateCrawlerState("ReadingSeedSet");
            for (int i = 0; i < fileContent.size(); i++) {

                Resources.addLinkToQueue2(fileContent.get(i));

            }
            DBManager.updateCrawlerState("crawling");

        }


        //TODO: Fill queue with retreived urls

    }




    @Override
    public void run()
    {
        //Commander: Get me that seed set at once :( !!!
        if(soldiersSize==0||DBManager.getCrawlerState().equals("ReadingSeedSet"))
            readSeedSet();
        else
        {
            System.out.println("soldiers.size: "+soldiers.size());
            System.out.println("soldiers size: "+soldiersSize);
            for(int i=0;i<soldiers.size();i++)
            {
                SoldierThread si=soldiers.get(i);
                System.out.println("from soldiers thread"+si.disallowedDirectories.size());
                executor.execute(soldiers.get(i));
            }
        }

        //Commander: I must think of a way to distribute the tasks among those lazy soldiers :( !!
        while((PageCounter.counter.get()<maxWebPages))
        {

            if(!Resources.isQueueEmpty2())
            {    String url=Resources.getLink2();
                String urlHash= LinkParser.hashLink(url);


                if(!visitedResources.isVisited(urlHash)) { //TODO: Hash value changed


                    visitedResources.updateVisited(urlHash); //TODO: Hash value changed
                    SoldierThread newSoldier=new SoldierThread(url,"", maxWebPages,downloadPath);

                    soldiers.add(newSoldier); //soldiers is a list of all tasks

                    executor.execute(newSoldier);


                }
            }

        }
        //When maximum number of pages has been downloaded


        executor.shutdown();
        try {
            executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("interrupted");
        }
        while(!executor.isTerminated()){}

    }

    public void serializeCommanderThread(ObjectOutputStream os)
    {
        try {

            os.writeObject(new Integer(threadNum));
            os.writeObject(new Integer(maxWebPages));
            os.writeObject(downloadPath);
            os.writeObject(seedSetPath);
            int ss=soldiers.size();
            os.writeObject(ss);

            for(int i=0;i<ss;i++)
            {
                soldiers.get(i).serializeSoldierThread(os);
            }


        } catch (IOException e) {
            e.printStackTrace();
        }





    }

    public void deSerializeCommanderThread(ObjectInputStream is)
    {
        try {

            threadNum=((Integer)is.readObject()).intValue();
            maxWebPages=((Integer)is.readObject()).intValue();
            downloadPath=(String)is.readObject();
            seedSetPath=(String)is.readObject();

            soldiersSize=(Integer)is.readObject();
            for(int i=0;i<soldiersSize;i++)
            {

                SoldierThread st=new SoldierThread();
                st.deSerializeSoldierThread(is);
                if((st.links2.size()>0||st.scheduledDownloads.size()>0)&&(st.level<400)) //unfinished task
                    soldiers.add(st);


            }


        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }





}

