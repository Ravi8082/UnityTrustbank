package com.example.UnityTrustBank.ServiceImple;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.UnityTrustBank.Service.FileService;
@Service
public class FileServiceImple implements FileService {

    @Override
    public String uploadfile(MultipartFile file, String basePath) {

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is required");
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.contains(".")) {
            throw new RuntimeException("Invalid file name");
        }

        String contentType = file.getContentType();
        if (contentType == null ||
                !(contentType.equals("image/jpeg") || contentType.equals("image/png"))) {
            throw new RuntimeException("Only JPG and PNG files are allowed");
        }

        try {
     
            Files.createDirectories(Paths.get(basePath));

            String extension = originalName.substring(originalName.lastIndexOf("."));
            String newFileName = UUID.randomUUID() + extension;

            File destination = Paths.get(basePath, newFileName).toFile();

      
            file.transferTo(destination);

            return destination.getAbsolutePath();

        } catch (Exception e) {
            throw new RuntimeException("File upload failed", e);
        }
    }

    @Override
    public InputStream getResourse(String basePath, String fileName) {

        try {
            File file = Paths.get(basePath, fileName).toFile();
            if (!file.exists()) {
                throw new RuntimeException("File not found");
            }
            return new FileInputStream(file);
        } catch (Exception e) {
            throw new RuntimeException("Unable to read file", e);
        }
    }
}
