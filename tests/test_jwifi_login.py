import base64
import json
import subprocess
import unittest

from poc.jwifi_login import COMMON_KEY, encrypt_payload, login


class LoginPocTest(unittest.TestCase):
    def test_encryption_round_trip(self):
        payload = {"ssid": "test", "uuid": "owned-device"}
        wrapped = encrypt_payload(payload, "abcdefghijklmnop")
        decrypted = subprocess.run(
            ["openssl", "enc", "-d", "-aes-128-cbc", "-K", COMMON_KEY.hex(),
             "-iv", wrapped["iv"].encode().hex(), "-nosalt"],
            input=base64.b64decode(wrapped["data"]), capture_output=True, check=True,
        ).stdout
        self.assertEqual(payload, json.loads(decrypted))
        self.assertEqual("nttbp", wrapped["apikey"])

    def test_dry_run_never_uses_client(self):
        class NoNetwork:
            def request(self, *args, **kwargs):
                raise AssertionError("network called during dry-run")

        endpoints = {"token": "own-session", "authentication": "https://auth.invalid",
                     "authorization": "https://authorize.invalid", "dispatcher": "https://dispatch.invalid"}
        result = login(NoNetwork(), endpoints, {"ssid": "test"}, execute=False)
        self.assertFalse(result["executed"])
        self.assertEqual(["authentication", "authorization"], [x["step"] for x in result["plan"]])


if __name__ == "__main__":
    unittest.main()

