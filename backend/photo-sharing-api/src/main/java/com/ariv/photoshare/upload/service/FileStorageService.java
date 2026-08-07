package com.ariv.photoshare.upload.service;

import com.ariv.photoshare.upload.dto.UploadResponse;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.Http;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.InputStream;
import java.util.UUID;

@ApplicationScoped
public class FileStorageService {

    private final MinioClient minioClient;

    @ConfigProperty(name = "minio.bucket")
    String bucket;

    @ConfigProperty(name = "minio.url")
    String minioUrl;

    public FileStorageService(
            @ConfigProperty(name = "minio.url")
            String url,

            @ConfigProperty(name = "minio.access-key")
            String accessKey,

            @ConfigProperty(name = "minio.secret-key")
            String secretKey) {

        this.minioClient =
                MinioClient.builder()
                        .endpoint(url)
                        .credentials(
                                accessKey,
                                secretKey)
                        .build();
    }

    public UploadResponse upload(
            FileUpload file)
            throws Exception {

        String originalFileName =
                file.fileName();

        String extension =
                getExtension(originalFileName);

        String objectName =
                UUID.randomUUID()
                        + extension;

        try (InputStream inputStream =
                     java.nio.file.Files.newInputStream(
                             file.uploadedFile())) {

            minioClient.putObject(

                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .stream(
                                    inputStream,
                                    file.size(),
                                    -1L)
                            .contentType(
                                    file.contentType())
                            .build()
            );
        }

        String imageUrl =
                "%s/%s/%s"
                        .formatted(
                                minioUrl,
                                bucket,
                                objectName);

        return new UploadResponse(
                imageUrl,
                objectName
        );
    }

    private String getExtension(
            String filename) {

        int index =
                filename.lastIndexOf(".");

        if (index == -1) {
            return "";
        }

        return filename.substring(index);
    }

    @WithSpan("generate-presigned-url")
    public String generatePresignedUrl(
            String imageUrl)
            throws Exception {

        String objectName = extractObjectName(imageUrl);

        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Http.Method.GET)
                        .bucket(bucket)
                        .object(objectName)
                        .expiry(60 * 60) // 1 hour
                        .build()
        );
    }

    private String extractObjectName(
            String imageUrl) {

        int index =
                imageUrl.lastIndexOf("/");

        return imageUrl.substring(index + 1);
    }

}