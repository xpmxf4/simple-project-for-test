package com.concurrency.shop.domain.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserGrade {
    BRONZE(1.0),
    SILVER(2.0),
    GOLD(3.0),
    VIP(5.0);

    private final double pointRewardRate;
}
