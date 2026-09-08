<template>
  <div class="profile-container">
    <a-card class="profile-card">
      <template #title>
        <div class="card-header">
          <h2>个人中心</h2>
        </div>
      </template>

      <a-tabs v-model:active-key="activeTab">
        <!-- 基本信息 Tab -->
        <a-tab-pane
          key="basic"
          tab="基本信息"
        >
          <div class="profile-info">
            <div class="avatar-container">
              <a-avatar
                :size="100"
                :src="avatarUrl"
              >
                {{ userForm.name?.charAt(0) || 'U' }}
              </a-avatar>
              <a-upload
                class="avatar-uploader"
                :action="uploadAction"
                :show-upload-list="false"
                :custom-request="customUploadAvatar"
                :before-upload="beforeAvatarUpload"
              >
                <a-button
                  size="small"
                  type="primary"
                >
                  更换头像
                </a-button>
              </a-upload>
            </div>

            <div class="info-form">
              <a-form
                ref="userFormRef"
                :model="userForm"
                :rules="rules"
                :label-col="{ span: 6 }"
                :wrapper-col="{ span: 18 }"
              >
                <a-form-item
                  label="用户名"
                  name="username"
                >
                  <a-input
                    v-model:value="userForm.username"
                    disabled
                  />
                </a-form-item>

                <a-form-item
                  label="姓名"
                  name="name"
                >
                  <a-input v-model:value="userForm.name" />
                </a-form-item>

                <a-form-item
                  label="性别"
                  name="sex"
                >
                  <a-radio-group v-model:value="userForm.sex">
                    <a-radio value="男">
                      男
                    </a-radio>
                    <a-radio value="女">
                      女
                    </a-radio>
                  </a-radio-group>
                </a-form-item>

                <a-form-item
                  label="电子邮箱"
                  name="email"
                >
                  <a-input v-model:value="userForm.email" />
                </a-form-item>

                <a-form-item
                  label="手机号码"
                  name="phone"
                >
                  <a-input v-model:value="userForm.phone" />
                </a-form-item>


                <a-form-item :wrapper-col="{ offset: 6, span: 18 }">
                  <a-button
                    type="primary"
                    :loading="profileSaving"
                    @click="submitUserInfo"
                  >
                    保存修改
                  </a-button>
                </a-form-item>
              </a-form>
            </div>
          </div>
        </a-tab-pane>

        <!-- 修改密码 Tab -->
        <a-tab-pane
          key="password"
          tab="修改密码"
        >
          <a-form
            ref="passwordFormRef"
            :model="passwordForm"
            :rules="passwordRules"
            :label-col="{ span: 6 }"
            :wrapper-col="{ span: 18 }"
            style="max-width: 500px; margin: 0 auto"
          >
            <a-form-item
              label="旧密码"
              name="oldPassword"
            >
              <a-input-password
                v-model:value="passwordForm.oldPassword"
                placeholder="请输入旧密码"
              />
            </a-form-item>

            <a-form-item
              label="新密码"
              name="newPassword"
            >
              <a-input-password
                v-model:value="passwordForm.newPassword"
                placeholder="请输入新密码"
              />
            </a-form-item>

            <a-form-item
              label="确认新密码"
              name="confirmPassword"
            >
              <a-input-password
                v-model:value="passwordForm.confirmPassword"
                placeholder="请再次输入新密码"
              />
            </a-form-item>

            <a-form-item :wrapper-col="{ offset: 6, span: 18 }">
              <a-button
                type="primary"
                :loading="passwordSaving"
                @click="submitPassword"
              >
                修改密码
              </a-button>
            </a-form-item>
          </a-form>
        </a-tab-pane>
      </a-tabs>
    </a-card>

  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from "vue";
import { message, Modal } from "ant-design-vue";
import { useUserStore } from "@/store/user";
import request from "@/utils/request";
import { getCurrentUser, updateUser, updatePassword } from '@/api/user';
import { getUserQuizStats } from '@/api/QuizApi';

