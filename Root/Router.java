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
    //Router startListenService çağrıldığında sürekli olarak port dinleniyor
    private void startListenService() throws IOException{
        do {
            //Port dinlemeye başlıyor
            listenSocket = serverSocket.accept();
            //Gelen veriyi okumak için scanner kullanıyoruz
            listenInput = new Scanner(listenSocket.getInputStream());
            //Veri göndermek için prinwriter kullanıyoruz
            listenOutput = new PrintWriter(listenSocket.getOutputStream(), true);
            //Thread oluşturuyor
            Thread CH = new ClientHandler(listenSocket,listenInput,listenOutput,this.SENDPORTS);
            //Thread çalıştırıyor
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
                //Socketi kapatıyor
                System.out.println(
                    "\n* Closing connections (Router side)*");
                listenSocket.close();
            } catch (IOException ioEx) {
                //Socketi kapatamazsa hata ile karşılaşırsa uygulamayı kapatıyor
                System.out.println(
                    "Unable to disconnect!");
                System.exit(1);
            }
        }
    }
    
    
    
    // ClientHandler class
    class ClientHandler extends Thread {

        final Scanner scanner;
        final PrintWriter printWriter;
        final Socket socket;
        private int SENDPORTS[];

        // Constructor
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
                //Gelen veri alınıyor
                message = this.getMessage();
                System.out.println("message from sender " + message);
                //Random sayı oluşturma
                Random randomGenerator = new Random();
                int randomInt = randomGenerator.nextInt(100);
                System.out.println("Generated random number for the packet is: " + randomInt);
                if (randomInt > 9) { //for random probability 20%,each packet has a random number between 0 to 99

                    //İki port numarası verilmiş ise gireceği kısım
                    if(this.SENDPORTS.length != 1){
                        //Port dizisndeki uzunluğu göre rastgele port numarasını getiriyor (0 veya 1)
                        int PORT = this.SENDPORTS[getRoute(this.SENDPORTS.length)];
                        //Port sayısına göre port adını getiriyor
                        PortName = String.format(" %s ", getPortName(PORT));
                        sender = new Rsender(new Socket(host, PORT));
                    }else{
                        PortName = String.format(" %s ", getPortName(this.SENDPORTS[0]));
                        sender = new Rsender(new Socket(host, this.SENDPORTS[0]));
                    }
                    //Gönderilecek olan portlardan birine gelen mesajı gönderiyor
                    sender.sendMessage(PortName+","+message);
                    //Gönderilecek olan portlardan birinden gelen isteği alıyoruz
                    //Gönderilen paketin ulaşıp ulaşmadığı bilgisi alınıyor
                    String str = sender.getRequest();
                    System.out.println("message from receiver: " + str);

                    //Dinlemiş olduğu porta
                    this.sendRequest(PortName+","+str);
                    sender.closeConn();
                    sender = null;

                    //Java çöp toplayacısını tetikliyor
                    System.gc();
                    Runtime.getRuntime().gc();
                } else {
                    this.sendRequest(str2);
                }
            } while (!message.equals("***CLOSE***"));
            return null;
        }
       
        //Bu metodlar alıcıdan mesaj almaya ve alıcıya cevap göndermeye yarar
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
            //Random sayı oluşturma için sınıf
            Random randomgenarator = new Random();
            return randomgenarator.nextInt(length);
        }

        //Port numarasına göre port adını geri döndüren fonksiyon
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
