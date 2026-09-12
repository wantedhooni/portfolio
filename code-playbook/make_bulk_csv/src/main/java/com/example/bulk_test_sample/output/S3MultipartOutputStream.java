package com.example.bulk_test_sample.output;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * S3 멀티파트 업로드를 지원하는 출력 스트림.
 */
public class S3MultipartOutputStream extends OutputStream {

    private final S3Client s3Client;
    private final String bucket;
    private final String key;
    private final long partSize;
    private final List<CompletedPart> completedParts = new ArrayList<>();
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private final String uploadId;
    private int partNumber = 1;

    /**
     * 멀티파트 업로드 스트림을 생성한다.
     *
     * @param s3Client S3 클라이언트
     * @param bucket 버킷 이름
     * @param key 업로드 키
     * @param partSize 파트 크기(바이트)
     */
    public S3MultipartOutputStream(S3Client s3Client, String bucket, String key, long partSize) {
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.key = key;
        this.partSize = partSize;
        CreateMultipartUploadResponse response = s3Client.createMultipartUpload(
                CreateMultipartUploadRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build()
        );
        this.uploadId = response.uploadId();
    }

    /**
     * 데이터를 버퍼에 기록하고 파트 크기에 도달하면 업로드한다.
     *
     * @param b 데이터 바이트
     * @throws IOException 기록 실패
     */
    @Override
    public void write(int b) throws IOException {
        buffer.write(b);
        if (buffer.size() >= partSize) {
            uploadPart();
        }
    }

    /**
     * 바이트 배열을 버퍼에 기록한다.
     *
     * @param b 데이터
     * @param off 시작 오프셋
     * @param len 길이
     * @throws IOException 기록 실패
     */
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        buffer.write(b, off, len);
        if (buffer.size() >= partSize) {
            uploadPart();
        }
    }

    /**
     * 스트림을 닫으면서 남은 데이터를 업로드하고 완료 처리한다.
     *
     * @throws IOException 닫기 실패
     */
    @Override
    public void close() throws IOException {
        if (buffer.size() > 0) {
            uploadPart();
        }
        CompleteMultipartUploadRequest completeRequest = CompleteMultipartUploadRequest.builder()
                .bucket(bucket)
                .key(key)
                .uploadId(uploadId)
                .multipartUpload(CompletedMultipartUpload.builder().parts(completedParts).build())
                .build();
        s3Client.completeMultipartUpload(completeRequest);
        super.close();
    }

    /**
     * 현재 버퍼를 멀티파트로 업로드한다.
     */
    private void uploadPart() {
        byte[] bytes = buffer.toByteArray();
        buffer.reset();
        UploadPartRequest request = UploadPartRequest.builder()
                .bucket(bucket)
                .key(key)
                .uploadId(uploadId)
                .partNumber(partNumber)
                .contentLength((long) bytes.length)
                .build();
        String eTag = s3Client.uploadPart(request, RequestBody.fromBytes(bytes)).eTag();
        completedParts.add(CompletedPart.builder().partNumber(partNumber).eTag(eTag).build());
        partNumber++;
    }
}