const uploadAction = "#";
const userStore = useUserStore();
const activeTab = ref("basic");
const profileSaving = ref(false);
const passwordSaving = ref(false);

// ========== 答题统计相关数据 ==========
const quizStats = reactive({
  correctCount: 0,
  totalCount: 0,
  correctRate: 0
});

// 计算属性
// 加载答题统计
const loadQuizStats = async () => {
  try {
    const data = await getUserQuizStats();
    if (data) {
      quizStats.correctCount = data.correctCount || 0;
      quizStats.totalCount = data.totalCount || 0;
      quizStats.correctRate = data.correctRate || 0;
    }
  } catch (error) {
    console.error('加载答题统计失败:', error);
  }
};

// 已移除旧版作品个人管理能力



// 表单引用
const userFormRef = ref(null);
const passwordFormRef = ref(null);

// 用户表单数据
const userForm = reactive({
  id: "",
  username: "",
  name: "",
  email: "",
  phone: "",
  sex: "",
  avatar: "",
});

// 头像地址
const avatarUrl = computed(() => {
  return userForm.avatar;
});

// 密码表单数据
const passwordForm = reactive({
  oldPassword: "",
  newPassword: "",
  confirmPassword: "",
});

// 表单校验规则
const rules = {
  name: [{ required: true, message: "请输入姓名", trigger: "blur" }],
  email: [
    { required: true, message: "请输入邮箱地址", trigger: "blur" },
    {
      type: "email",
      message: "请输入正确的邮箱地址",
      trigger: ["blur", "change"],
    },
  ],
  phone: [
    { required: false, trigger: "blur" },
    {
      pattern: /^1[3-9]\d{9}$/,
      message: "请输入正确的手机号码",
      trigger: ["blur", "change"],
    },
  ],
};

// 密码表单校验规则
const passwordRules = {
  oldPassword: [
    { required: true, message: "请输入旧密码", trigger: "blur" },
    { min: 6, message: "密码长度不能小于6个字符", trigger: "blur" },
  ],
  newPassword: [
    { required: true, message: "请输入新密码", trigger: "blur" },
    { min: 6, message: "密码长度不能小于6个字符", trigger: "blur" },
  ],
  confirmPassword: [
    { required: true, message: "请再次输入新密码", trigger: "blur" },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error("两次输入的密码不一致"));
        } else {
          callback();
        }
      },
      trigger: ["blur", "change"],
    },
  ],
};

// 获取用户信息
const getUserInfo = async () => {
  try {
    // 从后端获取最新用户信息
    const data = await getCurrentUser();
    
    // 更新store
    userStore.updateUserInfo(data);
    
    // 更新表单数据
    userForm.id = data.id || "";
    userForm.username = data.username || "";
    userForm.name = data.nickname || data.name || "";
    userForm.email = data.email || "";
    userForm.phone = data.phone || "";
    userForm.sex = data.sex || "男";
    userForm.avatar = data.avatar || "";

    // 设置作品筛选的创建人ID

    console.log("用户信息加载成功:", userForm);
  } catch (error) {
    console.error("获取用户信息失败", error);
    message.error("获取用户信息失败");
  }
};

// 上传头像前的校验
const beforeAvatarUpload = (file) => {
  const isJPG = file.type === "image/jpeg";
  const isPNG = file.type === "image/png";
  const isLt2M = file.size / 1024 / 1024 < 2;

  if (!isJPG && !isPNG) {
    message.error("头像只能是 JPG 或 PNG 格式!");
    return false;
  }
  if (!isLt2M) {
    message.error("头像大小不能超过 2MB!");
    return false;
  }
  return true;
};

