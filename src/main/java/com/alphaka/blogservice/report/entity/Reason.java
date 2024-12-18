package com.alphaka.blogservice.report.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Reason {

    // 게시글 및 댓글
    SPAM_ADVERTISEMENT("스팸홍보/도배글입니다."),
    OBSCENE_CONTENT("음란물/선정적인 내용입니다."),
    ILLEGAL_INFORMATION("불법정보를 포함하고 있습니다."),
    HATE_SPEECH("혐오스런 발언이 포함되어 있습니다."),
    VIOLATION_OF_PRIVACY("개인정보를 침해하고 있습니다."),
    FALSE_INFORMATION("사실과 다른 정보를 제공하고 있습니다."),

    // 사용자
    INAPPROPRIATE_NICKNAME("부적절한 닉네임을 사용하고 있습니다."),
    INAPPROPRIATE_PROFILE("부적절한 프로필 사진을 사용하고 있습니다."),
    INAPPROPRIATE_BIO("부적절한 자기소개를 사용하고 있습니다."),

    // 기타
    OTHER("기타");

    private final String message;
}
