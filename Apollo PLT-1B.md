## Apollo Automation PLT-1B Driver for Hubitat Elevation

This driver connects your [Apollo Automation PLT-1B](https://apolloautomation.com/products/plt-1-ultimate-plant-sensor-for-home-assistant) sensor **directly to your HE hub** thanks to the great work done by Jonathan Bradshaw (@jonathanb) - the [ESPHome Hubitat Toolkit](https://github.com/bradsjm/hubitat-public/tree/main/ESPHome).

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
- **Power**: USB-C 5V or rechargeable 18650 battery
- **Battery Life**: Up to 6 months with optimized sleep settings
- **Temperature Accuracy**: ±0.3°C (AHT20-F), ±0.5°C (DS18B20 probe)
- **Humidity Accuracy**: ±2% RH
- **Light Sensor**: LTR390 UV and ambient light sensor
- **Soil Moisture**: Capacitive sensor with conformal coating
- **Operating Range**: -40°C to +85°C (-40°F to +185°F)
- **Connectivity**: WiFi 802.11 b/g/n, Bluetooth 4.2
- **Dimensions**: Compact weatherproof enclosure for indoor plant monitoring
- **Mounting**: Stake design for soil insertion
</details>


<details>
<summary><b>Use Cases</b> (click to expand)</summary>

The Apollo Automation PLT-1B is ideal for:

- **Indoor Plant Care**: Monitor soil moisture, temperature, and light for optimal plant health
- **Greenhouse Monitoring**: Track environmental conditions for multiple plants
- **Garden Automation**: Automate watering systems based on soil moisture
- **Light Monitoring**: Ensure plants receive adequate light exposure
- **Humidity Control**: Monitor air humidity for plant environments
- **UV Exposure**: Track ultraviolet light for plant photosynthesis
- **Plant Health Alerts**: Get notifications when plants need attention

</details>


### Apollo Automation PLT-1B

|               |                 |
|---------------|-----------------|
| [![PLT-1B Main Device](https://wiki.apolloautomation.com/products/plt1b/assets/screenshot-2024-10-03-at-4-10-25-pm.png)](https://apolloautomation.com/products/plt-1-ultimate-plant-sensor-for-home-assistant) | **Apollo PLT-1(B) Features:**<br/>• ESP32-based platform with WiFi and Bluetooth connectivity<br/>• Battery-powered operation (rechargeable 18650 battery)<br/>• Soil Moisture: Capacitive sensor with conformal coating<br/>• Temperature: AHT20-F air sensor + optional DS18B20 soil probe<br/>• Light Monitoring: LTR390 UV and ambient light sensor<br/>• RGB LED indicator with customizable colors<br/>• Temperature Range: -40°C to +85°C (-40°F to +185°F)<br/>• Humidity Range: 0-100% RH with ±2% accuracy<br/>• Up to 6 months battery life with sleep mode |
| **Plant Monitoring Features:**<br/>• Soil moisture monitoring with capacitive sensor<br/>• Air temperature and humidity monitoring<br/>• Ambient light measurement (lux)<br/>• UV index monitoring for plant health<br/>• Optional soil temperature probe<br/>• Visual plant status with RGB LED<br/>• Automated watering alerts<br/>• Growth condition optimization | [![PLT-1B Dashboard](https://wiki.apolloautomation.com/products/plt1b/assets/screenshot-2024-10-03-at-2-25-47-pm.png)](https://apolloautomation.com/products/plt-1-ultimate-plant-sensor-for-home-assistant) | 
| [![DS18B20 Soil Probe](https://apolloautomation.com/cdn/shop/files/ds18b20.png?v=1733169988&width=960)](https://apolloautomation.com/products/ds18b20-soil-temperature-probe) | **Soil Temperature Probe Features:**<br/>• DS18B20 waterproof temperature sensor<br/>• Available in 20cm (~8in) and 1.5m (~5ft) lengths<br/>• Submersible and waterproof construction<br/>• Temperature Range: -55°C to +85°C (-67°F to +185°F)<br/>• ±0.5°C accuracy<br/>• OneWire protocol for reliable communication<br/>• Ideal for root zone temperature monitoring |


-----

[Apollo Automation](https://apolloautomation.com/) is a local tech startup building advanced hardware and software in Lexington, KY. 

![Made for ESPHome](https://esphome.io/_images/made-for-esphome-black-on-white.svg)



✨Apollo Automation devices come **pre-flashed with ESPHome firmware** - just connect your PLT-1B to your WiFi network and add the device IP address in the Preferences tab of this driver. No additional software, bridges, or complex configurations required.

## Driver Description


### Major Attributes

The Apollo Automation PLT-1B driver provides comprehensive plant monitoring and control capabilities with a dozen of available attributes. For everyday use, the driver displays only the essential attributes by default, keeping your device interface clean and focused. Advanced users can enable additional diagnostic attributes through the preferences when needed.


#### Main Attributes
- **`temperature`**: Primary temperature reading based on selected sensor preference (Air or Soil)
- **`humidity`**: Relative humidity from onboard AHT20-F sensor (0-100% RH)
- **`soilMoisture`**: Soil moisture percentage from capacitive sensor
- **`illuminance`**: Ambient light level in lux from LTR390 sensor
- **`ultravioletIndex`**: UV index measurement from LTR390 sensor
- **`networkStatus`**: Connection status ['connecting', 'online', 'offline']
- **`rgbLight`**: RGB LED control ['on', 'off']

### Advanced Attributes
These advanced attributes, disabled by default, provide additional insights and control for power users seeking enhanced functionality. 

<details>
<summary><b>Complete Attribute List</b> (click to expand)</summary>

| Attribute | Type | Description |
|-----------|------|-------------|
| `temperature` | number | Primary temperature reading (°C/°F) |
| `humidity` | number | Relative humidity percentage |
| `soilMoisture` | number | Soil moisture percentage |
| `illuminance` | number | Ambient light level (lux) |
| `ultravioletIndex` | number | UV index measurement |
| `networkStatus` | enum | Device connection status |
| `rgbLight` | enum | RGB LED control |
| `airTemperature` | number | AHT20-F air temperature sensor |
| `soilTemperature` | number | DS18B20 soil temperature probe |
| `airTemperatureOffset` | number | Air temperature calibration offset |
| `airHumidityOffset` | number | Air humidity calibration offset |
| `espTemperature` | number | ESP32 chip temperature |
| `soilAdc` | number | Raw soil sensor voltage |
| `waterVoltage100` | number | 100% water voltage calibration |
| `dryVoltage` | number | Dry soil voltage calibration |
| `uptime` | string | Device uptime since last restart |
| `rssi` | number | WiFi signal strength (dBm) |
| `preventSleep` | enum | Sleep prevention control |
| `sleepDuration` | number | Sleep duration in minutes |
| `selectedSensor` | string | Active temperature sensor selection |
### Commands

#### initialize() 
- Establishes connection to the device and starts monitoring. Automatically called during device setup or manually from device commands.

#### refresh()
- Refreshes device information and clears cached data. Manually refresh device status and request updated information from the device.

#### setRgbLight()
- **Purpose**: Control the RGB LED indicator
- **Parameters**: 
  - `value`: LED state ['off', 'on']
- **Usage**: `setRgbLight('on')` or `setRgbLight('off')`


### Preferences

#### Basic Settings
| Setting | Default | Description |
|---------|---------|-------------|
| `logEnable` | false | Enable debug logging for troubleshooting |
| `txtEnable` | true | Enable descriptive text logging |
| `ipAddress` | (required) | Device IP address for ESPHome API connection |
| `temperaturePreference` | 'Air' | Select primary temperature sensor ['Air', 'Soil'] |
| `airTemperatureOffset` | 0.0 | Air temperature calibration offset (-50° to +50°) |
| `airHumidityOffset` | 0.0 | Air humidity calibration offset (-50% to +50%) |

#### Advanced Options
| Setting | Default | Description |
|---------|---------|-------------|
| `password` | (optional) | Device password if required |
| `diagnosticsReporting` | false | Enable reporting of diagnostic attributes |
| `logWarnEnable` | true | Enable warning and info logging |

### More info on this driver

<details>
<summary><b>Driver Features & Technical Details</b> (click to expand)</summary>

The driver uses an intelligent entity management system that:

- **Automatically discovers** all available ESPHome entities
- **Maps entities** to appropriate Hubitat attributes
- **Handles missing entities** gracefully (device variants may not have all sensors)
- **Provides diagnostic control** - technical attributes can be hidden from main device view
- **Supports calibration** - offset values sync between Hubitat preferences and ESPHome

### Plant Monitoring

The driver provides comprehensive plant care monitoring:

- **Multi-Sensor Integration**: Combines soil, air, light, and UV sensors
- **Automatic Unit Conversion**: Converts between Celsius and Fahrenheit based on hub settings
- **Calibration Support**: Individual offset adjustments for temperature and humidity sensors
- **Sleep Mode Compatibility**: Optimized for battery-powered operation with sleep cycles

### Environmental Monitoring

For complete plant environment tracking:

- **Soil Conditions**: Moisture percentage and optional soil temperature
- **Air Conditions**: Temperature and humidity monitoring
- **Light Monitoring**: Ambient light (lux) and UV index measurement
- **Visual Indicators**: RGB LED for plant status notifications

### Battery Management

For battery-powered PLT-1B devices:

- **Extended Battery Life**: Up to 6 months with optimized sleep settings
- **Sleep Mode Support**: Configurable sleep duration to extend battery life
- **Prevent Sleep Option**: Keep device awake for continuous monitoring when needed
- **Power Monitoring**: Battery voltage and power management features

### Network Monitoring

- **Connection Status**: Real-time online/offline status
- **Signal Strength**: WiFi RSSI monitoring
- **Automatic Reconnection**: Built-in ESPHome API reconnection logic

### Diagnostic Features

When diagnostic reporting is enabled:

- **ESP32 Monitoring**: Internal chip temperature and performance
- **Uptime Tracking**: Device restart and reliability monitoring
- **Configuration Access**: View advanced device settings
- **Calibration Values**: View all sensor offset parameters
- **Raw Sensor Data**: Access to soil ADC voltage and calibration thresholds

### Plant Care Automation

The device supports plant care automation:

- **Watering Alerts**: Automated notifications based on soil moisture levels
- **Light Management**: Monitor and control grow lights based on ambient conditions
- **Environmental Control**: Trigger HVAC and humidifier based on conditions
- **Visual Status**: RGB LED indicates plant health status

</details>

