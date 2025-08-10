## Apollo Automation TEMP-1B Driver for Hubitat Elevation

This driver connects your [Apollo Automation TEMP-1B](https://geni.us/apollo_temp1) sensor **directly to your HE hub** thanks to the great work done by Jonathan Bradshaw (@jonathanb) - the [ESPHome Hubitat Toolkit](https://github.com/bradsjm/hubitat-public/tree/main/ESPHome).

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
- **Power**: USB 5V or CR123A or 16340 rechargeable battery
- **Battery Life**: Up to 6 months with optimized sleep settings
- **Temperature Accuracy**: ±0.5°C (probes), ±2°C (onboard sensor)
- **Humidity Accuracy**: ±2% RH
- **Operating Range**: -40°C to +85°C (-40°F to +185°F)
- **Connectivity**: WiFi 802.11 b/g/n, Bluetooth 4.2
- **Dimensions**: Compact 3D-printed enclosure
- **Mounting**: Optional magnetic mount available
</details>


<details>
<summary><b>Use Cases</b> (click to expand)</summary>

The Apollo Automation TEMP-1B is ideal for:

- **Kitchen Monitoring**: Food temperature during cooking and baking
- **Refrigeration**: Freezer and refrigerator temperature monitoring
- **Aquarium Management**: Water temperature monitoring
- **Pool/Spa Monitoring**: Water temperature tracking
- **Greenhouse Monitoring**: Air and soil temperature monitoring
- **HVAC Monitoring**: Room temperature and humidity tracking
- **Server Room Monitoring**: Environmental condition monitoring

</details>


### Apollo Automation TEMP-1B

|               |                 |
|---------------|-----------------|
| [![TEMP-1B Main Device](https://apolloautomation.com/cdn/shop/files/TEMP-1B_Fridge.png?v=1742996496&width=960)](https://geni.us/apollo_temp1) | **Apollo TMP-1(B) Features:**<br/>• ESP32-based platform with WiFi and Bluetooth connectivity<br/>• Battery-powered operation (CR123A or 16340 rechargeable battery)<br/>• Temperature Range: -40°C to +85°C (-40°F to +185°F)<br/>• Humidity Range: 0-100% RH with ±2% accuracy<br/>• Onboard AHT20-F temperature and humidity sensor<br/>• RGB LED indicator with customizable colors<br/>• Buzzer for temperature alerts<br/>• 3.5mm jack for external probes<br/>• Up to 6 months battery life with sleep mode |
| **Temperature Probe Features:**<br/>• DS18B20 waterproof temperature sensor<br/>• Available in 20cm (~8in) and 1.5m (~5ft) lengths<br/>• Flat cable design prevents interference with fridge seals<br/>• Submersible and waterproof construction<br/>• Temperature Range: -55°C to +85°C (-67°F to +185°F)<br/>• ±0.5°C accuracy<br/>• Ideal for freezer, fridge, aquarium, and pool monitoring | [![Temperature Probe](https://apolloautomation.com/cdn/shop/files/20241205-123547.jpg?v=1733420196&width=960)](https://geni.us/apollo-long-temp-probe) | 
| [![Food Probe](https://apolloautomation.com/cdn/shop/files/TEMP-1_with_Food_Probe.png?v=1742996496&width=960)](https://geni.us/apollo-food-probe) | **Food Probe Features:**<br/>• 1m (~3ft) stainless steel food-safe probe<br/>• NTC temperature sensor<br/>• Temperature Range: -40°C to +204°C (-40°F to +400°F)<br/>• Food-safe stainless steel construction<br/>• Perfect for grilling, baking, and cooking<br/>• Not dishwasher safe<br/>• Real-time temperature monitoring for perfect cooking results |


-----

[Apollo Automation](https://geni.us/apolloautomation) is a local tech startup building advanced hardware and software in Lexington, KY. 

![Made for ESPHome](https://esphome.io/_images/made-for-esphome-black-on-white.svg)



✨Apollo Automation devices come **pre-flashed with ESPHome firmware** - just connect your TEMP-1B to your WiFi network and add the device IP address in the Preferences tab of this driver. No additional software, bridges, or complex configurations required.

## Driver Description


### Major Attributes

The Apollo Automation TEMP-1B driver provides comprehensive monitoring and control capabilities with a dozen of available attributes. For everyday use, the driver displays only the essential attributes by default, keeping your device interface clean and focused. Advanced users can enable additional diagnostic attributes through the preferences when needed.


![Current States](https://github.com/kkossev/Hubitat-ESPHome-Apollo/raw/main/Images/current_states.png)


#### Main Attributes
- **`temperature`**: Primary temperature reading based on selected probe preference
- **`humidity`**: Relative humidity from onboard AHT20-F sensor (0-100% RH)
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

![Commands](https://github.com/kkossev/Hubitat-ESPHome-Apollo/raw/main/Images/apollo-temp1(b)-hubitat-commands.png)

#### initialize() 
- Establishes connection to the device and starts monitoring.  Automatically called during device setup or manually from device commands.

#### refresh()
- Refreshes device information and clears cached data. Manually refresh device status and request updated information from the device.

#### setRgbLight()
- **Purpose**: Control the RGB LED indicator
- **Parameters**: 
  - `value`: LED state ['off', 'on']
- **Usage**: `setRgbLight('on')` or `setRgbLight('off')`


### Preferences

![Preferences](https://github.com/kkossev/Hubitat-ESPHome-Apollo/raw/345e1f0e161a63dd60192f76075ca2de28b37142/Images/apollo-temp-1(b)-hubitat-preferences.png)

#### Basic Settings
| Setting | Default | Description |
|---------|---------|-------------|
| `logEnable` | false | Enable debug logging for troubleshooting |
| `txtEnable` | true | Enable descriptive text logging |
| `ipAddress` | (required) | Device IP address for ESPHome API connection |
| `selectedProbe` | 'Temperature' | Select primary temperature sensor ['Temperature', 'Food'] |
| `boardHumidityOffset` | 0.0 | Board humidity calibration offset (-50% to +50%) |

#### Advanced Options
| Setting | Default | Description |
|---------|---------|-------------|
| `password` | (optional) | Device password if required |
| `diagnosticsReporting` | false | Enable reporting of diagnostic attributes |
| `logWarnEnable` | true | Enable warning and info logging |

### More info on this driver

<details>
<summary><b>Driver Features & Technical Details</b> (click to expand)</summary>

The driver is a work in progress. uses an intelligent entity management system that:

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
- **Configuration Access**: View the advanced device settings
- **Calibration Values**: View all sensor offset parameters

### Alert System

The device supports customizable alerts:

- **Temperature Range Alarms**: Configurable min/max temperature thresholds (not implemented in this driver)
- **Visual Indicators**: RGB LED with customizable colors (only on/off is implemented in the driver)
- **Audible Alerts**: Onboard buzzer for critical notifications (not tested)
- **Notification Controls**: Flexible alert configuration options (not implemented in the driver and not tested)

</details>


![Temperature Monitoring Graph](https://github.com/kkossev/Hubitat-ESPHome-Apollo/raw/792096bd1a9266784e274f260e2b5ff3c6c8ccd5/Images/apollo-temp-1(b)-graph.png)

(Generated using the HE inbuilt [webCoRE](https://community.hubitat.com/c/comappsanddrivers/webcore/104) Graphs).

