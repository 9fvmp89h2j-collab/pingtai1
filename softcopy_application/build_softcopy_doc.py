from pathlib import Path

from docx import Document
from docx.enum.section import WD_SECTION
from docx.enum.style import WD_STYLE_TYPE
from docx.enum.table import WD_ALIGN_VERTICAL, WD_TABLE_ALIGNMENT
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
from docx.shared import Cm, Inches, Pt, RGBColor


ROOT = Path(__file__).resolve().parent
SCREENSHOT_DIR = ROOT / "screenshots"
OUTPUT = ROOT / "小铜人儿童针灸文化3D互动学习软件V1.0_软著申报说明材料.docx"

GREEN = "285C44"
PALE_GREEN = "EEF5E9"
PALE_GOLD = "FBF4DF"
LIGHT_GRAY = "D9D9D9"
TEXT = RGBColor(36, 45, 39)


def set_cell_fill(cell, fill):
    tc_pr = cell._tc.get_or_add_tcPr()
    shd = tc_pr.find(qn("w:shd"))
    if shd is None:
        shd = OxmlElement("w:shd")
        tc_pr.append(shd)
    shd.set(qn("w:fill"), fill)


def set_cell_margins(cell, top=120, start=140, bottom=120, end=140):
    tc = cell._tc
    tc_pr = tc.get_or_add_tcPr()
    tc_mar = tc_pr.first_child_found_in("w:tcMar")
    if tc_mar is None:
        tc_mar = OxmlElement("w:tcMar")
        tc_pr.append(tc_mar)
    for name, value in (("top", top), ("start", start), ("bottom", bottom), ("end", end)):
        node = tc_mar.find(qn(f"w:{name}"))
        if node is None:
            node = OxmlElement(f"w:{name}")
            tc_mar.append(node)
        node.set(qn("w:w"), str(value))
        node.set(qn("w:type"), "dxa")


def set_table_borders(table, color=LIGHT_GRAY, size="6"):
    tbl_pr = table._tbl.tblPr
    borders = tbl_pr.first_child_found_in("w:tblBorders")
    if borders is None:
        borders = OxmlElement("w:tblBorders")
        tbl_pr.append(borders)
    for edge in ("top", "left", "bottom", "right", "insideH", "insideV"):
        tag = f"w:{edge}"
        element = borders.find(qn(tag))
        if element is None:
            element = OxmlElement(tag)
            borders.append(element)
        element.set(qn("w:val"), "single")
        element.set(qn("w:sz"), size)
        element.set(qn("w:color"), color)


def set_repeat_table_header(row):
    tr_pr = row._tr.get_or_add_trPr()
    tbl_header = OxmlElement("w:tblHeader")
    tbl_header.set(qn("w:val"), "true")
    tr_pr.append(tbl_header)


def prevent_row_split(row):
    tr_pr = row._tr.get_or_add_trPr()
    cant_split = OxmlElement("w:cantSplit")
    cant_split.set(qn("w:val"), "true")
    tr_pr.append(cant_split)


def set_font(run, east_asia="Microsoft YaHei", size=None, bold=None, color=None):
    run.font.name = east_asia
    run._element.get_or_add_rPr().rFonts.set(qn("w:eastAsia"), east_asia)
    run._element.get_or_add_rPr().rFonts.set(qn("w:ascii"), "Arial")
    run._element.get_or_add_rPr().rFonts.set(qn("w:hAnsi"), "Arial")
    if size is not None:
        run.font.size = Pt(size)
    if bold is not None:
        run.bold = bold
    if color is not None:
        run.font.color.rgb = color


def set_paragraph_font(paragraph, size=10.5, bold=False, color=TEXT):
    for run in paragraph.runs:
        set_font(run, size=size, bold=bold, color=color)


def add_field(paragraph, code):
    run = paragraph.add_run()
    fld_char = OxmlElement("w:fldChar")
    fld_char.set(qn("w:fldCharType"), "begin")
    instr = OxmlElement("w:instrText")
    instr.set(qn("xml:space"), "preserve")
    instr.text = code
    separate = OxmlElement("w:fldChar")
    separate.set(qn("w:fldCharType"), "separate")
    text = OxmlElement("w:t")
    text.text = "1"
    end = OxmlElement("w:fldChar")
    end.set(qn("w:fldCharType"), "end")
    run._r.extend([fld_char, instr, separate, text, end])


