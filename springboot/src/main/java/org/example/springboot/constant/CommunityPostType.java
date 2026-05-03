package org.example.springboot.constant;

/**
 * 社区帖子板块（库字段 posttype 存的中文值）
 */
public final class CommunityPostType {

    private CommunityPostType() {
    }

    public static final String QA = "问答专区";
    public static final String SHARE = "分享专区";
    /** 管理员公告，展示在「公告与反馈」Tab 列表 */
    public static final String ANNOUNCE = "公告";
    /** 用户意见反馈，仅出现在后台「反馈消息」 */
    public static final String FEEDBACK = "反馈";

    public static boolean isAllowedForUserNormalPost(String type) {
        return SHARE.equals(type) || QA.equals(type);
    }

    /** 发帖接口允许的四种板块 */
    public static boolean isAnyKnown(String type) {
        return SHARE.equals(type) || QA.equals(type) || ANNOUNCE.equals(type) || FEEDBACK.equals(type);
    }
}
