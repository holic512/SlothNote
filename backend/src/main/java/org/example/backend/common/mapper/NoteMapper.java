package org.example.backend.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.backend.common.domain.Note;

import java.util.List;

@Mapper
public interface NoteMapper extends BaseMapper<Note> {

    @Select("""
            SELECT note_id, content, last_saved_at, created_at, updated_at
            FROM note_content
            WHERE content LIKE CONCAT('%', #{q}, '%')
            """)
    List<Note> findByContentLike(String q);
}
