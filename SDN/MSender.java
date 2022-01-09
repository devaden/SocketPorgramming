package SDN;

import Root.Sender;


public class MSender {
    
    public static void main(String[] args) {
        Sender sender = new Sender(1000);
        sender.start();
    }
}
