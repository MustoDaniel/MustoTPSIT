public class App {

    public void start(){

        Counter counter = new Counter();

        System.out.println("Inizio del programma");

        Thread t1 = new Thread(new MyRunnable(counter));
        Thread t2 = new MyThread(counter);
        t1.start();
        t2.start();

        try {
            t1.join();
            t2.join();
        }catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }

        System.out.println("Counter vale: " + counter.getCounter());

        System.out.println("Fine del programma");
    }
}