def add_body(doc, text, *, bold_lead=None, indent=True, keep_with_next=False):
    p = doc.add_paragraph()
    p.paragraph_format.space_after = Pt(6)
    p.paragraph_format.line_spacing = 1.45
    p.paragraph_format.keep_with_next = keep_with_next
    if indent:
        p.paragraph_format.first_line_indent = Cm(0.74)
    if bold_lead and text.startswith(bold_lead):
        lead = p.add_run(bold_lead)
        set_font(lead, size=10.5, bold=True, color=TEXT)
        body = p.add_run(text[len(bold_lead):])
        set_font(body, size=10.5, color=TEXT)
    else:
        run = p.add_run(text)
        set_font(run, size=10.5, color=TEXT)
    return p


def add_bullet(doc, text, level=0):
    p = doc.add_paragraph(style="List Bullet" if level == 0 else "List Bullet 2")
    p.paragraph_format.space_after = Pt(4)
    p.paragraph_format.line_spacing = 1.35
    run = p.add_run(text)
    set_font(run, size=10.5, color=TEXT)
    return p


def add_numbered_item(doc, title, text):
    p = doc.add_paragraph()
    p.paragraph_format.left_indent = Cm(0.6)
    p.paragraph_format.first_line_indent = Cm(-0.6)
    p.paragraph_format.space_after = Pt(6)
    p.paragraph_format.line_spacing = 1.4
    r1 = p.add_run(title)
    set_font(r1, size=10.5, bold=True, color=TEXT)
    r2 = p.add_run(text)
    set_font(r2, size=10.5, color=TEXT)
    return p


def add_heading(doc, text, level=1):
    p = doc.add_paragraph(text, style=f"Heading {level}")
    p.paragraph_format.left_indent = Cm(0)
    p.paragraph_format.first_line_indent = Cm(0)
    p.paragraph_format.keep_with_next = True
    num_pr = p._p.get_or_add_pPr().find(qn("w:numPr"))
    if num_pr is not None:
        p._p.get_or_add_pPr().remove(num_pr)
    for run in p.runs:
        set_font(run, size=16 if level == 1 else 12.5, bold=True, color=RGBColor(0, 0, 0))
    return p


def add_caption(doc, text):
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.space_before = Pt(4)
    p.paragraph_format.space_after = Pt(8)
    p.paragraph_format.keep_with_next = False
    run = p.add_run(text)
    set_font(run, size=9, color=RGBColor(85, 85, 85))
    return p


def add_screenshot_page(doc, image_name, title, caption, note):
    doc.add_page_break()
    add_heading(doc, title, 1)
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.space_before = Pt(6)
    p.paragraph_format.keep_with_next = True
    run = p.add_run()
    shape = run.add_picture(str(SCREENSHOT_DIR / image_name), width=Inches(6.45))
    doc_pr = shape._inline.docPr
    doc_pr.set("descr", caption)
    add_caption(doc, caption)
    add_body(doc, note, indent=False)


doc = Document()
section = doc.sections[0]
section.page_width = Cm(21)
section.page_height = Cm(29.7)
section.top_margin = Cm(2.2)
section.bottom_margin = Cm(2.0)
section.left_margin = Cm(2.35)
section.right_margin = Cm(2.35)

styles = doc.styles
normal = styles["Normal"]
normal.font.name = "Microsoft YaHei"
normal._element.rPr.rFonts.set(qn("w:eastAsia"), "Microsoft YaHei")
normal.font.size = Pt(10.5)
normal.font.color.rgb = TEXT

title_style = styles["Title"]
title_style.font.name = "Microsoft YaHei"
title_style._element.rPr.rFonts.set(qn("w:eastAsia"), "Microsoft YaHei")
title_style.font.size = Pt(25)
title_style.font.bold = True
title_style.font.color.rgb = RGBColor(0, 0, 0)
title_ppr = title_style._element.get_or_add_pPr()
title_border = title_ppr.find(qn("w:pBdr"))
if title_border is not None:
    title_ppr.remove(title_border)

