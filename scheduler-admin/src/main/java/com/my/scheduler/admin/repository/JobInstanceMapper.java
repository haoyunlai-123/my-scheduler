package com.my.scheduler.admin.repository;

import com.my.scheduler.admin.domain.JobInstance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface JobInstanceMapper {

    int insert(JobInstance ins);

    JobInstance selectById(@Param("id") Long id);

    List<JobInstance> selectByJobId(@Param("jobId") Long jobId, @Param("limit") int limit);

    List<JobInstance> selectWaitingForDispatch(@Param("limit") int limit);

    /**
     * admin接收到executor的接收任务响应，将任务状态改为running
     * @param id
     * @param executorId
     * @param startTime
     * @return
     */
    int markRunning(@Param("id") Long id,
                    @Param("executorId") Long executorId,
                    @Param("startTime") LocalDateTime startTime);

    /**
     * executor执行完成后，admin接收到executor的执行结果响应，将任务状态改为success或failed
     * @param id
     * @param status
     * @param endTime
     * @param durationMs
     * @param lastError
     * @return
     */
    int markFinished(@Param("id") Long id,
                     @Param("status") String status,
                     @Param("endTime") LocalDateTime endTime,
                     @Param("durationMs") Long durationMs,
                     @Param("lastError") String lastError);

}
