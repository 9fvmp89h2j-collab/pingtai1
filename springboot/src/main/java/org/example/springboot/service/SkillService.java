package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.example.springboot.dto.response.SkillNameResponseDTO;
import org.example.springboot.entity.Skill;
import org.example.springboot.mapper.SkillMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SkillService {

    @Resource
    private SkillMapper skillMapper;

    public List<SkillNameResponseDTO> listNamesByIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<Skill> w = new LambdaQueryWrapper<>();
        w.in(Skill::getSkillId, ids).select(Skill::getSkillId, Skill::getSkillName);
        List<Skill> rows = skillMapper.selectList(w);
        List<SkillNameResponseDTO> out = new ArrayList<>();
        for (Skill s : rows) {
            SkillNameResponseDTO dto = new SkillNameResponseDTO();
            dto.setSkillId(s.getSkillId());
            dto.setSkillName(s.getSkillName());
            out.add(dto);
        }
        return out;
    }

    public long countAllSkills() {
        return skillMapper.selectCount(null);
    }
}