for name, size, before, after in (("Heading 1", 16, 15, 8), ("Heading 2", 12.5, 10, 5)):
    style = styles[name]
    style.font.name = "Microsoft YaHei"
    style._element.rPr.rFonts.set(qn("w:eastAsia"), "Microsoft YaHei")
    style.font.size = Pt(size)
    style.font.bold = True
    style.font.color.rgb = RGBColor(0, 0, 0)
    style.paragraph_format.space_before = Pt(before)
    style.paragraph_format.space_after = Pt(after)

if "Figure Caption" not in styles:
    styles.add_style("Figure Caption", WD_STYLE_TYPE.PARAGRAPH)

# Cover
p = doc.add_paragraph(style="Title")
p.alignment = WD_ALIGN_PARAGRAPH.CENTER
p.paragraph_format.space_before = Pt(76)
p.paragraph_format.space_after = Pt(14)
r = p.add_run("小铜人儿童针灸文化3D互动学习软件 V1.0")
set_font(r, size=25, bold=True, color=RGBColor(0, 0, 0))
p_border = p._p.get_or_add_pPr().find(qn("w:pBdr"))
if p_border is not None:
    p._p.get_or_add_pPr().remove(p_border)

p = doc.add_paragraph()
p.alignment = WD_ALIGN_PARAGRAPH.CENTER
p.paragraph_format.space_after = Pt(50)
r = p.add_run("软件著作权申报说明材料")
set_font(r, size=17, bold=True, color=RGBColor(0, 0, 0))

cover_table = doc.add_table(rows=5, cols=2)
cover_table.alignment = WD_TABLE_ALIGNMENT.CENTER
cover_table.autofit = False
cover_table.columns[0].width = Cm(4.0)
cover_table.columns[1].width = Cm(9.5)
cover_rows = [
    ("软件简称", "小铜人3D互动学习软件"),
    ("版本号", "V1.0"),
    ("著作权人", "____________________________"),
    ("开发完成日期", "________年____月____日"),
    ("材料用途", "软件著作权登记前期资料整理与申报说明"),
]
for idx, (label, value) in enumerate(cover_rows):
    left, right = cover_table.rows[idx].cells
    left.text = label
    right.text = value
    set_cell_fill(left, PALE_GREEN)
    if idx % 2:
        set_cell_fill(right, "FAFAFA")
    for cell in (left, right):
        set_cell_margins(cell, 150, 160, 150, 160)
        cell.vertical_alignment = WD_ALIGN_VERTICAL.CENTER
        for paragraph in cell.paragraphs:
            paragraph.paragraph_format.space_after = Pt(0)
            set_paragraph_font(paragraph, size=10.5, bold=(cell is left))
set_table_borders(cover_table)

p = doc.add_paragraph()
p.alignment = WD_ALIGN_PARAGRAPH.CENTER
p.paragraph_format.space_before = Pt(70)
r = p.add_run("本软件面向儿童中医药文化科普，不提供诊断、治疗或真实针刺操作指导。")
set_font(r, size=9.5, bold=True, color=RGBColor(75, 89, 80))

# Footer page number
footer = section.footer
fp = footer.paragraphs[0]
fp.alignment = WD_ALIGN_PARAGRAPH.CENTER
fr = fp.add_run("第 ")
set_font(fr, size=9, color=RGBColor(100, 100, 100))
add_field(fp, "PAGE")
fr = fp.add_run(" 页")
set_font(fr, size=9, color=RGBColor(100, 100, 100))

doc.add_page_break()
add_heading(doc, "一 软件基本信息", 1)
add_body(doc, "本材料围绕现有儿童针灸文化科普网站中最具独立性和技术完整性的3D小铜人互动学习板块展开。建议以该板块作为软件著作权登记的核心功能范围，形成名称清晰、边界明确、可通过源代码和运行截图相互印证的软件申报材料。")

