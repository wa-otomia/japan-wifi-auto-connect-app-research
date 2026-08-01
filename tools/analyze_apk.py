#!/usr/bin/env python3
"""Produce a conservative, machine-readable static inventory of APK/XAPK files."""

import argparse
import hashlib
import json
import re
import sys
import zipfile
from io import BytesIO
from pathlib import Path

URL_RE = re.compile(rb"https?://[A-Za-z0-9._~:/?#\[\]@!$&'()*+,;=%-]{4,}")
DOMAIN_RE = re.compile(rb"(?<![A-Za-z0-9.-])(?:[A-Za-z0-9-]+\.)+(?:jp|com|net|org)(?![A-Za-z0-9.-])")
KEYWORDS = (
    b"Passpoint", b"OpenRoaming", b"CloudAPDispatcherJson",
    b"isUseAuthenticateAPI", b"lastAuthenticatedSSID", b"dispatcherUrl",
    b"CAPTIVE_PORTAL_URL", b"addJavascriptInterface", b"JavascriptInterface",
    b"WifiNetworkSuggestion", b"captive_portal", b"wifi-cloud",
)


def sha256(data):
    return hashlib.sha256(data).hexdigest()


DEFAULT_MAX_ENTRY = 64 * 1024 * 1024
DEFAULT_MAX_APK = 512 * 1024 * 1024


def scan_bytes(raw, name, max_entry_size=DEFAULT_MAX_ENTRY):
    """Scan one APK represented by bytes, refusing unexpectedly large entries."""
    result = {"file": name, "size": len(raw), "sha256": sha256(raw)}
    urls, domains, hits, entries, libraries, signatures = set(), set(), {}, [], [], []
    skipped = []
    with zipfile.ZipFile(BytesIO(raw)) as archive:
        for info in archive.infolist():
            entries.append({"name": info.filename, "size": info.file_size, "crc32": f"{info.CRC:08x}"})
            if info.is_dir():
                continue
            if info.file_size > max_entry_size:
                skipped.append({"name": info.filename, "size": info.file_size, "reason": "entry_size_limit"})
                continue
            data = archive.read(info)
            urls.update(x.decode("ascii", "ignore") for x in URL_RE.findall(data))
            domains.update(x.decode("ascii", "ignore") for x in DOMAIN_RE.findall(data))
            for keyword in KEYWORDS:
                if keyword.lower() in data.lower():
                    hits.setdefault(keyword.decode(), []).append(info.filename)
            if info.filename.startswith("lib/") and info.filename.endswith(".so"):
                parts = info.filename.split("/")
                libraries.append({"path": info.filename, "abi": parts[1] if len(parts) > 2 else None,
                                  "size": len(data), "sha256": sha256(data)})
            upper = info.filename.upper()
            if upper.startswith("META-INF/") and upper.endswith((".RSA", ".DSA", ".EC", ".SF")):
                signatures.append(info.filename)
    result.update({
        "dex_files": sorted(x["name"] for x in entries if re.fullmatch(r"classes\d*\.dex", x["name"])),
        "signatures": sorted(signatures), "native_libraries": sorted(libraries, key=lambda x: x["path"]),
        "urls": sorted(urls), "domains": sorted(domains),
        "keyword_hits": {k: sorted(v) for k, v in sorted(hits.items())},
        "skipped_entries": skipped,
        "entries": entries,
    })
    return result


def scan(path, max_entry_size=DEFAULT_MAX_ENTRY):
    return scan_bytes(path.read_bytes(), path.name, max_entry_size)


def scan_container(path, max_entry_size=DEFAULT_MAX_ENTRY, max_apk_size=DEFAULT_MAX_APK):
    """Scan a plain APK or every APK member of an XAPK/APKS/ZIP container."""
    raw = path.read_bytes()
    suffix = path.suffix.lower()
    if suffix == ".apk":
        return {"container": {"file": path.name, "size": len(raw), "sha256": sha256(raw)},
                "apks": [scan_bytes(raw, path.name, max_entry_size)]}
    reports = []
    with zipfile.ZipFile(BytesIO(raw)) as archive:
        for info in sorted(archive.infolist(), key=lambda item: item.filename):
            if info.filename.lower().endswith(".apk") and not info.is_dir():
                if info.file_size > max_apk_size:
                    reports.append({"file": info.filename, "size": info.file_size,
                                    "error": "apk_size_limit"})
                else:
                    try:
                        reports.append(scan_bytes(archive.read(info), info.filename, max_entry_size))
                    except zipfile.BadZipFile:
                        reports.append({"file": info.filename, "size": info.file_size,
                                        "error": "invalid_nested_apk"})
    return {"container": {"file": path.name, "size": len(raw), "sha256": sha256(raw)},
            "apks": reports}


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("apk", type=Path)
    parser.add_argument("--output", "-o", type=Path)
    parser.add_argument("--max-entry-size", type=int, default=DEFAULT_MAX_ENTRY,
                        help="maximum uncompressed bytes read from one ZIP entry")
    parser.add_argument("--max-apk-size", type=int, default=DEFAULT_MAX_APK,
                        help="maximum uncompressed bytes read for a nested APK")
    args = parser.parse_args(argv)
    if not args.apk.is_file():
        parser.error(f"not a file: {args.apk}")
    try:
        if args.max_entry_size < 1 or args.max_apk_size < 1:
            parser.error("size limits must be positive")
        report = scan_container(args.apk, args.max_entry_size, args.max_apk_size)
    except zipfile.BadZipFile:
        parser.error(f"not a valid APK/ZIP: {args.apk}")
    rendered = json.dumps(report, ensure_ascii=False, indent=2) + "\n"
    if args.output:
        args.output.write_text(rendered, encoding="utf-8")
    else:
        sys.stdout.write(rendered)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
