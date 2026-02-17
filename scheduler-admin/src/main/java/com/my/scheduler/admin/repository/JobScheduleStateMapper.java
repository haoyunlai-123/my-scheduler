package com.my.scheduler.admin.repository;

import com.my.scheduler.admin.domain.JobScheduleState;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface JobScheduleStateMapper {

    JobScheduleState selectByJobId(@Param("jobId") Long jobId);

    int insert(JobScheduleState state);

    /**
     * 乐观更新 next_trigger_time：只有当当前 next_trigger_time == expected 时才更新
     * 用于防重复触发（并发/多线程时只会成功一次）
     */
    int compareAndSetNextTime(@Param("jobId") Long jobId,
                              @Param("expected") LocalDateTime expected,
                              @Param("next") LocalDateTime next);

    /**
     * 拉取到期的 job 状态（next_trigger_time <= now）
     */
    List<JobScheduleState> selectDue(@Param("now") LocalDateTime now,
                                     @Param("limit") int limit);
}
