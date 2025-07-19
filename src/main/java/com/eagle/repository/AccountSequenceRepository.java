package com.eagle.repository;

import com.eagle.entity.AccountSequence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AccountSequenceRepository extends JpaRepository<AccountSequence, Long>  {
    @Query(value = "SELECT NEXTVAL('acc_seq')", nativeQuery = true)
    Long getNextSequenceValue();
}