// 自定义头像上传方法（使用策略C：直接业务绑定上传）
const customUploadAvatar = async (options) => {
  try {
    const { file } = options;

    // 创建 FormData 对象
    const formData = new FormData();
    formData.append("file", file);
    formData.append("businessType", "USER_AVATAR");
    formData.append("businessId", userForm.id.toString());
    formData.append("businessField", "avatar");
    formData.append("replaceOld", "true"); // 替换旧头像

    // 发送上传请求
    const response = await request.post("/file/upload", formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
        Authorization: `Bearer ${userStore.token}`,
      }
    });

    // 更新用户头像路径
    const avatarPath = response.filePath || response.path;
    userForm.avatar = avatarPath;

    // 更新用户信息到后端
    await updateUserAvatar(avatarPath);

    // 通知上传成功
    options.onSuccess(response);
    message.success("头像上传成功");
  } catch (error) {
    options.onError(error);
    console.error("头像上传过程发生错误:", error);
    message.error("头像上传失败: " + (error.message || "未知错误"));
  }
};

// 更新用户头像信息
const updateUserAvatar = async (avatarPath) => {
  try {
    // 更新用户信息到后端
    const response = await updateUser(userForm.id, { avatar: avatarPath });
    
    // 更新本地store
    userStore.updateUserInfo(response);
    
    console.log("头像信息已保存到后端");
  } catch (error) {
    console.error("头像信息保存失败", error);
    message.error("头像信息保存失败");
    throw error;
  }
};

// 提交用户信息更新
const submitUserInfo = async () => {
  if (!userFormRef.value) return;

  profileSaving.value = true;
  try {
    // 表单验证
    await userFormRef.value.validate();

    // 更新用户信息到后端
    const response = await updateUser(userForm.id, {
      name: userForm.name,
      email: userForm.email,
      phone: userForm.phone,
      sex: userForm.sex,
    });
    
    // 更新本地store
    userStore.updateUserInfo(response);
    
    message.success("个人信息更新成功!");

  } catch (error) {
    console.error("保存个人信息失败", error);
    if (!error?.errorFields) {
      message.error("保存个人信息失败: " + (error.message || "未知错误"));
    }
  } finally {
    profileSaving.value = false;
  }
};

// 提交密码修改
const submitPassword = async () => {
  if (!passwordFormRef.value) return;

  passwordSaving.value = true;
  try {
    // 表单验证
    await passwordFormRef.value.validate();

    // 调用后端修改密码接口
    await updatePassword(userForm.id, {
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
    }, {
      successMsg: "密码修改成功，即将跳转到登录页",
      showDefaultMsg: true
    });
    
    // 清空表单
    passwordForm.oldPassword = "";
    passwordForm.newPassword = "";
    passwordForm.confirmPassword = "";

    // 显示提示信息（不可取消）
    Modal.info({
      title: "密码修改成功",
      content: "为了您的账户安全，请重新登录",
      okText: "确定",
      onOk: async () => {
        // 清除用户信息并跳转到登录页
        await userStore.logout();
        window.location.href = "/auth/login";
      }
    });

    // 3秒后自动跳转（即使用户不点击确定按钮）
    setTimeout(async () => {
      await userStore.logout();
      window.location.href = "/auth/login";
    }, 3000);

  } catch (error) {
    console.error("密码修改失败", error);
    if (!error.errorFields) {
      message.error(error.message || "密码修改失败");
    }
  } finally {
    passwordSaving.value = false;
  }
};

// 监听用户表单数据变化
watch(
  userForm,
  (newVal) => {
    console.log("用户表单数据变化:", newVal);
  },
  { deep: true }
);

// 监听activeTab变化
watch(activeTab, (newTab) => {
  if (newTab === 'data') {
    loadUserStats();
  }
});

// ========== 我的数据相关方法 ==========

/**
 * 加载数据统计信息
 */
const loadUserStats = () => {
  loadQuizStats();
  // 可以在这里添加更多数据加载逻辑
};

// 组件挂载时获取用户信息
onMounted(() => {
  getUserInfo();
  loadQuizStats();

  // 检查URL参数，如果有tab参数则切换到对应tab
  const urlParams = new URLSearchParams(window.location.search);
  const tabParam = urlParams.get('tab');
  if (tabParam && ['basic', 'data', 'password'].includes(tabParam)) {
    activeTab.value = tabParam;
  }
});
</script>

