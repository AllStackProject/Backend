package app.allstackproject.privideo.common.util;

import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.IS_NOT_IMAGE_FILE;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.MULTIPARTFILE_CONVERT_FAIL_IN_MEMORY;

import app.allstackproject.privideo.common.exception.ApiException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3Util {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${cloud.aws.s3.buckets.img}")
    private String imgBucket;

    @Value("${cloud.aws.s3.buckets.original}")
    private String videoBucket;

    @Value("${cloud.aws.s3.presign.upload-expiration}")
    private Duration uploadExpiration;

    @Value("${cdn.base-url}")
    private String CDN_BASE_URL;

    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png");

    private static final String VIDEO_EXTENSION = ".mp4";
    private static final String PLAY_FILE = "master.m3u8";

    // ================== Upload ==================

    public String uploadImgWithKey(MultipartFile file, String key) {
        try {
            byte[] bytes = file.getBytes();

            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(imgBucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromBytes(bytes));

            String url = s3Client.utilities()
                    .getUrl(b -> b.bucket(imgBucket).key(key))
                    .toExternalForm();

            log.info("File uploaded successfully: {}", key);
            return url;
        } catch (IOException e) {
            log.error("Error uploading file: {}", key, e);
            throw new ApiException(MULTIPARTFILE_CONVERT_FAIL_IN_MEMORY);
        }
    }

    // ================== Presigned URL ==================

    public URL generatePresignedUploadUrl(String objectKey) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(videoBucket)
                .key(objectKey)
                .contentType(VIDEO_EXTENSION)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(uploadExpiration)
                .putObjectRequest(putObjectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
        log.info("Pre-signed Upload URL 생성 성공: {}", objectKey);

        return presignedRequest.url();
    }

    public String generatePlaybackUrl(String hlsPrefix) {
        return CDN_BASE_URL + hlsPrefix + PLAY_FILE;
    }

    // ================== Key 생성 ==================

    public String generateThumbnailKey(Long orgId, String originalFileName) {
        String extension = getFileExtension(originalFileName);
        if (extension.isEmpty()) {
            extension = ".png"; // fallback
        }

        String uuid = UUID.randomUUID().toString();
        return String.format("org-%d/thumbnail/%s%s", orgId, uuid, extension);
    }

    public String generateVideoKey(Long orgId) {
        String uuid = UUID.randomUUID().toString();
        return String.format("org-%d/%s/original%s", orgId, uuid, VIDEO_EXTENSION);
    }

    public String generateHlsPrefix(Long orgId, String key) {
        return String.format("hls/org-%d/%s/", orgId, key.substring(0, key.length() - 4));
    }

    public boolean isImageFile(MultipartFile file) {
        String extension = extractExtension(file.getOriginalFilename());
        log.info("Checking image extension: {}", extension);
        return ALLOWED_IMAGE_EXTENSIONS.contains(extension.toLowerCase());
    }

    // ================== Delete ==================

    public void deleteFileByUrl(String fileUrl) {
        try {
            URL url = new URL(fileUrl);
            String path = url.getPath();
            String key = path.startsWith("/") ? path.substring(1) : path;

            if (exists(key)) {
                s3Client.deleteObject(DeleteObjectRequest.builder()
                        .bucket(imgBucket)
                        .key(key)
                        .build());
                log.info("S3에서 파일 삭제됨: {}", key);
            } else {
                log.warn("삭제할 파일이 존재하지 않음: {}", key);
            }
        } catch (MalformedURLException e) {
            log.error("URL 파싱 오류: {}", fileUrl, e);
        }
    }

    private boolean exists(String key) {
        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(imgBucket)
                    .key(key)
                    .build());
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }

    // ================== Private Helper ==================

    private String extractExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }

    private String getContentTypeFromKey(String key) {
        String extension = getFileExtension(key).toLowerCase();
        if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            throw new ApiException(IS_NOT_IMAGE_FILE);
        }

        return switch (extension) {
            case ".jpeg", ".jpg" -> "image/jpeg";
            case ".png" -> "image/png";
            default -> "application/octet-stream";
        };
    }
}
