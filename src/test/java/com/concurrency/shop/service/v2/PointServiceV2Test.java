package com.concurrency.shop.service.v2;


import com.concurrency.shop.domain.point.PointHistory;
import com.concurrency.shop.domain.point.PointHistoryRepository;
import com.concurrency.shop.domain.user.User;
import com.concurrency.shop.domain.user.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import support.AbstractTest;
import support.AutoMockExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;

@ExtendWith(AutoMockExtension.class)
class PointServiceV2Test extends AbstractTest {

    @InjectMocks
    private PointServiceV2 sut;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PointHistoryRepository pointHistoryRepository;

    @Test
    void usePointsWithException() {
        // given
        BDDMockito.given(
                userRepository.findById(anyLong())
        ).willReturn(Optional.empty());

        var targetUserId = 1L;

        // when & then
        Assertions.assertThatThrownBy(() -> sut.usePoints(targetUserId,1L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(String.format("사용자를 찾을 수 없습니다: %d", targetUserId));

        BDDMockito.then(userRepository)
                .should()
                .findById(anyLong());

        BDDMockito.then(pointHistoryRepository)
                .should(never())
                .save(any(PointHistory.class));
    }

    @Test
    void usePoints() {
        // given
        BDDMockito.given(
                userRepository.findById(anyLong())
        ).willReturn(Optional.of(fixture.giveMeOne(User.class)));

        // when
        sut.usePoints(1L,1L, 1L);

        // then
        BDDMockito.then(userRepository)
                .should()
                .findById(anyLong());

        BDDMockito.then(pointHistoryRepository)
                .should()
                .save(any(PointHistory.class));
    }
}