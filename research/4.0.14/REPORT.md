# Japan Wi-Fi auto-connect 4.0.14 反编译结果

## 样本

* XAPK SHA-256：`d2449612ef18994fe7ef8b944456dc1ada6c513ba730919d3bbdb2dd4d75e0b5`
* XAPK 大小：30,937,233 bytes
* base APK：`com.nttbp.jw2.apk`，包含 `classes.dex`、`classes2.dex`、`classes3.dex`
* ABI split：arm64-v8a 与 armeabi-v7a；两者均包含 `libsigner.so`
* 反编译器：JADX 1.5.1

完整机器清单保存在 `artifacts/inventory.json`。XAPK 已由上游 release 提供，仓库不重复
提交二进制；`sources/` 只保存与认证协议直接相关的 JADX 输出。

## 已确认的 CloudAP 协议

这次已经串起调用链，不再只是字符串猜测：

1. 应用先 GET captive portal 检测得到的 endpoint URL，并解析出
   `token`、`authentication`、`authorization`、`dispatcher`。
2. authentication 是动态 URL POST，请求 JSON 不是明文，而是
   `{"data": Base64(AES-CBC(payload)), "iv": ..., "apikey": "nttbp"}`。
3. AES 为 `AES/CBC/PKCS5Padding`（对 AES 等同 PKCS#7），固定 16-byte key
   `my$?[kq&)a+4j6l$`，IV 是 16 个随机小写字母。
4. 明文 payload 字段是 `ssid`、`application`、`api_version`、`bssid`、`uuid`、
   `login_id`、`password`、`remote_address`。应用在这里把 BSSID 作为十进制字符串。
5. authentication 响应包含 `code` 和 `authentication_token`。
6. authorization URL 随后收到 form POST：`state=<endpoint token>`、
   `code=<authentication_token>`，并返回 CloudAP session JSON。
7. session 若为 `status=redirect`，应用按服务端给出的 GET/POST 及参数继续请求；POST
   参数白名单为 `username,user,cmd,url,password,success_url,continue_url`。

`libsigner.so` 属于 Adjust SDK 的 `com.adjust.sdk.sig.Signer`，与上述 CloudAP 加密请求
没有调用关系。此前“它可能给认证 API 签名”的假设已被反编译证伪。

## PoC

`poc/jwifi_login.py` 实现上述 authentication → authorization → redirect 流程。endpoint
必须来自当前获准热点的 captive portal，不能离线硬编码。默认只打印脱敏计划：

```bash
python3 poc/jwifi_login.py \
  --endpoint-url http://portal-provided/endpoint.json \
  --ssid '.Free Wi-Fi for Application' --bssid 123456789 \
  --uuid YOUR_UUID --login-id YOUR_LOGIN --password YOUR_PASSWORD
```

确认目标是自己有权使用的热点后增加 `--execute` 才会发包。未在目标日本热点现场进行
端到端测试，所以“字节级请求格式与应用一致”是静态分析已确认项，“指定现场能够放行”
仍需要现场验证。

