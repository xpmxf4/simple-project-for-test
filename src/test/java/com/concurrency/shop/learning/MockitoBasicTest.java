package com.concurrency.shop.learning;

import com.concurrency.shop.domain.user.User;
import com.concurrency.shop.domain.user.UserGrade;
import com.concurrency.shop.domain.user.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
public class MockitoBasicTest {

    @Mock
    private UserRepository userRepository;

    @Test
    void mockito_stubbing_pattern() {
        // given - Mock의 동작읠 정의
        BDDMockito.given(userRepository.findById(1L))
                  .willReturn(Optional.of(new User("aa", "bb@gmail.com", UserGrade.BRONZE, 1000L)));

        // when - 실제 호출
        User user = userRepository.findById(1L).orElseThrow();

        // then - 결과 검증
        Assertions.assertThat(user.getUsername()).isEqualTo("aa");
    }

    @Test
    void mockito_verification_pattern() {
        // given - 상황 만들기
        BDDMockito.given(userRepository.findById(anyLong()))
                  .willReturn(Optional.empty());

        // when - 동작
        userRepository.findById(999L);

        // then - 검증
        then(userRepository)
                .should() // 호출되어야 함
                .findById(anyLong());

        then(userRepository)
                .should(never())
                .save(any(User.class));
    }
}
