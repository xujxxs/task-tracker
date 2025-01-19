package io.tasks_tracker.profile.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import io.tasks_tracker.profile.entity.User;
import io.tasks_tracker.profile.exception.InvalidFileExtension;
import io.tasks_tracker.profile.exception.InvalidFileName;
import io.tasks_tracker.profile.exception.NotFoundException;
import io.tasks_tracker.profile.repository.UserRepository;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class S3StorageService 
{
    @Value("${cloud.s3.endpoint}")
    private String endpoint;

    @Value("${cloud.s3.bucket-name}")
    private String bucketName;

    @Autowired
    private S3Client s3Client;

    private List<String> supportedExtensions = List.of(".jpeg", ".jpg");

    @Autowired
    private UserRepository userRepository;

    private String getFileExtension(String fileName)
    {
        if(fileName == null) {
            throw new InvalidFileName("file name is empty");
        }
        int dotIndex = fileName.lastIndexOf(".");

        if(dotIndex < 0) {
            throw new InvalidFileName("invalid name");
        }

        String fileExtension = fileName.substring(dotIndex);
        if(!supportedExtensions.contains(fileExtension)) {
            throw new InvalidFileExtension(supportedExtensions);
        }
        return fileExtension;
    }

    public InputStream getAvatar(User userToGetAvatar)
    {
        if(userToGetAvatar.getAvatarLink() == null || userToGetAvatar.getAvatarLink().isEmpty()) {
            throw new NotFoundException("Avatar");
        }

        return s3Client.getObject(
            GetObjectRequest.builder()
                .bucket(bucketName)
                .key(userToGetAvatar.getAvatarLink())
                .build());
    }

    @CachePut(value = "users", key = "#userToUpdateAvatar.username")
    public void uploadAvatar(
            MultipartFile file,
            User userToUpdateAvatar
    ) throws IOException
    {
        String fileExtension = getFileExtension(file.getOriginalFilename());
        String fileName = "avatars/" + String.valueOf(userToUpdateAvatar.getId()) + fileExtension;
        
        if(userToUpdateAvatar.getAvatarLink() != null) {
            userToUpdateAvatar = deleteAvatar(userToUpdateAvatar);
        }
        
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
        userRepository.save(userToUpdateAvatar);
    }

    @CachePut(value = "users", key = "#userToDeleteAvatar.username")
    public User deleteAvatar(User userToDeleteAvatar)
    {
        if(userToDeleteAvatar.getAvatarLink() == null || userToDeleteAvatar.getAvatarLink().isEmpty()) {
            return userToDeleteAvatar;
        }

        s3Client.deleteObject(DeleteObjectRequest.builder()
            .bucket(bucketName)
            .key(userToDeleteAvatar.getAvatarLink())
            .build()
        );

        userToDeleteAvatar.setAvatarLink(null);
        return userRepository.save(userToDeleteAvatar);
    }
}
