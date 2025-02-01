package io.tasks_tracker.profile.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import io.tasks_tracker.profile.entity.User;
import io.tasks_tracker.profile.exception.InvalidFileExtension;
import io.tasks_tracker.profile.exception.InvalidFileName;
import io.tasks_tracker.profile.exception.NotFoundException;
import io.tasks_tracker.profile.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
@Service
public class S3StorageService 
{
    private final String bucketName;
    private final S3Client s3Client;
    private final UserRepository userRepository;
    private List<String> supportedExtensions = new ArrayList<>();

    public S3StorageService(
            @Value("${cloud.s3.bucket-name}") String bucketName,
            S3Client s3Client,
            UserRepository userRepository
    ) {
        this.bucketName = bucketName;
        this.s3Client = s3Client;
        this.userRepository = userRepository;
        this.supportedExtensions = List.of(".jpeg", ".jpg");
    }

    private String getFileExtension(String fileName)
    {
        if(fileName == null) {
            log.warn("Upload attemp with file empty");
            throw new InvalidFileName("file name is empty");
        }
        int dotIndex = fileName.lastIndexOf(".");

        if(dotIndex < 0) {
            log.warn("Invalid file name '{}'", fileName);
            throw new InvalidFileName("invalid name");
        }

        String fileExtension = fileName.substring(dotIndex);
        if(!supportedExtensions.contains(fileExtension)) {
            log.warn("Unsupported file extension '{}' for file '{}'", fileExtension, fileName);
            throw new InvalidFileExtension(supportedExtensions);
        }
        return fileExtension;
    }

    public InputStream getAvatar(User userToGetAvatar)
    {
        if(userToGetAvatar.getAvatarLink() == null || userToGetAvatar.getAvatarLink().isEmpty()) {
            log.warn("Avatar not found for user: {}", userToGetAvatar.getId());
            throw new NotFoundException("Avatar");
        }

        log.info("Retrieving avatar user: {}", userToGetAvatar.getId());
        return s3Client.getObject(
            GetObjectRequest.builder()
                .bucket(bucketName)
                .key(userToGetAvatar.getAvatarLink())
                .build());
    }

    @CachePut(value = "users", key = "#userToUpdateAvatar.id")
    public User uploadAvatar(
            MultipartFile file,
            User userToUpdateAvatar
    ) throws IOException
    {
        String fileExtension = getFileExtension(file.getOriginalFilename());
        String fileName = "avatars/" + userToUpdateAvatar.getId().toString() + fileExtension;
        
        if(userToUpdateAvatar.getAvatarLink() != null) {
            log.info("Deleting existing avatar for user: {}", userToUpdateAvatar.getId());
            userToUpdateAvatar = deleteAvatar(userToUpdateAvatar);
        }
        
        log.info("Uploading new avatar for user: {} to S3 as '{}'", userToUpdateAvatar.getId(), fileName);
        s3Client.putObject(
            PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build(),
            RequestBody.fromInputStream(
                file.getInputStream(), 
                file.getSize()
        ));

        userToUpdateAvatar.setAvatarLink(fileName);
        User savedUser = userRepository.save(userToUpdateAvatar);
        log.info("Avatar updated successfully for user: {}", savedUser.getId());
        return savedUser;
    }

    @CachePut(value = "users", key = "#userToDeleteAvatar.id")
    public User deleteAvatar(User userToDeleteAvatar)
    {
        if(userToDeleteAvatar.getAvatarLink() == null || userToDeleteAvatar.getAvatarLink().isEmpty()) {
            log.debug("No avatar to delete for user: {}", userToDeleteAvatar.getId());
            return userToDeleteAvatar;
        }

        log.info("Deleting avatar from S3 for user: {}", userToDeleteAvatar.getId());
        s3Client.deleteObject(DeleteObjectRequest.builder()
            .bucket(bucketName)
            .key(userToDeleteAvatar.getAvatarLink())
            .build()
        );

        userToDeleteAvatar.setAvatarLink(null);
        User savedUser = userRepository.save(userToDeleteAvatar);
        log.info("Avatar removed for user: {}", savedUser.getId());
        return savedUser;
    }
}
