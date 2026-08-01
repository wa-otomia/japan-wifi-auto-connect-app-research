#!/usr/bin/env python3
"""Proof of concept for the CloudAP authentication path in jw2 4.0.14.

Run only while connected to a hotspot you are authorized to use.  The program
defaults to printing a redacted request plan; --execute sends network traffic.
"""

import argparse
import base64
import http.cookiejar
import json
import os
import subprocess
import sys
import urllib.parse
import urllib.request

API_KEY = "nttbp"
COMMON_KEY = b"my$?[kq&)a+4j6l$"


def encrypt_payload(payload, iv=None):
    """Match AESStringEncryptor: AES-128-CBC/PKCS#7 and unwrapped Base64."""
    iv = iv or "".join(chr(ord("a") + b % 26) for b in os.urandom(16))
    if len(iv) != 16 or not iv.isascii():
        raise ValueError("IV must contain exactly 16 ASCII characters")
    compact = json.dumps(payload, ensure_ascii=False, separators=(",", ":")).encode()
    process = subprocess.run(
        ["openssl", "enc", "-aes-128-cbc", "-K", COMMON_KEY.hex(),
         "-iv", iv.encode().hex(), "-nosalt"], input=compact,
        capture_output=True, check=True,
    )
    return {"data": base64.b64encode(process.stdout).decode(), "iv": iv, "apikey": API_KEY}


class Client:
    def __init__(self, timeout=15):
        jar = http.cookiejar.CookieJar()
        self.opener = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(jar))
        self.timeout = timeout

    def request(self, url, data=None, headers=None):
        body = data
        if isinstance(data, dict):
            body = json.dumps(data, separators=(",", ":")).encode()
            headers = {"Content-Type": "application/json", **(headers or {})}
        request = urllib.request.Request(url, data=body, headers=headers or {})
        with self.opener.open(request, timeout=self.timeout) as response:
            raw = response.read()
            return response.geturl(), response.status, raw


def form(values):
    return urllib.parse.urlencode({k: v for k, v in values.items() if v is not None}).encode()


def load_endpoints(client, args):
    if args.endpoint_url:
        _, _, raw = client.request(args.endpoint_url, headers={"Accept": "application/json"})
        return json.loads(raw)
    return json.loads(open(args.endpoints, encoding="utf-8").read())


def login(client, endpoints, identity, execute=False):
    required = {"token", "authentication", "authorization", "dispatcher"}
    missing = required.difference(endpoints)
    if missing:
        raise ValueError("endpoint document lacks: " + ", ".join(sorted(missing)))
    encrypted = encrypt_payload(identity)
    plan = [
        {"step": "authentication", "method": "POST", "url": endpoints["authentication"],
         "json": {**encrypted, "data": "<base64 encrypted identity>"}},
        {"step": "authorization", "method": "POST", "url": endpoints["authorization"],
         "form": {"state": endpoints["token"], "code": "<authentication_token>"}},
    ]
    if not execute:
        return {"executed": False, "plan": plan}

    _, status, raw = client.request(endpoints["authentication"], encrypted)
    authentication = json.loads(raw)
    auth_token = authentication.get("authentication_token")
    if not auth_token:
        raise RuntimeError(f"authentication failed (HTTP {status}, code={authentication.get('code')})")
    _, _, raw = client.request(
        endpoints["authorization"],
        form({"state": endpoints["token"], "code": auth_token}),
        {"Content-Type": "application/x-www-form-urlencoded", "ContentType": "application/json"},
    )
    session = json.loads(raw)
    followed = None
    redirect = session.get("redirect") if session.get("status") == "redirect" else None
    if redirect:
        method = redirect.get("method", "GET").upper()
        if method == "POST":
            followed, _, _ = client.request(
                redirect["url"], form(redirect.get("params", {})),
                {"Content-Type": "application/x-www-form-urlencoded"},
            )
        else:
            followed, _, _ = client.request(redirect["url"])
    return {"executed": True, "status": session.get("status"), "code": session.get("code"),
            "redirect_followed": followed, "connection": session.get("connection")}


def parser():
    result = argparse.ArgumentParser(description=__doc__)
    source = result.add_mutually_exclusive_group(required=True)
    source.add_argument("--endpoint-url", help="on-network URL returning CloudAPEndpointJson")
    source.add_argument("--endpoints", help="saved CloudAPEndpointJson file")
    result.add_argument("--ssid", required=True)
    result.add_argument("--bssid", required=True, help="decimal BSSID value used by the app")
    result.add_argument("--uuid", required=True)
    result.add_argument("--login-id", required=True)
    result.add_argument("--password", required=True)
    result.add_argument("--remote-address", default="")
    result.add_argument("--execute", action="store_true", help="send requests; otherwise print a plan")
    return result


def main(argv=None):
    args = parser().parse_args(argv)
    client = Client()
    endpoints = load_endpoints(client, args)
    identity = {"ssid": args.ssid, "application": "", "api_version": "",
                "bssid": args.bssid, "uuid": args.uuid, "login_id": args.login_id,
                "password": args.password, "remote_address": args.remote_address}
    result = login(client, endpoints, identity, args.execute)
    print(json.dumps(result, ensure_ascii=False, indent=2))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

