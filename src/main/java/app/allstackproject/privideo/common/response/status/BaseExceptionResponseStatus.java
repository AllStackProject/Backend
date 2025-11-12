package app.allstackproject.privideo.common.response.status;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum BaseExceptionResponseStatus implements ResponseStatus {

    /**
     * 1000: 요청 성공 (OK)
     */
    SUCCESS(1000, HttpStatus.OK, "요청에 성공하였습니다."),

    /**
     * 2000: Request 오류 (BAD_REQUEST)
     */
    BAD_REQUEST(2000, HttpStatus.BAD_REQUEST, "유효하지 않은 요청입니다."),
    URL_NOT_FOUND(2001, HttpStatus.NOT_FOUND, "유효하지 않은 URL 입니다."),
    METHOD_NOT_ALLOWED(2002, HttpStatus.METHOD_NOT_ALLOWED, "해당 URL에서는 지원하지 않는 HTTP Method 입니다."),
    HTTP_MESSAGE_NOT_READABLE(2003, HttpStatus.BAD_REQUEST, "request body 양식에 문제가 있습니다"),

    /**
     * 3000: Server 오류 (INTERNAL_SERVER_ERROR)
     */
    SERVER_ERROR(3000, HttpStatus.INTERNAL_SERVER_ERROR, "서버에서 오류가 발생하였습니다."),
    DB_TEMPORARY_UNAVAILABLE(3001, HttpStatus.SERVICE_UNAVAILABLE, "일시적으로 데이터를 처리할 수 없습니다."),
    DB_CONSTRAINT_VIOLATE(3004, HttpStatus.BAD_REQUEST, "DB 무결성에 적합하지 않습니다."),

    /**
     * 4000: Authentication 오류
     */
    UNSUPPORTED_TOKEN_TYPE(4001, HttpStatus.UNAUTHORIZED, "지원되지 않는 토큰 형식입니다."),
    INVALID_TOKEN(4002, HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(4003, HttpStatus.UNAUTHORIZED, "만료된 token 입니다."),
    FORBIDDEN_ORG_MISMATCH(4004, HttpStatus.FORBIDDEN, "요청된 조직과 토큰의 조직이 다릅니다."),
    FORBIDDEN_NO_PERMISSION(4005, HttpStatus.FORBIDDEN, "해당 요청에 대한 권한이 없습니다."),
    CREATOR_CANNOT_CHANGE(4006, HttpStatus.FORBIDDEN, "슈퍼관리자 권한은 바꿀 수 없습니다."),

    /**
     * 5000: User/Member 오류
     */
    INVALID_USER_SIGNUP(5001, HttpStatus.BAD_REQUEST, "회원가입 요청에서 유효하지 않은 값이 존재합니다."),
    DUPLICATE_EMAIL(5002, HttpStatus.BAD_REQUEST, "이미 가입된 회원입니다."),
    INVALID_ORG_CODE(5003, HttpStatus.BAD_REQUEST, "유효하지 않은 조직 코드입니다."),
    INVALID_USER_LOGIN(5004, HttpStatus.BAD_REQUEST, "로그인 요청에서 유효하지 않은 값이 존재합니다"),
    USER_NOT_FOUND(5005, HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),
    INVALID_PASSWORD(5006, HttpStatus.BAD_REQUEST, "비밀번호가 틀렸습니다."),
    PASSWORD_MISMATCH(5007, HttpStatus.BAD_REQUEST, "비밀번호 확인 문자열이 일치하지 않습니다."),
    PASSWORD_SAME_AS_CURRENT(5008, HttpStatus.BAD_REQUEST, "새 비밀번호가 현재 비밀번호와 일치합니다."),
    MEMBER_NOT_FOUND(5009, HttpStatus.BAD_REQUEST, "존재하지 않는 멤버입니다."),
    ALREADY_APPROVED_MEMBER(5010, HttpStatus.BAD_REQUEST, "이미 해당 조직에 가입 승인 처리된 멤버입니다."),
    ALREADY_REJECTED_MEMBER(5011, HttpStatus.BAD_REQUEST, "이미 해당 조직에 가입 거절 처리된 멤버입니다."),
    ALREADY_REQUESTED_MEMBER(5012, HttpStatus.BAD_REQUEST, "이미 가입 요청을 보낸 멤버입니다. 관리자 승인을 기다려주세요."),
    MEMBER_NOT_IN_ORGANIZATION(5013, HttpStatus.NOT_FOUND, "해당 조직에서 찾을 수 없는 멤버입니다."),

    /**
     * 6000: Comment 오류
     */
    COMMENT_NOT_FOUND(6001, HttpStatus.NOT_FOUND, "존재하지 않는 댓글입니다."),
    COMMENT_UNAUTHORIZED_DELETE(6002, HttpStatus.FORBIDDEN, "본인의 댓글만 삭제할 수 있습니다."),
    INVALID_COMMENT_REQUEST(6003, HttpStatus.BAD_REQUEST, "댓글 조회 요청에서 올바르지 않은 값이 존재합니다."),
    VIDEO_COMMENT_NOT_ALLOWED(6004, HttpStatus.BAD_REQUEST, "댓글을 허용하지 않는 영상입니다."),
    INVALID_COMMENT_CREATE(6005, HttpStatus.BAD_REQUEST, "댓글 생성 요청에서 유효하지 않은 값이 존재합니다."),
    PARENT_COMMENT_NOT_FOUND(6006, HttpStatus.NOT_FOUND, "대댓글의 대상 댓글을 찾을 수 없습니다."),

    /**
     * 7000: Organization 오류
     */
    INVALID_ORG_CREATE(7001, HttpStatus.BAD_REQUEST, "조직 생성 요청에서 유효하지 않은 값이 존재합니다."),
    DUPLICATE_ORG_NAME(7002, HttpStatus.BAD_REQUEST, "조직 이름은 중복이 불가능합니다."),
    INVALID_ORG_JOIN(7003, HttpStatus.BAD_REQUEST, "조직 가입 요청에서 유효하지 않은 값이 존재합니다."),
    ORGANIZATION_NOT_FOUND(7004, HttpStatus.NOT_FOUND, "존재하지 않는 조직입니다."),
    INVALID_ORG_SELECT(7005, HttpStatus.BAD_REQUEST, "조직 선택 요청에서 유효하지 않은 값이 존재합니다."),
    INVALID_ORG_EXIT(7006, HttpStatus.BAD_REQUEST, "조직 탈퇴 요청에서 유효하지 않은 값이 존재합니다."),
    ORGANIZATION_CODE_IN_USE(7007, HttpStatus.CONFLICT, "조직 코드가 이미 사용중입니다."),
    ORG_CODE_NOT_AVAILABLE(7008, HttpStatus.NOT_FOUND, "조직 코드를 찾을 수 없습니다. 잠시 후 다시 시도해주세요."),

    /**
     * 8000: Video 오류
     */
    VIDEO_NOT_FOUND(8001, HttpStatus.NOT_FOUND, "존재하지 않는 영상입니다."),
    VIDEO_NOT_IN_ORGANIZATION(8002, HttpStatus.NOT_FOUND, "해당 조직에서 찾을 수 없는 영상입니다."),
    VIDEO_ALREADY_WATCHED(8003, HttpStatus.CONFLICT, "해당 영상은 이미 시청 중입니다."),
    VIDEO_NOT_ACCESSIBLE(8004, HttpStatus.BAD_REQUEST, "해당 영상에 접근 권한이 없습니다."),
    INVALID_VIDEO_LEAVE(8005, HttpStatus.BAD_REQUEST, "영상 시청 종료 요청에서 유효하지 않은 값이 존재합니다."),
    INVALID_SCRAP_REQUEST(8006, HttpStatus.BAD_REQUEST, "영상 스크랩 요청에서 올바르지 않은 값이 존재합니다."),
    VIDEO_ALREADY_SCRAPPED(8007, HttpStatus.BAD_REQUEST, "해당 영상은 이미 스크랩되었습니다."),
    VIDEO_NOT_SCRAPPED(8008, HttpStatus.NOT_FOUND, "해당 영상은 스크랩되어있지 않습니다."),

    /**
     * 9000: History 오류
     */
    HISTORY_NOT_FOUND(9001, HttpStatus.NOT_FOUND, "시청 내역을 찾을 수 없습니다."),

    /**
     * 10000: Quiz 오류
     */
    INVALID_SOLVE_REQUEST(10001, HttpStatus.BAD_REQUEST, "퀴즈 풀이 요청에서 유효하지 않은 값이 존재합니다."),
    INVALID_QUIZ_REQUEST(10002, HttpStatus.BAD_REQUEST, "퀴즈 요청에서 올바르지 않은 값이 존재합니다."),
    ALREADY_SOLVED_QUIZ(10003, HttpStatus.BAD_REQUEST, "이미 제출 이력이 있는 퀴즈가 포함되어있습니다."),
    QUIZ_NOT_FOUND(10004, HttpStatus.NOT_FOUND, "퀴즈를 찾을 수 없습니다."),

    /***
     * 11000: MultipartFile 오류
     */
    IS_NOT_IMAGE_FILE(11001, HttpStatus.UNSUPPORTED_MEDIA_TYPE, "지원되는 이미지 파일의 형식이 아닙니다."),
    MULTIPARTFILE_CONVERT_FAIL_IN_MEMORY(11002, HttpStatus.INTERNAL_SERVER_ERROR,
            "multipartFile memory 변환 과정에서 문제가 생겼습니다."),

    /**
     * 12000: MemberGroup 오류
     */
    MEMBER_GROUP_ALREADY_EXIST(12001, HttpStatus.BAD_REQUEST, "이미 존재하는 멤버 그룹명입니다.");

    private final int code;
    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public HttpStatus getStatus() {
        return httpStatus;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
