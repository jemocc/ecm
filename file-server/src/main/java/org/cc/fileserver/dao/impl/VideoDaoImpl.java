package org.cc.fileserver.dao.impl;

import org.cc.fileserver.dao.VideoDao;
import org.cc.fileserver.entity.Video;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class VideoDaoImpl implements VideoDao {

    private final JdbcTemplate jdbcTemplate;

    public VideoDaoImpl(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public int save(Video video) {
        return save(Collections.singletonList(video));
    }

    @Override
    public int save(List<Video> videos) {
        String sql = "insert into video (name, type, vf_uri, v_cover, create_at, form_type, remark1, remark2) values (?,?,?,?,?,?,?,?))";
        List<Object[]> args = videos.stream().map(v -> new Object[]{
                v.getName(),v.getType(),v.getVfUri(),v.getvCover(),v.getCreateAt(),v.getFormType(),v.getRemark1(),v.getRemark2()
        }).collect(Collectors.toList());
        return Arrays.stream(jdbcTemplate.batchUpdate(sql, args)).reduce(Integer::sum).orElse(-1);
    }
}
