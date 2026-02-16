package com.my.scheduler.admin.repository;

import com.my.scheduler.admin.domain.ExecutorNode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ExecutorNodeMapper {

    ExecutorNode selectByAddress(@Param("address") String address);

    ExecutorNode selectById(@Param("id") Long id);

    int insert(ExecutorNode node);

    int updateHeartbeat(@Param("id") Long id,
                        @Param("lastHeartbeatAt") java.time.LocalDateTime lastHeartbeatAt,
                        @Param("status") String status);

    int updateOnRegister(@Param("id") Long id,
                         @Param("name") String name,
                         @Param("meta") String meta,
                         @Param("lastHeartbeatAt") java.time.LocalDateTime lastHeartbeatAt,
                         @Param("status") String status);
}
