public class MyRunnable implements Runnable {

    private Counter counter;

    public MyRunnable(Counter counter){
        this.counter = counter;
    }

    @Override
    public void run() {
        System.out.println("Thread with Runnable is starting");
        for(int i = 0; i < 10000; i++)
            counter.increment();
    }
}
