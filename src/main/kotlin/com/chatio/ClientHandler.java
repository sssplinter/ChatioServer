package com.chatio;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClientHandler {
    private Server server;
    private Socket clientSocket;

    private DataOutputStream outMessage;
    private DataInputStream inMessage;

    private String nickname;

    public String getNickname() {
        return nickname;
    }

    private static final String HOST = "localhost";
    private static final int PORT = 3443;

    private String openKey;


    public String getOpenKey() {
        return openKey;
    }

    public ClientHandler(Socket socket, Server server) {
        try {
            this.server = server;
            this.clientSocket = socket;
            System.out.println(socket.getInetAddress().toString());

            this.outMessage = new DataOutputStream(socket.getOutputStream());
            this.inMessage = new DataInputStream(socket.getInputStream());

            new Thread(() -> {
                try {
                    while (true) {
                        String str = inMessage.readUTF();

                        if (str.startsWith("/auth")) {
                            String[] tokens = str.split("\\s");
                            //todo если нужно сто-то  для подписти
                            if (tokens.length == 2) {
                                System.out.println("[INFO] Check nickname for uniq.");
                              //  sendMsg("[INFO] Check \"" + tokens[1] + "\" nickname for uniqueness...");
                                if (server.isUniqNick(tokens[1])) {
                                    nickname = tokens[1];
                                    System.out.println("[INFO] Successful connection: " + nickname);
                                    server.subscribe(this);
                                  //  server.sendClientList();
                                    sendMsg("/authok");
                                    break;
                                } else {
                                    System.out.println("пользователь с таким именем уже есть");
                                    sendMsg("пользователь с таким именем уже есть");
                                }
                            } else {
                                System.out.println("неверный набор токенов");
                                sendMsg("неверный набор токенов");
                                //todo
                            }

                        }
                    }
                    while (true) {
                        String str = inMessage.readUTF();
                        if (str.startsWith("/start")) {
                            String[] tokens = str.split("\\s");
                            openKey = tokens[1];
                            server.subscribe(this);
                            System.out.println("[INFO] new client connection: " + nickname + "  openKey: " + openKey);
                            server.sendClientList();
                            break;
                        } else {
                            System.out.println(str);
                        }
                        //todo проверка валидна везде
//                        if (!str.startsWith("/")) {
//                            System.out.println("не правильный индекс");
//                            sendMsg("не правильный индекс");
//                        } else {
//                            if(str.startsWith("/msg")){
//                                String[] tokens = str.split("\\s");
//                                server.sendBroadcastMsg(nickname + ": " + tokens[1]);
//                                System.out.println(nickname + "отправил всем сообщение: " + tokens[1]);
//                            }
//                        }
                    }
                    while (true) {
                        String str = inMessage.readUTF();

                        String[] tokens = str.split("\\s");
                        if (str.startsWith("/getKey")) {
                            String key = server.getOpenKey(tokens[1]);
                            sendMsg("/key " + tokens[1] + " " + key);
                        }
                        if (str.startsWith("/clientsList")) {
                            server.sendClientList();
                        }
                        if (str.startsWith("/msg")) {
                            server.sentPrivateMsg(this, tokens[1], tokens[2]);
                        }
                    }
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                } finally {
                    try {
                        inMessage.close();
                        outMessage.close();
                        socket.close();
                    } catch (Exception e) {
                        //todo errors
                        e.printStackTrace();
                    }
                    server.unsubscribe(this);
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


//    public void run() {
//        try {
//            while (true) {
//                server.sendBroadcastMsg("New member entered the chat.");
//                server.sendBroadcastMsg("Members in chat: " + clientsCount);
//                break;
//            }
//            while (true){
//                if(inMessage.hasNext()){
//                    String clientMessage = inMessage.nextLine();
//                    if(clientMessage.startsWith("/private")){
//                        String[] tokens = clientMessage.split("\\s");
//                    }
//                    if(clientMessage.equalsIgnoreCase("##session##end##")){
//                        break;
//                    }
//                    System.out.println(clientMessage);
//                    server.sendBroadcastMsg(clientMessage);
//                }
//            }
//            Thread.sleep(100);
//        } catch (InterruptedException ex){
//            ex.printStackTrace();
//        } finally {
//            this.close();
//        }
//    }

//    private void close() {
//        server.removeClient(this);
//        clientsCount--;
//        server.sendBroadcastMsg("Members in the chat: " + clientsCount);
//    }

    public void sendMsg(String msg) {
        try {
            outMessage.writeUTF(msg);
        } catch (Exception ex) {
            System.out.println("Problem with send messages " + nickname);
            ex.printStackTrace();
        }
    }
}
