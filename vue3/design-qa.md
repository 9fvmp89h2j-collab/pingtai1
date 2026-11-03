# 首页欢迎对话框 Product Design QA

## Comparison Target

- Source visual truth: `C:/Users/dpf20/AppData/Local/Temp/codex-clipboard-aeeda87f-4cf2-4c25-b968-a285cd54f172.png`
- Implementation route: `http://127.0.0.1:8800/#/home-map`
- Browser-rendered implementation: `../output/home-welcome-dialog-desktop-final.png`
- Focused component crop: `../output/home-welcome-dialog-crop-final.png`
- Side-by-side evidence: `../output/home-welcome-dialog-comparison-final.png`
- Viewport: desktop browser `1904 x 903` CSS px; responsive browser check `390 x 844` CSS px.
- Pixels and density: source `314 x 204`; desktop capture `1912 x 903`; focused implementation crop `525 x 235`; browser device scale factor `1`. The source is a component-level composition reference rather than a full-page viewport, so the source and focused crop were displayed at comparable visual scale in the side-by-side evidence.
- State: guest/游学模式, `/home-map`, first daily welcome visible and focused.

## Findings

- No actionable P0/P1/P2 issue remains.
- Fonts and typography: the Kai-style display heading, bold compact body copy, short line lengths, and two-line wrapping match the supplied conversational treatment. Text remains readable at desktop and `390 px` width.
- Spacing and layout rhythm: the former map-covering panel is replaced by a compact `500 x 212` dialogue group anchored inside the map. The character peeks in from the left, the rounded bubble owns the copy, and two detached buttons sit directly below it. Mobile uses a centered `390 px`-bounded version without horizontal overflow.
- Colors and visual tokens: parchment white, copper border, dark brown text, orange primary action, and light secondary action follow both the reference and the existing homepage palette. The final bubble is opaque, preventing map labels from showing through the copy.
- Image quality and asset fidelity: the existing high-resolution transparent copper-detective asset is reused with `object-fit: contain`; it is not stretched, cropped, regenerated, or replaced with code-drawn art. The action arrow uses the project's existing Font Awesome icon font.
- Copy and content: title and two-line greeting follow the reference. The two existing actions and their behavior are preserved.
- Accessibility: the group remains a labelled `role="dialog"`, receives focus when shown, uses real buttons, keeps descriptive image alt text, and adds visible keyboard focus rings.

## Interaction Verification

- `带我去看看` dismisses the welcome and opens the current `报到处` mission card.
- `我先自己探索` dismisses the welcome and records the current daily-welcome date.
- Desktop and `390 x 844` responsive layouts were rendered in the in-app browser; both buttons remained visible and usable.
- Browser console checked. The logged-out page still emits an existing protected-API `403` message; no new console error is introduced by this component change.
- `npm run lint`: passed.
- `npm run build`: passed.

## Comparison History

- Pass 1: the requested compact composition was implemented and compared against the supplied reference. The translucent bubble allowed faint underlying map copy to show through, classified P2.
- Fix: changed the bubble surface to an opaque parchment color while preserving the existing map and illustration.
- Pass 2: focused side-by-side comparison confirms the character-overlap, rounded speech panel, two-line copy, detached actions, hierarchy, and visual density. No actionable P0/P1/P2 difference remains for the requested format.

## Follow-up Polish

- P3: the supplied reference uses a slightly smaller character relative to its bubble. The implementation keeps the existing homepage character more prominent so it remains legible at the full desktop map scale.

## Final Result

final result: passed

## 3D 小铜人馆界面优化（2026-09-09）

### 变更范围

- 参考图：`D:/总/Pediatric Acupuncture Education System/预览/3d铜人馆.png`（1672×941）。
- 实现页面：`src/views/frontend/CopperMan.vue`（路由 `/#/copper-man`）。
- 保留现有 3D 模型、旋转/缩放、星点点击、任务收集、奖励、穴位图鉴和安全提示逻辑。
- 新增右栏“症状推荐穴位”入口，并将页面重排为桌面三栏工作区，补充移动端纵向适配。

### 自动化验证

- `npm run lint`：通过。
- `npm run build`：通过。

### 浏览器验证状态

- 当前环境缺少 Playwright Chromium 可执行文件，无法完成截图、交互和控制台检查。
- 因此本次视觉 QA 结果为 `blocked`，需要在具备浏览器运行时的环境中复核桌面 1672×941 与移动 390×844 两个视口。

final result: blocked
