package com.codingmart.mockito

import java.util.UUID

object MockBleService {
    val SERVICE_UUID: UUID = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb") // Example UUID for Heart Rate Service
    val CHARACTERISTIC_UUID: UUID = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb") // Example UUID for Heart Rate Measurement
}