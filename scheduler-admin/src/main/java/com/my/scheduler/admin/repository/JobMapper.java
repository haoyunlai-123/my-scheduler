package com.my.scheduler.admin.repository;

import com.my.scheduler.admin.domain.Job;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface JobMapper {

    int insert(Job job);

    int update(Job job);

    Job selectById(@Param("id") Long id);

    List<Job> selectAll();

    int updateEnabled(@Param("id") Long id, @Param("enabled") int enabled);

    List<Job> selectEnabled();
}
