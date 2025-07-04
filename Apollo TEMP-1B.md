# Apollo Automation TEMP-1B Driver for Hubitat Elevaion

## Apollo Automation TEMP-1B Device Description

| Device Images | Device Features |
|---------------|-----------------|
| ![TEMP-1B Main Device](https://apolloautomation.com/cdn/shop/files/TEMP-1B_Fridge.png?v=1742996496&width=1920) | **Main Device Features:**<br/>• ESP32-based platform with WiFi and Bluetooth connectivity<br/>• Battery-powered operation (CR123A or 16340 rechargeable battery)<br/>• Temperature Range: -40°C to +85°C (-40°F to +185°F)<br/>• Humidity Range: 0-100% RH with ±2% accuracy<br/>• Onboard AHT20-F temperature and humidity sensor<br/>• RGB LED indicator with customizable colors<br/>• Buzzer for temperature alerts<br/>• 3.5mm jack for external probes<br/>• Up to 6 months battery life with sleep mode |
| ![Temperature Probe](https://apolloautomation.com/cdn/shop/files/20241205-123547.jpg?v=1733420196&width=1920) | **Temperature Probe Features:**<br/>• DS18B20 waterproof temperature sensor<br/>• Available in 20cm (~8in) and 1.5m (~5ft) lengths<br/>• Flat cable design prevents interference with fridge seals<br/>• Submersible and waterproof construction<br/>• Temperature Range: -55°C to +85°C (-67°F to +185°F)<br/>• ±0.5°C accuracy<br/>• Ideal for freezer, fridge, aquarium, and pool monitoring |
| ![Food Probe](https://apolloautomation.com/cdn/shop/files/TEMP-1_with_Food_Probe.png?v=1742996496&width=1920) | **Food Probe Features:**<br/>• 1m (~3ft) stainless steel food-safe probe<br/>• NTC temperature sensor<br/>• Temperature Range: -40°C to +204°C (-40°F to +400°F)<br/>• Food-safe stainless steel construction<br/>• Perfect for grilling, baking, and cooking<br/>• Not dishwasher safe<br/>• Real-time temperature monitoring for perfect cooking results |

## Driver Description

### Major Attributes

The Apollo Automation TEMP-1B Hubitat driver provides comprehensive monitoring and control capabilities through the following primary attributes:

#### Core Attributes
- **`temperature`** (number): Primary temperature reading based on selected probe preference
- **`humidity`** (number): Relative humidity from onboard AHT20-F sensor (0-100% RH)
- **`networkStatus`** (enum): Connection status ['connecting', 'online', 'offline']
- **`rgbLight`** (enum): RGB LED control ['on', 'off']

### Complete Attribute List

| Attribute | Type | Description | Diagnostic |
|-----------|------|-------------|------------|
| `temperature` | number | Primary temperature reading (°C/°F) | No |
| `humidity` | number | Relative humidity percentage | No |
| `networkStatus` | enum | Device connection status | Yes |
| `rgbLight` | enum | RGB LED control | No |
| `battery` | number | Battery charge level (0-100%) | No |
| `batteryVoltage` | number | Battery voltage measurement | No |
| `boardTemperature` | number | Internal board temperature | Yes |
| `boardTemperatureOffset` | number | Board temperature calibration offset | Yes |
| `boardHumidityOffset` | number | Board humidity calibration offset | Yes |
| `espTemperature` | number | ESP32 chip temperature | Yes |
| `temperatureProbe` | number | External temperature probe reading | Yes |
| `tempProbeOffset` | number | Temperature probe calibration offset | Yes |
| `foodProbe` | number | Food probe temperature reading | Yes |
| `foodProbeOffset` | number | Food probe calibration offset | Yes |
| `uptime` | string | Device uptime since last restart | Yes |
| `rssi` | number | WiFi signal strength (dBm) | Yes |
| `alarmOutsideTempRange` | enum | Temperature range alarm status | Yes |
| `notifyOnlyOutsideTempDifference` | enum | Temperature difference notification | Yes |
| `preventSleep` | enum | Sleep prevention control | Yes |
| `selectedProbe` | string | Active temperature probe selection | Yes |
| `sleepDuration` | number | Sleep duration in hours | Yes |
| `probeTempDifferenceThreshold` | number | Temperature difference threshold | Yes |
| `minProbeTemp` | number | Minimum probe temperature threshold | Yes |
| `maxProbeTemp` | number | Maximum probe temperature threshold | Yes |

### Device Capabilities

The driver implements the following Hubitat capabilities:

- **Sensor**: Basic sensor functionality
- **Refresh**: Manual device refresh capability
- **RelativeHumidityMeasurement**: Humidity sensing
- **SignalStrength**: WiFi signal monitoring
- **TemperatureMeasurement**: Temperature sensing
- **Battery**: Battery level monitoring
- **Initialize**: Device initialization

### Commands

#### setRgbLight
**Purpose**: Control the RGB LED indicator
**Parameters**: 
- `value` (enum): LED state ['off', 'on']
**Usage**: `setRgbLight('on')` or `setRgbLight('off')`

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

- **ESPHome Compatible**: Full integration with ESPHome firmware
- **Home Assistant Ready**: Seamless integration with Home Assistant
- **Local Operation**: No cloud dependency - all processing local
- **Open Source**: Fully open-source firmware and hardware designs

### Use Cases

The Apollo Automation TEMP-1B is ideal for:

- **Kitchen Monitoring**: Food temperature during cooking and baking
- **Refrigeration**: Freezer and refrigerator temperature monitoring
- **Aquarium Management**: Water temperature monitoring
- **Pool/Spa Monitoring**: Water temperature tracking
- **Greenhouse Monitoring**: Air and soil temperature monitoring
- **HVAC Monitoring**: Room temperature and humidity tracking
- **Server Room Monitoring**: Environmental condition monitoring

### Technical Specifications

- **Microcontroller**: ESP32 with WiFi and Bluetooth
- **Power**: CR123A or 16340 rechargeable battery
- **Battery Life**: Up to 6 months with optimized sleep settings
- **Temperature Accuracy**: ±0.5°C (probes), ±2°C (onboard sensor)
- **Humidity Accuracy**: ±2% RH
- **Operating Range**: -40°C to +85°C (-40°F to +185°F)
- **Connectivity**: WiFi 802.11 b/g/n, Bluetooth 4.2
- **Dimensions**: Compact 3D-printed enclosure
- **Mounting**: Optional magnetic mount available

### Support and Resources

- **Documentation**: Complete setup guides and troubleshooting
- **Community**: Active Discord community for support
- **Open Source**: GitHub repository with full source code
- **Updates**: Regular firmware updates with new features
- **Customization**: Fully customizable through ESPHome YAML configuration

This driver provides a comprehensive interface to all TEMP-1B capabilities while maintaining simplicity for everyday use. The diagnostic reporting system allows advanced users to access detailed technical information while keeping the standard interface clean and user-friendly.