info_table = doc.add_table(rows=1, cols=2)
info_table.alignment = WD_TABLE_ALIGNMENT.CENTER
info_table.autofit = False
headers = info_table.rows[0].cells
headers[0].text = "项目"
headers[1].text = "建议填写内容"
for c in headers:
    set_cell_fill(c, GREEN)
    set_cell_margins(c)
    for p0 in c.paragraphs:
        p0.alignment = WD_ALIGN_PARAGRAPH.CENTER
        for run in p0.runs:
            set_font(run, size=10, bold=True, color=RGBColor(255, 255, 255))
set_repeat_table_header(info_table.rows[0])
items = [
    ("软件全称", "小铜人儿童针灸文化3D互动学习软件"),
    ("软件简称", "小铜人3D互动学习软件"),
    ("版本号", "V1.0"),
    ("软件分类", "教育学习软件  中医药文化科普软件  Web应用软件"),
    ("开发目的", "将穴位名称、经络归属、身体区域和安全知识转化为可观察、可检索、可持续积累的儿童互动学习内容。"),
    ("适用对象", "儿童、家长、教师及中医药文化科普活动组织者。"),
    ("核心范围", "3D铜人观察、穴位星点交互、每日探案任务、穴位图鉴、学习进度与奖励、安全提示。"),
    ("非功能范围", "不提供针刺深度、进针角度、操作手法、治疗处方、诊断结论或个体化医疗建议。"),
]
for idx, (label, value) in enumerate(items):
    cells = info_table.add_row().cells
    cells[0].text = label
    cells[1].text = value
    if idx % 2:
        for cell in cells:
            set_cell_fill(cell, "F7FAF6")
    for cidx, cell in enumerate(cells):
        set_cell_margins(cell)
        cell.vertical_alignment = WD_ALIGN_VERTICAL.CENTER
        for p0 in cell.paragraphs:
            p0.paragraph_format.space_after = Pt(0)
            p0.alignment = WD_ALIGN_PARAGRAPH.CENTER if cidx == 0 else WD_ALIGN_PARAGRAPH.LEFT
            set_paragraph_font(p0, size=9.6, bold=(cidx == 0))
set_table_borders(info_table)

add_heading(doc, "二 核心板块选择", 1)
add_heading(doc, "2.1 核心板块名称", 2)
add_body(doc, "3D小铜人穴位探案互动模块。该模块在现有系统中对应“小铜人馆 人体探案实验室”，可以独立完成从任务生成、三维观察、线索查看、图鉴检索到学习成果保存的完整业务闭环。")

add_heading(doc, "2.2 选择理由", 2)
add_numbered_item(doc, "1 独立性强  ", "模块拥有独立入口、交互界面、前后端接口、数据库记录和用户进度，适合形成清晰的软件权利边界。")
add_numbered_item(doc, "2 技术特征集中  ", "模块综合使用Three.js三维渲染、射线拾取、穴位三维坐标映射、Vue状态驱动界面和Spring Boot事务处理。")
add_numbered_item(doc, "3 教育闭环完整  ", "用户经历接收任务、寻找星点、阅读线索、收录档案、获得奖励和积累图鉴的连续学习过程。")
add_numbered_item(doc, "4 安全定位明确  ", "系统把穴位学习限定为文化观察和身体认知，关键入口、学习过程、图鉴详情均设置安全提示。")

add_heading(doc, "三 软件功能介绍", 1)
add_body(doc, "软件以“身体地图探案”为学习情境，把传统的穴位文字资料转换为任务化、可视化、可积累的互动内容。用户登录后进入小铜人馆，系统读取当日学习任务和既往学习档案，并根据操作结果更新个人进度。")

