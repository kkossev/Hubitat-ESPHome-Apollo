# Apollo Automation TEMP-1B Driver for Hubitat Elevation

## ✨ Direct Hubitat Integration - No Home Assistant Required!

This driver connects your Apollo Automation TEMP-1B **directly to Hubitat Elevation** thanks to the great work done by Jonathan Bradshaw (@jonathanb) - https://github.com/bradsjm/hubitat-public/tree/main/ESPHome 

- **🚫 No Home Assistant Needed**: Direct WiFi connection to your Hubitat hub
- **🚫 No YAML Files to Edit**: Simple IP address configuration - that's it!
- **🚫 No Cloud Dependencies**: Everything runs locally on your network
- **⚡ Easy Setup**: Install this driver package from HPM (Hubitat Package Manager), connect the device to your WiFi network, configure the IP address.
- **🔧 Zero Configuration**: All device features work out-of-the-box
- **🏠 Native Hubitat Integration**: Full support for Hubitat automations, dashboards, and apps

![Made for ESPHome](https://esphome.io/_images/made-for-esphome-black-on-white.svg)


Apollo Automation devices come **pre-flashed with ESPHome firmware** - just connect your TEMP-1B to your WiFi network and add the device IP address to this driver. No additional software, bridges, or complex configurations required!


<details>
<summary>Technical Specifications</summary>

- **Microcontroller**: ESP32 with WiFi and Bluetooth
- **Power**: CR123A or 16340 rechargeable battery
- **Battery Life**: Up to 6 months with optimized sleep settings
- **Temperature Accuracy**: ±0.5°C (probes), ±2°C (onboard sensor)
- **Humidity Accuracy**: ±2% RH
- **Operating Range**: -40°C to +85°C (-40°F to +185°F)
- **Connectivity**: WiFi 802.11 b/g/n, Bluetooth 4.2
- **Dimensions**: Compact 3D-printed enclosure
- **Mounting**: Optional magnetic mount available
</details>


<details>
<summary>Use Cases</summary>

The Apollo Automation TEMP-1B is ideal for:

- **Kitchen Monitoring**: Food temperature during cooking and baking
- **Refrigeration**: Freezer and refrigerator temperature monitoring
- **Aquarium Management**: Water temperature monitoring
- **Pool/Spa Monitoring**: Water temperature tracking
- **Greenhouse Monitoring**: Air and soil temperature monitoring
- **HVAC Monitoring**: Room temperature and humidity tracking
- **Server Room Monitoring**: Environmental condition monitoring

</details>


## Apollo Automation TEMP-1B Device Description

| Device Images | Device Features |
|---------------|-----------------|
| ![TEMP-1B Main Device](https://apolloautomation.com/cdn/shop/files/TEMP-1B_Fridge.png?v=1742996496&width=960) | **Main Device Features:**<br/>• ESP32-based platform with WiFi and Bluetooth connectivity<br/>• Battery-powered operation (CR123A or 16340 rechargeable battery)<br/>• Temperature Range: -40°C to +85°C (-40°F to +185°F)<br/>• Humidity Range: 0-100% RH with ±2% accuracy<br/>• Onboard AHT20-F temperature and humidity sensor<br/>• RGB LED indicator with customizable colors<br/>• Buzzer for temperature alerts<br/>• 3.5mm jack for external probes<br/>• Up to 6 months battery life with sleep mode |
| **Temperature Probe Features:**<br/>• DS18B20 waterproof temperature sensor<br/>• Available in 20cm (~8in) and 1.5m (~5ft) lengths<br/>• Flat cable design prevents interference with fridge seals<br/>• Submersible and waterproof construction<br/>• Temperature Range: -55°C to +85°C (-67°F to +185°F)<br/>• ±0.5°C accuracy<br/>• Ideal for freezer, fridge, aquarium, and pool monitoring | ![Temperature Probe](https://apolloautomation.com/cdn/shop/files/20241205-123547.jpg?v=1733420196&width=960) | 
| ![Food Probe](https://apolloautomation.com/cdn/shop/files/TEMP-1_with_Food_Probe.png?v=1742996496&width=960) | **Food Probe Features:**<br/>• 1m (~3ft) stainless steel food-safe probe<br/>• NTC temperature sensor<br/>• Temperature Range: -40°C to +204°C (-40°F to +400°F)<br/>• Food-safe stainless steel construction<br/>• Perfect for grilling, baking, and cooking<br/>• Not dishwasher safe<br/>• Real-time temperature monitoring for perfect cooking results |

## Driver Description


### Major Attributes

The Apollo Automation TEMP-1B driver provides comprehensive monitoring and control capabilities with dozens of available attributes. For everyday use, the driver displays only the essential attributes by default, keeping your device interface clean and focused. Advanced users can enable additional diagnostic attributes through the preferences when needed.

#### Main Attributes
- **`temperature`**: Primary temperature reading based on selected probe preference
- **`humidity`**: Relative humidity from onboard AHT20-F sensor (0-100% RH)
- **`networkStatus`**: Connection status ['connecting', 'online', 'offline']
- **`rgbLight`**: RGB LED control ['on', 'off']

![Current States](https://github.com/kkossev/Hubitat-ESPHome-Apollo/raw/main/Images/current_states.png)

### Advanced Attributes
These advanced attributes, disabled by default, provide additional insights and control for power users seeking enhanced functionality. 

<details>
<summary>Complete Attribute List</summary>

| Attribute | Type | Description |
|-----------|------|-------------|
| `temperature` | number | Primary temperature reading (°C/°F) |
| `humidity` | number | Relative humidity percentage |
| `networkStatus` | enum | Device connection status |
| `rgbLight` | enum | RGB LED control |
| `battery` | number | Battery charge level (0-100%) |
| `batteryVoltage` | number | Battery voltage measurement |
| `boardTemperature` | number | Internal board temperature |
| `boardTemperatureOffset` | number | Board temperature calibration offset |
| `boardHumidityOffset` | number | Board humidity calibration offset |
| `espTemperature` | number | ESP32 chip temperature |
| `temperatureProbe` | number | External temperature probe reading |
| `tempProbeOffset` | number | Temperature probe calibration offset |
| `foodProbe` | number | Food probe temperature reading |
| `foodProbeOffset` | number | Food probe calibration offset |
| `uptime` | string | Device uptime since last restart |
| `rssi` | number | WiFi signal strength (dBm) |
| `alarmOutsideTempRange` | enum | Temperature range alarm status |
| `notifyOnlyOutsideTempDifference` | enum | Temperature difference notification |
| `preventSleep` | enum | Sleep prevention control |
| `selectedProbe` | string | Active temperature probe selection |
| `sleepDuration` | number | Sleep duration in hours |
| `probeTempDifferenceThreshold` | number | Temperature difference threshold |
| `minProbeTemp` | number | Minimum probe temperature threshold |
| `maxProbeTemp` | number | Maximum probe temperature threshold |

![Advanced Attributes](https://github.com/kkossev/Hubitat-ESPHome-Apollo/raw/main/Images/advanced_attributes.png)

</details>

----

### Commands

#### initialize
**Purpose**: Establishes connection to the device and starts monitoring
**Usage**: Automatically called during device setup or manually from device commands

#### refresh
**Purpose**: Refreshes device information and clears cached data
**Usage**: Manually refresh device status and request updated information from the device

#### setRgbLight
**Purpose**: Control the RGB LED indicator
**Parameters**: 
- `value`: LED state ['off', 'on']
**Usage**: `setRgbLight('on')` or `setRgbLight('off')`

![Commands](https://github.com/kkossev/Hubitat-ESPHome-Apollo/raw/main/Images/apollo-temp1(b)-hubitat-commands.png)

### Preferences

#### Basic Settings
| Setting | Type | Default | Description |
|---------|------|---------|-------------|
| `logEnable` | bool | false | Enable debug logging for troubleshooting |
| `txtEnable` | bool | true | Enable descriptive text logging |
| `ipAddress` | text | (required) | Device IP address for ESPHome API connection |
| `selectedProbe` | enum | 'Temperature' | Select primary temperature sensor ['Temperature', 'Food'] |
| `boardHumidityOffset` | decimal | 0.0 | Board humidity calibration offset (-50% to +50%) |

#### Advanced Options
| Setting | Type | Default | Description |
|---------|------|---------|-------------|
| `password` | text | (optional) | Device password if required |
| `diagnosticsReporting` | bool | false | Enable reporting of diagnostic attributes |
| `logWarnEnable` | bool | true | Enable warning and info logging |

### Entity Management

The driver uses an intelligent entity management system that:

- **Automatically discovers** all available ESPHome entities
- **Maps entities** to appropriate Hubitat attributes
- **Handles missing entities** gracefully (device variants may not have all sensors)
- **Provides diagnostic control** - technical attributes can be hidden from main device view
- **Supports calibration** - offset values sync between Hubitat preferences and ESPHome

### Temperature Handling

The driver provides flexible temperature management:

- **Dual Temperature Sources**: Choose between Temperature Probe and Food Probe
- **Automatic Unit Conversion**: Converts between Celsius and Fahrenheit based on hub settings
- **Calibration Support**: Individual offset adjustments for each temperature sensor
- **Sleep Mode Compatibility**: Optimized for battery-powered operation with sleep cycles

### Battery Management

For battery-powered TEMP-1B devices:

- **Battery Level Monitoring**: Real-time battery percentage
- **Voltage Monitoring**: Actual battery voltage for detailed analysis
- **Sleep Mode Support**: Configurable sleep duration to extend battery life
- **Prevent Sleep Option**: Keep device awake for continuous monitoring when needed

### Network Monitoring

- **Connection Status**: Real-time online/offline status
- **Signal Strength**: WiFi RSSI monitoring
- **Automatic Reconnection**: Built-in ESPHome API reconnection logic

### Diagnostic Features

When diagnostic reporting is enabled:

- **ESP32 Monitoring**: Internal chip temperature and performance
- **Uptime Tracking**: Device restart and reliability monitoring
- **Configuration Access**: View and modify advanced device settings
- **Calibration Values**: Access to all sensor offset parameters

### Alert System

The device supports customizable alerts:

- **Temperature Range Alarms**: Configurable min/max temperature thresholds
- **Visual Indicators**: RGB LED with customizable colors
- **Audible Alerts**: Onboard buzzer for critical notifications
- **Notification Controls**: Flexible alert configuration options

### Integration Notes

- **🏠 Direct Hubitat Connection**: Native ESPHome API integration - no bridges or middleware required
- **🚫 No Home Assistant Needed**: Connects directly to Hubitat without additional software
- **🚫 No YAML Configuration**: Simple IP address setup - no complex configuration files
- **⚡ Plug-and-Play**: Flash firmware, connect to WiFi, add IP address - done!
- **🔒 Local Operation**: No cloud dependency - all processing stays on your local network
- **📱 Native Hubitat Features**: Full support for automations, dashboards, and mobile apps
- **🔧 Zero Maintenance**: Self-managing connection with automatic reconnection
- **💻 Open Source**: Fully open-source firmware and hardware designs




