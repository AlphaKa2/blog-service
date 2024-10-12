package com.alphaka.blogservice.dto.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 사용자 회원가입 이벤트 발생시 자동으로 블로그 생성을 위해 사용되는 이벤트 객체
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserSignUpEvent {
    private Long id;
}
