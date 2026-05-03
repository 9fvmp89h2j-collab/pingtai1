package org.example.springboot.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.example.springboot.dto.command.TrainGameAdminCommandDTO;
import org.example.springboot.dto.response.TrainGameResponseDTO;
import org.example.springboot.entity.TrainGame;
import org.example.springboot.exception.BusinessException;
import org.example.springboot.mapper.TrainGameMapper;
import org.springframework.stereotype.Service;

@Service
public class TrainGameAdminService {

    @Resource
    private TrainGameMapper trainGameMapper;

    public Page<TrainGameResponseDTO> page(Long current, Long size, String jingluoName) {
        LambdaQueryWrapper<TrainGame> w = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(jingluoName)) {
            w.like(TrainGame::getJingluoName, jingluoName);
        }
        w.orderByDesc(TrainGame::getId);

        Page<TrainGame> page = trainGameMapper.selectPage(new Page<>(current, size), w);
        Page<TrainGameResponseDTO> dtoPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        dtoPage.setRecords(page.getRecords().stream().map(this::toDTO).toList());
        return dtoPage;
    }

    public TrainGameResponseDTO getById(Integer id) {
        TrainGame e = trainGameMapper.selectById(id);
        if (e == null) {
            throw new BusinessException("关卡不存在");
        }
        return toDTO(e);
    }

    public TrainGameResponseDTO create(TrainGameAdminCommandDTO dto) {
        TrainGame e = new TrainGame();
        fillEntity(e, dto.getJingluoName(), dto.getGame1(), dto.getGame2(), dto.getGame3(), dto.getGame4(), dto.getGame5(),
                dto.getGame1Brief(), dto.getGame2Brief(), dto.getGame3Brief(), dto.getGame4Brief(), dto.getGame5Brief());
        trainGameMapper.insert(e);
        return toDTO(e);
    }

    public TrainGameResponseDTO update(Integer id, TrainGameAdminCommandDTO dto) {
        TrainGame e = trainGameMapper.selectById(id);
        if (e == null) {
            throw new BusinessException("关卡不存在");
        }
        fillEntity(e, dto.getJingluoName(), dto.getGame1(), dto.getGame2(), dto.getGame3(), dto.getGame4(), dto.getGame5(),
                dto.getGame1Brief(), dto.getGame2Brief(), dto.getGame3Brief(), dto.getGame4Brief(), dto.getGame5Brief());
        trainGameMapper.updateById(e);
        return toDTO(e);
    }

    public void delete(Integer id) {
        trainGameMapper.deleteById(id);
    }

    private void fillEntity(TrainGame e,
                            String jingluoName,
                            String game1, String game2, String game3, String game4, String game5,
                            String game1Brief, String game2Brief, String game3Brief, String game4Brief, String game5Brief) {
        e.setJingluoName(jingluoName);
        e.setGame1(game1);
        e.setGame2(game2);
        e.setGame3(game3);
        e.setGame4(game4);
        e.setGame5(game5);
        e.setGame1Brief(game1Brief);
        e.setGame2Brief(game2Brief);
        e.setGame3Brief(game3Brief);
        e.setGame4Brief(game4Brief);
        e.setGame5Brief(game5Brief);
    }

    private TrainGameResponseDTO toDTO(TrainGame e) {
        TrainGameResponseDTO dto = new TrainGameResponseDTO();
        dto.setId(e.getId());
        dto.setJingluoName(e.getJingluoName());
        dto.setGame1(e.getGame1());
        dto.setGame2(e.getGame2());
        dto.setGame3(e.getGame3());
        dto.setGame4(e.getGame4());
        dto.setGame5(e.getGame5());
        dto.setGame1Brief(e.getGame1Brief());
        dto.setGame2Brief(e.getGame2Brief());
        dto.setGame3Brief(e.getGame3Brief());
        dto.setGame4Brief(e.getGame4Brief());
        dto.setGame5Brief(e.getGame5Brief());
        return dto;
    }
}
