## Apollo Automation AIR-1 Driver for Hubitat Elevation

This driver connects your [Apollo Automation AIR-1](https://geni.us/apollo-air-1) sensor **directly to your HE hub** thanks to the great work done by Jonathan Bradshaw (@jonathanb) - the [ESPHome Hubitat Toolkit](https://github.com/bradsjm/hubitat-public/tree/main/ESPHome).

The driver can be installed using the community [Hubitat Package Manager](https://community.hubitat.com/t/release-hubitat-package-manager-hpm-hubitatcommunity/94471/1) app or manually as a Bundle archive from [GitHub](https://github.com/kkossev/Hubitat-ESPHome-Apollo).

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
- **Air Quality Sensors**: SEN55 multi-sensor (PM, VOC, NOx, T, RH)
- **Gas Sensors**: ENS160 (CO, NH₃, C₂H₅OH, H₂, CH₄, NO₂)
- **CO₂ Sensor**: SCD40 NDIR sensor (400-40,000 ppm)
- **Pressure Sensor**: DPS310 (300-1200 hPa)
- **Particulate Matter**: PM1, PM2.5, PM4, PM10 detection
- **VOC Detection**: Volatile Organic Compounds index
- **NOx Detection**: Nitrogen Oxides index
- **Operating Range**: -40°C to +85°C (-40°F to +185°F)
- **Connectivity**: WiFi 802.11 b/g/n, Bluetooth 4.2
- **Dimensions**: Compact desktop air quality monitor
- **Mounting**: Desktop stand with optional wall mount
</details>


<details>
<summary><b>Use Cases</b> (click to expand)</summary>

The Apollo Automation AIR-1 is ideal for:

- **Indoor Air Quality Monitoring**: Comprehensive air quality assessment for homes and offices
- **HVAC Optimization**: Monitor air quality to control ventilation and filtration systems
- **Health Monitoring**: Track pollutants and allergens for respiratory health
- **Smart Home Automation**: Automate air purifiers, fans, and windows based on air quality
- **Office Environment**: Monitor CO₂ levels and air quality in workspaces
- **Kitchen Monitoring**: Detect cooking fumes and gas levels
- **Allergen Detection**: Monitor particulate matter and VOCs that affect allergies
- **Energy Efficiency**: Optimize HVAC operation based on actual air quality needs
- **Sleep Quality**: Monitor bedroom air quality for optimal sleep environment

</details>


### Apollo Automation AIR-1

|               |                 |
|---------------|-----------------|
| [![AIR-1 Main Device](https://apolloautomation.com/cdn/shop/files/AIR-1_Hero.png?v=1742996496&width=960)](https://geni.us/apollo-air-1) | **Apollo AIR-1 Features:**<br/>• ESP32-based platform with WiFi and Bluetooth connectivity<br/>• Mains-powered operation (USB-C 5V)<br/>• SEN55 Multi-Sensor: PM1/PM2.5/PM4/PM10, VOC, NOx, Temperature, Humidity<br/>• ENS160 Gas Sensor: CO, NH₃, Ethanol, H₂, CH₄, NO₂<br/>• SCD40 CO₂ Sensor: 400-40,000 ppm NDIR detection<br/>• DPS310 Pressure Sensor: Barometric pressure monitoring<br/>• RGB LED indicator with air quality status colors<br/>• Temperature Range: -40°C to +85°C (-40°F to +185°F)<br/>• Comprehensive air quality monitoring for smart homes |

-----

[Apollo Automation](https://apolloautomation.com/) is a local tech startup building advanced hardware and software in Lexington, KY. 

![Made for ESPHome](https://esphome.io/_images/made-for-esphome-black-on-white.svg)

✨Apollo Automation devices come **pre-flashed with ESPHome firmware** - just connect your AIR-1 to your WiFi network and add the device IP address in the Preferences tab of this driver. No additional software, bridges, or complex configurations required.

## Driver Description

The Apollo Automation AIR-1 driver provides comprehensive air quality monitoring capabilities with dozens of available attributes. For everyday use, the driver displays only the essential air quality attributes by default, keeping your device interface clean and focused. Advanced users can enable additional diagnostic attributes through the preferences when needed.

#### Main Attributes
- **`carbonDioxide`**: CO₂ concentration from SCD40 sensor (400-40,000 ppm)
- **`temperature`**: Primary temperature reading from SEN55 sensor
- **`humidity`**: Relative humidity from SEN55 sensor (0-100% RH)
- **`pm1`**: PM <1µm weight concentration (µg/m³)
- **`pm25`**: PM <2.5µm weight concentration (µg/m³)
- **`pm4`**: PM <4µm weight concentration (µg/m³)
- **`pm10`**: PM <10µm weight concentration (µg/m³)
- **`sen55Voc`**: VOC (Volatile Organic Compounds) index
- **`sen55Nox`**: NOx (Nitrogen Oxides) index
- **`vocQuality`**: VOC quality text rating (Good, Fair, Poor, etc.)
- **`networkStatus`**: Connection status ['connecting', 'online', 'offline']
- **`online`**: Device online status from ESPHome ['true', 'false']
- **`rgbLight`**: RGB LED control ['on', 'off']

### Advanced Attributes
These advanced attributes, disabled by default, provide additional insights and control for power users seeking enhanced functionality.

<details>
<summary><b>Complete Attribute List</b> (click to expand)</summary>

| Attribute | Type | Description |
|-----------|------|-------------|
| `carbonDioxide` | number | CO₂ concentration (ppm) from SCD40 sensor |
| `temperature` | number | Primary temperature reading (°C/°F) |
| `humidity` | number | Relative humidity percentage |
| `pm1` | number | PM <1µm weight concentration (µg/m³) |
| `pm25` | number | PM <2.5µm weight concentration (µg/m³) |
| `pm4` | number | PM <4µm weight concentration (µg/m³) |
| `pm10` | number | PM <10µm weight concentration (µg/m³) |
| `sen55Voc` | number | SEN55 VOC index |
| `sen55Nox` | number | SEN55 NOx index |
| `vocQuality` | string | VOC quality text rating |
| `networkStatus` | enum | Device connection status |
| `online` | enum | Device online status from ESPHome |
| `rgbLight` | enum | RGB LED control |
| `rtt` | number | Round-trip time measurement (ms) |
| `ammonia` | number | Ammonia (NH₃) gas concentration |
| `carbonMonoxide` | number | Carbon Monoxide (CO) gas concentration |
| `ethanol` | number | Ethanol (C₂H₅OH) gas concentration |
| `hydrogen` | number | Hydrogen (H₂) gas concentration |
| `methane` | number | Methane (CH₄) gas concentration |
| `nitrogenDioxide` | number | Nitrogen Dioxide (NO₂) gas concentration |
| `sen55Temperature` | number | SEN55 sensor temperature |
| `sen55Humidity` | number | SEN55 sensor humidity |
| `sen55TemperatureOffset` | number | SEN55 temperature calibration offset |
| `sen55HumidityOffset` | number | SEN55 humidity calibration offset |
| `dps310Pressure` | number | DPS310 barometric pressure sensor |
| `pm03To1` | number | PM 0.3 to 1 µm concentration |
| `pm1To25` | number | PM 1 to 2.5 µm concentration |
| `pm25To4` | number | PM 2.5 to 4 µm concentration |
| `pm4To10` | number | PM 4 to 10 µm concentration |
| `espTemperature` | number | ESP32 chip temperature |
| `uptime` | string | Device uptime since last restart |
| `rssi` | number | WiFi signal strength (dBm) |
| `preventSleep` | enum | Sleep prevention control |
| `sleepDuration` | number | Sleep duration in minutes |

</details>

### Commands

- **`initialize()`** - Establishes connection to the device and starts monitoring. Automatically called during device setup or manually from device commands.
- **`refresh()`** - Refreshes device information and clears cached data. Manually refresh device status and request updated information from the device.
- **`setRgbLight(value)`** - Control the RGB LED indicator. Parameters: `value` ['off', 'on']. Usage: `setRgbLight('on')` or `setRgbLight('off')`
- **`espReboot(value)`** - Restart the ESP32 device. Parameters: `value` ['reboot']. Usage: `espReboot('reboot')`
- **`ping(value)`** - Measure network round-trip time to device. Parameters: `value` ['ping']. Usage: `ping('ping')`. Updates `rtt` attribute with response time in milliseconds.
- **`calibrateScd40(value)`** - Calibrate CO₂ sensor to 420ppm (outdoor air reference). Parameters: `value` ['calibrate']. Usage: `calibrateScd40('calibrate')`
- **`cleanSen55(value)`** - Initiate SEN55 sensor cleaning cycle. Parameters: `value` ['clean']. Usage: `cleanSen55('clean')`

### Preferences

#### Basic Settings
- **`logEnable`** (false) - Enable debug logging for troubleshooting
- **`txtEnable`** (true) - Enable descriptive text logging  
- **`ipAddress`** (required) - Device IP address for ESPHome API connection
- **`sen55TemperatureOffset`** (0.0) - SEN55 temperature calibration offset (-70° to +70°)
- **`sen55HumidityOffset`** (0.0) - SEN55 humidity calibration offset (-70% to +70%)
- **`sleepDuration`** (5) - Sleep duration between measurements (0-800 minutes)

#### Advanced Options
- **`password`** (optional) - Device password if required
- **`diagnosticsReporting`** (false) - Enable reporting of diagnostic attributes
- **`logWarnEnable`** (false) - Enable warning and info logging

### More info on this driver

<details>
<summary><b>Driver Features & Technical Details</b> (click to expand)</summary>

The driver uses an intelligent entity management system that:

- **Automatically discovers** all available ESPHome entities
- **Maps entities** to appropriate Hubitat attributes
- **Handles missing entities** gracefully (device variants may not have all sensors)
- **Provides diagnostic control** - technical attributes can be hidden from main device view
- **Supports calibration** - offset values sync between Hubitat preferences and ESPHome
- **Common Library Integration** - leverages the Apollo Common Library for shared functionality across all Apollo device drivers, ensuring consistent behavior and simplified maintenance

### Air Quality Monitoring

The driver provides comprehensive air quality monitoring:

- **Multi-Sensor Integration**: Combines particulate matter, gas, CO₂, and environmental sensors
- **Automatic Unit Conversion**: Converts between Celsius and Fahrenheit based on hub settings
- **Calibration Support**: Individual offset adjustments for temperature and humidity sensors
- **Real-time Monitoring**: Continuous monitoring with configurable sleep intervals

### Environmental Monitoring

For complete indoor air quality tracking:

- **Particulate Matter**: PM1, PM2.5, PM4, PM10 concentration monitoring
- **Gas Detection**: Six different gas sensors (CO, NH₃, C₂H₅OH, H₂, CH₄, NO₂)
- **Air Quality Indices**: VOC and NOx indices with quality ratings
- **Environmental Conditions**: Temperature, humidity, and barometric pressure
- **Visual Indicators**: RGB LED for air quality status notifications

### Sensor Calibration

For accurate readings and optimal performance:

- **CO₂ Calibration**: Manual calibration to 420ppm outdoor air reference
- **Temperature Offset**: Adjustable temperature calibration for SEN55 sensor
- **Humidity Offset**: Adjustable humidity calibration for SEN55 sensor
- **Sensor Cleaning**: Automated SEN55 cleaning cycle for maintenance
- **Factory Reset**: Complete device reset capability

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
- **Configuration Access**: View advanced device settings
- **Calibration Values**: View all sensor offset parameters
- **Raw Sensor Data**: Access to detailed particulate matter size distributions

### Air Quality Automation

The device supports comprehensive air quality automation:

- **HVAC Control**: Automate ventilation based on CO₂ and air quality
- **Air Purifier Control**: Trigger air purifiers based on particulate matter levels
- **Window/Fan Control**: Automate natural ventilation based on indoor vs outdoor conditions
- **Health Alerts**: Notifications for poor air quality conditions
- **Energy Optimization**: Smart HVAC operation based on actual air quality needs
- **Visual Status**: RGB LED indicates current air quality status

</details>
