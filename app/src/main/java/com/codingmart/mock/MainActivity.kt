package com.codingmart.mock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codingmart.mock.ui.theme.MockTheme
import com.codingmart.mockito.MockBleDevice

class MainActivity : ComponentActivity() {
    private lateinit var mockBleDevice: MockBleDevice
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MockTheme {
                mockBleDevice = MockBleDevice(this)
                mockBleDevice.startAdvertising()
                HomeScreen(mockBleDevice)
            }
        }
    }
}

@Composable
fun HomeScreen (
    mockBleDevice : MockBleDevice
) {
    var toggle by remember { mutableStateOf(false) }
    var text by remember {
        mutableStateOf("Click to Start heart beat")
    }
    val heartRates by remember { mockBleDevice::heartRateValues }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Mocking Device",
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.padding(10.dp))
        Text(
            text = "Use any BLC scanner to connect",
            fontSize = 16.sp
        )
        Spacer(modifier =Modifier.padding(10.dp))
        Button(onClick = {
            if(toggle){
                mockBleDevice.startHeartRateSimulation()
                text = "Click to Stop heart beat"
            } else {
                mockBleDevice.stopHeartRateSimulation()
                text = "Click to Start heart beat"
            }
            toggle = !toggle
        }) {
            Text(text = text)
        }
        Spacer(modifier = Modifier.padding(20.dp))
        Text(
            text = "Heart Rate Values: ${heartRates.joinToString(", ")}",
            fontSize = 16.sp
        )
    }
}