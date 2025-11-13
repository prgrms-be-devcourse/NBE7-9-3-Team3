package org.example.backend.domain.point.repository;

import org.example.backend.domain.member.entity.Member;
import org.example.backend.domain.point.entity.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PointRepository extends JpaRepository<Point, Long> {

    List<Point> findAllByMemberOrderByCreateDateDesc(Member member);
}
