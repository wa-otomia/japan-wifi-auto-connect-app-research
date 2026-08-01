#!/usr/bin/env swift
import CoreWLAN
import Foundation

struct Network: Codable {
    let ssid: String
    let bssid: String?
    let rssi: Int
    let channel: Int
    let security: String
}

struct Result: Codable {
    let ok: Bool
    let operation: String
    let interface: String?
    let networks: [Network]?
    let ssid: String?
    let bssid: String?
    let error: String?
}

func emit(_ result: Result, status: Int32 = 0) -> Never {
    let encoder = JSONEncoder()
    encoder.outputFormatting = [.prettyPrinted, .sortedKeys]
    FileHandle.standardOutput.write((try! encoder.encode(result)))
    FileHandle.standardOutput.write(Data("\n".utf8))
    exit(status)
}

let arguments = Array(CommandLine.arguments.dropFirst())
let operation = arguments.first ?? "help"
guard let interface = CWWiFiClient.shared().interface() else {
    emit(Result(ok: false, operation: operation, interface: nil, networks: nil,
                ssid: nil, bssid: nil, error: "CoreWLAN did not return a Wi-Fi interface"), status: 2)
}

do {
    switch operation {
    case "scan":
        let ssid = arguments.count > 1 ? arguments[1] : nil
        let found = try interface.scanForNetworks(withSSID: ssid?.data(using: .utf8))
        let networks = found.map {
            Network(ssid: $0.ssid ?? "", bssid: $0.bssid, rssi: $0.rssiValue,
                    channel: $0.wlanChannel?.channelNumber ?? 0,
                    security: String(describing: $0.security))
        }.sorted { $0.rssi > $1.rssi }
        emit(Result(ok: true, operation: operation, interface: interface.interfaceName,
                    networks: networks, ssid: nil, bssid: nil, error: nil))
    case "current":
        emit(Result(ok: true, operation: operation, interface: interface.interfaceName,
                    networks: nil, ssid: interface.ssid(), bssid: interface.bssid(), error: nil))
    case "connect":
        guard arguments.count >= 2 else {
            emit(Result(ok: false, operation: operation, interface: interface.interfaceName,
                        networks: nil, ssid: nil, bssid: nil,
                        error: "usage: jwifi-core connect SSID [PASSWORD]"), status: 2)
        }
        let ssid = arguments[1]
        let password = arguments.count > 2 ? arguments[2] : nil
        guard let network = try interface.scanForNetworks(withName: ssid)
            .sorted(by: { $0.rssiValue > $1.rssiValue }).first else {
            emit(Result(ok: false, operation: operation, interface: interface.interfaceName,
                        networks: nil, ssid: ssid, bssid: nil, error: "SSID not found"), status: 3)
        }
        try interface.associate(to: network, password: password)
        emit(Result(ok: true, operation: operation, interface: interface.interfaceName,
                    networks: nil, ssid: interface.ssid(), bssid: interface.bssid(), error: nil))
    case "disconnect":
        interface.disassociate()
        emit(Result(ok: true, operation: operation, interface: interface.interfaceName,
                    networks: nil, ssid: nil, bssid: nil, error: nil))
    default:
        emit(Result(ok: false, operation: operation, interface: interface.interfaceName,
                    networks: nil, ssid: nil, bssid: nil,
                    error: "operations: scan [SSID], current, connect SSID [PASSWORD], disconnect"), status: 2)
    }
} catch {
    emit(Result(ok: false, operation: operation, interface: interface.interfaceName,
                networks: nil, ssid: nil, bssid: nil, error: String(describing: error)), status: 1)
}
