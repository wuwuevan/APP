#!/usr/bin/env python3
from pathlib import Path
import shutil
import sys

if len(sys.argv) != 2:
    raise SystemExit('用法: apply_full_zh.py <LSFG-Android-Application目录>')
root = Path(sys.argv[1]).resolve()
if not (root / 'app/src/main').exists():
    raise SystemExit(f'未找到 Android 项目: {root}')
here = Path(__file__).resolve().parent
zh = here / 'strings.xml'
values = root / 'app/src/main/res/values'
values.mkdir(parents=True, exist_ok=True)
shutil.copy2(zh, values / 'strings.xml')
zh_cn = root / 'app/src/main/res/values-zh-rCN'
zh_cn.mkdir(parents=True, exist_ok=True)
shutil.copy2(zh, zh_cn / 'strings.xml')

R = {
'Frame generation via Vulkan':'通过 Vulkan 实现帧生成','Share crash report':'分享崩溃报告','Export diagnostic log':'导出诊断日志',
'Shizuku permission denied.':'Shizuku 权限被拒绝。','Shizuku is not running or is too old. Start Shizuku, then try again.':'Shizuku 未运行或版本过旧。请启动 Shizuku 后重试。',
'Ready to start in Shizuku capture mode. Frames are captured from the target UID so the LSFG overlay is not fed back into itself.':'已可使用 Shizuku 捕获模式启动。画面按目标 UID 捕获，可避免 LSFG 悬浮层被重复录入。',
'Ready to start in root capture mode. Frames are captured from the target UID with root privilege — no recording dialog.':'已可使用 Root 捕获模式启动。画面通过 Root 权限按目标 UID 捕获，不会出现录屏确认窗口。',
'Ready to start. When Android asks what to share, choose the target app, not the entire screen.':'已可启动。Android 询问共享内容时，请选择目标应用，不要选择整个屏幕。',
'Complete steps 1 and 2 below to enable the session.':'请先完成下方第 1、2 步以启用会话。','SHIZUKU CAPTURE':'SHIZUKU 捕获','ROOT CAPTURE':'ROOT 捕获',
"Uses Shizuku's privileged UID-filtered capture for the target app, avoiding MediaProjection overlay feedback. Requires the Shizuku app, Shizuku permission, and ADB or wireless debugging active before starting.":'使用 Shizuku 的特权 UID 过滤方式捕获目标应用，避免 MediaProjection 将悬浮层再次捕获。启动前需要安装并运行 Shizuku、授予权限，并开启 ADB 或无线调试。',
'Uses root UID-filtered capture for the target app — no MediaProjection consent dialog. Requires root access granted to LLS in your root manager (Magisk / KernelSU / APatch).':'使用 Root 权限按 UID 捕获目标应用，不会显示 MediaProjection 授权窗口。请在 Root 管理器中向 LLS 授予 Root 权限。',
'Ready':'已就绪','Pending':'处理中','Required':'必需','No app selected':'尚未选择应用','Set':'已设置','Off — raw capture passthrough':'关闭——直接传递原始捕获画面',
'Multiplier ':'倍率 ',' · flow ':' · 光流 ',' · perf':' · 性能模式',' · anti-artifacts':' · 抗伪影','Shizuku capture':'Shizuku 捕获','Root capture':'Root 捕获',
'left handle':'左侧把手','right handle':'右侧把手','top handle':'顶部把手','bottom handle':'底部把手','MORE':'更多','Re-read legal notice':'重新查看法律声明',
'Search apps':'搜索应用','Clear':'清除','Loading installed apps…':'正在加载已安装应用…','No apps match ':'没有匹配的应用：',
'Selected file is ':'所选文件为 ', ', expected ':'，应为 ', '. Pick the correct file.':'。请选择正确的文件。','No file selected':'尚未选择文件',
'Shaders extracted and cached.':'着色器已提取并缓存。','DLL selected. Shaders not extracted yet.':'已选择 DLL，但尚未提取着色器。',
'Extracting and translating shaders…':'正在提取并转换着色器…','Extraction succeeded. SPIR-V cached.':'提取成功，SPIR-V 已缓存。','Extraction failed: ':'提取失败：',
'SOURCE':'文件来源','Pick Lossless.dll from your own legally purchased copy of Lossless Scaling on Steam.':'请从你在 Steam 合法购买的 Lossless Scaling 副本中选择 Lossless.dll。',
'LSFG Frame Gen':'LSFG 帧生成','Master toggle for frame generation. Off = raw capture passthrough.':'帧生成总开关。关闭时直接传递原始捕获画面。',
'Pacing controls timing of generated frames — preset, VSYNC alignment and FPS cap. Full pacing tuning is only available in the in-game overlay (advanced EMA/slack/queue sliders).':'节奏控制用于调整生成帧的时序，包括预设、垂直同步对齐和 FPS 上限。完整高级参数仅可在游戏内悬浮菜单中调整。',
'Current preset: ':'当前预设：',' · VSYNC-aligned: ':' · 垂直同步对齐：',' · FPS cap: ':' · FPS 上限：','NNAPI unavailable':'NNAPI 不可用',
'FRAME GENERATION':'帧生成','Performance mode':'性能模式','HDR mode':'HDR 模式','Anti-artifacts':'抗伪影','FP16 frame-gen shaders':'FP16 帧生成着色器',
'Frame multiplier':'帧倍增倍率','Flow scale':'光流缩放','PACING':'节奏控制','HUD & OVERLAY':'HUD 与悬浮层','FPS counter':'FPS 计数器',
'Frame pacing graph':'帧节奏图','Drawer handle':'抽屉把手','End session':'结束会话','GPU IMAGE QUALITY':'GPU 图像质量','NPU IMAGE QUALITY':'NPU 图像质量',
'CPU IMAGE QUALITY':'CPU 图像质量','GPU upscaler / enhancer':'GPU 放大/增强','NPU image enhancement':'NPU 图像增强','CPU post-process':'CPU 后处理',
'Left':'左侧','Right':'右侧','Top':'顶部','Bottom':'底部','Auto':'自动','Smooth':'平滑','Balanced':'均衡','Responsive':'响应优先','Custom':'自定义',
'real':'真实帧','generated':'生成帧','total':'总帧率',
'1. Accessibility permissions — Step 1':'1. 无障碍权限——步骤 1',
"LSFG-Android needs accessibility permissions to drive the overlay and frame generation correctly. Open your device's Accessibility settings either directly from the system Settings app, or by tapping the accessibility icon in the top-right corner of the LSFG-Android home screen.":'LSFG-Android 需要无障碍权限才能正确运行悬浮层和帧生成。可从系统设置进入“无障碍”，或点按主页右上角的无障碍图标。',
'1. Accessibility permissions — Step 2':'1. 无障碍权限——步骤 2','In the Accessibility settings list, find and tap "LSFG Touch Passthrough".':'在无障碍服务列表中找到并点按“LSFG 触控穿透”。',
'1. Accessibility permissions — Step 3':'1. 无障碍权限——步骤 3','Toggle the accessibility permission on for LSFG-Android, exactly as shown in the screenshot.':'按照截图所示，为 LSFG-Android 开启无障碍权限。',
'1. Accessibility permissions — Step 4':'1. 无障碍权限——步骤 4',
"If the toggle won't turn on, your device is likely blocking restricted settings for sideloaded apps. Open the Android app info screen for LSFG-Android and look at the top-right corner — you'll see a menu icon (highlighted by the red square in the screenshot). Tap it and enable the option that appears (usually \"Allow restricted settings\"). Once done, go back and repeat the previous step — the accessibility toggle will now turn on.":'若开关无法启用，设备可能阻止了侧载应用的受限设置。打开应用信息页面，点按右上角菜单，启用“允许受限设置”，然后返回重试。',
'2. Loading Lossless.dll — Step 1':'2. 加载 Lossless.dll——步骤 1','From the home screen, tap "Select DLL" and pick the Lossless.dll file from your own copy of Lossless Scaling.':'在主页点按“选择 DLL”，然后从你自己的 Lossless Scaling 副本中选择 Lossless.dll。',
'2. Loading Lossless.dll — Step 2':'2. 加载 Lossless.dll——步骤 2','Read the legal notice carefully. To use this app you must own a legitimate copy of Lossless Scaling purchased from Steam — the Lossless.dll you load must come from your own personal copy. The app does not ship the DLL and never will.':'请仔细阅读法律声明。使用本应用必须已在 Steam 合法购买 Lossless Scaling，DLL 必须来自你自己的副本。',
'2. Loading Lossless.dll — Step 3':'2. 加载 Lossless.dll——步骤 3','Use the system file picker to select Lossless.dll from your copy of Lossless Scaling.':'使用系统文件选择器，从你的 Lossless Scaling 副本中选择 Lossless.dll。',
'2. Loading Lossless.dll — Step 4':'2. 加载 Lossless.dll——步骤 4','Once the DLL has been processed, you should land on this screen — the shaders are now extracted and ready.':'DLL 处理完成后会显示此页面，表示着色器已经提取并可用。',
'3. Picking the target app — Step 1':'3. 选择目标应用——步骤 1','To choose which app frame generation should run on, open section 2 of the home menu — "Target app".':'请打开主页第 2 项“目标应用”。',
'3. Picking the target app — Step 2':'3. 选择目标应用——步骤 2',"You'll see every app and game installed on your device. Use the search bar to quickly find the one you want, then tap it to set it as the target.":'页面会列出设备上已安装的应用和游戏。可使用搜索栏查找，然后点按设为目标。',
'4. Overlay & display — Step 1':'4. 悬浮层与显示——步骤 1',"This section contains all the settings related to the LSFG overlay and how it's displayed on screen.":'此部分包含 LSFG 悬浮层及其显示方式的全部设置。',
'4. Overlay & display — Step 2':'4. 悬浮层与显示——步骤 2','This setting controls how you reach the in-session overlay menu. The default is an icon button: tap it during a session to open the live settings drawer. Alternatively you can switch to a swipe-in Drawer (drag from the screen edge toward the center). Warning: on some Android devices the Drawer mode does not work reliably, leaving you unable to open the overlay menu or even close the overlay. If this happens, you may need to reboot the device to restore the icon button mode. Try the icon button first.':'此设置决定会话中如何打开悬浮菜单。默认方式是悬浮图标；也可改为侧边抽屉。部分设备的抽屉模式可能不稳定，建议优先使用悬浮图标。',
'4. Overlay & display — Step 3':'4. 悬浮层与显示——步骤 3','If your device supports the Drawer, you can use this setting to choose which screen edge it lives on (left, right, top or bottom).':'若设备支持抽屉模式，可在此选择把手所在边缘。',
'4. Overlay & display — Step 4':'4. 悬浮层与显示——步骤 4','Here you can enable the on-screen FPS counter and the frame-pacing graph. Note: these readouts are best-effort and may not be 100% accurate.':'可在此启用 FPS 计数器和帧节奏图，相关读数仅供参考。',
'4. Overlay & display — Step 5':'4. 悬浮层与显示——步骤 5',"This option hosts the overlay through the accessibility service instead of a regular system overlay. If the standard overlay misbehaves on your device, try enabling this — it's more robust against aggressive OEM background killers.":'此选项通过无障碍服务托管悬浮层。若普通模式异常，可尝试启用。',
'4. Overlay & display — Step 6':'4. 悬浮层与显示——步骤 6',"Choose between MediaProjection capture (the default) and Shizuku-assisted capture. If you've already tried MediaProjection — both with and without the accessibility-hosted overlay from the previous step — and the overlay still doesn't work, switch to Shizuku here as a fallback.":'可选择默认的 MediaProjection 捕获或 Shizuku 辅助捕获。若前者无法工作，可尝试 Shizuku。',
'5. Frame generation & pacing — Step 1':'5. 帧生成与节奏控制——步骤 1','This section gathers every setting that controls frame generation behavior.':'此部分汇集所有控制帧生成行为的设置。',
'5. Frame generation & pacing — Step 2':'5. 帧生成与节奏控制——步骤 2','These are the settings I consider optimal on top-tier devices such as the Snapdragon 8 Elite Gen 5 — though Flow Scale can safely be pushed up to 1 on those chips. Even on flagship devices I recommend sticking to the Performance variant of LSFG rather than the standard one. Anti-artifacts mode is experimental and I do not recommend enabling it.':'图中是高端设备的推荐设置。建议优先使用性能模式，抗伪影仍属实验功能。',
'6. Starting the overlay — Step 1':'6. 启动悬浮层——步骤 1','Once everything is configured, tap "START SESSION" to launch the target app together with the frame-generation overlay.':'配置完成后，点按“启动会话”。',
'6. Starting the overlay — Step 2':'6. 启动悬浮层——步骤 2','On some Android devices a screen-sharing picker may appear at this point. When it does, choose to share the same app you already selected as the target inside LSFG-Android — not the entire screen.':'若出现屏幕共享选择器，请共享目标应用，不要共享整个屏幕。',
'6. Starting the overlay — Step 3':'6. 启动悬浮层——步骤 3','The target app will now launch with the LSFG overlay running on top, and frame generation will be active.':'目标应用将启动，LSFG 悬浮层显示在上方并启用帧生成。',
'7. Overlay menu — Step 1':'7. 悬浮菜单——步骤 1','If you picked the icon button from the settings, this is how it appears on the side of the screen during a session — tap it to open the overlay menu.':'若选择悬浮图标，点按屏幕侧边的图标即可打开菜单。',
'7. Overlay menu — Step 2':'7. 悬浮菜单——步骤 2','If you picked the Drawer from the settings, this is how it appears on the screen edge you selected. To open the overlay menu, swipe the Drawer from the edge toward the center of the screen.':'若选择侧边抽屉，从指定边缘向屏幕中心滑动即可打开菜单。',
'7. Overlay menu — Step 3':'7. 悬浮菜单——步骤 3','These are all the options available inside the overlay menu — every parameter listed here can be tweaked live during a session.':'这里列出了悬浮菜单中的全部选项，参数可在会话期间实时调整。',
'7. Overlay menu — Step 4':'7. 悬浮菜单——步骤 4','7. Overlay menu — Step 5':'7. 悬浮菜单——步骤 5','7. Overlay menu — Step 6':'7. 悬浮菜单——步骤 6',
}
changed=[]
for p in root.rglob('*.kt'):
    old=p.read_text(encoding='utf-8'); new=old
    for a,b in sorted(R.items(),key=lambda kv:len(kv[0]),reverse=True): new=new.replace(a,b)
    if new!=old:
        p.write_text(new,encoding='utf-8'); changed.append(str(p.relative_to(root)))
print(f'已覆盖中文资源并修改 {len(changed)} 个 Kotlin 文件')
for x in changed: print(' -',x)
