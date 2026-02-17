package com.my.scheduler.admin.repository;

import com.my.scheduler.admin.domain.ExecutorNode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

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

    /**
     * 查询在线的执行器节点，并按照心跳时间降序排序，限制返回数量
     * @param limit
     * @return
     */
    List<ExecutorNode> selectOnlineOrderByHeartbeatDesc(@Param("limit") int limit);


    int markOfflineBefore(@Param("threshold") LocalDateTime threshold);

}
