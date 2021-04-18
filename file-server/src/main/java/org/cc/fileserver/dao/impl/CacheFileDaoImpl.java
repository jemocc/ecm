package org.cc.fileserver.dao.impl;

import org.cc.fileserver.dao.CacheFileDao;
import org.cc.fileserver.entity.CacheFile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class CacheFileDaoImpl implements CacheFileDao {
    private final JdbcTemplate jdbcTemplate;

    public CacheFileDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public int save(CacheFile cacheFile) {
        return save(Collections.singletonList(cacheFile));
    }

    @Override
    public int save(List<CacheFile> cacheFiles) {
        String sql = "insert into file (name, type, uri, create_at, form_type, remark1, remark2) values (?,?,?,?,?,?,?))";
        List<Object[]> args = cacheFiles.stream().map(f -> new Object[]{
                f.getName(),f.getType(),f.getUri(),f.getCreateAt(),f.getFormType(),f.getRemark1(), f.getRemark2()
        }).collect(Collectors.toList());
        return Arrays.stream(jdbcTemplate.batchUpdate(sql, args)).reduce(Integer::sum).orElse(-1);
    }
}