<style scoped>
.profile-container {
  min-height: 100vh;
  background: #faf8f3; /* 宣纸白 */
  padding: 32px 20px;
}

.profile-card {
  max-width: 1200px;
  margin: 0 auto;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(139, 69, 19, 0.12);
  border: 1px solid #e8e8e8;

  :deep(.ant-card-head) {
    background: linear-gradient(135deg, #1890ff 0%, #1890ff 100%);
    border-bottom: 2px solid rgba(197, 165, 114, 0.3);
  }

  :deep(.ant-card-head-title) {
    color: #fff;
    font-size: 24px;
    font-weight: 700;
    font-family: 'SimSun', '宋体', serif;
    letter-spacing: 3px;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  color: #fff;
}

.profile-info {
  display: flex;
  flex-direction: column;
  gap: 40px;
}

@media (min-width: 768px) {
  .profile-info {
    flex-direction: row;
  }
}

.avatar-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  padding: 24px;
  background: rgba(139, 69, 19, 0.03);
  border-radius: 8px;
  border: 1px solid #e8e8e8;
}

.avatar-uploader {
  text-align: center;
}

.avatar-uploader :deep(.ant-btn-primary) {
  background: linear-gradient(135deg, #1890ff 0%, #1890ff 100%);
  border: none;
  font-weight: 600;
  letter-spacing: 2px;

  &:hover {
    transform: translateY(-2px);
    /* box-shadow intentionally omitted to keep the hover state quiet */
  }
}

.info-form {
  flex: 1;
}

.info-form :deep(.ant-btn-primary) {
  background: linear-gradient(135deg, #1890ff 0%, #1890ff 100%);
  border: none;
  font-weight: 600;
  letter-spacing: 2px;

  &:hover {
    transform: translateY(-2px);
    /* box-shadow intentionally omitted to keep the hover state quiet */
  }
}

/* ========== 答题统计卡片样式 ========== */
.quiz-stats-card {
  background: linear-gradient(135deg, #f0f5ff 0%, #e6f7ff 100%);
  border: 1px solid #91d5ff;
  border-radius: 12px;
  padding: 20px;
  width: 100%;
  margin-top: 8px;
}

.quiz-stats-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #d9d9d9;
}

.quiz-title {
  font-size: 16px;
  font-weight: 600;
  color: #1890ff;
}

.quiz-stats-content {
  display: flex;
  justify-content: center;
}

.quiz-rate-display {
  display: flex;
  align-items: center;
  gap: 32px;
}

.quiz-stats-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.stat-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.stat-value {
  font-size: 16px;
  font-weight: 600;
  color: #333;

  &.correct {
    color: #52c41a;
  }
}

.rate-circle {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.rate-value {
  font-size: 20px;
  font-weight: 600;
  color: #1890ff;
}

.ant-tabs {
  margin-top: 0;
}

:deep(.ant-tabs-nav) {
  margin-bottom: 28px;
}

:deep(.ant-tabs-tab) {
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 1px;
  color: #666;

  &:hover {
    color: #1890ff;
  }
}

:deep(.ant-tabs-tab-active .ant-tabs-tab-btn) {
  color: #1890ff;
}

:deep(.ant-tabs-ink-bar) {
  background: #1890ff;
}

/* ========== 我的作品样式 ========== */
.heritage-section {
  padding: 24px 0;
}

.heritage-actions {
  margin-bottom: 24px;
  padding: 20px;
  background: rgba(139, 69, 19, 0.03);
  border-radius: 8px;
  border: 1px solid #e8e8e8;
}

.heritage-actions :deep(.ant-btn-primary) {
  background: linear-gradient(135deg, #1890ff 0%, #1890ff 100%);
  border: none;
  font-weight: 600;
  letter-spacing: 2px;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(139, 69, 19, 0.4);
  }

  & i {
    margin-right: 6px;
  }
}

.heritage-list {
  min-height: 400px;
}

.heritage-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
  margin-bottom: 24px;
}

.heritage-item {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(139, 69, 19, 0.08);
  transition: all 0.3s ease;
  border: 1px solid #e8e8e8;
}

.heritage-item:hover {
  box-shadow: 0 8px 20px rgba(139, 69, 19, 0.15);
  transform: translateY(-4px);
  border-color: #c5a572;
}

.item-cover {
  position: relative;
  height: 200px;
  overflow: hidden;
  background: #faf8f3;
}

.item-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.heritage-item:hover .item-cover img {
  transform: scale(1.08);
}

.no-image {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #ccc;
  font-size: 56px;
}

.item-status {
  position: absolute;
  top: 12px;
  right: 12px;
}

.item-content {
  padding: 18px;
  background: rgba(139, 69, 19, 0.02);
}

.item-title {
  font-size: 17px;
  font-weight: 600;
  margin: 0 0 10px 0;
  color: #2c2c2c;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  letter-spacing: 0.5px;
}

.item-category {
  font-size: 14px;
  color: #666;
  margin: 0 0 6px 0;
  font-weight: 500;
}

.item-time {
  font-size: 13px;
  color: #999;
  margin: 0 0 14px 0;
}

.item-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.item-actions :deep(.ant-btn-primary) {
  background: linear-gradient(135deg, #8b4513 0%, #a0522d 100%);
  border: none;
  font-weight: 500;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(139, 69, 19, 0.4);
  }
}

.empty-heritage {
  text-align: center;
  padding: 80px 20px;
  background: rgba(139, 69, 19, 0.02);
  border-radius: 8px;
  border: 1px dashed #e8e8e8;
}

.empty-heritage :deep(.ant-btn-primary) {
  background: linear-gradient(135deg, #8b4513 0%, #a0522d 100%);
  border: none;
  font-weight: 600;
  letter-spacing: 2px;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(139, 69, 19, 0.4);
  }
}

.heritage-pagination {
  display: flex;
  justify-content: center;
  margin-top: 24px;
  padding: 24px 0;
}

.heritage-pagination :deep(.ant-pagination-item-active) {
  border-color: #8b4513;
  background: #8b4513;
}

.heritage-pagination :deep(.ant-pagination-item-active a) {
  color: #fff;
}

/* ========== 收货地址样式 ========== */
.address-section {
  padding: 24px 0;
}

.address-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 24px;
}

.address-card {
  cursor: default;
  transition: all 0.3s;
  box-shadow: 0 4px 12px rgba(139, 69, 19, 0.08);
  border: 1px solid #e8e8e8;
}

.address-card :deep(.ant-card-body) {
  padding: 16px;
}

.address-card:hover {
  box-shadow: 0 8px 20px rgba(139, 69, 19, 0.15);
  transform: translateY(-4px);
}

.default-address {
  border-color: #8b4513;
  background: linear-gradient(135deg, rgba(139, 69, 19, 0.02) 0%, rgba(197, 165, 114, 0.04) 100%);
}

.address-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.address-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.receiver {
  font-weight: 600;
  font-size: 16px;
  color: #2c2c2c;
  letter-spacing: 0.5px;
}

.phone {
  color: #666;
  font-weight: 500;
  font-size: 14px;
}

.address-actions {
  display: flex;
  gap: 6px;
}

.address-actions :deep(.ant-btn-link) {
  color: #8b4513;

  &:hover {
    color: #a0522d;
  }

  &.ant-btn-dangerous {
    color: #c5322d;

    &:hover {
      color: #ff4d4f;
    }
  }
}

.address-detail {
  color: #666;
  line-height: 1.6;
  font-size: 14px;
}

.add-address-card {
  cursor: pointer;
  border: 2px dashed #c5a572;
  transition: all 0.3s;
  background: rgba(139, 69, 19, 0.02);
}

.add-address-card :deep(.ant-card-body) {
  padding: 32px 16px;
}

.add-address-card:hover {
  border-color: #8b4513;
  background: rgba(139, 69, 19, 0.06);
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(139, 69, 19, 0.15);
}

.add-address-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #8b4513;
}

.add-address-content i {
  font-size: 40px;
  margin-bottom: 10px;
}

.add-address-content span {
  font-size: 15px;
  font-weight: 500;
  letter-spacing: 1px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .heritage-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .heritage-actions {
    padding: 12px;
  }
  
  .heritage-actions :deep(.ant-space) {
    flex-direction: column;
    width: 100%;
  }
  
  .heritage-actions :deep(.ant-space-item) {
    width: 100%;
  }
  
  .heritage-actions :deep(.ant-btn),
  .heritage-actions :deep(.ant-select),
  .heritage-actions :deep(.ant-input-search) {
    width: 100% !important;
  }


}

/* ========== 我的数据样式 ==========
.data-section {
  padding: 24px 0;
}

.data-overview {
  margin-bottom: 24px;
}

.data-details {
  margin-top: 24px;
}

.data-card {
  box-shadow: 0 4px 12px rgba(139, 69, 19, 0.08);
  border: 1px solid #e8e8e8;
  transition: all 0.3s;
}

.data-card:hover {
  box-shadow: 0 8px 20px rgba(139, 69, 19, 0.15);
  transform: translateY(-4px);
}

.data-card :deep(.ant-card-head) {
  border-bottom: 1px solid #f0f0f0;
}

.data-card-title {
  font-size: 16px;
  font-weight: 600;
  color: #2c2c2c;
  letter-spacing: 0.5px;
}

.data-stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 24px;
  padding: 20px 0;
}

.data-stat-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: rgba(139, 69, 19, 0.02);
  border-radius: 8px;
  border: 1px solid #f0f0f0;
  transition: all 0.3s;
}

.data-stat-item:hover {
  background: rgba(139, 69, 19, 0.04);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(139, 69, 19, 0.1);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #8b4513 0%, #a0522d 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 20px;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #2c2c2c;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

.data-subsection {
  margin-bottom: 32px;
}

.subsection-title {
  font-size: 16px;
  font-weight: 600;
  color: #2c2c2c;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.heritage-stats {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px;
  background: rgba(139, 69, 19, 0.02);
  border-radius: 8px;
  border: 1px solid #f0f0f0;
}

.heritage-stats .stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.heritage-stats .stat-number {
  font-size: 32px;
  font-weight: 700;
  color: #8b4513;
}

.heritage-stats .stat-text {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

.heritage-stats :deep(.ant-btn-link) {
  color: #8b4513;
  font-weight: 600;
  
  &:hover {
    color: #a0522d;
  }
}

.quiz-stats-card {
  background: linear-gradient(135deg, #f0f5ff 0%, #e6f7ff 100%);
  border: 1px solid #91d5ff;
  border-radius: 12px;
  padding: 24px;
}

.quiz-stats-content {
  display: flex;
  justify-content: center;
}

.quiz-rate-display {
  display: flex;
  align-items: center;
  gap: 40px;
}

.quiz-stats-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.stat-row {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-row .stat-label {
  font-size: 14px;
  color: #666;
  min-width: 80px;
}

.stat-row .stat-value {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  
  &.correct {
    color: #52c41a;
  }
  
  &.error {
    color: #ff4d4f;
  }
}

.rate-circle {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.rate-value {
  font-size: 24px;
  font-weight: 600;
  color: #1890ff;
}

.rate-label {
  font-size: 12px;
  color: #666;
  font-weight: 500;
}

/* 响应式设计补充 */
@media (max-width: 768px) {
  .data-stats-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .data-stat-item {
    flex-direction: column;
    text-align: center;
    gap: 12px;
  }

  .quiz-rate-display {
    flex-direction: column;
    gap: 24px;
    text-align: center;
  }

  .heritage-stats {
    flex-direction: column;
    gap: 20px;
    text-align: center;
  }
}
</style>
