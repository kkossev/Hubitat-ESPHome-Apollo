# Hubitat-ESPHome-Apollo

**Apollo Automation ESPHome devices support for Hubitat Elevation hubs**

This driver package connects your [Apollo Automation](https://geni.us/apolloautomation) ESPHome devices **directly to your Hubitat Elevation hub** thanks to the great work done by Jonathan Bradshaw (@jonathanb) - the [ESPHome Hubitat Toolkit](https://github.com/bradsjm/hubitat-public/tree/main/ESPHome).

The drivers can be installed using the community [Hubitat Package Manager](https://community.hubitat.com/t/release-hubitat-package-manager-hpm-hubitatcommunity/94471/1) app or manually as a Bundle archive from [GitHub](https://github.com/kkossev/Hubitat-ESPHome-Apollo).

- **🚫 No Home Assistant Needed**: Direct WiFi connection to your Hubitat hub
- **🚫 No YAML Files to Edit**: Simple IP address configuration - that's it!
- **🚫 No Cloud Dependencies**: Everything runs locally on your network
- **⚡ Easy Setup**: Install this driver package from HPM, connect devices to your WiFi network, configure IP addresses - done.
- **🔧 Zero Configuration**: All device features work out-of-the-box
- **🏠 Native Hubitat Integration**: Full support for Hubitat automations, dashboards, and apps

## Supported Apollo Automation Devices

### Apollo Automation TEMP-1B

|               |                 |
|---------------|-----------------|
| [![TEMP-1B Main Device](https://apolloautomation.com/cdn/shop/files/TEMP-1B_Fridge.png?v=1742996496&width=960)](https://geni.us/apollo_temp1) | **Apollo TEMP-1(B) Features:**<br/>• ESP32-based platform with WiFi and Bluetooth connectivity<br/>• Battery-powered operation (CR123A or 16340 rechargeable battery)<br/>• Temperature Range: -40°C to +85°C (-40°F to +185°F)<br/>• Humidity Range: 0-100% RH with ±2% accuracy<br/>• Onboard AHT20-F temperature and humidity sensor<br/>• RGB LED indicator with customizable colors<br/>• Buzzer for temperature alerts<br/>• 3.5mm jack for external probes<br/>• Up to 6 months battery life with sleep mode |

### Apollo Automation PLT-1B

|               |                 |
|---------------|-----------------|
| [![PLT-1B Main Device](https://github.com/kkossev/Hubitat-ESPHome-Apollo/blob/main/Images/apollo-plt1-hero.png?raw=true)](https://geni.us/plt1-plant-sensor) | **Apollo PLT-1(B) Features:**<br/>• ESP32-based platform with WiFi and Bluetooth connectivity<br/>• Battery-powered operation (rechargeable 18650 battery)<br/>• Soil Moisture: Capacitive sensor with conformal coating<br/>• Temperature: AHT20-F air sensor + optional DS18B20 soil probe<br/>• Light Monitoring: LTR390 UV and ambient light sensor<br/>• RGB LED indicator with customizable colors<br/>• Temperature Range: -40°C to +85°C (-40°F to +185°F)<br/>• Humidity Range: 0-100% RH with ±2% accuracy<br/>• Up to 6 months battery life with sleep mode |

-----

[Apollo Automation](https://apolloautomation.com/) is a local tech startup building advanced hardware and software in Lexington, KY. 

![Made for ESPHome](https://esphome.io/_images/made-for-esphome-black-on-white.svg)

✨Apollo Automation devices come **pre-flashed with ESPHome firmware** - just connect your devices to your WiFi network and add the device IP addresses in the Preferences tab of each driver. No additional software, bridges, or complex configurations required.

## Installation

### Via Hubitat Package Manager (Recommended)

1. Install [Hubitat Package Manager](https://community.hubitat.com/t/release-hubitat-package-manager-hpm-hubitatcommunity/94471/1) if not already installed
2. Open HPM and search for "ESPHome Apollo"
3. Install the package
4. The drivers will be automatically installed

### Manual Installation

1. Download the latest release from [GitHub](https://github.com/kkossev/Hubitat-ESPHome-Apollo/releases)
2. Import the bundle file in Hubitat's Bundle Manager
3. The drivers and apps will be installed automatically

## Setup

1. **Connect your Apollo device to WiFi** using the Apollo Automation mobile app or web interface
2. **Find the device IP address** in your router's DHCP client list or Apollo app
3. **Create a new device** in Hubitat:
   - Go to Devices → Add Device → Virtual
   - Select the appropriate Apollo driver (TEMP-1B or PLT-1B)
   - Enter a device name and label
4. **Configure the device**:
   - Enter the device IP address in the Preferences
   - Adjust any other preferences as needed
   - Click "Save Preferences"
5. **Initialize the device**:
   - Click "Initialize" or "Refresh" in the device commands
   - The device should come online and start reporting data

## Features

- **Direct ESPHome API Connection**: No intermediate software required
- **Real-time Monitoring**: Instant updates when device values change
- **Battery Optimization**: Sleep mode support for extended battery life
- **Comprehensive Diagnostics**: Optional advanced attributes for troubleshooting
- **Calibration Support**: Temperature and humidity offset adjustments
- **Network Monitoring**: Connection status and signal strength tracking
- **RGB LED Control**: Visual device status indicators
- **Common Library Architecture**: Shared functionality across all Apollo drivers

## Documentation

- [TEMP-1B Driver Documentation](Apollo%20TEMP-1B.md)
- [PLT-1B Driver Documentation](Apollo%20PLT-1B.md)

## Support

- **Hubitat Community**: [ESPHome Apollo Thread](https://community.hubitat.com/)
- **GitHub Issues**: [Report bugs or request features](https://github.com/kkossev/Hubitat-ESPHome-Apollo/issues)
- **Apollo Automation**: [Official support](https://apolloautomation.com/pages/contact)

## Credits

- **ESPHome Hubitat Toolkit**: [Jonathan Bradshaw (@jonathanb)](https://github.com/bradsjm/hubitat-public/tree/main/ESPHome)
- **Apollo Automation**: [Hardware and ESPHome firmware](https://apolloautomation.com/)
