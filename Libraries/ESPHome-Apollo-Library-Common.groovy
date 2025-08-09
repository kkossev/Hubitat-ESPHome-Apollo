/**
 *  MIT License
 *  Copyright 2025 Krassimir Kossev (kkossev)
 *
 *  version 1.0.0 - 2025-07-12 kkossev - initial version
 *  version 1.0.1 - 2025-08-09 kkossev - added universal threshold checking
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

@Field static final String APOLLO_COMMON_LIBRARY_VERSION = '1.0.1'

metadata {
        if (_DEBUG) {
            command 'test', [[name: 'test', type: 'STRING', description: 'test', defaultValue : '']]
        }

        // common capabilities for all device types
        //capability 'Configuration'
        capability 'Refresh'
        capability 'HealthCheck'
        capability 'PowerSource'       // powerSource - ENUM ["battery", "dc", "mains", "unknown"]

        // common attributes for all device types
        attribute 'healthStatus', 'enum', ['unknown', 'offline', 'online']
        attribute 'rtt', 'number'
        attribute 'Status', 'string'
        
        // Common attributes across all Apollo drivers
        attribute 'networkStatus', 'enum', ['connecting', 'online', 'offline']  // Device network connection status
        attribute 'online', 'enum', ['true', 'false']     // Device online status from ESPHome
        attribute 'espTemperature', 'number'           // ESP32 internal temperature
        attribute 'rgbLight', 'enum', ['on', 'off']    // RGB LED control
        attribute 'preventSleep', 'enum', ['on', 'off'] // Prevent device sleep mode
        attribute 'sleepDuration', 'number'            // Sleep duration between measurements
        attribute 'uptime', 'string'                   // Device uptime since last restart
        attribute 'rssi', 'number'                     // WiFi signal strength

        // Common attributes for battery-powered devices (PLT-1B, TEMP-1B)
        attribute 'batteryVoltage', 'number'           // Battery voltage measurement

        // common commands for all device types
        //command 'configure', [[name:'normally it is not needed to configure anything', type: 'ENUM', constraints: ConfigureOpts.keySet() as List<String>]]
        command 'setRgbLight', [[name:'LED control', type: 'ENUM', constraints: ['off', 'on']]]
        command 'espReboot', [[name:'Reboot ESP32', type: 'ENUM', constraints: ['reboot']]]
        command 'ping', [[name:'Ping device (measure RTT)', type: 'ENUM', constraints: ['ping']]]


    preferences {
        // txtEnable and logEnable moved to the custom driver settings - copy& paste there ...
        //input name: 'txtEnable', type: 'bool', title: '<b>Enable descriptionText logging</b>', defaultValue: true, description: '<i>Enables command logging.'
        //input name: 'logEnable', type: 'bool', title: '<b>Enable debug logging</b>', defaultValue: true, description: 'Turns on debug logging for 24 hours.'

        if (device) {
            input name: 'advancedOptions', type: 'bool', title: '<b>Advanced Options</b>', description: 'These advanced options should be already automatically set in an optimal way for your device...', defaultValue: false
            if (advancedOptions == true) {
                //input name: 'healthCheckMethod', type: 'enum', title: '<b>Healthcheck Method</b>', options: HealthcheckMethodOpts.options, defaultValue: HealthcheckMethodOpts.defaultValue, required: true, description: 'Method to check device online/offline status.'
                //input name: 'healthCheckInterval', type: 'enum', title: '<b>Healthcheck Interval</b>', options: HealthcheckIntervalOpts.options, defaultValue: HealthcheckIntervalOpts.defaultValue, required: true, description: 'How often the hub will check the device health.<br>3 consecutive failures will result in status "offline"'
                //input name: 'traceEnable', type: 'bool', title: '<b>Enable trace logging</b>', defaultValue: false, description: 'Turns on detailed extra trace logging for 30 minutes.'
            }
        }
    }
}

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
        if (settings?.txtEnable) { log.info "${device} RGB light toggling from ${currentValue} to ${value}" }
    }
    
    if (value == 'on') {
        if (settings?.txtEnable) { log.info "${device} RGB light on" }
        espHomeLightCommand(key: lightKey, state: true)
    } else if (value == 'off') {
        if (settings?.txtEnable) { log.info "${device} RGB light off" }
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
        if (settings?.txtEnable) { log.info "RGB Light is ${rgbLightState ? 'on' : 'off'}" }
    } else {
        if (logEnable) { log.warn "RGB light message does not contain state: ${message}" }
    }
}

/**
 * Handle temperature sensor entities - supports multiple device types
 * Always updates both individual sensor attributes AND main temperature attribute
 * @param message state message from ESPHome
 * @param entity entity information from state.entities
 * @param entitiesMap the ALL_ENTITIES map from the calling driver
 */
