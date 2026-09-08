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
