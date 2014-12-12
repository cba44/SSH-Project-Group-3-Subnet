
package subnet;

public class Subnet {

    public static void main(String[] args) throws InterruptedException {
        
        final DBsubnet snet = new DBsubnet();
        
        Thread thread1 = new Thread() {
            
            public void run() {
        
                snet.blocking();
                
            }
            
        };
        
        Thread thread2 = new Thread() {
    
            public void run() {
                
                snet.unblocking();
                
            }
            
        };
        
        thread1.start();
        thread2.start();
        
        thread1.join();
        thread2.join();
        
    }
    
}
