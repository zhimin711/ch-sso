package com.ch.cloud.api.mapper2;

import com.ch.cloud.api.dto.ApiProjectRoleDTO;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * decs:
 *
 * @author Zhimin.Ma
 * @date 2019/11/6
 */
@Mapper
public interface IApiProjectUserMapper {

    @Select("select PROJECT_ID from rt_api_project_user where USER_ID=#{userId}")
    List<Long> findProjectIdsByUserId(String userId);
    
    @Select("select USER_ID,PROJECT_ID,ROLE from rt_api_project_user where USER_ID=#{userId} and PROJECT_ID=#{projectId}")
    ApiProjectRoleDTO findByUserIdAndProjectId(String userId, Long projectId);

    //    @Select("select t1.CODE from rt_project_user t inner join bt_project_code t1 on t.PROJECT_ID = t1.ID where USER_ID=#{userId}")
    @Select("select CONCAT(IF (t1.PARENT_CODE IS NULL or t1.PARENT_CODE = '', '', CONCAT(t1.PARENT_CODE, ':')), t1.`CODE`) PROJECT_CODE" +
            " from rt_api_project_user t inner join bt_project t1 on t.PROJECT_ID = t1.ID where t.USER_ID=#{userId}")
    List<String> findProjectCodesByUserId(String userId);

    @Select("select USER_ID from rt_api_project_user where PROJECT_ID=#{projectId}")
    List<String> findUserIdsByProjectId(Long projectId);

    @Delete("delete from rt_api_project_user where PROJECT_ID = #{projectId}")
    int deleteUserIds(Long projectId);

    @Select("select CONCAT(IF(t1.PARENT_CODE IS NULL or t1.PARENT_CODE = '', '', CONCAT(t1.PARENT_CODE, ',')),CONCAT(IF (t1.PARENT_CODE IS NULL or t1.PARENT_CODE = '', '', CONCAT(t1.PARENT_CODE, ':')), t1.`CODE`)) PROJECT_CODE" +
            " from rt_api_project_user t inner join bt_project t1 on t.PROJECT_ID = t1.ID where t.USER_ID=#{userId}")
    List<String> findProjectCodesContainsParentCodeByUserId(String username);

    @Select("select count(1) from rt_api_project_user where PROJECT_ID=#{projectId} and USER_ID=#{userId}")
    int exists(@Param("projectId") Long projectId, @Param("userId") String userId);

    /**
     * 插入访客
     */
    @Insert("INSERT INTO rt_api_project_user (PROJECT_ID, USER_ID, ROLE) VALUES (#{projectId}, #{userId}, #{role})")
    int insertVisitor(@Param("projectId") Long projectId, @Param("userId") String userId, @Param("role") String role);

    /**
     * 删除访客
     */
    @Delete("DELETE FROM rt_api_project_user WHERE PROJECT_ID = #{projectId} AND USER_ID = #{userId} AND ROLE = #{role}")
    int deleteVisitor(@Param("projectId") Long projectId, @Param("userId") String userId, @Param("role") String role);

    /**
     * 查询项目所有访客userId
     */
    @Select("SELECT USER_ID FROM rt_api_project_user WHERE PROJECT_ID = #{projectId} AND ROLE = #{role}")
    List<String> findVisitorUserIds(@Param("projectId") Long projectId, @Param("role") String role);

//    int insertUserIds(@Param("projectId") Long projectId, @Param("userIds")  List<String> userIds);
}