features = [
    ("3.1 三维铜人观察", "使用Three.js构建三维场景，提供正面、背面和侧面视角，以及旋转、缩放和视角重置操作。穴位数据中的三维坐标被映射为模型上的发光星点，用户通过鼠标或触控方式进行观察。"),
    ("3.2 每日穴位探案", "系统按日期从带三维坐标的穴位数据中生成当日展示内容，并选取3个目标星点组成任务。任务列表、发现状态和搜查进度同步更新，使学习内容具有连续性和重复使用价值。"),
    ("3.3 星点识别与线索档案", "用户点击目标星点后，系统展示穴位编号、拼音、所属经络、身体区域、儿童化知识说明、传统文化用途说明和安全提示。只有当日目标星点可被收入线索档案。"),
    ("3.4 百穴图鉴", "图鉴当前收录361条启用穴位数据，覆盖14类经络编码。其中100条数据配置三维坐标，可参与三维星点展示；其余内容以图鉴卡片方式提供名称与文化资料。图鉴支持按名称、编号、身体区域、经络和发现状态检索。"),
    ("3.5 学习进度与奖励", "系统保存用户每日发现记录、累计完成案件数、铜片和经络星砂等学习奖励。完成当日3个目标后只发放一次奖励，避免重复领取，并可用于系统内后续学习场景。"),
    ("3.6 儿童安全提示", "欢迎页、观察页、线索详情和图鉴均反复提示“只观察、只学习，不自己针刺”。系统不展示针刺操作参数，不把文化知识解释为诊断或治疗建议。"),
]
for title, body in features:
    add_heading(doc, title, 2)
    add_body(doc, body)

workflow_heading = add_heading(doc, "四 业务流程", 1)
flow = doc.add_table(rows=1, cols=5)
flow.alignment = WD_TABLE_ALIGNMENT.CENTER
flow.autofit = False
labels = ["进入小铜人馆", "读取今日任务", "旋转并点击星点", "查看并收录线索", "保存进度与更新图鉴"]
for idx, (cell, label) in enumerate(zip(flow.rows[0].cells, labels)):
    cell.text = label
    set_cell_fill(cell, PALE_GREEN if idx % 2 == 0 else PALE_GOLD)
    set_cell_margins(cell, 150, 80, 150, 80)
    cell.vertical_alignment = WD_ALIGN_VERTICAL.CENTER
    for p0 in cell.paragraphs:
        p0.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p0.paragraph_format.space_after = Pt(0)
        set_paragraph_font(p0, size=9, bold=True)
prevent_row_split(flow.rows[0])
set_table_borders(flow)
add_body(doc, "流程中的每一步都由界面状态和服务端数据共同校验。前端负责展示与交互反馈，服务端负责确定当日目标、验证目标编码、保存发现记录并控制完成奖励的唯一发放。", indent=False)

add_heading(doc, "五 核心技术实现", 1)
tech_table = doc.add_table(rows=1, cols=3)
tech_table.alignment = WD_TABLE_ALIGNMENT.CENTER
tech_table.autofit = False
for idx, text0 in enumerate(("层次", "主要技术", "在核心模块中的作用")):
    cell = tech_table.rows[0].cells[idx]
    cell.text = text0
    set_cell_fill(cell, GREEN)
    set_cell_margins(cell)
    for p0 in cell.paragraphs:
        p0.alignment = WD_ALIGN_PARAGRAPH.CENTER
        for run in p0.runs:
            set_font(run, size=10, bold=True, color=RGBColor(255, 255, 255))
set_repeat_table_header(tech_table.rows[0])
tech_rows = [
    ("前端表现层", "Vue 3  Vue Router  Pinia", "组织页面组件、路由入口、登录状态、任务状态和学习进度反馈。"),
    ("三维交互层", "Three.js  OrbitControls  OBJLoader  Raycaster", "加载铜人模型，控制旋转缩放，生成穴位星点并识别用户点击目标。"),
    ("接口服务层", "Spring Boot  REST API  JWT", "提供穴位列表、今日任务、线索发现三个核心接口，并识别当前用户。"),
    ("数据访问层", "MyBatis Plus  MySQL  Flyway", "维护穴位知识、三维坐标、每日发现记录、用户铜人档案与版本化数据库结构。"),
    ("一致性控制", "事务处理  唯一记录  当日完成校验", "确保同一星点不重复写入、同一日任务奖励只发放一次。"),
]
for ridx, row_data in enumerate(tech_rows):
    cells = tech_table.add_row().cells
    for cidx, value in enumerate(row_data):
        cells[cidx].text = value
        if ridx % 2:
            set_cell_fill(cells[cidx], "F7FAF6")
        set_cell_margins(cells[cidx])
        cells[cidx].vertical_alignment = WD_ALIGN_VERTICAL.CENTER
        for p0 in cells[cidx].paragraphs:
            p0.alignment = WD_ALIGN_PARAGRAPH.CENTER if cidx < 2 else WD_ALIGN_PARAGRAPH.LEFT
            p0.paragraph_format.space_after = Pt(0)
            set_paragraph_font(p0, size=9.5, bold=(cidx == 0))
