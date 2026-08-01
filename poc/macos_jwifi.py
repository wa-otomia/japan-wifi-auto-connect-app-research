#!/usr/bin/env python3
"""macOS scan -> associate -> CloudAP authentication orchestrator.

Only use networks and accounts you are authorized to access. Authentication is
dry-run unless --execute-auth is explicitly supplied.
"""
import argparse
import json
import os
import re
import subprocess
import sys
import urllib.request

from poc.jwifi_login import Client, load_endpoints, login


def run_core(binary, *arguments):
    process = subprocess.run([binary, *arguments], text=True, capture_output=True)
    try:
        result = json.loads(process.stdout)
    except json.JSONDecodeError as exc:
        raise RuntimeError(f"CoreWLAN helper returned invalid JSON: {process.stderr.strip()}") from exc
    if process.returncode or not result.get("ok"):
        raise RuntimeError(result.get("error") or "CoreWLAN operation failed")
    return result


def select_network(networks, ssid_pattern, minimum_rssi=-80):
    pattern = re.compile(ssid_pattern)
    candidates = [n for n in networks if n.get("ssid") and n.get("rssi", -999) >= minimum_rssi
                  and pattern.search(n["ssid"])]
    return max(candidates, key=lambda n: n["rssi"], default=None)


def bssid_decimal(bssid):
    """Convert aa:bb:cc:dd:ee:ff to the decimal representation used by jw2."""
    if not bssid or not re.fullmatch(r"(?:[0-9a-fA-F]{2}:){5}[0-9a-fA-F]{2}", bssid):
        raise ValueError("a canonical BSSID is required for CloudAP authentication")
    return str(int(bssid.replace(":", ""), 16))


def probe(url, timeout=10):
    request = urllib.request.Request(url, headers={"User-Agent": "jwifi-macos-poc/1"})
    with urllib.request.urlopen(request, timeout=timeout) as response:
        return {"status": response.status, "final_url": response.geturl(),
                "content_type": response.headers.get_content_type()}


def parser():
    result = argparse.ArgumentParser(description=__doc__)
    result.add_argument("--core", default=os.path.join(os.path.dirname(__file__), "..", "macos", "jwifi-core"))
    result.add_argument("--ssid-regex", required=True, help="allow-list regex; strongest match is selected")
    result.add_argument("--minimum-rssi", type=int, default=-80)
    result.add_argument("--wifi-password", default=os.getenv("JWIFI_WIFI_PASSWORD"))
    result.add_argument("--probe-url", default="http://captive.apple.com/hotspot-detect.html")
    result.add_argument("--endpoint-url", help="CloudAP endpoint JSON URL obtained from this WLAN")
    result.add_argument("--uuid", default=os.getenv("JWIFI_UUID"))
    result.add_argument("--login-id", default=os.getenv("JWIFI_LOGIN_ID"))
    result.add_argument("--password", default=os.getenv("JWIFI_PASSWORD"))
    result.add_argument("--remote-address", default="")
    result.add_argument("--execute-auth", action="store_true")
    result.add_argument("--scan-only", action="store_true")
    return result


def main(argv=None):
    args = parser().parse_args(argv)
    scanned = run_core(args.core, "scan")
    chosen = select_network(scanned.get("networks", []), args.ssid_regex, args.minimum_rssi)
    if not chosen:
        raise SystemExit("No allow-listed AP met the RSSI threshold")
    output = {"selected": chosen}
    if args.scan_only:
        print(json.dumps(output, ensure_ascii=False, indent=2))
        return 0
    connected = run_core(args.core, "connect", chosen["ssid"], *([args.wifi_password] if args.wifi_password else []))
    output["connected"] = connected
    output["captive_probe"] = probe(args.probe_url)
    if args.endpoint_url:
        required = (args.uuid, args.login_id, args.password)
        if not all(required):
            raise SystemExit("CloudAP requires JWIFI_UUID, JWIFI_LOGIN_ID and JWIFI_PASSWORD (or flags)")
        endpoint_args = argparse.Namespace(endpoint_url=args.endpoint_url, endpoints=None)
        client = Client()
        endpoints = load_endpoints(client, endpoint_args)
        identity = {"ssid": chosen["ssid"], "application": "", "api_version": "",
                    "bssid": bssid_decimal(connected.get("bssid") or chosen.get("bssid")),
                    "uuid": args.uuid, "login_id": args.login_id, "password": args.password,
                    "remote_address": args.remote_address}
        output["authentication"] = login(client, endpoints, identity, args.execute_auth)
    print(json.dumps(output, ensure_ascii=False, indent=2))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
