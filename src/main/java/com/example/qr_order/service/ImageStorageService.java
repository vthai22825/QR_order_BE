package com.example.qr_order.service;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ImageStorageService {

    private final S3Client s3Client;

    @Value("${cloudflare.r2.bucket-name}")
    private String bucketName;

    @Value("${cloudflare.r2.public-domain}")
    private String publicDomain;

    public ImageStorageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    /**
     * Uploads an image to Cloudflare R2 asynchronously.
     * Use "Fire & Forget" pattern: The calling thread returns immediately,
     * and the 'onSuccess' callback handles persistence when the upload finishes.
     *
     * @param file       The image file to upload.
     * @param folderName The folder in the bucket to store the image (e.g.,
     *                   "Foods").
     * @param onSuccess  Callback to execute when upload is successful (e.g., save
     *                   URL to DB).
     */
    @Async("imageUploadExecutor")
    public void uploadImage(MultipartFile file, String folderName, java.util.function.Consumer<String> onSuccess) {
        if (file.isEmpty()) {
            return;
        }

        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        String key = (folderName != null && !folderName.isEmpty()) ? folderName + "/" + fileName : fileName;

        try {
            // Processing image: resize to max 1024px and compress quality to 80%
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            Thumbnails.of(file.getInputStream())
                    .size(1024, 1024)
                    .outputQuality(0.8)
                    .toOutputStream(os);

            byte[] buffer = os.toByteArray();
            ByteArrayInputStream is = new ByteArrayInputStream(buffer);

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength((long) buffer.length)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(is, buffer.length));

            // Construct the public URL
            String imageUrl = publicDomain + "/" + key;

            // Trigger the callback to save URL to Database
            if (onSuccess != null) {
                onSuccess.accept(imageUrl);
            }

        } catch (Exception e) {
            // Log error for Admin
            log.error("CRITICAL: Failed to upload image to R2. File: {}, Error: {}", fileName, e.getMessage(), e);
        }
    }

    // Bỏ @Async, đổi void thành String
    public String uploadImageSync(MultipartFile file, String folderName) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        String key = (folderName != null && !folderName.isEmpty()) ? folderName + "/" + fileName : fileName;

        try {
            // Nén ảnh hoàn toàn trên RAM
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            Thumbnails.of(file.getInputStream())
                    .size(1024, 1024)
                    .outputQuality(0.8)
                    .toOutputStream(os);

            byte[] buffer = os.toByteArray();
            ByteArrayInputStream is = new ByteArrayInputStream(buffer);

            // Cấu hình đẩy lên R2
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength((long) buffer.length)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(is, buffer.length));

            // Trả thẳng URL về cho người gọi hàm
            return publicDomain + "/" + key;

        } catch (Exception e) {
            log.error("CRITICAL: Failed to upload image to R2. File: {}, Error: {}", fileName, e.getMessage(), e);
            // Có thể throw lỗi ra để API báo lỗi 500 cho FE biết là upload tạch, không lưu DB nữa
            throw new RuntimeException("Lỗi khi upload ảnh lên server!");
        }
    }
}