set_table_borders(tech_table)

add_heading(doc, "5.1 核心数据对象", 2)
for text0 in (
    "穴位知识对象：记录穴位编号、名称、拼音、经络编码、身体区域、儿童化说明、安全提示、资料来源和三维坐标。",
    "每日探案对象：记录日期、候选穴位、3个目标编码、已发现编码、是否持久化和累计完成案件数。",
    "用户铜人档案对象：记录累计铜片、经络星砂、完成案件数和最近完成日期。",
    "用户每日进度对象：按用户、日期和穴位编码保存发现记录，为图鉴“已发现”状态提供依据。",
):
    add_bullet(doc, text0)

add_heading(doc, "5.2 关键控制逻辑", 2)
add_body(doc, "服务端只接受当日目标编码。发现记录写入后，再按固定顺序汇总当日已发现内容；当3个目标全部完成时，通过任务完成逻辑更新用户档案和奖励。该设计把页面动画与真实进度分离，刷新页面后仍能恢复学习状态。")

add_heading(doc, "六 创新点说明", 1)
add_body(doc, "以下创新点用于概括本软件在界面表达、交互流程、数据组织和程序控制方面的独立设计。相较于仅展示文字与图片的常见科普页面，本软件形成了三维观察与学习档案相结合的交互方式；相关表述不对中医针灸知识本身主张著作权。")

innovations = [
    ("1 穴位知识与三维坐标的双层表达  ", "同一穴位既可作为图鉴知识卡展示，又可在配置三维坐标后成为铜人模型上的交互星点。无坐标数据仍保留图鉴学习价值，便于内容分阶段扩充。"),
    ("2 日期驱动的轻量探案机制  ", "系统根据日期从可建模穴位中循环选取候选内容，并固定3个当日目标，使内容每日变化、同日结果稳定，同时避免完全随机导致的进度不可复现。"),
    ("3 三维发现与永久图鉴联动  ", "用户在3D场景中发现星点后，结果同步写入个人档案，并改变百穴图鉴的发现状态，将一次性交互转化为可持续积累的学习记录。"),
    ("4 任务奖励的服务端唯一控制  ", "界面展示任务与奖励，但是否完成、是否首次完成由服务端事务逻辑判断，降低重复提交造成的奖励重复和学习记录不一致。"),
    ("5 儿童化安全信息分层嵌入  ", "安全边界不是单独免责声明，而是分别嵌入入口确认、观察提示、穴位详情和图鉴说明，使用户在关键操作节点持续接收“只观察、不操作”的引导。"),
    ("6 探案叙事与结构化知识融合  ", "使用“星点、线索、档案、图鉴”的统一叙事包装编号、经络、区域和文化说明，降低儿童理解抽象穴位分类信息的难度。"),
]
for title0, text0 in innovations:
    innovation_paragraph = add_numbered_item(doc, title0, text0)
    innovation_paragraph.paragraph_format.line_spacing = 1.25
    innovation_paragraph.paragraph_format.space_after = Pt(3)

add_screenshot_page(
    doc,
    "01-3D小铜人探案欢迎页.png",
    "七 软件运行截图",
    "图1  3D小铜人探案欢迎页",
    "欢迎页以“身体地图出现神秘星点”为任务引导，明确当日需要寻找3条线索，并在开始操作前展示“只观察、只学习、不自己针刺”的安全边界。",
)
add_screenshot_page(
    doc,
    "02-学习安全边界提示.png",
    "7.2 学习安全边界提示",
    "图2  进入三维观察前的安全铃铛提醒",
    "用户开始观察后，系统再次展示禁止自行针刺、禁止模仿视频、身体不适时告知家长并咨询医生等提示。该提示将科普用途与诊疗用途明确分离。",
)
add_screenshot_page(
    doc,
    "04-百穴图鉴检索与详情页.png",
    "7.3 百穴图鉴检索与详情",
    "图3  百穴图鉴列表与单穴位文化档案",
    "图鉴支持穴位名称、编号、身体区域、经络和发现状态筛选。右侧详情同步展示身体区域、观察提示、文化说明、传统用途资料和安全提示，形成可检索的结构化学习档案。",
)