void handleTemperatureState(Map message, Map entity, Map entitiesMap) {
    if (!message.hasState) {
        return
    }
    
    String objectId = entity.objectId
    def entityInfo = getEntityInfo(entitiesMap, objectId)
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
    
    // Use universal threshold checking if available, otherwise fall back to simple change detection
    boolean shouldReport = true
    String suffix = ""
    
    if (this.respondsTo('shouldReportValue')) {
        Map reportResult = shouldReportValue(objectId, attributeName, temp)
        shouldReport = reportResult.shouldReport
        if (reportResult.reason == 'max_interval') {
            suffix = ' [MaxReportingInterval]'
        } else if (reportResult.reason == 'refresh') {
            suffix = ' [Refresh]'
        }
    } else {
        // Fallback: only report if the formatted value has changed
        def currentTempState = device.currentState(attributeName)
        String previousValue = currentTempState?.value
        shouldReport = (previousValue != tempStr)
    }
    
    if (shouldReport && shouldReportDiagnostic(entitiesMap, objectId, settings.diagnosticsReporting)) {
        boolean forceStateChange = suffix.contains('[MaxReportingInterval]') || suffix.contains('[Refresh]')
        
        // Send individual sensor event
        Map eventData1 = [name: attributeName, value: tempStr, unit: unit, descriptionText: "${description} is ${tempStr} ${unit}${suffix}"]
        if (forceStateChange) eventData1.isStateChange = true
        sendEvent(eventData1)
        
        // Also update main temperature attribute for Hubitat capability compatibility
        Map eventData2 = [name: "temperature", value: tempStr, unit: unit, descriptionText: "Temperature is ${tempStr} ${unit}${suffix}"]
        if (forceStateChange) eventData2.isStateChange = true
        sendEvent(eventData2)
        
        if (settings?.txtEnable) { 
            log.info "${description} is ${tempStr} ${unit}${suffix}" 
        }
    }
}

/**
 * Handle humidity sensor entity (air_humidity)
 * Always updates both individual sensor attributes AND main humidity attribute
 * @param message state message from ESPHome
 * @param entity entity information from state.entities
 * @param entitiesMap the ALL_ENTITIES map from the calling driver
 */
