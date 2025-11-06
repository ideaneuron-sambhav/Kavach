package com.login.Login.service.filesystemservice;

import com.login.Login.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;


@Service
public class FileSystemService {

    @Autowired
    private JwtUtil jwtUtil;
    private final Path rootLocation = Paths.get("Uploads");
    private final Path binLocation = Paths.get("Recycle Bin");

    public void createFolder(String parentPath, String folderName) {
        Path parentDir = Paths.get(rootLocation.toString(), parentPath);
        Path newFolder = parentDir.resolve(folderName);
        try {
            Files.createDirectories(newFolder);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create folder", e);
        }
    }

    public boolean moveToBin(String folderPath) {
        Long userId = jwtUtil.getUserIdFromContext();
        Path folderToDelete = Paths.get(rootLocation.toString(), folderPath);

        try {
            if (!Files.exists(binLocation)) {
                Files.createDirectories(binLocation);
            }

            if (Files.exists(folderToDelete)) {
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                Path binDir = Paths.get(String.valueOf(binLocation), String.valueOf(userId));
                Path destination = binDir.resolve(folderToDelete.getFileName().toString() + "_" + timestamp);


                Files.move(folderToDelete, destination, StandardCopyOption.REPLACE_EXISTING);
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to move folder to the bin", e);
        }
    }

    public boolean moveToOriginal(){
        return true;
    }

    public boolean moveFileToBin(String filePath) {
        Path fileToDelete = Paths.get(rootLocation.toString(), filePath);
        try {
            if (Files.exists(fileToDelete)) {
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                Path destination = binLocation.resolve(fileToDelete.getFileName().toString() + "_" + timestamp);
                Files.move(fileToDelete, destination, StandardCopyOption.REPLACE_EXISTING);
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to move file to the bin", e);
        }

    }
}

