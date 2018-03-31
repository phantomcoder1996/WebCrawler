import javafx.util.Pair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;


public class WebCrawler {



  public static void main(String[] args)
  {



      long prevTime=System.currentTimeMillis();
      long prevTime2=prevTime;
       int maxWebPages=5000;
      Integer num = 20;

      if(!checkExists(System.getProperty("user.dir"),"backup")) {
          //Read number of pages from user
          System.out.println("number of threads ?");
          BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));
          num = 20;
          try {
              String ans = bf.readLine();
              num = Integer.parseInt(ans);
          } catch (IOException e) {
              e.printStackTrace();
          }

      }
      //First: Connect to the dataBase

      DBManager.connect("localhost",27017,"SearchEngineDB");

      //Crawler shall record its runTime





      CommanderThread commanderThread=null;
      commanderThread = new CommanderThread(maxWebPages, num, System.getProperty("user.dir")+"/.idea/WebPages/", System.getProperty("user.dir")+"/.idea/SeedSet/seedSet.txt");

      //start Timer and backup every two minutes
//      Timer timer=new Timer();
//      timer.scheduleAtFixedRate(new TimedSerialization(commanderThread),1*60*1000,1*60*1000);

      // loadResources(); //To start from checkpoint
      if(checkExists(System.getProperty("user.dir"),"backup")) {
          commanderThread=deserializeCrawler(commanderThread);
          System.out.println("Backing up after previous shutdown");

      }
      else
      {
          try {
              Files.createFile(Paths.get(System.getProperty("user.dir")+"/backup.txt"));
          } catch (IOException e) {
              e.printStackTrace();
          }
          System.out.println("Crawling started");
          DBManager.updateCrawlerState("started");

      }
      commanderThread.start();

      while(PageCounter.counter.get()<maxWebPages)
      {
          long curTime=System.currentTimeMillis();
          if(curTime-prevTime>=60*1000) {
              prevTime=curTime;
              serializeCrawler(commanderThread);
          }
//         if(curTime-prevTime2>=360000)
//          {
//              prevTime2=curTime;
//              DBManager.AddFilesToDB();
//          }
      }

     // DBManager.AddFilesToDB();
      System.out.println("up inlinks");
      DBManager.updateCrawlerState("finished");
      DBManager.updateInLinks();

      try {
          // commanderThread.join();
          //Wait until commander thread terminates and then delete backup.txt
          Files.deleteIfExists(Paths.get(System.getProperty("user.dir") + "/backup.txt"));
      }
//       catch (InterruptedException e) {
//          e.printStackTrace();
//      }
      catch (IOException e) {
          e.printStackTrace();
      }




  }



//  private static void loadResources()
//  {
//      try {
//          Scanner reader=new Scanner(new File("../backup.txt"));
//
//          int pageCnt;
//          int j=0;
//          int setSize=1;
//
//          while(reader.hasNext()&&j<setSize) {
//              pageCnt = reader.nextInt();
//
//              Resources.setCurrentCnt(pageCnt);
//
//              int qSize = reader.nextInt();
//              for (int i = 0; i < 2*qSize; i++) {
//                  String link=reader.nextLine();
//                  String parent=reader.nextLine();
//                  Resources.addLinkToQueue(link,parent);
//              }
//
//                setSize = reader.nextInt();
//              for ( j = 0; j < setSize; j++) {
//                  Resources.updateVisited(reader.nextLine());
//              }
//          }
//      } catch (FileNotFoundException e) {
//          e.printStackTrace();
//      } catch (IOException e) {
//          e.printStackTrace();
//      }
//  }

    public static void serializeCrawler(CommanderThread commanderThread)
    {
        try {
            System.out.println("timer task entered");
            if(commanderThread!=null)
            {
                System.out.println("Starting serialization");
                ObjectOutputStream os=new ObjectOutputStream(new FileOutputStream("backup2.txt"));
                PageCounter.serializePageCount(os);
                Resources.serializeResources(os);
                InLinksMonitor.serializeInlinks(os);
                visitedResources.serializeResources(os);
                DownloadedMonitor.serializeDownloaded(os);
                commanderThread.serializeCommanderThread(os);
              //  os.writeObject(commanderThread);
                os.close();
                Files.deleteIfExists(Paths.get(System.getProperty("user.dir") + "/backup.txt"));
                File f=new File(System.getProperty("user.dir")+"/backup2.txt");
                f.renameTo(new File(System.getProperty("user.dir") + "/backup.txt"));

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


  public static CommanderThread deserializeCrawler(CommanderThread commanderThread)
  {

      try {

          ObjectInputStream is=new ObjectInputStream(new FileInputStream(System.getProperty("user.dir")+"/backup.txt"));

          PageCounter.deserializePageCount(is);
          Resources.deSerializeResources(is);
          InLinksMonitor.deserializeInlinks(is);
          System.out.println("After serialization");
          System.out.println("Resources count="+InLinksMonitor.inLinks.size());
          //commanderThread=(CommanderThread)is.readObject();
          visitedResources.deSerializeResources(is);
          System.out.println("Resources count="+visitedResources.visitedLinks.size());

          DownloadedMonitor.deserializeDownloaded(is);

          commanderThread.deSerializeCommanderThread(is);

      } catch (IOException e) {
          e.printStackTrace();
          System.out.println("File backup does not exist");
      }
//      } catch (ClassNotFoundException e) {
//          e.printStackTrace();
//      }

      return commanderThread;

  }


    public static boolean checkExists(String directory, String file) {
        File dir = new File(directory);
        File[] dir_contents = dir.listFiles();
        String temp = file + ".txt";
        boolean check = new File(directory,temp).exists();
        System.out.println("Check"+check);  // -->always says false



        return check;
    }

}


