/**
 *  MIT License
 *  Copyright 2025 Krassimir Kossev (kkossev)
 *
 *  version 1.0.0 - 2025-07-12 kkossev - initial version
 *
 *                             TODO: 
 **/

library(
        name: 'espHomeApolloLibraryCommon',
        namespace: 'apollo',
        author: 'Krassimir Kossev',
        description: 'Common library for ESPHome Apollo drivers',
        importUrl: 'https://raw.githubusercontent.com/kkossev/Hubitat/refs/heads/ESPHome/Drivers/ESPHome/Libraries/ESPHome-Apollo-Library-Common.groovy'
)

@Field static final String APOLLO_COMMON_LIBRARY_VERSION = '1.0.0'

/**
 * Convert temperature based on hub's temperature scale setting
 * @param tempC temperature in Celsius
 * @return temperature in the hub's preferred scale
 */
def convertTemperature(Float tempC) {
    if (location.temperatureScale == 'F') {
        return (tempC * 9/5) + 32
    }
    return tempC
}

/**
 * Get temperature unit based on hub's temperature scale setting
 * @return temperature unit string
 */
String getTemperatureUnit() {
    return location.temperatureScale == 'F' ? '°F' : '°C'
}

/**
 * Set RGB light state
 * @param value 'on', 'off', or null/empty to toggle current state
 */
void setRgbLight(String value = null) {
    if (logEnable) { log.info "${device} setRgbLight called with value: ${value}" }
    def lightKey = state.rgbLightKey
    
    if (lightKey == null) {
        log.warn "RGB light entity not found"
        return
    }
    
    // If no value provided, toggle the current state
    if (isNullOrEmpty(value)) {
        def currentState = device.currentState('rgbLight')
        String currentValue = currentState?.value
        value = (currentValue == 'on') ? 'off' : 'on'
        if (txtEnable) { log.info "${device} RGB light toggling from ${currentValue} to ${value}" }
    }
    
    if (value == 'on') {
        if (txtEnable) { log.info "${device} RGB light on" }
        espHomeLightCommand(key: lightKey, state: true)
    } else if (value == 'off') {
        if (txtEnable) { log.info "${device} RGB light off" }
        espHomeLightCommand(key: lightKey, state: false)
    } else {
        log.warn "Unsupported RGB light value: ${value}"
    }
}

/**
 * Handle RGB light state changes from ESPHome
 * @param message state message from ESPHome
 */
void handleRgbLightState(Map message) {
    // For light entities, check for 'state' directly since they don't use 'hasState'
    if (message.state != null) {
        def rgbLightState = message.state as Boolean
        sendEvent(name: "rgbLight", value: rgbLightState ? 'on' : 'off', descriptionText: "RGB Light is ${rgbLightState ? 'on' : 'off'}")
        if (txtEnable) { log.info "RGB Light is ${rgbLightState ? 'on' : 'off'}" }
    } else {
        if (logEnable) { log.warn "RGB light message does not contain state: ${message}" }
    }
}

/**
 * Handle temperature sensor entities - supports multiple device types
 * Always updates both individual sensor attributes AND main temperature attribute
 * @param message state message from ESPHome
 * @param entity entity information from state.entities
 */
void handleTemperatureState(Map message, Map entity) {
    if (!message.hasState) {
        return
    }
    
    String objectId = entity.objectId
    def entityInfo = getEntityInfo(objectId)
    if (!entityInfo) {
        if (logEnable) { log.warn "No entity info found for objectId: ${objectId}" }
        return
    }
    
    Float tempC = message.state as Float
    Float temp = convertTemperature(tempC)
    String unit = getTemperatureUnit()
    String tempStr = String.format("%.1f", temp)
    String attributeName = entityInfo.attr
    String description = entityInfo.description
    
    // Get the previous temperature value
    def currentTempState = device.currentState(attributeName)
    String previousValue = currentTempState?.value
    
    // Only send event and log if the value has changed
    if (previousValue != tempStr) {
        // Send individual sensor event
        sendEvent(name: attributeName, value: tempStr, unit: unit, descriptionText: "${description} is ${tempStr} ${unit}")
        
        // Also update main temperature attribute for Hubitat capability compatibility
        sendEvent(name: "temperature", value: tempStr, unit: unit, descriptionText: "Temperature is ${tempStr} ${unit}")
        
        if (txtEnable) { 
            log.info "${description} is ${tempStr} ${unit}" 
        }
    }
}

