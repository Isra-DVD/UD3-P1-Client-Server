package net.salesianos.server.threads;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.stream.Collectors;

import net.salesianos.utils.Constants;
import net.salesianos.utils.FileTransferUtil;

public class ClientHandler extends Thread {
    private Socket clientSocket;
    private String clientName;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try (DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream())) {

            this.clientName = dataInputStream.readUTF();
            System.out.println("Client " + clientName + " identified.");

            while (true) {
                String command = dataInputStream.readUTF();

                if ("QUIT".equals(command)) {
                    System.out.println("Client " + clientName + " requested disconnection.");
                    break;
                }

                if ("LIST".equals(command)) {
                    File serverDir = new File(Constants.SERVER_FILES_DIRECTORY);
                    String[] fileList = serverDir.list();
                    String fileListString = (fileList != null)
                            ? Arrays.stream(fileList).collect(Collectors.joining("\n"))
                            : "No files found.";

                    dataOutputStream.writeUTF(fileListString);
                    dataOutputStream.flush();
                    continue;
                }

                String filename = dataInputStream.readUTF();

                if ("UPLOAD".equals(command)) {
                    String readyStatus = dataInputStream.readUTF();
                    if ("READY".equals(readyStatus)) {
                        System.out.println("Receiving file from " + clientName + ": " + filename);

                        String baseFilename;
                        String extension = "";
                        int dotIndex = filename.lastIndexOf('.');
                        if (dotIndex > 0) {
                            baseFilename = filename.substring(0, dotIndex);
                            extension = filename.substring(dotIndex);
                        } else {
                            baseFilename = filename;
                        }
                        String newFilename = baseFilename + "_" + clientName + extension;

                        FileTransferUtil.receiveFile(dataInputStream,
                                Constants.SERVER_FILES_DIRECTORY + "\\" + newFilename);
                        System.out.println("File received: " + newFilename);
                    } else {
                        System.out.println("Client " + clientName + " could not find file: " + filename);
                    }

                } else if ("DOWNLOAD".equals(command)) {
                    System.out.println("Sending file to " + clientName + ": " + filename);
                    File fileToSend = new File(Constants.SERVER_FILES_DIRECTORY + "\\" + filename);
                    if (fileToSend.exists()) {
                        dataOutputStream.writeUTF("FOUND");
                        dataOutputStream.flush();
                        FileTransferUtil.sendFile(dataOutputStream, fileToSend);
                        System.out.println("File sent: " + filename);
                    } else {
                        dataOutputStream.writeUTF("NOT_FOUND");
                        dataOutputStream.flush();
                        System.out.println("File not found: " + filename);
                    }
                } else {
                    System.out.println("Invalid command from client " + clientName + ": " + command);
                }
            }

        } catch (SocketException e) {
            System.out.println("Connection closed with client " + this.clientName + ".");
        } catch (EOFException e) {
            System.out.println("Client " + clientName + " disconnected.");
        } catch (IOException e) {
            System.err.println("Error handling client " + clientName + ": " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing client socket: " + e.getMessage());
            }
        }
    }
}