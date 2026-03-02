package com.example.urbannest.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Presigner s3Presigner;

    @InjectMocks
    private S3Service s3Service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(s3Service, "s3Bucket", "test-bucket");
    }

    @Test
    void generateUploadUrl_returnsPresignedUrl() throws Exception {
        PresignedPutObjectRequest mockPresigned = mock(PresignedPutObjectRequest.class);
        when(mockPresigned.url()).thenReturn(URI.create("https://s3.example.com/upload").toURL());
        when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(mockPresigned);

        String url = s3Service.generateUploadUrl("listings/photo.jpg", "image/jpeg");

        assertThat(url).isEqualTo("https://s3.example.com/upload");
    }

    @Test
    void generateDownloadUrl_returnsPresignedUrl() throws Exception {
        PresignedGetObjectRequest mockPresigned = mock(PresignedGetObjectRequest.class);
        when(mockPresigned.url()).thenReturn(URI.create("https://s3.example.com/download").toURL());
        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(mockPresigned);

        String url = s3Service.generateDownloadUrl("listings/photo.jpg");

        assertThat(url).isEqualTo("https://s3.example.com/download");
    }

    @Test
    void deleteObject_callsS3Client() {
        s3Service.deleteObject("listings/photo.jpg");

        verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
    }
}
