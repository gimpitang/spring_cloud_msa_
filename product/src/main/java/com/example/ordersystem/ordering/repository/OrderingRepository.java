package com.example.ordersystem.ordering.repository;

import com.example.ordersystem.member.entity.Member;
import com.example.ordersystem.ordering.entity.Ordering;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderingRepository extends JpaRepository<Ordering, Long> {
    List<Ordering> findByMember(Member member);
}
