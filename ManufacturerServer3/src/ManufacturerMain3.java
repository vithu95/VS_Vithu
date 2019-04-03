public class ManufacturerMain3 {
    static Runnable m = new ManufacturerServer3();
    static Runnable p = new PersistenceServer3();
    static Thread t1 = new Thread(m);
    static Thread t2 = new Thread(p);


    public static void main(String[] args) throws InterruptedException {
        t1.start();
        t2.start();


    }

}
