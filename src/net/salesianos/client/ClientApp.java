package net.salesianos.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;
import java.util.stream.Collectors;

import net.salesianos.utils.Constants;
import net.salesianos.utils.FileTransferUtil;

public class ClientApp {

    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", Constants.SERVER_PORT);
                Scanner scanner = new Scanner(System.in);
                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream())) {

            System.out.print("Enter your name: ");
            String name = scanner.nextLine();
            dataOutputStream.writeUTF(name);
            dataOutputStream.flush();

            while (true) {
                System.out.print("Enter command (UPLOAD, DOWNLOAD, LIST, or QUIT): ");
                String command = scanner.nextLine().toUpperCase();

                if ("QUIT".equalsIgnoreCase(command)) {
                    dataOutputStream.writeUTF(command);
                    dataOutputStream.flush();
                    break;
                }

                if (!"UPLOAD".equals(command) && !"DOWNLOAD".equals(command) && !"LIST".equals(command)) {
                    System.out.println("Invalid command. Please enter UPLOAD, DOWNLOAD, LIST, or QUIT.");
                    continue;
                }

                if ("LIST".equals(command)) {
                    dataOutputStream.writeUTF(command);
                    String fileList = dataInputStream.readUTF();
                    System.out.println("Files on server:\n" + fileList);
                    continue;
                }

                if ("UPLOAD".equals(command)) {
                    File clientDir = new File(Constants.USER_FILES_DIRECTORY);
                    String[] clientFiles = clientDir.list();
                    String clientFileList = (clientFiles != null)
                            ? Arrays.stream(clientFiles).collect(Collectors.joining("\n"))
                            : "No files found in " + Constants.USER_FILES_DIRECTORY + " directory.";
                    System.out.println("Files in " + Constants.USER_FILES_DIRECTORY + ":\n" + clientFileList);
                }

                System.out.print("Enter filename: ");
                String filename = scanner.nextLine();

                dataOutputStream.writeUTF(command);
                dataOutputStream.writeUTF(filename);

                if ("UPLOAD".equals(command)) {
                    File fileToUpload = new File(Constants.USER_FILES_DIRECTORY + "\\" + filename);
                    if (fileToUpload.exists()) {
                        dataOutputStream.writeUTF("READY");
                        dataOutputStream.flush();
                        try {
                            FileTransferUtil.sendFile(dataOutputStream, fileToUpload);
                            System.out.println("File sent: " + filename);
                        } catch (IOException e) {
                            System.err.println("Error sending file: " + e.getMessage());
                        }
                    } else {
                        dataOutputStream.writeUTF("NOT_READY");
                        dataOutputStream.flush();
                        System.out.println("File not found: " + fileToUpload.getAbsolutePath());
                    }

                } else if ("DOWNLOAD".equals(command)) {
                    String fileStatus = dataInputStream.readUTF();
                    if ("FOUND".equals(fileStatus)) {
                        try {
                            FileTransferUtil.receiveFile(dataInputStream,
                                    Constants.USER_FILES_DIRECTORY + "\\" + filename);
                            System.out.println("File received: " + filename);
                        } catch (IOException e) {
                            System.err.println("Error receiving file: " + e.getMessage());
                        }
                    } else {
                        System.out.println("File not found on server.");
                    }
                }
            }
            System.out.println("Closing connection...");

        } catch (IOException e) {
            System.err.println("Client error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}