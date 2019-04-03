public class ManufacturerMain1_test {
    static Runnable m = new ManufacturerServer_test();
    //static Runnable p = new Persistence_test();
    static Thread t1 = new Thread(m);
    //static Thread t2 = new Thread(p);


    public static void main(String[] args) throws InterruptedException {
        t1.start();
        //t2.start();


    }

}
