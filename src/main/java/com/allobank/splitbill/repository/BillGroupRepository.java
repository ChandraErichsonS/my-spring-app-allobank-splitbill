package com.allobank.splitbill.repository;

import com.allobank.splitbill.entity.BillGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BillGroupRepository extends JpaRepository<BillGroup, Long> {

    @Query("""
        select distinct g
        from BillGroup g
        left join fetch g.participants
        where g.id = :id
    """)
    Optional<BillGroup> findByIdWithParticipants(@Param("id") Long id);
}