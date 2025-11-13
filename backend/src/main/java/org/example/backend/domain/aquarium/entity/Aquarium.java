package org.example.backend.domain.aquarium.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.backend.domain.member.entity.Member;
import org.example.backend.global.jpa.entity.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Aquarium extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id")
  private Member member;

  @Column(length = 50)
  private String name;

  // 기본값 = false
  private boolean ownedAquarium;

  @Column(columnDefinition = "int default 0") // 기본값 = 0
  @PositiveOrZero // 0 이상의 숫자만 가능
  private int cycleDate;

  private LocalDateTime lastDate;

  private LocalDateTime nextDate;

  public Aquarium(Member member, String name) {
    this.member = member;
    this.name = name;
  }

  public Aquarium(Member member, String name, boolean ownedAquarium) {
    this.member = member;
    this.name = name;
    this.ownedAquarium = ownedAquarium;
  }

  public void changeSchedule(int cycleDate, LocalDateTime lastDate, LocalDateTime nextDate) {
    this.cycleDate = cycleDate;
    this.lastDate = lastDate;
    this.nextDate = nextDate;
  }

  public void changeName(String name) {
    this.name = name;
  }
}
