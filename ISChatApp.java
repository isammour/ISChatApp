/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ischatapp;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.*;

/**
 *
 * @author isammour
 */
public class ISChatApp extends JFrame implements ActionListener{

    /**
     * @param args the command line arguments
     */
    // unique client id for each client
    static int clientId = 1;
    // contains clients sockets
    static ClientObject[] sockets = new ClientObject[3];

    //UI things 
    JPanel panel;
    JTextField client1_message,client2_message;
    JTextArea client1_chat,client2_chat,server_data;
    JButton client1_send,client2_send;
    
    public ISChatApp()
    {
        panel = new JPanel();
        client1_message = new JTextField();
        client2_message = new JTextField();
        client1_chat = new JTextArea();
        client2_chat = new JTextArea();
        server_data = new JTextArea();
        client1_send = new JButton();
        client2_send = new JButton();
        
        this.setSize(1500, 800);
        this.setVisible(true);
	    setDefaultCloseOperation(EXIT_ON_CLOSE);
        panel.setLayout(null);
	    this.add(panel);
        server_data.setBounds(250,10,1000,100);
        panel.add(server_data);
        client1_chat.setBounds(250,130,480,500);
        panel.add(client1_chat);
        client2_chat.setBounds(770,130,480,500);
        panel.add(client2_chat);
        client1_message.setBounds(250,640,300,40);
        panel.add(client1_message);
        client1_send.setText("Send");
        client1_send.setBounds(580,640,150,40);
        panel.add(client1_send);
        client2_message.setBounds(770,640,300,40);
        panel.add(client2_message);
        client2_send.setText("Send");
        client2_send.setBounds(1100,640,150,40);
        panel.add(client2_send);
        
        client1_send.addActionListener(this);
        client2_send.addActionListener(this);
        
        
        Server server = new Server();
        // Start server thread
        new Thread(server).start();
        
        // Start clients threads
        new Thread(new Client(1)).start();
        new Thread(new Client(2)).start();

        
    }
    public static void main(String[] args) {
        new ISChatApp();
    }

    // Handling send buttons clicks
    public void actionPerformed(ActionEvent e) {
        // if send button of client 1
        if ((e.getSource() == client1_send) && (client1_message.getText() != ""))
        {
            try
            {
                // send the message to server then clear the textbox
                sockets[1].getOut().writeUTF(client1_message.getText());
                client1_message.setText("");
            }    
            catch(Exception ex)
            {
                System.out.println(ex);
            }
        }
        // if send button of client 2
        if ((e.getSource() == client2_send) && (client2_message.getText() != ""))
        {
            try
            {
                // send the message to server then clear the textbox
                sockets[2].getOut().writeUTF(client2_message.getText());
                client2_message.setText("");
            }    
            catch(Exception ex)
            {
                System.out.println(ex);
            }
        }
    }
   class Server implements Runnable {
    
    public void run(){
        try{
            // create server socket
            ServerSocket server = new ServerSocket(5050);
            server_data.append("Server Started at port 5050 \n");
            server_data.append("Waiting for clients ...  \n");
            
            while(true)
            {
                // get newly connected  client socket
                Socket client = server.accept(); 
                // get ip of the client
                InetAddress inetAddress = client.getInetAddress();
                server_data.append("Client connected : "+ inetAddress.getHostAddress() + "\n");
                // handle the client 
                new Thread(new HandleClient(client)).start();
            }
        }
        catch(Exception e)
        {
            System.out.print(e);
        }
        
        }
    }
   
    class HandleClient implements Runnable {
        // declared down, holds the socket,id,in,and output streams of the client
        private ClientObject co;
        
        public HandleClient(Socket socket)
        {
            co = new ClientObject(clientId,socket);
            sockets[clientId] = co;
            clientId++;
        }
         public void run()
         {
             try
             {

                DataInputStream in= new DataInputStream(co.getSocket().getInputStream());
                // Send the id to the client
                new DataOutputStream(co.getSocket().getOutputStream()).writeInt(co.getId());
                while(true)
                {
                    try
                    {
                        // read a message from the user
                        String text = in.readUTF();
                        // check the current client id and send the message to the other one
                        if(co.getId() == 1)
                        {
                            new DataOutputStream(sockets[2].getSocket().getOutputStream()).writeUTF(text);
                        }
                        else
                        {
                            new DataOutputStream(sockets[1].getSocket().getOutputStream()).writeUTF(text);
                        }
                    }
                    catch(Exception ex)
                    {
                        System.out.println(ex);
                    }
                }
             }
             catch(Exception ex)
             {
                System.out.println(ex);
             }
         }
    }
    
    public class Client implements Runnable
    {
        private Socket connection;
        private int id;
        public Client(int id)
        {
        }
        
        public void run()
        {
            try
            {
                // connect to the server at port 5050
               connection = new Socket(InetAddress.getLocalHost(), 5050);
               DataInputStream in = new DataInputStream(connection.getInputStream());
               // read the id from the server
               this.id = in.readInt();
               while(true)
               {
                   try
                   {
                       // read the message from the server
                       String text = in.readUTF();
                       // check client id and print recieved message
                       if(id == 1)
                       {
                           client2_chat.append("> "+text +"\n");
                           client1_chat.append("Me: "+text +"\n");
                       }
                       else
                       {
                           client1_chat.append("> "+text +"\n");
                           client2_chat.append("Me: "+text +"\n");
                       }
                   }
                   catch(Exception ex)
                   {
                       System.out.println(ex);
                   }
                   
                   
               }
            }
            catch(Exception ex)
            {
                System.out.println(ex);
            }
            
        }
    }
    
    public class ClientObject implements java.io.Serializable 
    {
        private int Id;
        private Socket socket;
        private DataInputStream in;
        private DataOutputStream out;
        public ClientObject(int Id,Socket socket)
        {
            this.Id = Id;
            this.socket = socket;
            try
            {
                this.in = new DataInputStream(socket.getInputStream());
                this.out = new DataOutputStream(socket.getOutputStream());
            }
            catch(Exception ex)
            {
                System.out.println(ex);
            }
            
        }
        public Socket getSocket()
        {
            return socket;
        }
        public int getId()
        {
            return Id;
        }
        public DataInputStream getIn(){ return in;}
        public DataOutputStream getOut(){ return out;}
    }
 
}
