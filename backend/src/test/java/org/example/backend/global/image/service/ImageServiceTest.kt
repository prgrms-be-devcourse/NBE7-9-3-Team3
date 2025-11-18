package org.example.backend.global.image.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.example.backend.global.exception.BusinessException
import org.example.backend.global.exception.ErrorCode
import org.example.backend.global.image.ImageService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.test.util.ReflectionTestUtils
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import java.net.URL

@ExtendWith(MockitoExtension::class)
class ImageServiceTest {
    @Mock
    private lateinit var s3Client: S3Client

    @Mock
    private lateinit var s3Presigner: S3Presigner

    @InjectMocks
    private lateinit var imageService: ImageService

    @BeforeEach
    fun setUp() {
        ReflectionTestUtils.setField(imageService, "bucket", BUCKET_NAME)
        ReflectionTestUtils.setField(imageService, "region", REGION)
    }

    // ========== Presigned URL 생성 테스트 ==========
    @Test
    @DisplayName("t1: Presigned URL 생성 성공")
    fun t1() {
        // given
        val fileName = "test-image.jpg"
        val directory = "trades"

        val mockPresignedRequest = mock<PresignedPutObjectRequest>()
        whenever(mockPresignedRequest.url())
            .thenReturn(URL("https://test-bucket.s3.amazonaws.com/presigned-url"))
        whenever(s3Presigner.presignPutObject(anyOrNull<PutObjectPresignRequest>()))
            .thenReturn(mockPresignedRequest)

        // when
        val response = imageService.createPresignedUrl(fileName, directory)

        // then
        assertThat(response).isNotNull()
        assertThat(response.toString()).contains("presignedUrl")
        assertThat(response.toString()).contains("fileUrl")
    }

    // ========== 파일 URL 생성 테스트 ==========
    @Test
    @DisplayName("t2: S3 파일 URL 생성 검증")
    fun t2() {
        // given
        val key = "trades/abc123.jpg"

        // when
        val fileUrl = imageService.getFileUrl(key)

        // then
        assertThat(fileUrl)
            .isEqualTo("https://test-bucket.s3.ap-northeast-2.amazonaws.com/trades/abc123.jpg")
    }

    // ========== 단일 파일 삭제 테스트 ==========
    @Test
    @DisplayName("t3: 단일 파일 삭제 성공")
    fun t3() {
        // given
        val fileUrl = "https://test-bucket.s3.ap-northeast-2.amazonaws.com/trades/abc123.jpg"

        // when
        imageService.deleteFile(fileUrl)

        // then
        verify(s3Client).deleteObject(anyOrNull<DeleteObjectRequest>())
    }

    @Test
    @DisplayName("t4: 단일 파일 삭제 실패 - 잘못된 URL 형식")
    fun t4() {
        // given
        val invalidUrl = "https://invalid-url.com/image.jpg"

        // when & then
        assertThatThrownBy { imageService.deleteFile(invalidUrl) }
            .isInstanceOf(BusinessException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.IMAGE_URL_NOT_ALLOWED)
    }

    @Test
    @DisplayName("t5: 단일 파일 삭제 실패 - 빈 URL")
    fun t5() {
        // given
        val emptyUrl = ""

        // when & then
        assertThatThrownBy {
            imageService.deleteFile(emptyUrl)
        }
            .isInstanceOf(BusinessException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.IMAGE_URL_INVALID)
    }

    @Test
    @DisplayName("t6: 단일 파일 삭제 실패 - 다른 버킷 URL")
    fun t6() {
        // given
        val otherBucketUrl = "https://other-bucket.s3.ap-northeast-2.amazonaws.com/image.jpg"

        // when & then
        assertThatThrownBy {
            imageService.deleteFile(otherBucketUrl)
        }
            .isInstanceOf(BusinessException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.IMAGE_URL_NOT_ALLOWED)
    }

    // ========== 다중 파일 삭제 테스트 ==========
    @Test
    @DisplayName("t7: 다중 파일 삭제 성공")
    fun t7() {
        // given
        val fileUrls = listOf(
            "https://test-bucket.s3.ap-northeast-2.amazonaws.com/trades/abc123.jpg",
            "https://test-bucket.s3.ap-northeast-2.amazonaws.com/trades/def456.jpg",
            "https://test-bucket.s3.ap-northeast-2.amazonaws.com/trades/ghi789.jpg"
        )

        // when
        imageService.deleteFiles(fileUrls)

        // then
        verify(s3Client).deleteObjects(ArgumentMatchers.any(DeleteObjectsRequest::class.java))
    }

    @Test
    @DisplayName("t8: 다중 파일 삭제 - 빈 리스트")
    fun t8() {
        // given
        val emptyList = emptyList<String>()

        // when
        imageService.deleteFiles(emptyList)

        // then
        verify(s3Client, never()).deleteObjects(ArgumentMatchers.any(DeleteObjectsRequest::class.java))
    }

    @Test
    @DisplayName("t9: 다중 파일 삭제 실패 - 잘못된 URL 포함")
    fun t9() {
        // given
        val fileUrls = listOf(
            "https://test-bucket.s3.ap-northeast-2.amazonaws.com/trades/abc123.jpg",
            "https://invalid-url.com/image.jpg",  // 잘못된 URL
            "https://test-bucket.s3.ap-northeast-2.amazonaws.com/trades/ghi789.jpg"
        )

        // when & then
        assertThatThrownBy {
            imageService.deleteFiles(fileUrls)
        }
            .isInstanceOf(BusinessException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.IMAGE_URL_NOT_ALLOWED)
    }

    // ========== URL 검증 추가 테스트 ==========
    @Test
    @DisplayName("t10: URL 검증 - 다른 리전")
    fun t10() {
        // given
        val differentRegionUrl = "https://test-bucket.s3.us-east-1.amazonaws.com/image.jpg"

        // when & then
        assertThatThrownBy {
            imageService.deleteFile(differentRegionUrl)
        }
            .isInstanceOf(BusinessException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.IMAGE_URL_NOT_ALLOWED)
    }

    @Test
    @DisplayName("t11: URL 검증 - HTTP 프로토콜 (HTTPS 아님)")
    fun t11() {
        // given
        val httpUrl = "http://test-bucket.s3.ap-northeast-2.amazonaws.com/image.jpg"

        // when & then
        assertThatThrownBy {
            imageService.deleteFile(httpUrl)
        }
            .isInstanceOf(BusinessException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.IMAGE_URL_NOT_ALLOWED)
    }

    @Test
    @DisplayName("t12: URL 검증 - 공백 문자열")
    fun t12() {
        // given
        val blankUrl = "   "

        // when & then
        assertThatThrownBy {
            imageService.deleteFile(blankUrl)
        }
            .isInstanceOf(BusinessException::class.java)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.IMAGE_URL_INVALID)
    }

    companion object {
        private const val BUCKET_NAME = "test-bucket"
        private const val REGION = "ap-northeast-2"
    }
}