void handleHumidityState(Map message, Map entity, Map entitiesMap) {
    if (!message.hasState) {
        return
    }
    
    String objectId = entity.objectId
    def entityInfo = getEntityInfo(entitiesMap, objectId)
    if (!entityInfo) {
        if (logEnable) { log.warn "No entity info found for objectId: ${objectId}" }
        return
    }
    
    Float humidity = message.state as Float
    String humidityStr = String.format("%.1f", humidity)
    String attributeName = entityInfo.attr
    String description = entityInfo.description
    String unit = "%rh"  // Hubitat standard unit for relative humidity
    
    // Use universal threshold checking if available, otherwise fall back to simple change detection
    boolean shouldReport = true
    String suffix = ""
    
    if (this.respondsTo('shouldReportValue')) {
        Map reportResult = shouldReportValue(objectId, attributeName, humidity)
        shouldReport = reportResult.shouldReport
        if (reportResult.reason == 'max_interval') {
            suffix = ' [MaxReportingInterval]'
        } else if (reportResult.reason == 'refresh') {
            suffix = ' [Refresh]'
        }
    } else {
        // Fallback: only report if the formatted value has changed
        def currentHumidityState = device.currentState(attributeName)
        String previousValue = currentHumidityState?.value
        shouldReport = (previousValue != humidityStr)
    }
    
    if (shouldReport && shouldReportDiagnostic(entitiesMap, objectId, settings.diagnosticsReporting)) {
        boolean forceStateChange = suffix.contains('[MaxReportingInterval]') || suffix.contains('[Refresh]')
        
        // Send individual sensor event
        Map eventData1 = [name: attributeName, value: humidityStr, unit: unit, descriptionText: "${description} is ${humidityStr} ${unit}${suffix}"]
        if (forceStateChange) eventData1.isStateChange = true
        sendEvent(eventData1)
        
        // Also update main humidity attribute for Hubitat capability compatibility
        Map eventData2 = [name: "humidity", value: humidityStr, unit: unit, descriptionText: "Humidity is ${humidityStr} ${unit}${suffix}"]
        if (forceStateChange) eventData2.isStateChange = true
        sendEvent(eventData2)
        
        if (settings?.txtEnable) { 
            log.info "${description} is ${humidityStr} ${unit}${suffix}" 
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
 * Refresh sensors only - enables 5-second sensor reporting window without clearing state
 */
void refreshSensors(String value = 'refresh') {
    if (value == 'refresh') {
        if (settings?.logEnable) log.debug "refreshSensors() called - enabling 5-second sensor refresh window"
        
        // Enhanced refresh: enable 5-second sensor reporting window
        state.refreshMode = true
        state.refreshTimestamp = now()
        
        // Schedule to disable refresh mode after 5 seconds
        runIn(5, 'disableRefreshMode')
        
        // Safe check for txtEnable setting
        if (settings?.txtEnable) log.info "Sensor refresh: reporting restrictions lifted for 5 seconds"
    } else {
        log.warn "Unsupported refresh sensors value: ${value}"
    }
}

/**
 * Disable refresh mode after the 5-second window
 */
void disableRefreshMode() {
    state.refreshMode = false
    state.remove('refreshTimestamp')
    if (settings?.logEnable) log.debug "Refresh mode disabled - normal sensor reporting thresholds restored"
}

/**
 * Check if we're currently in refresh mode (within 5 seconds of refresh() call)
 */
boolean isInRefreshMode() {
    if (!state.refreshMode || !state.refreshTimestamp) {
        if (settings?.logEnable) log.debug "isInRefreshMode(): Not in refresh mode - refreshMode:${state.refreshMode}, timestamp:${state.refreshTimestamp}"
        return false
    }
    
    long timeSinceRefresh = now() - state.refreshTimestamp
    boolean inRefreshWindow = timeSinceRefresh < 5000 // 5 seconds in milliseconds
    
    if (settings?.logEnable) log.debug "isInRefreshMode(): timeSinceRefresh:${timeSinceRefresh}ms, inWindow:${inRefreshWindow}"
    
    if (!inRefreshWindow && state.refreshMode) {
        // Auto-disable if we missed the scheduled disable
        if (settings?.logEnable) log.debug "isInRefreshMode(): Window expired, auto-disabling refresh mode"
        disableRefreshMode()
        return false
    }
    
    return inRefreshWindow
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
 * Get entity information from the provided entities map
 * @param entitiesMap the ALL_ENTITIES map from the calling driver
 * @param objectId ESPHome entity objectId
 * @return entity information map or null if not found
 */
static Map getEntityInfo(Map entitiesMap, String objectId) {
    return entitiesMap[objectId]
}

/**
 * Get the unit for a specific entity from state.entities, with fallback to ALL_ENTITIES
 * @param entitiesMap the ALL_ENTITIES map from the calling driver
 * @param objectId ESPHome entity objectId
 * @return unit string with temperature scale applied
 */
String getEntityUnit(Map entitiesMap, String objectId) {
    // First try to get unit from state.entities
    def entity = state.entities?.values()?.find { it.objectId == objectId }
    String unit = entity?.unitOfMeasurement as String
    
    // If no unit found in state.entities, use fallback from entitiesMap (if provided)
    if (!unit) {
        def entityInfo = entitiesMap[objectId]
        unit = entityInfo?.unit as String ?: ''
    }
    
    // Convert temperature units based on hub setting
    if (unit == '°C' && location.temperatureScale == 'F') {
        return '°F'
    }
    
    return unit
}

/**
 * Get entity type for classification
 * @param entitiesMap the ALL_ENTITIES map from the calling driver
 * @param objectId ESPHome entity objectId
 * @return entity type string
 */
String getEntityType(Map entitiesMap, String objectId) {
    def entityInfo = getEntityInfo(entitiesMap, objectId)
    return entityInfo?.type as String ?: 'unknown'
}

/**
 * Get entity description for logging
 * @param entitiesMap the ALL_ENTITIES map from the calling driver
 * @param objectId ESPHome entity objectId
 * @return entity description string
 */
String getEntityDescription(Map entitiesMap, String objectId) {
    def entityInfo = getEntityInfo(entitiesMap, objectId)
    return entityInfo?.description as String ?: objectId
}

/**
 * Check if diagnostic reporting is enabled for the given entity
 * @param entitiesMap the ALL_ENTITIES map from the calling driver
 * @param objectId ESPHome entity objectId
 * @param diagnosticsReporting the diagnosticsReporting setting from the calling driver
 * @return true if events should be sent, false if diagnostic reporting is disabled
 */
boolean shouldReportDiagnostic(Map entitiesMap, String objectId, Boolean diagnosticsReporting) {
    // If diagnosticsReporting is enabled, always report
    if (diagnosticsReporting == true) {
        return true
    }
    
    // If the entity is not in the map, always report
    if (!entitiesMap.containsKey(objectId)) {
        return true
    }
    
    // Check if the entity is marked as diagnostic
    def entityInfo = entitiesMap[objectId]
    if (entityInfo?.isDiag != true) {
        return true
    }
    
    // Entity is diagnostic and reporting is disabled
    return false
}

/**
 * ESP32 reboot command - sends reboot command to ESPHome device
 * @param value command value ('reboot' to execute, anything else is ignored)
 */
void espReboot(String value) {
    def rebootKey = state.espRebootKey
    
    if (rebootKey == null) {
        log.warn "ESP reboot entity not found"
        return
    }
    
    if (value == 'reboot') {
        if (settings?.txtEnable) { log.info "${device} rebooting ESP32" }
        espHomeButtonCommand(key: rebootKey)
    } else {
        log.warn "Unsupported ESP reboot value: ${value}"
    }
}

/**
 * Ping command - measures round-trip time to ESPHome device
 * Sends a harmless device info request and measures response time
 * @param value command value ('ping' to execute, anything else is ignored)
 */
void ping(String value = 'ping') {
    if (value != 'ping') {
        log.warn "Unsupported ping value: ${value}"
        return
    }
    
    // Record the start time for RTT measurement
    state.pingStartTime = now()
    
    if (settings?.txtEnable) { log.info "${device} sending ping to measure RTT" }
    
    // Send a harmless device info request - all ESPHome devices support this
    espHomeDeviceInfoRequest()
}

/**
 * Handle ping response by calculating and reporting RTT
 * This should be called when a device info response is received during a ping
 */
void handlePingResponse() {
    if (state.pingStartTime != null) {
        Long rttMs = now() - state.pingStartTime
        state.remove('pingStartTime')
        
        // Send RTT event
        sendEvent(name: "rtt", value: rttMs, unit: "ms", descriptionText: "Round-trip time is ${rttMs} ms")
        
        if (settings?.txtEnable) { 
            log.info "Ping response received - RTT: ${rttMs} ms" 
        }
    }
}

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
        if (settings?.txtEnable) { 
            log.info "checkDriverVersion: updating the settings from the current driver version ${state.driverVersion} to the new version ${versionString}" 
        }
        state.driverVersion = versionString
    }
}


