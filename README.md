# Japan Wi-Fi auto-connect 静态研究

这个仓库记录 Android 应用 **Japan Wi-Fi auto-connect**（包名
`com.nttbp.jw2`）的可复现静态分析方法。研究目标是分清应用的普通 captive
portal、厂商认证 API 和 Passpoint/OpenRoaming 三类行为，而不是猜测一个万能的
“登录 POST”。

> 本仓库不包含 APK、用户凭据或现场会话 token；PoC 中的固定协议常量来自公开分发的
> 客户端。请只分析你有权检查的软件，并遵守热点的使用条款。

## 4.0.14 实测静态结论

已下载并用 JADX 1.5.1 反编译 4.0.14 样本。CloudAP 传统路径已经从调用者串到 HTTP
请求：endpoint 文档提供动态 URL 和会话 token；身份 JSON 经 AES-128-CBC 加密后 POST
到 authentication URL，再以返回的 authentication token POST authorization URL，最后
按 session JSON 的 redirect 请求热点网关。完整字段和样本哈希见
[`research/4.0.14/REPORT.md`](research/4.0.14/REPORT.md)。

样本仍包含两条不同的数据路径：

1. **传统热点路径**：应用识别 SSID，探测 captive portal，再根据 dispatcher
   返回的配置选择 Web portal 或认证 API。出现过的主机包括
   `jw2.cdn.wifi-cloud.jp`、`dispatcher.wifi-cloud2.jp` 和
   `portal.wifi-cloud.jp`。
2. **Passpoint/OpenRoaming 路径**：应用注册或取得 Passpoint 配置，再交给 Android
   的 `WifiNetworkSuggestion` / `PasspointConfiguration`。这不是在桌面浏览器中
   重放一个表单就能等价完成的流程。

仓库现在包含可运行的 [`poc/jwifi_login.py`](poc/jwifi_login.py)。它默认只生成脱敏请求
计划，显式增加 `--execute` 才发包。由于没有处于目标 WLAN，尚未把“静态确认的请求
格式”提升为“现场放行成功”；endpoint/token 必须由当前热点合法会话提供。

## macOS 核心 PoC

[`docs/macos-core-design.md`](docs/macos-core-design.md) 给出了最终连接状态机与时序图、
CoreWLAN AP 列表获取方式、会用到的 API 清单、权限/安全边界和现场验收计划。
[`macos/JWiFiCore.swift`](macos/JWiFiCore.swift) 提供扫描、当前网络、连接和断开的 JSON
接口；[`poc/macos_jwifi.py`](poc/macos_jwifi.py) 将它与 captive probe 和既有 CloudAP
认证串成一个默认不发认证请求的端到端编排器。

## 对 APK 做可复现扫描

脚本只使用 Python 标准库，不修改 APK：

```bash
python3 tools/analyze_apk.py path/to/app.xapk --output report.json
```

它会输出：

- 文件 SHA-256 和 ZIP 条目清单；
- DEX / manifest / resources 中的 URL、域名和目标关键词；
- native 库的架构、名称和 SHA-256；
- 是否存在多 DEX、split 元数据及签名文件。

脚本会直接展开 XAPK/APKS/ZIP，并分别报告所有嵌套 APK；无需手工解压。不要只分析
语言或 ABI split：主要 Java/Kotlin 代码通常在 base APK，特定 native 逻辑也可能在
ABI split 的 `.so` 中。扫描设置了单条目解压上限，避免异常压缩包耗尽内存；可以用
`--max-entry-size` 明确调整。

## 建议的下一步

1. 保存应用版本、来源和每个 APK 的 SHA-256。
2. 用本仓库脚本建立机器可读基线。
3. 用 JADX 打开 base APK，从 `PasspointService`、`CloudAPDispatcherJson`、
   `isUseAuthenticateAPI` 和 `dispatcherUrl` 做交叉引用，而不是只看字符串。
4. 对 native 库必须同时检查 JNI 调用者；本样本的 `libsigner.so` 已确认属于 Adjust
   SDK，而不是 CloudAP 认证签名。
5. 仅在自有设备和获准热点上记录请求方法、响应以及认证前后的连通性，并对账号、
   MAC、令牌和 Cookie 脱敏。

## 测试

```bash
python3 -m unittest discover -s tests -v
```
