package com.chatio;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Server {
    //todo make constant fo r all project
    static final int PORT = 3443;

    private final ArrayList<ClientHandler> clients;
    private final HashMap<String, ClientHandler> clientsMap;

    public Server() {
        clients = new ArrayList<ClientHandler>();
        clientsMap = new HashMap<String, ClientHandler>();
        clientsMap.put("kr", null);

        Socket clientSocket = null;
        ServerSocket serverSocket = null;
        //todo try with arguments
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server is running.");

            while (true) {
                System.out.println("Wait for clients connection.");
                clientSocket = serverSocket.accept();

                ClientHandler client = new ClientHandler(clientSocket, this);
                System.out.println("clientHandler");
                //clients.add(client);
//                clientsMap.put("test", client);
                // any new client connection processed in a new thread
//                new Thread(client).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
                System.out.println("Server stopped.");
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void  subscribe(ClientHandler client){
        if(!(client == null)){
            sendBroadcastMsg("/log " + client.getNickname() + " connected to the chat.");
            clientsMap.put(client.getNickname(), client);
            clients.add(client);
            client.sendMsg("/log " + client.getNickname() + ", welcome!");
        }
        //todo refresh people online list
    }

    public void unsubscribe(ClientHandler client){
        if(!(client == null)){
            sendBroadcastMsg("/log" + client.getNickname() + " left the chat.");
            clients.remove(client);
            clientsMap.remove(client.getNickname());
        }
        //todo refresh people online list
    }

    public void sendBroadcastMsg(String msg) {
        for(Map.Entry<String, ClientHandler> client: clientsMap.entrySet()){
            client.getValue().sendMsg(msg);
        }
    }

    //todo trim nick to avoid same nicknames
    public boolean isUniqNick(String nick){
        for(Map.Entry<String, ClientHandler> client: clientsMap.entrySet()){
            if(client.getKey().equals(nick)){
                return false;
            }
        }
        return true;
    }

    public void sentPrivateMsg(ClientHandler sender, String recipientName, String msg){
        ClientHandler recipient = findClient(recipientName);
        if(recipient != null){
            recipient.sendMsg("from " + sender.getNickname() + ": " + msg);
            sender.sendMsg("to " + sender.getNickname() + ": " + msg);
        } else{
            //todo /warning?
            //todo отправление будет по щелчку по клиенту или по вводу никнейма
            sender.sendMsg("/log " + "There is no any client with name " + recipientName + " int this chat.");
        }
    }

    public ClientHandler findClient(String clientName){
        for(Map.Entry<String, ClientHandler> client: clientsMap.entrySet()){
            if(client.getKey().equals(clientName)){
                return client.getValue();
            }
        }
        return null;
    }

    public void removeClient(ClientHandler client) {
        clients.remove(client);
        clientsMap.remove(client);
    }
}
