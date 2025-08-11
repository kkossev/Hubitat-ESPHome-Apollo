## Apollo Automation MSR-2 Driver for Hubitat Elevation

This driver connects your [Apollo Automation MSR-2](https://apolloautomation.com/products/msr-2) sensor **directly to your HE hub** thanks to the great work done by Jonathan Bradshaw (@jonathanb) - the [ESPHome Hubitat Toolkit](https://github.com/bradsjm/hubitat-public/tree/main/ESPHome).

The driver can be installed ~~using the community [Hubitat Package Manager](https://community.hubitat.com/t/release-hubitat-package-manager-hpm-hubitatcommunity/94471/1) app or~~ manually as a Bundle archive from [GitHub](https://github.com/kkossev/Hubitat-ESPHome-Apollo).

- **🚫 No Home Assistant Needed**: Direct WiFi connection to your Hubitat hub
- **🚫 No YAML Files to Edit**: Simple IP address configuration - that's it!
- **🚫 No Cloud Dependencies**: Everything runs locally on your network
- **⚡ Easy Setup**: Install this driver package from HPM (Hubitat Package Manager), connect the device to your WiFi network, configure the IP address in the driver - done.
- **🔧 Zero Configuration**: All device features work out-of-the-box
- **🏠 Native Hubitat Integration**: Full support for Hubitat automations, dashboards, and apps


------

<details>
<summary><b>Technical Specifications</b> (click to expand)</summary>

- **Microcontroller**: ESP32 with WiFi and Bluetooth
- **Power**: USB-C 5V (mains powered)
- **Radar Sensor**: LD2410 mmWave radar for human presence detection
- **Environmental Sensors**: DPS310 pressure sensor, LTR390 light/UV sensor
- **CO₂ Sensor**: SCD40 NDIR sensor (400-40,000 ppm)
- **Motion Detection**: Moving and still target detection with distance measurement
- **Zone Detection**: 3 configurable zones for targeted presence detection
- **Operating Range**: Indoor human presence detection up to 8 meters
- **Connectivity**: WiFi 802.11 b/g/n, Bluetooth 4.2
- **Dimensions**: Compact wall-mountable motion sensor
- **Mounting**: Wall mount with adjustable angle
- **Detection**: Human presence, motion, and stillness with precise distance measurement

</details>


<details>
<summary><b>Use Cases</b> (click to expand)</summary>

The Apollo Automation MSR-2 is ideal for:

- **Advanced Presence Detection**: Accurate human presence detection even when stationary
- **Room Automation**: Automate lights, HVAC, and devices based on room occupancy
- **Security Monitoring**: Detect human presence and movement for security systems
- **Energy Saving**: Turn off devices when rooms are unoccupied
- **Zone-Based Control**: Control different areas based on specific zone occupancy
- **Sleep Monitoring**: Detect presence and movement in bedrooms
- **Office Automation**: Monitor desk and meeting room occupancy
- **Smart Lighting**: Responsive lighting that understands human presence vs movement
- **Climate Control**: Intelligent HVAC control based on actual room occupancy
- **Distance Monitoring**: Track exact distance of moving and stationary targets

</details>


### Apollo Automation MSR-2

|               |                 |
|---------------|-----------------|
| [![MSR-2 Main Device](https://apolloautomation.com/cdn/shop/files/MSR-2-Hero_1200x.jpg?v=1700767395)](https://apolloautomation.com/products/msr-2) | **Apollo MSR-2 Features:**<br/>• ESP32-based platform with WiFi and Bluetooth connectivity<br/>• Mains-powered operation (USB-C 5V)<br/>• LD2410 mmWave Radar: Human presence detection with distance measurement<br/>• DPS310 Pressure Sensor: Barometric pressure monitoring<br/>• LTR390 Light/UV Sensor: Illuminance and UV index measurement<br/>• SCD40 CO₂ Sensor: 400-40,000 ppm NDIR detection<br/>• 3-Zone Detection: Configurable presence zones for targeted automation<br/>• RGB LED indicator with customizable status colors<br/>• Detection Range: Up to 8 meters for human presence<br/>• Advanced motion sensor for smart home automation |

-----

[Apollo Automation](https://apolloautomation.com/) is a local tech startup building advanced hardware and software in Lexington, KY. 

![Made for ESPHome](https://esphome.io/_images/made-for-esphome-black-on-white.svg)

✨Apollo Automation devices come **pre-flashed with ESPHome firmware** - just connect your MSR-2 to your WiFi network and add the device IP address in the Preferences tab of this driver. No additional software, bridges, or complex configurations required.

## Driver Description

The Apollo Automation MSR-2 driver provides advanced motion and presence detection capabilities with comprehensive environmental monitoring. The driver displays essential motion detection and environmental attributes by default, keeping your device interface clean and focused. Advanced users can enable additional diagnostic attributes through the preferences when needed.

#### Main Attributes
![Commands and Attributes Overview](https://raw.githubusercontent.com/kkossev/Hubitat-ESPHome-Apollo/refs/heads/main/Images/apollo-temp-1-commands-and-attributes.png)
- **`motion`**: Primary motion detection status ['active', 'inactive']
- **`radarTarget`**: Moving target detection from mmWave radar
- **`radarStillTarget`**: Stationary target detection from mmWave radar
- **`radarZone1Occupancy`**: Zone 1 presence detection ['active', 'inactive']
- **`radarZone2Occupancy`**: Zone 2 presence detection ['active', 'inactive']  
- **`radarZone3Occupancy`**: Zone 3 presence detection ['active', 'inactive']
- **`illuminance`**: Light level measurement from LTR390 sensor (lx)
- **`pressure`**: Barometric pressure from DPS310 sensor (hPa)
- **`boardTemperature`**: Temperature reading from DPS310 sensor
- **`radarMovingDistance`**: Distance to moving target (cm) - diagnostic
- **`radarStillDistance`**: Distance to stationary target (cm) - diagnostic
- **`networkStatus`**: Connection status ['connecting', 'online', 'offline']
- **`online`**: Device online status from ESPHome ['true', 'false']
- **`rgbLight`**: RGB LED control ['on', 'off']

### Advanced Attributes
These advanced attributes, disabled by default, provide additional insights and control for power users seeking enhanced functionality.

<details>
<summary><b>Complete Attribute List</b> (click to expand)</summary>

| Attribute | Type | Description |
|-----------|------|-------------|
| `motion` | enum | Primary motion detection status |
| `radarTarget` | string | Moving target detection status |
| `radarStillTarget` | string | Stationary target detection status |
| `radarZone1Occupancy` | enum | Zone 1 presence detection |
| `radarZone2Occupancy` | enum | Zone 2 presence detection |
| `radarZone3Occupancy` | enum | Zone 3 presence detection |
| `illuminance` | number | Light level measurement (lx) |
| `pressure` | number | Barometric pressure (hPa) |
| `boardTemperature` | number | Temperature reading (°C/°F) |
| `radarMovingDistance` | number | Distance to moving target (cm) |
| `radarStillDistance` | number | Distance to stationary target (cm) |
| `networkStatus` | enum | Device connection status |
| `online` | enum | Device online status from ESPHome |
| `rgbLight` | enum | RGB LED control |
| `rtt` | number | Round-trip time measurement (ms) |
| `espTemperature` | number | ESP32 chip temperature |
| `uptime` | string | Device uptime since last restart |
| `rssi` | number | WiFi signal strength (dBm) |

</details>

### Commands

- **`initialize()`** - Establishes connection to the device and starts monitoring. Automatically called during device setup or manually from device commands.
- **`refresh()`** - Refreshes device information and clears cached data. Manually refresh device status and request updated information from the device.
- **`setRgbLight(value)`** - Control the RGB LED indicator. Parameters: `value` ['off', 'on']. Usage: `setRgbLight('on')` or `setRgbLight('off')`
- **`restart(value)`** - Restart the ESP32 device. Parameters: `value` ['restart']. Usage: `restart('restart')`
- **`ping(value)`** - Measure network round-trip time to device. Parameters: `value` ['ping']. Usage: `ping('ping')`. Updates `rtt` attribute with response time in milliseconds.
- **`refreshSensors(value)`** - Refresh all sensors and bypass thresholds temporarily. Parameters: `value` ['refresh']. Usage: `refreshSensors('refresh')`. Forces immediate sensor readings regardless of threshold settings, with events marked `[Refresh]`.

### Preferences

![Preferences Overview](https://github.com/kkossev/Hubitat-ESPHome-Apollo/blob/main/Images/apollo-air1-preferences.png?raw=true)
#### Basic Settings
- **`logEnable`** (false) - Enable debug logging for troubleshooting
- **`txtEnable`** (true) - Enable descriptive text logging  
- **`ipAddress`** (required) - Device IP address for ESPHome API connection

#### Reporting & Thresholds
- **`maxReportingInterval`** (1800) - Maximum time between sensor reports in seconds (60-14400, default: 30 minutes)
- **`temperatureChangeThreshold`** (0.5) - Minimum temperature change to report in degrees (0-2°)
- **`pressureChangeThreshold`** (0.5) - Minimum pressure change to report in hPa (0-2)
- **`illuminanceChangeThreshold`** (10) - Minimum illuminance change to report in lx (1-100)
- **`distanceChangeThreshold`** (5) - Minimum distance change to report in cm (1-50)

#### Advanced Options
- **`password`** (optional) - Device password if required
- **`diagnosticsReporting`** (false) - Enable reporting of diagnostic attributes
- **`distanceReporting`** (false) - Enable distance reporting from radar sensors
- **`logWarnEnable`** (false) - Enable warning and info logging

### Intelligent Reporting System

The driver features an advanced threshold-based reporting system that prevents log spam while ensuring important changes are captured:

#### Threshold-Based Reporting
- **Smart Filtering**: Only reports sensor changes that exceed configurable thresholds
- **Customizable Thresholds**: Individual threshold settings for each sensor type
- **Maximum Interval**: Guarantees at least one report every 30 minutes (configurable)
- **Event Indicators**: Clear suffixes show why each event was reported:
  - `[MaxReportingInterval]` - Reported due to time elapsed
  - `[Refresh]` - Reported due to manual refresh command

#### Benefits
- **Reduced Log Spam**: Eliminates unnecessary frequent updates
- **Important Changes Captured**: Significant sensor changes are always reported
- **Guaranteed Updates**: Maximum interval ensures regular status updates
- **Manual Override**: `refreshSensors` command bypasses thresholds for immediate readings

### More info on this driver

<details>
<summary><b>Driver Features & Technical Details</b> (click to expand)</summary>

The driver uses an intelligent entity management system that:

- **Automatically discovers** all available ESPHome entities
- **Maps entities** to appropriate Hubitat attributes
- **Handles missing entities** gracefully (device variants may not have all sensors)
- **Provides diagnostic control** - technical attributes can be hidden from main device view
- **Supports distance reporting** - optional distance measurements can be enabled/disabled
- **Common Library Integration** - leverages the Apollo Common Library for shared functionality across all Apollo device drivers, ensuring consistent behavior and simplified maintenance

### Motion Detection

The driver provides comprehensive motion and presence detection:

- **mmWave Radar**: LD2410 sensor for accurate human presence detection
- **Intelligent Reporting**: Threshold-based system prevents log spam while capturing important changes
- **Multi-Zone Detection**: 3 configurable zones for targeted presence monitoring
- **Distance Measurement**: Optional distance reporting for moving and stationary targets
- **Real-time Monitoring**: Continuous presence detection with instant response
- **Standard Compliance**: Uses Hubitat standard motion capability for seamless integration

### Environmental Monitoring

For complete environmental tracking:

- **Light Detection**: LTR390 sensor for illuminance measurement
- **Barometric Pressure**: DPS310 sensor for pressure monitoring
- **Temperature Monitoring**: Board temperature from DPS310 sensor
- **Standard Attributes**: Uses Hubitat standard capability attributes (illuminance, pressure, temperature)
- **Visual Indicators**: RGB LED for status notifications

### Presence Detection

For advanced automation and security:

- **Human Presence**: Detects stationary humans that traditional PIR sensors miss
- **Motion Detection**: Combines movement and presence for comprehensive detection
- **Zone-Based Control**: 3 independent zones for targeted automation
- **Distance Tracking**: Know exactly how far targets are from the sensor
- **Target Classification**: Distinguish between moving and stationary targets
- **Instant Response**: Immediate detection without warm-up time

### Network Monitoring

- **Connection Status**: Real-time online/offline status
- **Signal Strength**: WiFi RSSI monitoring
- **Round-Trip Time**: Measure network latency with ping command
- **Enhanced Status**: Improved online/offline detection
- **Automatic Reconnection**: Built-in ESPHome API reconnection logic

### Diagnostic Features

When diagnostic reporting is enabled:

- **ESP32 Monitoring**: Internal chip temperature and performance
- **Uptime Tracking**: Device restart and reliability monitoring
- **Network Diagnostics**: Round-trip time monitoring and connection quality
- **Enhanced Status Reporting**: Improved online/offline detection and reporting
- **Distance Reporting**: Optional detailed distance measurements
- **Configuration Access**: View advanced device settings
- **Threshold Control**: Individual threshold settings prevent log spam while ensuring important data is captured

### Motion Automation

The device supports comprehensive motion and presence automation:

- **Smart Lighting**: Lights that respond to actual human presence
- **HVAC Control**: Climate control based on room occupancy
- **Security Integration**: Advanced presence detection for security systems
- **Energy Saving**: Automatic device control when rooms are unoccupied
- **Zone-Based Automation**: Different actions for different areas of a room
- **Sleep Detection**: Bedroom automation that understands when you're sleeping
- **Office Automation**: Desk and meeting room occupancy monitoring
- **Visual Status**: RGB LED indicates current detection status

</details>
