public class MyThread extends Thread {

    private Counter counter;

    public MyThread(Counter counter){
        this.counter = counter;
    }

    @Override
    public void run() {
        System.out.println("Thread with Thread class is starting");
        for(int i = 0; i < 10000; i++)
            counter.increment();
    }
}
