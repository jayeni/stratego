package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Handles an individual client connection
 * @author Aaron Roberts
 *
 */
public class SocketHandler extends Thread {

    public boolean DEBUG = true;
    public Socket socket;
    public int id = 0;
    public Server hostServer;

    /**
     * Packet handler (Messages incoming from client)
     **/

    public final String P_PING = "PING";
    public final String P_REQUEST_ID = "REQUESTID";                //Request it's own client ID
    public final String P_REQUEST_ADD = "REQUESTADDRESS";        //Request server's address
    public final String P_DESTROY = "DESTROY";                    //Destroy this connection

    /**
     * End of packet handler
     */

    public SocketHandler(Socket uniqueClient, int clientId, Server s) {
    	
	        this.socket = uniqueClient;
	        this.id = clientId;
	        this.hostServer = s;
    	
    }

    public void debug(String s) {
        if (DEBUG) {
            String timeStamp = new SimpleDateFormat("hh:mm:ss").format(new Date());
            System.out.println("\t[CLIENTID:" + id + "/" + timeStamp + "]: " + s);
        }
    }

    public Socket getSocket() {
    	return socket;
    }
    @Override
    public void run() {
   
        InputStream in = null;
        DataInputStream dIn = null;
        DataOutputStream dOut = null;
        try {
            in = socket.getInputStream();
            dIn = new DataInputStream(in);
            dOut = new DataOutputStream(socket.getOutputStream());
            debug("New client connected");
            dOut.writeUTF("Welcome to the server");

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        while (true) {
        	if (( socket.isClosed() || !(socket.isConnected())) )
        		return;
        	if (socket == null) 
        		return;
            try {
                if ((dIn != null) && (dOut != null)) {

                    String packet = dIn.readUTF();
                    String response = handlePacket(packet);
                    dOut.writeUTF(response);
                    debug("Packet received: " + packet + " | Packet sent: " + response);

                    if (response.equals("Closing connection...")) {
                        socket.close();
                        hostServer.decrementClientCount(id);
                        return;
                    }
                }
            } catch (SocketException e) {
	           	 
	             hostServer.decrementClientCount(id);
	             return;
            }catch (IOException e) {
                e.printStackTrace();
            } 
            
        }
    }

    public String getAddress() {
    	if (socket == null) {
    		return "Invalid address";
    	}
    	return socket.getRemoteSocketAddress().toString();
    }
    /**
     * @param s Incoming message from client
     * @return Returns the appropriate message, or an unknown request
     */
    public String handlePacket(String s) {
        String n = "";
        switch (s) {

            case P_PING:
                n = "PONG";
                break;
            case P_REQUEST_ID:
                n = Integer.toString(id);
                break;
            case P_REQUEST_ADD:
                n = socket.getLocalSocketAddress().toString();
                break;
            case P_DESTROY:
                n = "Closing connection...";
                break;

            default:
                n = "Unknown request on call (" + s + ")";

        }
        return n;
    }
}
