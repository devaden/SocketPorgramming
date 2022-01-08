package Root;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;


public class Receiver extends Root{
    Socket link = null;
    Scanner input;
    PrintWriter output;


    public Receiver(int PORT) {
        this.openListenPort(PORT);
    }


    @Override
    public void run() {
        while (true) {            
            try {
                //Port dinlemeye açýlýyor
                link = serverSocket.accept();
                //Gelen bilgi
                input = new Scanner(link.getInputStream()); //Step 3.
                //Sockete yazdýrma gibi düþün çýktý
                output = new PrintWriter(link.getOutputStream(), true); //Step 3.
                //Thread oluþturuluyor
                Thread th = new ClientHandler(link,input,output);
                //Thread baþlatýlýyor
                th.start();
            } catch (IOException ex) {
                System.err.println("Root.Receiver.startService()");
            }
        }
       
    }
    
    
    class ClientHandler extends Thread
    {
        final Scanner input;
        final PrintWriter output;
        final Socket link;

        // Constructor
        public ClientHandler(Socket s, Scanner dis, PrintWriter dos) {
            this.link   = s;
            this.input  = dis;
            this.output = dos;
        }


        @Override
        public void run()
        {
            try {
                handleRouter();
            } catch (IOException ioEx) {
                ioEx.printStackTrace();
            } finally {
                try {
                    //Baðlantý kapatalýyor
                    System.out.println("\n* Closing connections (Receiver side)*");
                    link.close();
                } catch (IOException ioEx) {
                    //Eðer baðlantý kapatýlamazsa hata veriyor
                    System.out.println("Unable to disconnect!");
                    System.exit(1);
                }
            }
        }
        private void handleRouter() throws IOException {

            //Paketten gelen bilgi mesajý
            String message;
            do {
                message = this.getMessage();
                this.sendMessage("ACK"+ message.substring(message.length() - 1));
                System.out.println(" - " +message);
                //Gelen mesaj bitip bitmediðini kontrol ediyor
                //Son gelen mesaj close olarak geliyor
            } while (!message.equals("***CLOSE***"));

        }

        //Pakette gelen mesajý okuma fonksiyonu
        private String getMessage(){
            while (true) {            
                if (input.hasNext()) {
                    return input.nextLine();
                }
            }
        }

        private void sendMessage(String message){
             output.println(message);
        }
    }
}
