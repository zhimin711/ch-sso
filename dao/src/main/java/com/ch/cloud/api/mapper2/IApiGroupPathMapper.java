package com.ch.cloud.api.mapper2;

import com.ch.cloud.api.dto.ApiGroupPathDTO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * desc: IApiGroupMapper
 * </p>
 *
 * @author Zhimin.Ma
 * @since 2022/10/14
 */
@Mapper
public interface IApiGroupPathMapper {
    
    @Select("select max(sort) from rt_api_group_path where group_id=#{groupId}")
    Integer maxSortByGroupId(Long groupId);
    
    @Select("select path_id from rt_api_group_path where group_id=#{groupId} order by sort,path_id")
    List<Long> listPathIds(Long groupId);
    
//    @Select("select path_id from rt_api_group_path where group_id=#{groupId} order by sort,path_id")
    @Select("<script>select group_id, path_id, sort from rt_api_group_path where group_id in "
            + " <foreach item=\"item\" index=\"index\" collection=\"groupIds\" open=\"(\" separator=\",\" close=\")\">"
            + " #{item} </foreach> order by sort,path_id</script>")
    List<ApiGroupPathDTO> listPathIdsByGroupIds(List<Long> groupIds);
    
    @Select("<script>select t2.id from rt_api_group_path t1 inner join bt_api_group t2 on t1.group_id = t2.id"
            + " where t1.path_id=#{pathId}" +
            " <if test='groupType!=null' >and t2.type=#{groupType}</if></script>")
    List<Long> listGroupIdsByPathIdAndGroupType(Long pathId, String groupType);
    
    @Select("select count(1) from rt_api_group_path where group_id=#{groupId} and path_id=#{pathId}")
    long count(Long groupId, Long pathId);
    
    @Delete("delete from rt_api_group_path where group_id=#{groupId} and path_id=#{pathId}")
    int remove(Long groupId, Long pathId);
    
    @Insert("insert ignore rt_api_group_path(group_id,path_id,sort) values(#{groupId},#{pathId},#{sort})")
    int insert(Long groupId, Long pathId, Integer sort);
}
