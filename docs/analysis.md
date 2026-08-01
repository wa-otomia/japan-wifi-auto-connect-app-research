# 深度分析笔记

## 1. 证据等级

为了避免把合理猜测写成事实，本研究使用以下等级：

| 等级 | 含义 |
| --- | --- |
| A | 可由 APK 哈希、反编译调用链或已记录的网络响应直接复现 |
| B | APK 中有类名、常量或 Android API 引用，但尚未串起完整调用链 |
| C | 根据常见 captive portal 架构作出的假设，必须实验验证 |

4.0.14 样本哈希、相关 JADX 输出和 PoC 已保存，因此以下 CloudAP 项目提升为 **A 级**：

- endpoint JSON 的 `token/authentication/authorization/dispatcher` 数据模型；
- authentication 的加密 JSON envelope、AES 模式、固定 key、随机 IV 和明文字段；
- authentication token 到 authorization form POST，再到 session redirect 的调用链。

以下项目仍属于 **B 级**：

- 包名 `com.nttbp.jw2`，既有分析所针对版本为 4.0.14；
- `PasspointCheckRequestParams`、`PasspointRegisterRequestParams`、
  `PasspointService` 等符号；
- `CloudAPDispatcherJson`、`isUseAuthenticateAPI`、`lastAuthenticatedSSID`、
  `dispatcherUrl` 和 `CAPTIVE_PORTAL_URL` 等符号；
- `WifiNetworkSuggestion`、OpenRoaming 添加/删除 suggestion 的日志文本；
- `wifi-cloud.jp` / `wifi-cloud2.jp` 相关 URL；
- WebView JavaScript bridge；
- Passpoint/OpenRoaming 注册路径（本轮未继续串服务端交互）。

`libsigner.so` 已定位为 Adjust SDK 的签名组件，Java 调用入口是
`com.adjust.sdk.sig.NativeLibHelper`，不在 CloudAP 请求调用链中。JavaScript bridge
是否属于认证页面仍不能仅凭字符串判断。完整 XAPK 由 release 托管，本仓库保存哈希、
机器清单和最小相关 JADX 输出，避免重复提交约 31 MB 的第三方二进制。

## 2. 最可能的状态机

下面是后续反编译应验证的模型，而非已经确认的协议：

```text
扫描/系统回调
      |
      v
匹配本地热点数据库 ---- 不支持 ----> 不处理
      |
      v
连接并探测 captive portal
      |
      v
dispatcher/热点配置
      |
      +---- Web portal ----------> WebView/浏览器交互
      |
      +---- authenticate API ----> 应用请求 --> 网关放行当前会话

另一入口：OpenRoaming 注册 --> Passpoint 配置 --> Android Wi-Fi 框架自动接入
```

关键问题不是“登录 URL 是什么”，而是 dispatcher 的**输入**如何绑定当前 AP/网关，
以及返回值如何选择认证策略。如果请求只有离线常量，桌面复现相对简单；如果依赖
接入网注入参数、短期令牌、设备证明或 Passpoint 凭据，则不能靠复制 URL 完成。

## 3. JADX 调用链清单

对合法取得的 base APK 运行：

```bash
jadx -d out base.apk
rg -n 'Passpoint(Check|Register)|PasspointService|CloudAPDispatcherJson' out
rg -n 'isUseAuthenticateAPI|dispatcherUrl|CAPTIVE_PORTAL_URL' out
rg -n 'addJavascriptInterface|@JavascriptInterface|native ' out/sources
rg -n 'wifi-cloud|OpenRoaming|lastAuthenticatedSSID' out
```

逐个记录：构造参数、调用者、线程、请求方法、请求体序列化类、响应模型、错误分支和
最终状态写入。混淆后的方法名不重要；数据从 SSID/BSSID/portal redirect 流到 HTTP
客户端再流到成功状态的路径才是证据。

## 4. `libsigner.so` 验证

先做无执行的静态检查：

```bash
readelf -hWs libsigner.so
readelf -p .rodata libsigner.so
strings -a libsigner.so | sort -u
```

在 JADX 中寻找 `System.loadLibrary("signer")` 和对应 `native` 声明。只有当参数能从
HTTP 请求构建过程进入 JNI、返回值又进入 header/body 时，才能称其为“请求签名”。
它也可能只是 SDK 的完整性、许可证或其他功能组件。

## 5. 授权动态验证

在自有 Android 设备和允许测试的热点上，按以下最小方案记录：

1. 清除应用数据，记录首次注册与热点登录两阶段，避免混淆长期注册和每次放行。
2. 同时保存 `adb logcat`、系统网络状态和代理/设备侧抓包时间线。
3. 比较认证前后同一个纯 HTTP 探测 URL、DNS 和 HTTPS 请求的结果。
4. 只重放无账号、无设备证明且服务条款允许的自有会话；不要测试他人的标识。
5. 发布前删除 token、Cookie、邮箱、MAC/BSSID、精确位置和设备标识。

应优先回答四个问题：

- dispatcher 是否只在接入目标 WLAN 时可达或有意义？
- 服务端用什么关联当前接入会话（源 IP、网关注入值、MAC 或 token）？
- `isUseAuthenticateAPI` 的 false 分支是否仍提供普通 Web portal？
- OpenRoaming 注册产生的是标准 Passpoint 配置还是厂商私有凭据？

## 6. 对桌面端可行性的判断门槛

只有满足以下全部条件，才值得实现桌面客户端原型：

- 目标热点确实走传统认证 API，而非 Passpoint；
- 服务条款允许非官方客户端；
- 请求不需要 Android Keystore、设备证明或不可导出客户端证书；
- 能以自己的会话合法取得所需 token；
- 成功条件和退出/过期行为已有抓包验证。

在此之前，最准确的结论是：**传统路径存在值得继续验证的桌面复现可能性，
OpenRoaming 路径则应优先使用操作系统原生 Passpoint 支持，而不是模拟 portal。**
