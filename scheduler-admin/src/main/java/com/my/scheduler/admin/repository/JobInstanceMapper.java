package com.my.scheduler.admin.repository;

import com.my.scheduler.admin.domain.JobInstance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface JobInstanceMapper {

    int insert(JobInstance ins);

    JobInstance selectById(@Param("id") Long id);

    List<JobInstance> selectByJobId(@Param("jobId") Long jobId, @Param("limit") int limit);
}
