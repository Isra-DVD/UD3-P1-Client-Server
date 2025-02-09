package net.salesianos.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileTransferUtil {

    public static void sendFile(DataOutputStream dataOutputStream, File file) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(file);
                BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream)) {

            dataOutputStream.writeLong(file.length());

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
                dataOutputStream.write(buffer, 0, bytesRead);
            }
            dataOutputStream.flush();
        }
    }

    public static void receiveFile(DataInputStream dataInputStream, String filename) throws IOException {
        File outputFile = new File(filename);
        outputFile.getParentFile().mkdirs();

        try (FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream)) {

            long fileSize = dataInputStream.readLong();
            byte[] buffer = new byte[8192];
            int bytesRead;
            long totalBytesRead = 0;

            while (totalBytesRead < fileSize && (bytesRead = dataInputStream.read(buffer, 0,
                    (int) Math.min(buffer.length, fileSize - totalBytesRead))) != -1) {
                bufferedOutputStream.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
            }
        }
    }
}