package com.concurrency.shop.service.v2.concurrency;


import com.concurrency.shop.domain.point.PointHistory;
import com.concurrency.shop.domain.point.PointHistoryRepository;
import com.concurrency.shop.domain.point.PointType;
import com.concurrency.shop.domain.user.User;
import com.concurrency.shop.domain.user.UserRepository;
import com.concurrency.shop.service.v2.PointServiceV2;
import org.assertj.core.api.Assertions;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import support.AbstractConcurrencyTest;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

@DisplayName("포인트 서비스 V2 서비스 통합 테스트")
class PointServiceV2ConcurrencyTest extends AbstractConcurrencyTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PointHistoryRepository pointHistoryRepository;

    @Autowired
    private PointServiceV2 pointServiceV2;

    @Test
    @DisplayName("[정상 케이스] - 같은 유저가 동시에 10회 포인트 사용 요청")
    void use_point_when_request_concurrently() {
        // given
        var userEntity = fixture.giveMeBuilder(User.class)
                .setNull("id")
                .set("pointBalance", 10_000L)
                .sample();

        testTransactionSupport.executeWithNewTx(() -> userRepository.save(userEntity));

        var targetUsePont = 1_000L;
        var targetOrderId = 1L;
        final var executor = Executors.newFixedThreadPool(10);;

        // when
        try {
           var futures = IntStream.range(0, 10)
                    .mapToObj(it -> CompletableFuture.runAsync(() -> {
                        pointServiceV2.usePoints(userEntity.getId(), targetUsePont, targetOrderId);
                    }, executor))
                   .toArray(CompletableFuture[]::new);

           CompletableFuture.allOf(futures).join();
        } finally {
            executor.shutdown();
        }

        // then

        // 1. 포인트 잔액 검증
        var findUser = userRepository.findById(userEntity.getId())
                .orElseThrow();

        Assertions.assertThat(findUser.getPointBalance()).isEqualTo(0L);

        // 2. 포인트 사용 이력 저장 검증
        var historyList = pointHistoryRepository.findByOrderId(targetOrderId);

        Assertions.assertThat(historyList)
                .isNotEmpty()
                .hasSize(10)
                .extracting(
                        PointHistory::getType,
                        PointHistory::getOrderId
                ).containsOnly(
                        Tuple.tuple(PointType.USE, targetOrderId)
                );
    }
}