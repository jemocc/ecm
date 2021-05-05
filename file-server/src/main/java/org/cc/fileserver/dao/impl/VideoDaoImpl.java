package org.cc.fileserver.dao.impl;

import org.cc.common.model.Page;
import org.cc.common.model.PageQuery;
import org.cc.common.model.Pageable;
import org.cc.common.utils.PublicUtil;
import org.cc.common.utils.SequenceGenerator;
import org.cc.fileserver.dao.VideoDao;
import org.cc.fileserver.entity.Video;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Repository
public class VideoDaoImpl implements VideoDao {

    private final JdbcTemplate jdbcTemplate;

    public VideoDaoImpl(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public int save(Video video) {
        final String sql = "insert into video (name, type, uri, create_at, form_type, remark1, remark2, cover_uri) values (?,?,?,?,?,?,?,?)";
        final Object[] args = new Object[]{
                video.getName(),video.getType(),video.getUri(),video.getCreateAt(),video.getFormType().name(),video.getRemark1(),video.getRemark2(),video.getCoverUri()
        };
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> PublicUtil.prepare(conn, sql, args), keyHolder);
        return Objects.requireNonNullElse(keyHolder.getKey(), -1).intValue();
    }

    @Override
    public List<Video> save(List<Video> videos) {
        long batchSeq = SequenceGenerator.newUSeq();
        String sql = "insert into video (name, type, uri, create_at, form_type, remark1, remark2, cover_uri, batch_seq) values (?,?,?,?,?,?,?,?,?)";
        List<Object[]> args = videos.stream().map(v -> new Object[]{
                v.getName(),v.getType(),v.getUri(),v.getCreateAt(),v.getFormType().name(),v.getRemark1(),v.getRemark2(),v.getCoverUri(),batchSeq
        }).collect(Collectors.toList());
        jdbcTemplate.batchUpdate(sql, args);

        String sql2 = "select * from video where batch_seq = ?";
        return jdbcTemplate.query(sql2, new Object[]{batchSeq}, new BeanPropertyRowMapper<>(Video.class));
    }

    @Override
    public Video queryOne(Integer id) {
        String sql2 = "select * from video where id = ?";
        return jdbcTemplate.queryForObject(sql2, new Object[]{id}, new BeanPropertyRowMapper<>(Video.class));
    }

    @Override
    public Page<Video> queryAll(Pageable pageable) {
        return PageQuery.of(pageable, "select * from video order by id asc").exec(jdbcTemplate, Video.class);
    }

    @Override
    public List<Video> queryAllWithoutCacheCover(Pageable pageable) {
        String sql = Pageable.warp(pageable, "select * from video where cover_uri like 'http%'");
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Video.class));
    }

    @Override
    public Integer delRepeat() {
        String sql = "delete from video where name in (select name from (select name,count(*) c from video group by name) t where t.c > 1)and id not in (select mid from (select min(id) mid,count(*) c from video group by name) t where t.c > 1)";
        return jdbcTemplate.update(sql);
    }
}
