package echoclient;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import shared.ProtocolStrings;

public class EchoClient extends Thread {

  Socket socket;
  private int port;
  private InetAddress serverAddress;
  private Scanner input;
  private PrintWriter output;
  private List<EchoListener> listeners = new ArrayList();

  public void connect(String address, int port) throws UnknownHostException, IOException {
    this.port = port;
    serverAddress = InetAddress.getByName(address);
    socket = new Socket(serverAddress, port);
    input = new Scanner(socket.getInputStream());
    output = new PrintWriter(socket.getOutputStream(), true);  //Set to true, to get auto flush behaviour
    start();
  }

  public void send(String msg) {
    output.println(msg);
  }

  public void stopClient() throws IOException {
    output.println(ProtocolStrings.STOP);
  }

  public void registerEchoListener(EchoListener l) {
    listeners.add(l);
  }

  public void unRegisterEchoListener(EchoListener l) {
    listeners.remove(l);
  }

  @Override
  public void run()  {
    String msg = input.nextLine();
    while (!msg.equals(ProtocolStrings.STOP)) {
      System.out.println(msg);
      notifyListeners(msg);
      msg = input.nextLine();
    }
    try {
      socket.close();
    } catch (IOException ex) {
      Logger.getLogger(EchoClient.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  
  private void notifyListeners(String msg){
    for(EchoListener listener : listeners){
      listener.messageArrived(msg);
    }
  }
  
  
  //Only for test purposes
  public static void main(String[] args) {
    int port = 9090;
    String ip = "localhost";
    if (args.length == 2) {
      port = Integer.parseInt(args[0]);
      ip = args[1];
    }
    try {
      EchoClient tester = new EchoClient();
      tester.connect(ip, port);
      
      tester.registerEchoListener(new EchoListener(){
        @Override
        public void messageArrived(String message) {
          System.out.println("Received : "+message);
        } 
      });
      System.out.println("Sending 'Hello world'");
      tester.send("Hello World");
      System.out.println("Waiting for a reply");    
      tester.stopClient();
      //System.in.read();      
    } catch (UnknownHostException ex) {
      Logger.getLogger(EchoClient.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IOException ex) {
      Logger.getLogger(EchoClient.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
}
