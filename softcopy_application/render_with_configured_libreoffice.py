import importlib.util
from pathlib import Path
import sys

renderer_path = Path(r"C:\Users\dpf20\.codex\plugins\cache\openai-primary-runtime\documents\26.905.11957\skills\documents\render_docx.py")
spec = importlib.util.spec_from_file_location("codex_render_docx", renderer_path)
module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(module)
module._resolve_soffice = lambda: r"E:\liberoffice\program\soffice.exe"
module.main()
