package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.example.springboot.dto.response.AcupointKnowledgeResponseDTO;
import org.example.springboot.dto.response.CopperManDailyCaseResponseDTO;
import org.example.springboot.dto.response.CopperManDiscoverResponseDTO;
import org.example.springboot.entity.AcupointKnowledge;
import org.example.springboot.entity.UserCopperManProfile;
import org.example.springboot.exception.BusinessException;
import org.example.springboot.mapper.AcupointKnowledgeMapper;
import org.example.springboot.mapper.UserAcupointDailyProgressMapper;
import org.example.springboot.mapper.UserCopperManProfileMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class CopperManService {
    private static final int FEATURED_COUNT = 5;
    private static final int TARGET_COUNT = 3;

    @Resource
    private AcupointKnowledgeMapper acupointKnowledgeMapper;

    @Resource
    private UserAcupointDailyProgressMapper progressMapper;

    @Resource
    private UserCopperManProfileMapper profileMapper;

    @Autowired(required = false)
    private GameStateService gameStateService;

    public List<AcupointKnowledgeResponseDTO> listAcupoints(Long userId) {
        Set<String> discovered = userId == null
                ? Set.of()
                : new LinkedHashSet<>(progressMapper.selectDistinctCodes(userId));
        return loadEnabledAcupoints().stream()
                .map(point -> toDto(point, discovered.contains(point.getCode())))
                .toList();
    }

    public CopperManDailyCaseResponseDTO getDailyCase(Long userId) {
        return getDailyCase(userId, LocalDate.now());
    }

    CopperManDailyCaseResponseDTO getDailyCase(Long userId, LocalDate caseDate) {
        List<AcupointKnowledge> featured = featuredFor(caseDate);
        List<String> targetCodes = featured.stream().limit(TARGET_COUNT).map(AcupointKnowledge::getCode).toList();
        List<String> discoveredCodes = new ArrayList<>();
        UserCopperManProfile profile = null;

        if (userId != null) {
            profileMapper.ensureProfile(userId);
            discoveredCodes = filterTargets(progressMapper.selectCodes(userId, caseDate), targetCodes);
            profile = profileMapper.selectById(userId);
        }

        return CopperManDailyCaseResponseDTO.builder()
                .caseDate(caseDate)
                .featuredPoints(featured.stream().map(this::toDto).toList())
                .targetCodes(targetCodes)
                .discoveredCodes(discoveredCodes)
                .totalTargets(TARGET_COUNT)
                .copperTokens(valueOrZero(profile == null ? null : profile.getCopperTokens()))
                .starSand(valueOrZero(profile == null ? null : profile.getStarSand()))
                .completedCases(valueOrZero(profile == null ? null : profile.getCompletedCases()))
                .persisted(userId != null)
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    public CopperManDiscoverResponseDTO discover(Long userId, String rawCode) {
        return discover(userId, rawCode, LocalDate.now());
    }

    CopperManDiscoverResponseDTO discover(Long userId, String rawCode, LocalDate caseDate) {
        String code = rawCode == null ? "" : rawCode.trim().toUpperCase();
        List<String> targetCodes = featuredFor(caseDate).stream()
                .limit(TARGET_COUNT)
                .map(AcupointKnowledge::getCode)
                .toList();
        if (!targetCodes.contains(code)) {
            throw new BusinessException("该星点不在今日探案任务中");
        }

        if (userId == null) {
            return CopperManDiscoverResponseDTO.builder()
                    .discoveredCodes(List.of(code))
                    .completed(false)
                    .newlyCompleted(false)
                    .persisted(false)
                    .copperTokens(0)
                    .starSand(0)
                    .completedCases(0)
                    .rewardCopperTokens(0)
                    .rewardStarSand(0)
                    .build();
        }

        profileMapper.ensureProfile(userId);
        progressMapper.insertIgnore(userId, caseDate, code);
        List<String> discoveredCodes = filterTargets(progressMapper.selectCodes(userId, caseDate), targetCodes);
        boolean completed = discoveredCodes.containsAll(targetCodes);
        boolean newlyCompleted = completed && (gameStateService != null
                ? gameStateService.completeCopperManCase(userId, caseDate)
                : profileMapper.awardDailyCase(userId, caseDate) == 1);
        UserCopperManProfile profile = profileMapper.selectById(userId);

        return CopperManDiscoverResponseDTO.builder()
                .discoveredCodes(discoveredCodes)
                .completed(completed)
                .newlyCompleted(newlyCompleted)
                .persisted(true)
                .copperTokens(valueOrZero(profile.getCopperTokens()))
                .starSand(valueOrZero(profile.getStarSand()))
                .completedCases(valueOrZero(profile.getCompletedCases()))
                .rewardCopperTokens(newlyCompleted ? 2 : 0)
                .rewardStarSand(newlyCompleted ? 5 : 0)
                .build();
    }

    private List<AcupointKnowledge> featuredFor(LocalDate caseDate) {
        List<AcupointKnowledge> all = loadModeledAcupoints();
        if (all.size() < FEATURED_COUNT) {
            throw new BusinessException("小铜人穴位知识尚未准备完成");
        }
        int start = Math.floorMod(caseDate.toEpochDay(), all.size());
        List<AcupointKnowledge> featured = new ArrayList<>(FEATURED_COUNT);
        for (int i = 0; i < FEATURED_COUNT; i++) {
            featured.add(all.get((start + i) % all.size()));
        }
        return featured;
    }

    private List<AcupointKnowledge> loadEnabledAcupoints() {
        LambdaQueryWrapper<AcupointKnowledge> query = new LambdaQueryWrapper<>();
        query.eq(AcupointKnowledge::getEnabled, true)
                .orderByAsc(AcupointKnowledge::getSortOrder);
        return acupointKnowledgeMapper.selectList(query);
    }

    private List<AcupointKnowledge> loadModeledAcupoints() {
        LambdaQueryWrapper<AcupointKnowledge> query = new LambdaQueryWrapper<>();
        query.eq(AcupointKnowledge::getEnabled, true)
                .isNotNull(AcupointKnowledge::getPositionX)
                .isNotNull(AcupointKnowledge::getPositionY)
                .isNotNull(AcupointKnowledge::getPositionZ)
                .orderByAsc(AcupointKnowledge::getSortOrder);
        return acupointKnowledgeMapper.selectList(query);
    }

    private List<String> filterTargets(List<String> discovered, List<String> targets) {
        Set<String> ordered = new LinkedHashSet<>();
        for (String target : targets) {
            if (discovered.contains(target)) {
                ordered.add(target);
            }
        }
        return new ArrayList<>(ordered);
    }

    private int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }

    private AcupointKnowledgeResponseDTO toDto(AcupointKnowledge point) {
        return toDto(point, false);
    }

    private AcupointKnowledgeResponseDTO toDto(AcupointKnowledge point, boolean discovered) {
        return AcupointKnowledgeResponseDTO.builder()
                .code(point.getCode())
                .pointNumber(point.getPointNumber())
                .name(point.getName())
                .pinyin(point.getPinyin())
                .meridianCode(point.getMeridianCode())
                .meridianName(point.getMeridianName())
                .modelId(point.getModelId())
                .bodyArea(point.getBodyArea())
                .childLocation(point.getChildLocation())
                .childDescription(point.getChildDescription())
                .childTraditionalUse(point.getChildTraditionalUse())
                .safetyTip(point.getSafetyTip())
                .sourceName(point.getSourceName())
                .sourceLink(point.getSourceLink())
                .traditionalUseSourceName(point.getTraditionalUseSourceName())
                .traditionalUseSourceLink(point.getTraditionalUseSourceLink())
                .positionX(point.getPositionX())
                .positionY(point.getPositionY())
                .positionZ(point.getPositionZ())
                .discovered(discovered)
                .build();
    }
}
