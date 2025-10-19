package com.concurrency.shop.learning;


import com.concurrency.shop.domain.user.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import support.AbstractTest;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Fixture Monkey 사용법 숙지 테스트")
public class FixtureMonkeyLearning extends AbstractTest {

    @Test
    void make_testObj_with_fixtureMonkey() {
        // given - 상황 만들기
        var userEntity = fixture.giveMeBuilder(User.class)
                                .setNull("id")
                                .set("pointBalance", 10_000L)
                                .sample();

        // then - 검증
        var mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        try {
            System.out.println("userEntity =\n" + mapper.writeValueAsString(userEntity));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        assertThat(userEntity).isNotNull();        assertThat(userEntity).isNotNull();
        assertThat(userEntity.getPointBalance()).isEqualTo(10_000L);
    }
}
