import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.atomic.AtomicInteger;

public class PageCounter {

    static int pageCount=0;

    public static AtomicInteger counter=new AtomicInteger(0);


    public static void setCurrentCnt(int cnt)
    {
        pageCount=cnt;
    }


    public static synchronized void incrementCount()
    {

        pageCount++;
        System.out.println("current page count is "+pageCount);

    }


    public static synchronized int  getCount()
    {
        return pageCount;
    }

    public synchronized static void decrementCount() {
        pageCount--;
    }



    public static void serializePageCount(ObjectOutputStream os)
    {
        try {
           // os.writeObject(new Integer(pageCount));
            os.writeObject(counter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deserializePageCount(ObjectInputStream is)
    {
        try {
           // pageCount= ((Integer) is.readObject()).intValue();
            counter=(AtomicInteger)is.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}
