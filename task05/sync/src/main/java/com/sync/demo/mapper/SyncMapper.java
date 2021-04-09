package com.sync.demo.mapper;


import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by Admin on 2021/4/7.
 */

@Mapper
@Repository
public interface SyncMapper  {

    void executeSql(String sql);

    Set<Integer> getListId(String sql);
}
