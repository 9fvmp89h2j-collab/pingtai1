package org.example.springboot.service;

import org.example.springboot.dto.command.UserRegisterCommandDTO;
import org.example.springboot.dto.command.UserUpdateCommandDTO;
import org.example.springboot.dto.command.AdminUserCreateCommandDTO;
import org.example.springboot.dto.command.AdminUserPasswordResetCommandDTO;
import org.example.springboot.mapper.BadgeMapper;
import org.example.springboot.mapper.UserAcupointDailyProgressMapper;
import org.example.springboot.mapper.UserBackpackMapper;
import org.example.springboot.mapper.UserCollectMapper;
import org.example.springboot.mapper.UserCopperManProfileMapper;
import org.example.springboot.mapper.UserQuizMistakeMapper;
import org.example.springboot.mapper.UserQuizStatsMapper;
import org.example.springboot.entity.User;
import org.example.springboot.exception.BusinessException;
import org.example.springboot.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceAuthorizationTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private BadgeService badgeService;

    @Mock
    private PasswordResetCodeService passwordResetCodeService;

    @Mock
    private BadgeMapper badgeMapper;

    @Mock
    private UserQuizStatsMapper userQuizStatsMapper;

    @Mock
    private UserQuizMistakeMapper userQuizMistakeMapper;

    @Mock
    private UserBackpackMapper userBackpackMapper;

    @Mock
    private UserCollectMapper userCollectMapper;

    @Mock
    private UserAcupointDailyProgressMapper userAcupointDailyProgressMapper;

    @Mock
    private UserCopperManProfileMapper userCopperManProfileMapper;

    @InjectMocks
    private UserService userService;

    @Test
    void publicRegistrationCannotCreateAdministrator() {
        UserRegisterCommandDTO registration = new UserRegisterCommandDTO();
        registration.setUsername("child-user");
        registration.setEmail("parent@example.com");
        registration.setPassword("secure-password");
        registration.setConfirmPassword("secure-password");
        registration.setUserType("ADMIN");

        assertThrows(BusinessException.class, () -> userService.register(registration));

        verify(userMapper, never()).insert(org.mockito.ArgumentMatchers.any(User.class));
    }

    @Test
    void completedOnboardingStartsAtTheSecondAdventureMapLevel() {
        UserRegisterCommandDTO registration = new UserRegisterCommandDTO();
        registration.setUsername("new-detective");
        registration.setEmail("parent@example.com");
        registration.setPassword("secure-password");
        registration.setConfirmPassword("secure-password");
        registration.setUserType("USER");
        registration.setName("小侦探");
        when(userMapper.selectCount(any())).thenReturn(0L);

        userService.register(registration);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).insert(captor.capture());
        User registeredUser = captor.getValue();
        registeredUser.setId(10L);
        when(userMapper.selectById(10L)).thenReturn(registeredUser);

        var map = userService.getUserAdventureMap(10L);

        assertEquals(10, registeredUser.getScore());
        assertEquals("safe-start", map.getCurrentLevelId());
        assertEquals(Boolean.TRUE, map.getLevels().get(0).getCompleted());
        assertEquals(Boolean.TRUE, map.getLevels().get(1).getCurrent());
        assertEquals(Boolean.TRUE, map.getLevels().get(1).getUnlocked());
        assertEquals(Boolean.FALSE, map.getLevels().get(2).getUnlocked());
        assertEquals(Boolean.FALSE, map.getLevels().get(2).getCompleted());
    }

    @Test
    void eachNewAdventureLevelUnlocksOnlyTheNextLevel() {
        User user = new User();
        user.setId(13L);
        user.setScore(20);
        when(userMapper.selectById(13L)).thenReturn(user);

        var map = userService.getUserAdventureMap(13L);

        assertEquals("bamboo", map.getCurrentLevelId());
        assertEquals(Boolean.TRUE, map.getLevels().get(0).getCompleted());
        assertEquals(Boolean.TRUE, map.getLevels().get(1).getCompleted());
        assertEquals(Boolean.TRUE, map.getLevels().get(2).getUnlocked());
        assertEquals(Boolean.TRUE, map.getLevels().get(2).getCurrent());
        assertEquals(Boolean.FALSE, map.getLevels().get(3).getUnlocked());
    }

    @Test
    void legacyRegisteredUserWithoutScoreIsBackfilledToTheSecondAdventureMapLevel() {
        User legacyUser = new User();
        legacyUser.setId(11L);
        when(userMapper.selectById(11L)).thenReturn(legacyUser);

        var map = userService.getUserAdventureMap(11L);

        assertEquals(10, legacyUser.getScore());
        assertEquals("safe-start", map.getCurrentLevelId());
        verify(userMapper).updateById(legacyUser);
    }

    @Test
    void legacyRegisteredUserWithZeroScoreIsBackfilledToTheSecondAdventureMapLevel() {
        User legacyUser = new User();
        legacyUser.setId(12L);
        legacyUser.setScore(0);
        when(userMapper.selectById(12L)).thenReturn(legacyUser);

        var map = userService.getUserAdventureMap(12L);

        assertEquals(10, legacyUser.getScore());
        assertEquals("safe-start", map.getCurrentLevelId());
        verify(userMapper).updateById(legacyUser);
    }

    @Test
    void ordinaryUserCannotPromoteOwnAccount() {
        UserUpdateCommandDTO update = new UserUpdateCommandDTO();
        update.setUserType("ADMIN");
        update.setStatus(1);

        assertThrows(BusinessException.class, () -> userService.updateUser(10L, update, false));

        verify(userMapper, never()).updateById(org.mockito.ArgumentMatchers.any(User.class));
    }

    @Test
    void shuntingRewardUsesFixedDailyAmount() {
        User user = new User();
        user.setId(10L);
        user.setScore(7);
        when(userMapper.selectById(10L)).thenReturn(user);

        userService.claimShuntingReward(10L);

        assertEquals(17, user.getScore());
        verify(userMapper).insertGameReward(eq(10L), eq("shunting"), any(LocalDate.class), eq(10));
        verify(userMapper).updateById(user);
    }

    @Test
    void shuntingRewardCannotBeClaimedTwiceInOneDay() {
        User user = new User();
        user.setId(10L);
        user.setScore(7);
        when(userMapper.selectById(10L)).thenReturn(user);
        when(userMapper.insertGameReward(eq(10L), eq("shunting"), any(LocalDate.class), eq(10)))
                .thenThrow(new DuplicateKeyException("duplicate"));

        assertThrows(BusinessException.class, () -> userService.claimShuntingReward(10L));

        assertEquals(7, user.getScore());
        verify(userMapper, never()).updateById(any(User.class));
    }

    @Test
    void administratorCannotDisableTheAccountUsedForTheRequest() {
        User admin = new User();
        admin.setId(2L);
        admin.setUserType("ADMIN");
        admin.setStatus(1);

        assertThrows(BusinessException.class, () -> userService.adminUpdateStatus(2L, 2L, 0));

        verify(userMapper, never()).updateById(any(User.class));
    }

    @Test
    void administratorCreatedAccountIsAlwaysAnOrdinaryUserWithOnboardingScore() {
        AdminUserCreateCommandDTO command = new AdminUserCreateCommandDTO();
        command.setUsername("created-user");
        command.setEmail("created@example.com");
        command.setPassword("secure-password");
        command.setConfirmPassword("secure-password");
        command.setName("新用户");
        when(userMapper.selectCount(any())).thenReturn(0L);

        userService.adminCreateUser(2L, command);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).insert(captor.capture());
        assertEquals("USER", captor.getValue().getUserType());
        assertEquals(1, captor.getValue().getStatus());
        assertEquals(10, captor.getValue().getScore());
    }

    @Test
    void administratorPasswordResetStoresOnlyAnEncodedPassword() {
        User user = new User();
        user.setId(3L);
        user.setPassword("old-password-hash");
        when(userMapper.selectById(3L)).thenReturn(user);

        AdminUserPasswordResetCommandDTO command = new AdminUserPasswordResetCommandDTO();
        command.setNewPassword("new-secure-password");
        command.setConfirmPassword("new-secure-password");

        userService.adminResetPassword(2L, 3L, command);

        assertNotEquals("new-secure-password", user.getPassword());
        verify(userMapper).updateById(user);
    }

    @Test
    void administratorOverviewDoesNotBackfillLegacyScore() {
        User legacyUser = new User();
        legacyUser.setId(14L);
        legacyUser.setUserType("USER");
        when(userMapper.selectById(14L)).thenReturn(legacyUser);
        when(badgeService.listUserBadges(14L)).thenReturn(java.util.Collections.emptyList());

        var overview = userService.getAdminUserOverview(14L);

        assertEquals(0, overview.getLevel().getCurrentScore());
        assertEquals(1, overview.getAdventureMap().getUserLevel());
        verify(userMapper, never()).updateById(any(User.class));
    }
}
