**Homepage layout design QA**

- Source visual truth: `C:/Users/dpf20/AppData/Local/Temp/codex-clipboard-33705fca-5bab-4801-89a2-58a56100689f.png`
- Intro issue reference: `C:/Users/dpf20/AppData/Local/Temp/codex-clipboard-893b68e2-8c3c-4f66-ad71-3ae229458f80.png`
- Implementation screenshot: `audit-output/homepage-layout-20260826/03-homepage-final-1920x1000.png`
- Viewport and pixels: source and implementation are both 1920 x 1000 CSS px at device scale factor 1; no density normalization was needed.
- State: guest mode, current level `报到处`, guide closed for the homepage comparison.

**Findings**

- No actionable P0/P1/P2 layout mismatch remains.
- Fonts and typography: the existing families, weights, hierarchy, and copy are preserved. The 1201-1600 px breakpoint reduces only the mission CTA size so its full label and arrow fit.
- Spacing and layout rhythm: the task card now uses shrinkable tracks; its scroll width equals its client width. The right rail ends at 978 px in the 1000 px viewport, and the document measures 1920 x 1000 with no horizontal or vertical overflow.
- Colors and visual tokens: unchanged from the supplied page; the warm parchment palette, borders, radii, and shadows remain intact.
- Image quality and asset fidelity: all existing repository artwork is preserved. The guide map uses the whole supplied map image instead of cropping its left and right edges.
- Copy and content: unchanged.

**Interaction and responsive verification**

- Opened the home guide, opened its skip confirmation, confirmed the skip, and reopened the guide.
- At 1920 x 1000, the primary mission CTA and all three right-rail cards are visible in the first viewport.
- At 1366 x 768, the task card has no internal horizontal overflow and its CTA label is complete.
- At 1024 x 768, the page reflows to identity card, map, then three supporting cards without horizontal overflow.
- Browser console: no warnings or errors.
- `npm run lint`, `npm run build`, and `npm run validate:game` passed.

**Comparison history**

- Earlier P1: the mission card had 835 px of content in a 754 px visible box, clipping the primary CTA. Fix: replaced hard minimum tracks with shrinkable tracks and responsive asset/button sizing. Post-fix: card scroll width and client width are both 754 px at 1920 x 1000.
- Earlier P1: the 1154 px right rail pushed the repair plan below the 1000 px viewport. Fix: reduced desktop-only card density while preserving every item and action. Post-fix: right rail is 858 px tall and ends at 978 px.
- Earlier P2: the intro map used `cover`, cropping the left and right map content. Fix: fit the full map image to the guide canvas. Post-fix browser capture showed the first level and left-side labels inside the frame.

Full-view comparison was used because the reported defects were page-level crop and overflow problems. Focused image crops were not needed; browser geometry supplied exact overflow evidence for the task card and right rail.

final result: passed