doc.add_page_break()
add_heading(doc, "八 申报材料使用建议", 1)
add_heading(doc, "8.1 建议在申请表中采用的功能描述", 2)
add_body(doc, "本软件是一套面向儿童的中医针灸文化3D互动学习软件。软件通过三维铜人观察、穴位星点交互、每日探案任务、穴位图鉴检索、个人学习进度和奖励机制，帮助用户认识穴位名称、经络归属和身体区域。软件设置多层安全提示，仅用于中医药文化科普和身体认知，不提供真实针刺操作、诊断或治疗建议。")

add_heading(doc, "8.2 建议重点提交的源程序范围", 2)
add_body(doc, "源程序鉴别材料建议优先覆盖以下连续功能链路，并在最终提交前统一软件名称、版本号、著作权人信息和代码页眉：")
for text0 in (
    "三维铜人页面、三维模型构建与穴位坐标映射代码。",
    "今日探案、穴位列表和线索发现接口代码。",
    "穴位知识、每日发现记录和用户铜人档案的数据对象及访问代码。",
    "任务完成、进度恢复、奖励唯一发放和安全提示相关代码。",
):
    add_bullet(doc, text0)

add_heading(doc, "8.3 提交前核对", 2)
check_table = doc.add_table(rows=1, cols=3)
check_table.alignment = WD_TABLE_ALIGNMENT.CENTER
for idx, text0 in enumerate(("核对项", "当前建议", "最终处理")):
    cell = check_table.rows[0].cells[idx]
    cell.text = text0
    set_cell_fill(cell, GREEN)
    set_cell_margins(cell)
    for p0 in cell.paragraphs:
        p0.alignment = WD_ALIGN_PARAGRAPH.CENTER
        for run in p0.runs:
            set_font(run, size=10, bold=True, color=RGBColor(255, 255, 255))
set_repeat_table_header(check_table.rows[0])
rows = [
    ("软件名称", "小铜人儿童针灸文化3D互动学习软件", "与申请表和代码页眉完全一致"),
    ("版本号", "V1.0", "与运行界面和说明书一致"),
    ("著作权人", "封面预留", "填写真实个人或单位名称"),
    ("完成日期", "封面预留", "按实际开发完成日期填写"),
    ("截图账号", "本地演示管理员账号", "正式提交时可换为无个人信息的演示账号"),
    ("医疗边界", "已明确为文化科普", "保持所有材料口径一致"),
]
for ridx, row_data in enumerate(rows):
    cells = check_table.add_row().cells
    for cidx, value in enumerate(row_data):
        cells[cidx].text = value
        if ridx % 2:
            set_cell_fill(cells[cidx], "F7FAF6")
        set_cell_margins(cells[cidx])
        cells[cidx].vertical_alignment = WD_ALIGN_VERTICAL.CENTER
        for p0 in cells[cidx].paragraphs:
            p0.alignment = WD_ALIGN_PARAGRAPH.CENTER if cidx < 2 else WD_ALIGN_PARAGRAPH.LEFT
            p0.paragraph_format.space_after = Pt(0)
            set_paragraph_font(p0, size=9.3, bold=(cidx == 0))
set_table_borders(check_table)

add_body(doc, "建议将本材料作为申报说明底稿使用。正式登记时，应依据最终提交版本核对申请人主体、首次发表状态、开发完成日期、代码行数和鉴别材料页数等信息。", indent=False)

doc.core_properties.title = "小铜人儿童针灸文化3D互动学习软件 V1.0 软件著作权申报说明材料"
doc.core_properties.subject = "软件著作权申报功能说明与运行截图"
doc.core_properties.keywords = "软件著作权, 儿童中医药文化科普, 3D小铜人, 穴位图鉴"
doc.core_properties.author = ""
doc.save(OUTPUT)
print(OUTPUT)
