package com.safetrip.backend.infrastructure.persistence.adapter;

import com.safetrip.backend.domain.repository.FileStorageRepository;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Value;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

public class MinioFileStorageRepositoryImpl implements FileStorageRepository {
    private final S3Client s3Client;
    private final String bucket;

    public MinioFileStorageRepositoryImpl(S3Client s3Client,
                                      @Value("${minio.bucket}") String bucket) {
        this.s3Client = s3Client;
        this.bucket = bucket;

        if (!s3Client.listBuckets().buckets().stream().anyMatch(b -> b.name().equals(bucket))) {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
        }
    }

    @Override
    public String upload(String fileName, String contentType, byte[] data) {
        s3Client.putObject(PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(fileName)
                        .contentType(contentType)
                        .build(),
                software.amazon.awssdk.core.sync.RequestBody.fromBytes(data));
        return fileName;
    }

    @Override
    public byte[] download(String fileName) {
        return s3Client.getObject(GetObjectRequest.builder()
                                .bucket(bucket)
                                .key(fileName)
                                .build(),
                        software.amazon.awssdk.core.sync.ResponseTransformer.toBytes())
                .asByteArray();
    }

    @Override
    public void delete(String fileName) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(fileName)
                .build());
    }
}