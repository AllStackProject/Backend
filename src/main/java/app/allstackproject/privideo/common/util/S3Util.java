package app.allstackproject.privideo.common.util;

import static app.allstackproject.privideo.common.enumStatus.S3ImgType.THUMBNAIL;
import static app.allstackproject.privideo.common.response.status.BaseExceptionResponseStatus.MULTIPARTFILE_CONVERT_FAIL_IN_MEMORY;

import app.allstackproject.privideo.common.enumStatus.S3ImgType;
import app.allstackproject.privideo.common.exception.ApiException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
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

    @Value("${cloud.aws.cloudfront.distribution-domain}")
    private String DISTRIBUTION_DOMAIN;

    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png");

    private static final String HLS_NAME = "hls";
    private static final String VIDEO_EXTENSION = ".mp4";
    private static final String VIDEO_CONTENT_TYPE = "video/mp4";
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
                .contentType(VIDEO_CONTENT_TYPE)
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
        return DISTRIBUTION_DOMAIN + "/" + hlsPrefix + "/" + PLAY_FILE;
    }

    // ================== Key 생성 ==================

    public String generateImgKey(Long orgId, String originalFileName, String uuid, S3ImgType imgType) {
        String extension = getFileExtension(originalFileName);
        if (extension.isEmpty()) {
            extension = ".png"; // fallback
        }

        if (imgType.equals(THUMBNAIL)) {
            return String.format("images/org-%d/thumbnail/%s%s", orgId, uuid, extension);
        } else {
            return String.format("images/org-%d/%s%s", orgId, uuid, extension);
        }
    }

    public String generateVideoKey(Long orgId, String uuid) {
        return String.format("%s/org-%d/%s/video%s", HLS_NAME, orgId, uuid, VIDEO_EXTENSION);
    }

    public String generateHlsPrefix(String key) {
        int lastSlash = key.lastIndexOf('/');
        return key.substring(0, lastSlash);
    }

    public boolean isImageFile(MultipartFile file) {
        String extension = extractExtension(file.getOriginalFilename());
        log.info("Checking image extension: {}", extension);
        return ALLOWED_IMAGE_EXTENSIONS.contains(extension.toLowerCase());
    }

    // ================== Download ==================

    public File downloadToTempFile(String bucket, String key) throws IOException {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        try (ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest)) {

            String extension = getFileExtension(key);
            if (extension.isEmpty()) {
                extension = ".tmp";
            }

            File tempFile = File.createTempFile("s3_", extension);
            tempFile.deleteOnExit();

            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = s3Object.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }

            log.info("S3 파일 임시 다운로드 완료: bucket={}, key={}, size={}MB",
                    bucket, key, tempFile.length() / 1024 / 1024);

            return tempFile;
        }
    }

    // ================== Delete ==================

    public void deleteFileByKey(String fileKey, boolean isImage) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(isImage ? imgBucket : videoBucket)
                .key(fileKey)
                .build());
        log.info("S3에서 파일 삭제됨: {}", fileKey);
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
}
