package com.fileUpload.fileUpload.Utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileStorageService {

    public static final String FILE_DOWNLOAD_API_ENDPOINT = "/downloadFile";
    private final Path baseStorageLocation;

    @Autowired
    public FileStorageService(FileStorageProperties fileStorageProperties) {
        this.baseStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.baseStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public String storeMultipartFile(MultipartFile file, String subDirectoryPath, String newName) {
        // Normalize file name
        String fileName = "";
        if(newName != null && !newName.trim().equalsIgnoreCase("")){
            String[] fileNameSplits = file.getOriginalFilename().split("\\.");
            int extensionIndex = fileNameSplits.length - 1;
            fileName = newName + "." + fileNameSplits[extensionIndex];
        }else {
            fileName = StringUtils.cleanPath(file.getOriginalFilename());
        }

        try {
            // Check if the file's name contains invalid characters
            if(fileName.contains("..")) {
                throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileName);
            }

            Path actualStoragePath = Paths.get(baseStorageLocation.toString(), subDirectoryPath)
                    .toAbsolutePath().normalize();
            try {
                Files.createDirectories(actualStoragePath);
            } catch (Exception ex) {
                throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
            }

            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = actualStoragePath.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    public String storeFile(File file, String subDirectoryPath, String newName) {
        // Normalize file name
        String fileName = "";
        if(newName != null && !newName.trim().equalsIgnoreCase("")){
            String[] fileNameSplits = file.getName().split("\\.");
            int extensionIndex = fileNameSplits.length - 1;
            fileName = newName + "." + fileNameSplits[extensionIndex];
        }else {
            fileName = StringUtils.cleanPath(file.getName());
        }

        try {
            // Check if the file's name contains invalid characters
            if(fileName.contains("..")) {
                throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileName);
            }

            Path actualStoragePath = Paths.get(baseStorageLocation.toString(), subDirectoryPath)
                    .toAbsolutePath().normalize();
            try {
                Files.createDirectories(actualStoragePath);
            } catch (Exception ex) {
                throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
            }

            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = actualStoragePath.resolve(fileName);
            Files.copy(Paths.get(file.getAbsolutePath()), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    public Resource loadFileAsResource(String subDirectoryPath, String fileName) {
        try {
            Path actualStoragePath = Paths.get(baseStorageLocation.toString(), subDirectoryPath)
                    .toAbsolutePath().normalize();
            Path filePath = actualStoragePath.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if(resource.exists()) {
                return resource;
            } else {
                throw new MyFileNotFoundException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new MyFileNotFoundException("File not found " + fileName, ex);
        }
    }
}
