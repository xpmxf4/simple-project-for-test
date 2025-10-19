package com.concurrency.shop.domain.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "users")
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserGrade grade;

    @Column(nullable = false)
    private Long pointBalance = 0L;

    public User(String username, String email, UserGrade grade, Long initialPoints) {
        this.username = username;
        this.email = email;
        this.grade = grade;
        this.pointBalance = initialPoints;
    }

    public void usePoints(Long points) {
        if (this.pointBalance < points) {
            throw new IllegalStateException(
                String.format("포인트가 부족합니다. 현재: %d, 필요: %d", this.pointBalance, points)
            );
        }
        this.pointBalance -= points;
    }

    public void addPoints(Long points) {
        this.pointBalance += points;
    }

    public Long calculateRewardPoints(Long orderAmount) {
        return (long) (orderAmount * this.grade.getPointRewardRate() / 100);
    }
}
