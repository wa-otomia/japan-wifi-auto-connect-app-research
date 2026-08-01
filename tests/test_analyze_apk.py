import json
import subprocess
import sys
import tempfile
import unittest
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


class AnalyzeApkTest(unittest.TestCase):
    def test_inventory_and_indicators(self):
        with tempfile.TemporaryDirectory() as directory:
            apk = Path(directory) / "sample.apk"
            with zipfile.ZipFile(apk, "w") as archive:
                archive.writestr("classes.dex", b"Passpoint https://dispatcher.wifi-cloud2.jp/redir")
                archive.writestr("lib/arm64-v8a/libsigner.so", b"fake elf")
                archive.writestr("META-INF/CERT.RSA", b"signature")
            process = subprocess.run(
                [sys.executable, str(ROOT / "tools/analyze_apk.py"), str(apk)],
                check=True, capture_output=True, text=True,
            )
            report = json.loads(process.stdout)["apks"][0]
            self.assertEqual(["classes.dex"], report["dex_files"])
            self.assertIn("https://dispatcher.wifi-cloud2.jp/redir", report["urls"])
            self.assertEqual(["classes.dex"], report["keyword_hits"]["Passpoint"])
            self.assertEqual("arm64-v8a", report["native_libraries"][0]["abi"])
            self.assertEqual(["META-INF/CERT.RSA"], report["signatures"])

    def test_xapk_scans_all_splits_and_limits_entries(self):
        with tempfile.TemporaryDirectory() as directory:
            directory = Path(directory)
            base = directory / "base.apk"
            split = directory / "split.apk"
            with zipfile.ZipFile(base, "w") as archive:
                archive.writestr("classes.dex", b"Passpoint")
                archive.writestr("assets/large.bin", b"x" * 20)
            with zipfile.ZipFile(split, "w") as archive:
                archive.writestr("lib/x86_64/libsigner.so", b"native")
            xapk = directory / "bundle.xapk"
            with zipfile.ZipFile(xapk, "w") as archive:
                archive.write(base, "base.apk")
                archive.write(split, "splits/config.x86_64.apk")
            process = subprocess.run(
                [sys.executable, str(ROOT / "tools/analyze_apk.py"), str(xapk),
                 "--max-entry-size", "10"], check=True, capture_output=True, text=True,
            )
            report = json.loads(process.stdout)
            self.assertEqual(["base.apk", "splits/config.x86_64.apk"],
                             [apk["file"] for apk in report["apks"]])
            self.assertEqual("assets/large.bin", report["apks"][0]["skipped_entries"][0]["name"])
            self.assertEqual("x86_64", report["apks"][1]["native_libraries"][0]["abi"])


if __name__ == "__main__":
    unittest.main()
