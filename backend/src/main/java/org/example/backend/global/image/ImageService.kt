package org.example.backend.global.image

import org.example.backend.global.exception.BusinessException
import org.example.backend.global.exception.ErrorCode
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.*
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import java.time.Duration
import java.util.*

/**
 * AWS S3 이미지 Presigned URL 및 삭제 공통 서비스
 *
 * Presigned URL을 통한 클라이언트 직접 업로드 방식 사용
 * 업로드된 파일은 UUID로 고유한 이름 생성
 */
@Service
class ImageService(
    private val s3Client: S3Client,
    private val s3Presigner: S3Presigner
) {
    @Value("\${cloud.aws.s3.bucket}")
    private lateinit var bucket: String

    @Value("\${cloud.aws.region.static}")
    private lateinit var region: String

    /** Presigned URL + 최종 URL 함께 생성  */
    fun createPresignedUrl(fileName: String, directory: String?): PresignedUrlResponse {
        val key = generateKey(fileName, directory)
        val presignedUrl = generatePresignedUrl(key)
        val finalUrl = getFileUrl(key)

        return PresignedUrlResponse(presignedUrl, finalUrl)
    }

    /** S3에 PUT 요청할 수 있는 임시 URL 생성  */
    private fun generatePresignedUrl(key: String): String {
        val putObjectRequest = PutObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .build()

        val presignRequest = PutObjectPresignRequest.builder()
            .putObjectRequest(putObjectRequest)
            .signatureDuration(Duration.ofMinutes(10))
            .build()

        val presignedRequest = s3Presigner.presignPutObject(presignRequest)
        return presignedRequest.url().toString()
    }

    /** S3 객체 키 생성 (디렉토리/UUID.확장자)  */
    private fun generateKey(fileName: String, directory: String?): String {
        val extension = getFileExtension(fileName)
        val uuid: String = UUID.randomUUID().toString()

        return if (directory.isNullOrBlank()) {
            "$uuid.$extension"
        } else {
            "$directory/$uuid.$extension"
        }
    }

    /** S3 객체의 공개 접근 URL 생성  */
    fun getFileUrl(key: String): String {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, key)
    }

    /**
     * S3에서 단일 파일 삭제
     * @param fileUrl 삭제할 파일의 S3 URL
     *
     * TODO : 테스트 Kotlin 마이그레이션 시
     *  - nullable(String?) -> non-null(String)로 변경
     *  - validateS3Url도 non-null로 변경
     *  - ImageServiceTest의 T6, T10 테스트 제거 또는 수정
     */
    fun deleteFile(fileUrl: String) {
        val key = extractNameFromUrl(fileUrl)

        val deleteObjectRequest = DeleteObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .build()

        s3Client.deleteObject(deleteObjectRequest)
    }

    /** S3에서 여러 파일 삭제  */
    fun deleteFiles(fileUrls: List<String>?) {
        if (fileUrls == null || fileUrls.isEmpty()) {
            return
        }

        // URL -> S3 키로 변경
        val objectIdentifiers = fileUrls
            .map { fileUrl -> extractNameFromUrl(fileUrl) }
            .map { key -> ObjectIdentifier.builder().key(key).build() }

        // 한 번의 요청으로 여러 파일 삭제
        val delete = Delete.builder()
            .objects(objectIdentifiers)
            .build()

        val deleteObjectsRequest = DeleteObjectsRequest.builder()
            .bucket(bucket)
            .delete(delete)
            .build()

        s3Client.deleteObjects(deleteObjectsRequest)
    }

    /** 파일 확장자 추출  */
    private fun getFileExtension(filename: String): String {
        val lastIndexOf = filename.lastIndexOf('.')
        if (lastIndexOf == -1) {
            return ""
        }
        return filename.substring(lastIndexOf + 1)
    }

    /** S3 URL에서 키(경로) 추출 및 검증  */
    private fun extractNameFromUrl(fileUrl: String): String {
        validateS3Url(fileUrl)

        val expectedPrefix = "https://$bucket.s3.$region.amazonaws.com/"
        return fileUrl.substring(expectedPrefix.length)
    }

    /** S3 URL 형식 및 버킷 검증  */
    private fun validateS3Url(fileUrl: String?) {
        if (fileUrl.isNullOrBlank()) {
            throw BusinessException(ErrorCode.IMAGE_URL_INVALID)
        }

        val expectedPrefix = "https://$bucket.s3.$region.amazonaws.com/"

        if (!fileUrl.startsWith(expectedPrefix)) {
            throw BusinessException(ErrorCode.IMAGE_URL_NOT_ALLOWED)
        }
    }
}