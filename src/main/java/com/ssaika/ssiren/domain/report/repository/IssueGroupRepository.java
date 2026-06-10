package com.ssaika.ssiren.domain.report.repository;

import com.ssaika.ssiren.domain.report.entity.IssueGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface IssueGroupRepository extends JpaRepository<IssueGroup, Long> {

    // 동시 수동 병합 과정 존재함. ForUpdate 추후 고려
    // @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from IssueGroup i where i.id in :ids")
    List<IssueGroup> findAllByIdIn(@Param("ids") Collection<Long> ids);
}
