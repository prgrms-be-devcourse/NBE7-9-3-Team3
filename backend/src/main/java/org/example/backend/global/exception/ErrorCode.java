package org.example.backend.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // ========== 공통 에러 ==========
    VALIDATION_FAILED("CMN001", HttpStatus.BAD_REQUEST, "입력값 검증에 실패했습니다."),
    INTERNAL_ERROR("CMN002", HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
    INVALID_INPUT_VALUE("CMN003", HttpStatus.BAD_REQUEST, "잘못된 입력값입니다."),
    INVALID_TYPE_VALUE("CMN004", HttpStatus.BAD_REQUEST, "잘못된 타입의 값입니다."),
    MISSING_REQUEST_PARAMETER("CMN005", HttpStatus.BAD_REQUEST, "필수 요청 파라미터가 누락되었습니다."),
    UNAUTHORIZED_ACCESS("CMN006", HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    FORBIDDEN_ACCESS("CMN007", HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    NOT_FOUND_DATA("CMN008", HttpStatus.NOT_FOUND, "존재하지 않는 데이터입니다."),
    BAD_REQUEST_FORMAT("CMN009", HttpStatus.BAD_REQUEST, "잘못된 형식의 요청 데이터입니다."),

    // ========== Member 도메인 에러 ==========
    MEMBER_NOT_FOUND("M001", HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),
    MEMBER_EMAIL_DUPLICATE("M002", HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    MEMBER_NICKNAME_DUPLICATE("M003", HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),
    MEMBER_PASSWORD_MISMATCH("M004", HttpStatus.UNAUTHORIZED, "현재 비밀번호가 일치하지 않습니다."),
    MEMBER_LOGIN_FAILED("M005", HttpStatus.UNAUTHORIZED, "로그인에 실패했습니다."),

    // ========== Trade 도메인 에러 ==========
    TRADE_NOT_FOUND("T001", HttpStatus.NOT_FOUND, "존재하지 않는 게시글입니다."),
    TRADE_BOARD_TYPE_INVALID("T002", HttpStatus.BAD_REQUEST, "게시판 타입은 필수입니다."),
    TRADE_BOARD_TYPE_MISMATCH("T003", HttpStatus.BAD_REQUEST, "해당 게시판의 게시글이 아닙니다."),
    TRADE_OWNER_MISMATCH("T004", HttpStatus.FORBIDDEN, "게시글 작성자만 수정/삭제할 수 있습니다."),
    TRADE_ALREADY_SOLD("T005", HttpStatus.BAD_REQUEST, "해당 물픔은 이미 판매되었습니다."),
    // ========== TradeComment 도메인 에러 ==========
    TRADE_COMMENT_NOT_FOUND("TC001", HttpStatus.NOT_FOUND, "존재하지 않는 댓글입니다."),
    TRADE_COMMENT_POST_MISMATCH("TC002", HttpStatus.BAD_REQUEST, "해당 게시글의 댓글이 아닙니다."),
    TRADE_COMMENT_OWNER_MISMATCH("TC003", HttpStatus.FORBIDDEN, "댓글 작성자만 수정/삭제할 수 있습니다."),

    // ========== Follow 도메인 에러 ==========
    FOLLOW_SELF_FOLLOW("F001", HttpStatus.BAD_REQUEST, "자기 자신을 팔로우할 수 없습니다."),
    FOLLOW_ALREADY_EXISTS("F002", HttpStatus.BAD_REQUEST, "이미 팔로우하고 있습니다."),
    FOLLOW_NOT_FOUND("F003", HttpStatus.NOT_FOUND, "팔로워를 찾을 수 없습니다."),
    FOLLOWEE_NOT_FOUND("F004", HttpStatus.NOT_FOUND, "팔로이를 찾을 수 없습니다."),

    // ========== Aquarium 도메인 에러 ==========
    AQUARIUM_NOT_FOUND("A001", HttpStatus.NOT_FOUND, "어항이 존재하지 않습니다."),
    AQUARIUM_OWNED_NOT_FOUND("A002", HttpStatus.NOT_FOUND, "'내가 키운 물고기' 어항이 존재하지 않습니다."),
    AQUARIUM_OWNED_ALREADY_HAVE("A003", HttpStatus.NOT_FOUND, "어항 이름으로 '내가 키운 물고기'는 사용할 수 없습니다."),
    AQUARIUM_LOG_NOT_FOUND("A004", HttpStatus.NOT_FOUND, "어항 로그가 존재하지 않습니다."),

    // ========== Fish 도메인 에러 ==========
    FISH_NOT_FOUND("F001", HttpStatus.NOT_FOUND, "물고기가 존재하지 않습니다."),
    FISH_LOG_NOT_FOUND("F002", HttpStatus.NOT_FOUND, "물고기 로그가 존재하지 않습니다."),

    // ========== Point 도메인 에러 ==========
    POINT_MEMBER_NOT_FOUND("P001", HttpStatus.NOT_FOUND, "회원이 존재하지 않습니다."),
    POINT_HISTORY_NOT_FOUND("P002", HttpStatus.NOT_FOUND, "포인트 내역이 존재하지 않습니다."),
    POINT_INSUFFICIENT("P003", HttpStatus.BAD_REQUEST, "포인트가 부족합니다."),
    POINT_BUYER_NOT_FOUND("P004", HttpStatus.NOT_FOUND, "구매자가 존재하지 않습니다."),
    POINT_SELLER_NOT_FOUND("P005", HttpStatus.NOT_FOUND, "판매자가 존재하지 않습니다."),

    // ========== Image 도메인 에러 ==========
    IMAGE_NOT_UPLOADED("I001", HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다."),
    IMAGE_NOT_DELETED("I002", HttpStatus.INTERNAL_SERVER_ERROR, "파일 삭제에 실패했습니다."),
    IMAGE_FILE_EMPTY("I003", HttpStatus.BAD_REQUEST, "파일이 비어있습니다."),
    IMAGE_SIZE_EXCEEDED("I004", HttpStatus.BAD_REQUEST, "파일 크기는 5MB를 초과할 수 없습니다."),
    IMAGE_NAME_INVALID("I005", HttpStatus.BAD_REQUEST, "파일 이름이 유효하지 않습니다."),
    IMAGE_EXTENSION_NOT_ALLOWED("I006", HttpStatus.BAD_REQUEST, "허용하지 않는 파일 형식입니다. (jpg, jpeg, png, gif, webp만 가능)"),
    IMAGE_URL_NOT_ALLOWED("I007", HttpStatus.BAD_REQUEST, "허용되지 않은 URL입니다."),
    IMAGE_URL_INVALID("I008", HttpStatus.BAD_REQUEST, "유효하지 않은 S3 URL 형식입니다."),

    // ========== TradeChat 도메인 에러 ==========
    TRADE_CHAT_ROOM_NOT_FOUND("TC001", HttpStatus.NOT_FOUND, "채팅방이 존재하지 않습니다."),
    TRADE_CHAT_SENDER_NOT_FOUND("TC002", HttpStatus.NOT_FOUND, "보낸 사용자가 존재하지 않습니다."),
    TRADE_CHAT_TRADE_NOT_FOUND("TC003", HttpStatus.NOT_FOUND, "거래글이 존재하지 않습니다."),
    TRADE_CHAT_BUYER_NOT_FOUND("TC004", HttpStatus.NOT_FOUND, "구매자가 존재하지 않습니다."),

    // ========== Post 도메인 에러 ==========
    POST_FORBIDDEN_ACCESS("PS001", HttpStatus.FORBIDDEN, "비공개 글입니다.");

    private final String code;
    private final HttpStatus status;
    private final String message;

    ErrorCode(String code, HttpStatus status, String message) {
        this.code = code;
        this.status = status;
        this.message = message;
    }
}
