package org.example.backend.global.image.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.example.backend.global.image.ImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@ExtendWith(MockitoExtension.class)
public class ImageServiceTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Presigner s3Presigner;

    @InjectMocks
    private ImageService imageService;

    private static final String BUCKET_NAME = "test-bucket";
    private static final String REGION = "ap-northeast-2";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(imageService, "bucket", BUCKET_NAME);
        ReflectionTestUtils.setField(imageService, "region", REGION);
    }

    // ========== Presigned URL 생성 테스트 ==========
    @Test
    @DisplayName("t1: Presigned URL 생성 성공")
    void t1() throws Exception {
        // given
        String fileName = "test-image.jpg";
        String directory = "trades";

        PresignedPutObjectRequest mockPresignedRequest = mock(PresignedPutObjectRequest.class);
        when(mockPresignedRequest.url()).thenReturn(new URL("https://test-bucket.s3.amazonaws.com/presigned-url"));
        when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(mockPresignedRequest);

        // when
        Object response = imageService.createPresignedUrl(fileName, directory);

        // then
        assertThat(response).isNotNull();
        assertThat(response.toString()).contains("presignedUrl");
        assertThat(response.toString()).contains("fileUrl");
    }

    // ========== 파일 URL 생성 테스트 ==========
    @Test
    @DisplayName("t2: S3 파일 URL 생성 검증")
    void t2() {
        // given
        String key = "trades/abc123.jpg";

        // when
        String fileUrl = imageService.getFileUrl(key);

        // then
        assertThat(fileUrl).isEqualTo("https://test-bucket.s3.ap-northeast-2.amazonaws.com/trades/abc123.jpg");
    }

    // ========== 단일 파일 삭제 테스트 ==========
    @Test
    @DisplayName("t3: 단일 파일 삭제 성공")
    void t3() {
        // given
        String fileUrl = "https://test-bucket.s3.ap-northeast-2.amazonaws.com/trades/abc123.jpg";

        // when
        imageService.deleteFile(fileUrl);

        // then
        verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    @DisplayName("t4: 단일 파일 삭제 실패 - 잘못된 URL 형식")
    void t4() {
        // given
        String invalidUrl = "https://invalid-url.com/image.jpg";

        // when & then
        assertThatThrownBy(() -> imageService.deleteFile(invalidUrl))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.IMAGE_URL_NOT_ALLOWED);
    }

    @Test
    @DisplayName("t5: 단일 파일 삭제 실패 - 빈 URL")
    void t5() {
        // given
        String emptyUrl = "";

        // when & then
        assertThatThrownBy(() -> imageService.deleteFile(emptyUrl))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.IMAGE_URL_INVALID);
    }

    // TODO: Kotlin null safety로 인해 컴파일 타임에 null 전달 불가
    //  테스트 Kotlin 마이그레이션 시 ImageService.deleteFile을 non-null로 변경하고 이 테스트 제거
    @Test
    @DisplayName("t6: 단일 파일 삭제 실패 - null URL")
    void t6() {
        // given
        String nullUrl = null;

        // when & then
        assertThatThrownBy(() -> imageService.deleteFile(nullUrl))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.IMAGE_URL_INVALID);
    }

    @Test
    @DisplayName("t7: 단일 파일 삭제 실패 - 다른 버킷 URL")
    void t7() {
        // given
        String otherBucketUrl = "https://other-bucket.s3.ap-northeast-2.amazonaws.com/image.jpg";

        // when & then
        assertThatThrownBy(() -> imageService.deleteFile(otherBucketUrl))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.IMAGE_URL_NOT_ALLOWED);
    }

    // ========== 다중 파일 삭제 테스트 ==========
    @Test
    @DisplayName("t8: 다중 파일 삭제 성공")
    void t8() {
        // given
        List<String> fileUrls = Arrays.asList(
            "https://test-bucket.s3.ap-northeast-2.amazonaws.com/trades/abc123.jpg",
            "https://test-bucket.s3.ap-northeast-2.amazonaws.com/trades/def456.jpg",
            "https://test-bucket.s3.ap-northeast-2.amazonaws.com/trades/ghi789.jpg"
        );

        // when
        imageService.deleteFiles(fileUrls);

        // then
        verify(s3Client).deleteObjects(any(DeleteObjectsRequest.class));
    }

    @Test
    @DisplayName("t9: 다중 파일 삭제 - 빈 리스트")
    void t9() {
        // given
        List<String> emptyList = Collections.emptyList();

        // when
        imageService.deleteFiles(emptyList);

        // then
        verify(s3Client, never()).deleteObjects(any(DeleteObjectsRequest.class));
    }

    // TODO: Kotlin null safety로 인해 컴파일 타임에 null 전달 불가
    //  테스트 Kotlin 마이그레이션 시 ImageService.deleteFiles를 non-null로 변경하고 이 테스트 제거
    @Test
    @DisplayName("t10: 다중 파일 삭제 - null 리스트")
    void t10() {
        // given
        List<String> nullList = null;

        // when
        imageService.deleteFiles(nullList);

        // then
        verify(s3Client, never()).deleteObjects(any(DeleteObjectsRequest.class));
    }

    @Test
    @DisplayName("t11: 다중 파일 삭제 실패 - 잘못된 URL 포함")
    void t11() {
        // given
        List<String> fileUrls = Arrays.asList(
            "https://test-bucket.s3.ap-northeast-2.amazonaws.com/trades/abc123.jpg",
            "https://invalid-url.com/image.jpg",  // 잘못된 URL
            "https://test-bucket.s3.ap-northeast-2.amazonaws.com/trades/ghi789.jpg"
        );

        // when & then
        assertThatThrownBy(() -> imageService.deleteFiles(fileUrls))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.IMAGE_URL_NOT_ALLOWED);
    }

    // ========== URL 검증 추가 테스트 ==========
    @Test
    @DisplayName("t12: URL 검증 - 다른 리전")
    void t12() {
        // given
        String differentRegionUrl = "https://test-bucket.s3.us-east-1.amazonaws.com/image.jpg";

        // when & then
        assertThatThrownBy(() -> imageService.deleteFile(differentRegionUrl))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.IMAGE_URL_NOT_ALLOWED);
    }

    @Test
    @DisplayName("t13: URL 검증 - HTTP 프로토콜 (HTTPS 아님)")
    void t13() {
        // given
        String httpUrl = "http://test-bucket.s3.ap-northeast-2.amazonaws.com/image.jpg";

        // when & then
        assertThatThrownBy(() -> imageService.deleteFile(httpUrl))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.IMAGE_URL_NOT_ALLOWED);
    }

    @Test
    @DisplayName("t14: URL 검증 - 공백 문자열")
    void t14() {
        // given
        String blankUrl = "   ";

        // when & then
        assertThatThrownBy(() -> imageService.deleteFile(blankUrl))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.IMAGE_URL_INVALID);
    }
}

