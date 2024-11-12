import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Counter {
    private int counter = 0;
    private final Lock l = new ReentrantLock();

    public void increment(){ //oppure utilizzo di synchronized
        try{
            l.lock();
            counter++;
        }finally{
            l.unlock();
        }
    }

    public int getCounter(){
        return counter;
    }
}
