package org.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.springboot.entity.QuizStageQuestion;

@Mapper
public interface QuizStageQuestionMapper extends BaseMapper<QuizStageQuestion> {
}
