package com.safetrip.backend.infrastructure.persistence.adapter;

import com.safetrip.backend.domain.repository.FileStorageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

@Slf4j
@Repository
public class FileStorageRepositoryImpl implements FileStorageRepository {

    private final S3Client s3Client;
    private final String bucket;

    public FileStorageRepositoryImpl(S3Client s3Client,
                                     @Value("${minio.bucket}") String bucket) {
        this.s3Client = s3Client;
        this.bucket = bucket;
        ensureBucketExists();
    }

    private void ensureBucketExists() {
        try {
            boolean bucketExists = s3Client.listBuckets()
                    .buckets()
                    .stream()
                    .anyMatch(b -> b.name().equals(bucket));

            if (!bucketExists) {
                s3Client.createBucket(CreateBucketRequest.builder()
                        .bucket(bucket)
                        .build());
                log.info("✅ Bucket creado: {}", bucket);
            }
        } catch (S3Exception e) {
            log.error("❌ Error verificando/creando bucket: {}", e.getMessage());
        }
    }

    @Override
    public String upload(String fileName, String contentType, byte[] data) {
        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(fileName)
                            .contentType(contentType)
                            .build(),
                    RequestBody.fromBytes(data)
            );
            log.debug("☁️ Archivo subido a MinIO: {}/{}", bucket, fileName);
            return fileName;
        } catch (S3Exception e) {
            log.error("❌ Error subiendo archivo a MinIO: {}", e.getMessage());
            throw new RuntimeException("Error subiendo archivo al storage", e);
        }
    }

    @Override
    public byte[] download(String fileName) {
        try {
            return s3Client.getObject(
                    GetObjectRequest.builder()
                            .bucket(bucket)
                            .key(fileName)
                            .build(),
                    ResponseTransformer.toBytes()
            ).asByteArray();
        } catch (S3Exception e) {
            log.error("❌ Error descargando archivo de MinIO: {}", e.getMessage());
            throw new RuntimeException("Error descargando archivo del storage", e);
        }
    }

    @Override
    public void delete(String fileName) {
        try {
            s3Client.deleteObject(
                    DeleteObjectRequest.builder()
                            .bucket(bucket)
                            .key(fileName)
                            .build()
            );
            log.debug("🗑️ Archivo eliminado de MinIO: {}/{}", bucket, fileName);
        } catch (S3Exception e) {
            log.error("❌ Error eliminando archivo de MinIO: {}", e.getMessage());
            throw new RuntimeException("Error eliminando archivo del storage", e);
        }
    }
}