package Root;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Router extends Root{
    private Socket      listenSocket = null;
    private Scanner     listenInput;
    private PrintWriter listenOutput;
    
    private int SENDPORTS[] = new int[1];
    
    public Router(int LISTENPORT,int[] SENDPORTS) {
        this.openListenPort(LISTENPORT);
        this.SENDPORTS = SENDPORTS;
    }
    public Router(int LISTENPORT,int SENDPORTT) {
        this.openListenPort(LISTENPORT);
        this.SENDPORTS[0] = SENDPORTT;
    }

    private void startListenService() throws IOException{
        do {
            
            listenSocket = serverSocket.accept();
           
            listenInput = new Scanner(listenSocket.getInputStream());
           
            listenOutput = new PrintWriter(listenSocket.getOutputStream(), true);
            
            Thread CH = new ClientHandler(listenSocket,listenInput,listenOutput,this.SENDPORTS);
            
            CH.start();
        } while (true);
    }
    @Override
    public void run() {
        try {
            startListenService();
        } catch (IOException ioEx) {
            ioEx.printStackTrace();
        } finally {
            try {
           
                System.out.println(
                    "\n* Closing connections (Router side)*");
                listenSocket.close();
            } catch (IOException ioEx) {
        
                System.out.println(
                    "Unable to disconnect!");
                System.exit(1);
            }
        }
    }
    
    

    class ClientHandler extends Thread {

        final Scanner scanner;
        final PrintWriter printWriter;
        final Socket socket;
        private int SENDPORTS[];

  
        public ClientHandler(Socket s, Scanner dis, PrintWriter dos ,int[] SENDPORTS) {
            this.socket      = s;
            this.scanner     = dis;
            this.printWriter = dos;
            this.SENDPORTS   = SENDPORTS;
        }

        @Override
        public void run()
        {
            try {
                handleClient();
            } catch (IOException ex) {
                Logger.getLogger(Router.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        private String handleClient() throws IOException {
            String str2 = null;
            String  message;
            Rsender sender;
            String PortName ;
           
            do {
 
                message = this.getMessage();
                System.out.println("message from sender " + message);
     
                Random randomGenerator = new Random();
                int randomInt = randomGenerator.nextInt(100);
                System.out.println("Generated random number for the packet is: " + randomInt);
                if (randomInt > 9) { 

                   
                    if(this.SENDPORTS.length != 1){
                        int PORT = this.SENDPORTS[getRoute(this.SENDPORTS.length)];
                        PortName = String.format(" %s ", getPortName(PORT));
                        sender = new Rsender(new Socket(host, PORT));
                    }else{
                        PortName = String.format(" %s ", getPortName(this.SENDPORTS[0]));
                        sender = new Rsender(new Socket(host, this.SENDPORTS[0]));
                    }
                    sender.sendMessage(PortName+","+message);
                    String str = sender.getRequest();
                    System.out.println("message from receiver: " + str);

                    this.sendRequest(PortName+","+str);
                    sender.closeConn();
                    sender = null;

                    System.gc();
                    Runtime.getRuntime().gc();
                } else {
                    this.sendRequest(str2);
                }
            } while (!message.equals("***CLOSE***"));
            return null;
        }
       
        private void sendRequest(String message){
            printWriter.println(message);
        }
        private String getMessage(){
            while (true) {     
                if(scanner.hasNext()){
                    return scanner.nextLine();
                }
            }
        }
       
        public boolean getRoute(){
            Random randomGenerator = new Random();
            return randomGenerator.nextBoolean();
        }

        private int getRoute(int length) {
            Random randomgenarator = new Random();
            return randomgenarator.nextInt(length);
        }

        public String getPortName(int PORT){
            return switch(PORT){
                case 1000->"X";
                case 1010->"A";
                case 1333->"B";
                case 1002->"C";
                case 1003->"D";
                case 1004->"E";
                case 1005->"F";
                case 1006->"G";
                case 1007->"H";
                case 1009->"Y";
                default->"";
            };
        }
    }
    
   
}
