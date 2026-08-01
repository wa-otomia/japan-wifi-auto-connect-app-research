import unittest
from unittest.mock import patch

from poc.macos_jwifi import bssid_decimal, run_core, select_network


class MacOSJWiFiTest(unittest.TestCase):
    def test_selects_strongest_allowlisted_network(self):
        networks = [{"ssid": "Cafe", "rssi": -40}, {"ssid": "FreeWiFi-A", "rssi": -70},
                    {"ssid": "FreeWiFi-B", "rssi": -51}]
        self.assertEqual("FreeWiFi-B", select_network(networks, r"^FreeWiFi", -75)["ssid"])

    def test_bssid_decimal(self):
        self.assertEqual("187723572702975", bssid_decimal("aa:bb:cc:dd:ee:ff"))
        with self.assertRaises(ValueError):
            bssid_decimal("not-a-bssid")

    @patch("poc.macos_jwifi.subprocess.run")
    def test_core_failure_is_reported(self, mocked):
        mocked.return_value.returncode = 1
        mocked.return_value.stdout = '{"ok":false,"error":"denied"}'
        mocked.return_value.stderr = ""
        with self.assertRaisesRegex(RuntimeError, "denied"):
            run_core("helper", "scan")


if __name__ == "__main__":
    unittest.main()
