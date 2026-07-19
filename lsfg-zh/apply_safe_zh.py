#!/usr/bin/env python3
from pathlib import Path
import ast
import re
import shutil
import sys

if len(sys.argv) != 2:
    raise SystemExit('用法: apply_safe_zh.py <LSFG-Android-Application目录>')
root = Path(sys.argv[1]).resolve()
here = Path(__file__).resolve().parent
if not (root / 'app/src/main').exists():
    raise SystemExit(f'未找到 Android 项目: {root}')

# 使用中文资源覆盖默认资源，因此不受手机系统语言影响。
for rel in ('app/src/main/res/values', 'app/src/main/res/values-zh-rCN'):
    dst = root / rel
    dst.mkdir(parents=True, exist_ok=True)
    shutil.copy2(here / 'strings.xml', dst / 'strings.xml')

# 从翻译表脚本中静态读取字典，不执行其中的旧替换逻辑。
tree = ast.parse((here / 'apply_full_zh.py').read_text(encoding='utf-8'))
translations = None
for node in tree.body:
    if isinstance(node, ast.Assign) and any(isinstance(t, ast.Name) and t.id == 'R' for t in node.targets):
        translations = ast.literal_eval(node.value)
        break
if not isinstance(translations, dict):
    raise SystemExit('无法读取翻译表 R')

# 这些短词会出现在 Kotlin 字符串插值表达式的变量名中，不能直接替换。
for unsafe in ('real', 'generated', 'total'):
    translations.pop(unsafe, None)
items = sorted(translations.items(), key=lambda kv: len(kv[0]), reverse=True)

# 只改 Kotlin 双引号字符串内部。排除上述短词后，不会改变插值表达式中的标识符。
string_re = re.compile(r'"((?:\\.|[^"\\])*)"')
def translate_literal(match: re.Match[str]) -> str:
    body = match.group(1).replace('\\"', '"')
    for src, dst in items:
        body = body.replace(src, dst)
    body = body.replace('"', '\\"')
    return '"' + body + '"'

changed = []
for path in root.rglob('*.kt'):
    old = path.read_text(encoding='utf-8')
    new = string_re.sub(translate_literal, old)
    if new != old:
        path.write_text(new, encoding='utf-8')
        changed.append(str(path.relative_to(root)))

print(f'中文资源已覆盖；安全修改 {len(changed)} 个 Kotlin 文件。')
for path in changed:
    print(' -', path)