/**
 * Handle humidity sensor entity (air_humidity)
 * Always updates both individual sensor attributes AND main humidity attribute
 * @param message state message from ESPHome
 * @param entity entity information from state.entities
 */
void handleHumidityState(Map message, Map entity) {
    if (!message.hasState) {
        return
    }
    
    String objectId = entity.objectId
    def entityInfo = getEntityInfo(objectId)
    if (!entityInfo) {
        if (logEnable) { log.warn "No entity info found for objectId: ${objectId}" }
        return
    }
    
    Float humidity = message.state as Float
    String humidityStr = String.format("%.1f", humidity)
    String attributeName = entityInfo.attr
    String description = entityInfo.description
    String unit = "%rh"  // Hubitat standard unit for relative humidity
    
    // Get the previous humidity value
    def currentHumidityState = device.currentState(attributeName)
    String previousValue = currentHumidityState?.value
    
    // Only send event and log if the value has changed
    if (previousValue != humidityStr) {
        // Send individual sensor event
        sendEvent(name: attributeName, value: humidityStr, unit: unit, descriptionText: "${description} is ${humidityStr} ${unit}")
        
        // Also update main humidity attribute for Hubitat capability compatibility
        sendEvent(name: "humidity", value: humidityStr, unit: unit, descriptionText: "Humidity is ${humidityStr} ${unit}")
        
        if (txtEnable) { 
            log.info "${description} is ${humidityStr} ${unit}" 
        }
    }
}

// ============================= DRIVER LIFECYCLE METHODS =============================

/**
 * Initialize the driver - opens socket connection and sets up logging timeout
 */
void initialize() {
    // API library command to open socket to device, it will automatically reconnect if needed
    openSocket()

    if (logEnable) {
        runIn(1800, 'logsOff')
    }
}

/**
 * Driver installation handler
 */
void installed() {
    log.info "${device} driver installed"
}

/**
 * Disable debug logging and device logging
 */
void logsOff() {
    espHomeSubscribeLogs(LOG_LEVEL_INFO, false) // disable device logging
    device.updateSetting('logEnable', false)
    log.info "${device} debug logging disabled"
}

/**
 * Refresh device state - clears state and requests device info
 */
void refresh() {
    checkDriverVersion()
    log.info "${device} refresh"
    state.clear()
    state.requireRefresh = true
    espHomeDeviceInfoRequest()
}

/**
 * Driver uninstallation handler - closes socket connection
 */
void uninstalled() {
    closeSocket('driver uninstalled') // make sure the socket is closed when uninstalling
    log.info "${device} driver uninstalled"
}

// ============================= UTILITY METHODS =============================

/**
 * Check if the specified value is null or empty
 * @param value value to check
 * @return true if the value is null or empty, false otherwise
 */
static boolean isNullOrEmpty(final Object value) {
    return value == null || (value as String).trim().isEmpty()
}

/**
 * Check driver version and update if needed
 * @param driverVersion current driver version constant
 * @param dateTimeStamp current driver date/time stamp
 * @param debugFlag debug mode flag
 */
void checkDriverVersion(String driverVersion, String dateTimeStamp, Boolean debugFlag) {
    String debugSuffix = debugFlag ? ' (debug version!)' : ''
    String versionString = "${driverVersion} ${dateTimeStamp} ${debugSuffix} (${getHubVersion()} ${location.hub.firmwareVersionString})"
    
    if (state.driverVersion == null || versionString != state.driverVersion) {
        if (txtEnable) { 
            log.info "checkDriverVersion: updating the settings from the current driver version ${state.driverVersion} to the new version ${versionString}" 
        }
        state.driverVersion = versionString
    }
}


