/**
 *  MIT License
 *  Copyright 2022 Jonathan Bradshaw (jb@nrgup.net)
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 *
 *  ver. 1.0.0  2025-07-07 kkossev  - first beta version
 * 
 *                         TODO: add driver version
*/

import groovy.transform.Field

@Field static final Boolean _DEBUG = true
@Field static final String DRIVER_VERSION =  '1.0.0'
@Field static final String DATE_TIME_STAMP = '07/08/2025 6:09 PM'

metadata {
    definition(
        name: 'ESPHome Apollo PLT-1(B)',
        namespace: 'apollo',
        author: 'Krassimir Kossev',
        singleThreaded: true,
        importUrl: 'https://raw.githubusercontent.com/kkossev/Hubitat-ESPHome-Apollo/refs/heads/main/Apollo%20PLT-1B.groovy') {

        capability 'Sensor'
        capability 'Refresh'
        capability 'RelativeHumidityMeasurement'
        capability 'SignalStrength'
        capability 'TemperatureMeasurement'
        capability 'IlluminanceMeasurement'
        capability 'UltravioletIndex'
        capability 'Battery'
        capability 'Initialize'

        // attribute populated by ESPHome API Library automatically
        attribute 'networkStatus', 'enum', [ 'connecting', 'online', 'offline' ]
        attribute 'airTemperature', 'number'          // AHT20-F air temperature sensor
        attribute 'airTemperatureOffset', 'number'    // Air temperature calibration offset
        attribute 'airHumidityOffset', 'number'       // Air humidity calibration offset
        attribute 'espTemperature', 'number'
        attribute 'soilMoisture', 'number'
        attribute 'soilTemperature', 'number'         // Optional DS18B20 soil temperature probe
        attribute 'soilAdc', 'number'                 // Soil ADC voltage measurement
        attribute 'uptime', 'number'
        attribute 'rgbLight', 'enum', ['on', 'off'] 
        attribute 'batteryVoltage', 'number'          // Battery voltage measurement
        attribute 'accessoryPower', 'enum', ['on', 'off']  // Control for sensor power (PLT-1 only)
        attribute 'preventSleep', 'enum', ['on', 'off']
        attribute 'sleepAfterConnecting', 'enum', ['on', 'off']  // PLT-1 only
        attribute 'sleepDuration', 'number'
        attribute 'selectedSensor', 'string'          // PLT-1 only
        attribute 'waterVoltage100', 'number'         // 100% water voltage calibration
        attribute 'dryVoltage', 'number'              // Dry soil voltage calibration
        attribute 'firmwareUpdate', 'string'          // Firmware update status

        command 'setRgbLight', [[name:'LED control', type: 'ENUM', constraints: ['off', 'on']]]
        //command 'setAccessoryPower', [[name:'Accessory Power control', type: 'ENUM', constraints: ['off', 'on']]]
    }

    preferences {
        input name: 'logEnable', type: 'bool', title: 'Enable Debug Logging', required: false, defaultValue: false    // if enabled the library will log debug details
        input name: 'txtEnable', type: 'bool', title: 'Enable descriptionText logging', required: false, defaultValue: true
        input name: 'ipAddress', type: 'text', title: 'Device IP Address', required: true    // required setting for API library
        input name: 'temperaturePreference', type: 'enum', title: 'Temperature Sensor Selection', required: false, options: ['Air', 'Soil'], defaultValue: 'Air', description: 'Select which sensor to use for main temperature attribute (applies to both PLT-1 and PLT-1B devices)'    
        input name: 'airTemperatureOffset', type: 'decimal', title: 'Air Temperature Offset (°)', required: false, defaultValue: 0.0, range: '-50..50', description: 'Calibration offset for air temperature sensor'
        input name: 'airHumidityOffset', type: 'decimal', title: 'Air Humidity Offset (%)', required: false, defaultValue: 0.0, range: '-50..50', description: 'Calibration offset for air humidity sensor'
        input name: 'advancedOptions', type: 'bool', title: '<b>Advanced Options</b>', description: 'Flip to see or hide the advanced options', defaultValue: false
        if (advancedOptions == true) {
            input name: 'password', type: 'text', title: 'Device Password <i>(if required)</i>', required: false     // optional setting for API library
            input name: 'diagnosticsReporting', type: 'bool', title: 'Enable Diagnostic Attributes', required: false, defaultValue: false, description: 'Enable reporting of technical diagnostic attributes (advanced users only)'
            input name: 'logWarnEnable', type: 'bool', title: 'Enable warning logging', required: false, defaultValue: false, description: '<i>Enables API Library warnings and info logging.</i>'
        }
    }
}

@Field static final Map<String, Map<String, Object>> ALL_ENTITIES = [
    // PLT-1B specific entities (based on actual device data)
    '100__water_voltage':                  [attr: 'waterVoltage100',                isDiag: true,  type: 'config',      description: '100% water voltage calibration threshold'],
    'air_humidity_offset':                 [attr: 'airHumidityOffset',              isDiag: true,  type: 'offset',      description: 'Air humidity calibration offset'],
    'air_temperature_offset':              [attr: 'airTemperatureOffset',           isDiag: true,  type: 'offset',      description: 'Air temperature calibration offset'],
    'soil_adc':                            [attr: 'soilAdc',                        isDiag: true,  type: 'sensor',      description: 'Soil ADC voltage measurement'],
    
    // Common entities (present in both PLT-1 and PLT-1B)
    'air_humidity':                        [attr: 'humidity',                       isDiag: false, type: 'sensor',      description: 'Air humidity sensor (AHT20-F)'],
    'air_temperature':                     [attr: 'airTemperature',                 isDiag: false, type: 'temperature', description: 'Air temperature sensor (AHT20-F)'],
    'dry_voltage':                         [attr: 'dryVoltage',                     isDiag: true,  type: 'config',      description: 'Dry soil voltage calibration threshold'],
    'esp_reboot':                          [attr: 'espReboot',                      isDiag: true,  type: 'button',      description: 'ESP device reboot button'],
    'esp_temperature':                     [attr: 'espTemperature',                 isDiag: true,  type: 'temperature', description: 'ESP32 chip internal temperature'],
    'factory_reset_esp':                   [attr: 'factoryResetEsp',                isDiag: true,  type: 'button',      description: 'Factory reset ESP device button'],
    'firmware_update':                     [attr: 'firmwareUpdate',                 isDiag: true,  type: 'status',      description: 'Firmware update status'],
    'ltr390_light':                        [attr: 'illuminance',                    isDiag: false, type: 'sensor',      description: 'LTR390 ambient light sensor'],
    'ltr390_uv_index':                     [attr: 'ultravioletIndex',               isDiag: false, type: 'sensor',      description: 'LTR390 UV index sensor'],
    'online':                              [attr: 'networkStatus',                  isDiag: true,  type: 'status',      description: 'Network connection status'],
    'prevent_sleep':                       [attr: 'preventSleep',                   isDiag: true,  type: 'switch',      description: 'Prevent device sleep mode switch'],
    'rgb_light':                           [attr: 'rgbLight',                       isDiag: false, type: 'light',       description: 'RGB status light control'],
    'rssi':                                [attr: 'rssi',                           isDiag: true,  type: 'signal',      description: 'WiFi signal strength indicator'],
    'sleep_duration':                      [attr: 'sleepDuration',                  isDiag: true,  type: 'config',      description: 'Device sleep duration between measurements'],
    'soil_moisture':                       [attr: 'soilMoisture',                   isDiag: false, type: 'sensor',      description: 'Soil moisture sensor'],
    'soil_temperature':                    [attr: 'soilTemperature',                isDiag: false, type: 'temperature', description: 'Soil temperature sensor (DS18B20 probe)'],
    'uptime':                              [attr: 'uptime',                         isDiag: true,  type: 'status',      description: 'Device uptime since last restart'],
    
    // PLT-1 specific entities (may be present in some devices)
    'accessory_power':                     [attr: 'accessoryPower',                 isDiag: false, type: 'switch',      description: 'Accessory power control switch'],
    'battery_level':                       [attr: 'battery',                        isDiag: false, type: 'sensor',      description: 'Battery charge level percentage'],
    'battery_voltage':                     [attr: 'batteryVoltage',                 isDiag: false, type: 'sensor',      description: 'Battery voltage measurement'],
    'select_sensor':                       [attr: 'selectedSensor',                 isDiag: true,  type: 'selector',    description: 'Active temperature sensor selection'],
    'sleep_after_connecting':              [attr: 'sleepAfterConnecting',           isDiag: true,  type: 'switch',      description: 'Sleep after connecting switch']
]

/**
 * Get entity information from the ALL_ENTITIES map
 * @param objectId ESPHome entity objectId
 * @return entity information map or null if not found
 */
private Map getEntityInfo(String objectId) {
    return ALL_ENTITIES[objectId]
}

/**
 * Get the unit for a specific entity from state.entities, with fallback to ALL_ENTITIES
 * @param objectId ESPHome entity objectId
 * @return unit string with temperature scale applied
 */
private String getEntityUnit(String objectId) {
    // First try to get unit from state.entities
    def entity = state.entities?.values()?.find { it.objectId == objectId }
    String unit = entity?.unitOfMeasurement as String
    
    // If no unit found in state.entities, use fallback from ALL_ENTITIES (if provided)
    if (!unit) {
        def entityInfo = ALL_ENTITIES[objectId]
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
 * @param objectId ESPHome entity objectId
 * @return entity type string
 */
private String getEntityType(String objectId) {
    def entityInfo = getEntityInfo(objectId)
    return entityInfo?.type as String ?: 'unknown'
}

/**
 * Get entity description for logging
 * @param objectId ESPHome entity objectId
 * @return entity description string
 */
private String getEntityDescription(String objectId) {
    def entityInfo = getEntityInfo(objectId)
    return entityInfo?.description as String ?: objectId
}

/**
 * Check if diagnostic reporting is enabled for the given entity
 * @param objectId ESPHome entity objectId
 * @return true if events should be sent, false if diagnostic reporting is disabled
 */
private boolean shouldReportDiagnostic(String objectId) {
    // If diagnosticsReporting is enabled, always report
    if (settings.diagnosticsReporting == true) {
        return true
    }
    
    // If the entity is not in the map, always report
    if (!ALL_ENTITIES.containsKey(objectId)) {
        return true
    }
    
    // Check if the entity is marked as diagnostic
    def entityInfo = ALL_ENTITIES[objectId]
    if (entityInfo?.isDiag != true) {
        return true
    }
    
    // Entity is diagnostic and reporting is disabled
    return false
}

public void initialize() {
    // API library command to open socket to device, it will automatically reconnect if needed
    openSocket()

    if (logEnable) {
        runIn(1800, 'logsOff')
    }
}

public void installed() {
    log.info "${device} driver installed"
}

public void logsOff() {
    espHomeSubscribeLogs(LOG_LEVEL_INFO, false) // disable device logging
    device.updateSetting('logEnable', false)
    log.info "${device} debug logging disabled"
}

public void refresh() {
    checkDriverVersion()
    log.info "${device} refresh"
    state.clear()
    state.requireRefresh = true
    espHomeDeviceInfoRequest()
}

public void updated() {
    checkDriverVersion()
    log.info "${device} driver configuration updated"
    
    // Delete diagnostic attribute states if diagnostics reporting is disabled
    if (settings.diagnosticsReporting == false) {
        ALL_ENTITIES.each { entityId, entityInfo ->
            def attributeName = entityInfo.attr
            // Skip networkStatus as it's important for connection status
            if (attributeName != 'networkStatus' && entityInfo.isDiag == true) {
                device.deleteCurrentState(attributeName)
            }
        }
    }
    
    // Sync temperature preference with ESPHome select_sensor entity (PLT-1 only)
    if (settings.temperaturePreference) {
        syncTemperatureSelection()
    }
    
    // Sync air humidity offset preference with ESPHome (PLT-1B has air_humidity_offset entity)
    if (settings.airHumidityOffset != null) {
        syncAirHumidityOffset()
    }
    
    // Sync air temperature offset preference with ESPHome (PLT-1B has air_temperature_offset entity)
    if (settings.airTemperatureOffset != null) {
        syncAirTemperatureOffset()
    }
    
    initialize()
}

private void syncTemperatureSelection() {
    // Find the select_sensor entity key
    def selectKey = null
    state.entities?.each { key, entity ->
        if (entity.objectId == 'select_sensor') {
            selectKey = key as Long
        }
    }
    
    if (selectKey == null) {
        if (logEnable) { 
            log.warn "Select sensor entity not found - available entities: ${state.entities?.values()?.collect { it.objectId }}" 
        }
        return
    }
    
    String selectedSensor = settings.temperaturePreference
    if (txtEnable) { log.info "${device} syncing selected sensor to ${selectedSensor} (key: ${selectKey})" }
    
    // Send the selection to ESPHome
    espHomeSelectCommand(key: selectKey, state: selectedSensor)
}

private void syncAirHumidityOffset() {
    // Find the air_humidity_offset entity key (if it exists)
    def offsetKey = null
    state.entities?.each { key, entity ->
        if (entity.objectId == 'air_humidity_offset') {
            offsetKey = key as Long
        }
    }
    
    if (offsetKey == null) {
        if (logEnable) { 
            log.warn "Air humidity offset entity not found - available entities: ${state.entities?.values()?.collect { it.objectId }}" 
        }
        return
    }
    
    Float offset = settings.airHumidityOffset as Float
    if (txtEnable) { log.info "${device} syncing air humidity offset to ${offset}% (key: ${offsetKey})" }
    
    // Send the offset to ESPHome
    espHomeNumberCommand(key: offsetKey, state: offset)
}

private void syncAirTemperatureOffset() {
    // Find the air_temperature_offset entity key (if it exists)
    def offsetKey = null
    state.entities?.each { key, entity ->
        if (entity.objectId == 'air_temperature_offset') {
            offsetKey = key as Long
        }
    }
    
    if (offsetKey == null) {
        if (logEnable) { 
            log.warn "Air temperature offset entity not found - available entities: ${state.entities?.values()?.collect { it.objectId }}" 
        }
        return
    }
    
    Float offset = settings.airTemperatureOffset as Float
    if (txtEnable) { log.info "${device} syncing air temperature offset to ${offset}° (key: ${offsetKey})" }
    
    // Send the offset to ESPHome
    espHomeNumberCommand(key: offsetKey, state: offset)
}

public void uninstalled() {
    closeSocket('driver uninstalled') // make sure the socket is closed when uninstalling
    log.info "${device} driver uninstalled"
}

// the parse method is invoked by the API library when messages are received
void parse(final Map message) {
    checkDriverVersion()
    if (logEnable) { log.debug "ESPHome received: ${message}" }

    switch (message.type) {
        case 'device':
            // Device information
            break

        case 'entity':
            parseKeys(message)
            break

        case 'state':
            parseState(message)
    }
}

void parseKeys(final Map message) {
    if (state.entities == null) { state.entities = [:] }
    
    // Convert key to Long for consistency
    Long key = message.key as Long
    
    // Check if the message contains the required keys
    if (message.objectId && message.key) {
        // Store the entity using string representation of key for consistent map access
        state.entities["$key"] = message
        
        // Store specific entity keys for quick access
        if (message.objectId == 'rgb_light') {
            state.rgbLightKey = key
        }
        if (message.objectId == 'accessory_power') {
            state.accessoryPowerKey = key
        }
        
        if (logEnable) { 
            log.debug "entity registered: ${message.objectId} (key=${key}, platform=${message.platform})" 
        }
    } else {
        if (logEnable) { 
            log.warn "Message does not contain required keys: ${message}" 
        }
    }
}

void parseState(final Map message) {
    if (message.key == null) { return }
    
    final Long key = message.key as Long        
    def entity = state.entities["$key"]
    if (logEnable) {
        log.debug "parseState: key=${key}, objectId=${entity?.objectId}, state=${message.state}"
    }
    
    if (entity == null) { 
        log.warn "Entity for key ${key} not found" 
        return 
    }
    
    if (isNullOrEmpty(message.state)) { 
        if (logEnable) { log.warn "Message state is null or empty for key ${key}" }
        return 
    }
    
    def objectId = entity.objectId
    if (isNullOrEmpty(objectId)) { 
        if (logEnable) { log.warn "ObjectId is null or empty for key ${key}" }
        return 
    }

    // Handle special cases that need custom logic
    switch (objectId) {
        case 'rgb_light':
            handleRgbLightState(message)
            break
        case 'accessory_power':
            handleAccessoryPowerState(message)
            break
        case 'prevent_sleep':
            handlePreventSleepState(message)
            break
        case 'select_sensor':
            handleSelectSensorState(message)
            break
        case 'soil_temperature':
        case 'air_temperature':
            handleTemperatureState(message, entity)
            break
        case 'air_humidity':
            handleHumidityState(message, entity)
            break
        case 'ltr390_light':
            handleIlluminanceState(message, entity)
            break
        case 'ltr390_uv_index':
            handleUVIndexState(message, entity)
            break
        case 'soil_moisture':
            handleSoilMoistureState(message, entity)
            break
        default:
            // Use common handler for most entities
            handleGenericEntityState(message, entity)
            break
    }
}

/**
 * Common handler for most entity state updates
 * @param message state message from ESPHome
 * @param entity entity information from state.entities
 */
private void handleGenericEntityState(Map message, Map entity) {
    if (!message.hasState) {
        return
    }
    
    String objectId = entity.objectId
    def entityInfo = getEntityInfo(objectId)
    if (!entityInfo) {
        if (logEnable) { log.warn "No entity info found for objectId: ${objectId}" }
        return
    }
    
    String attributeName = entityInfo.attr
    String description = entityInfo.description
    String unit = getEntityUnit(objectId)
    def rawValue = message.state
    def processedValue = rawValue
    String formattedValue = ""
    
    // Process value based on entity type
    switch (entityInfo.type) {
        case 'temperature':
            Float tempC = rawValue as Float
            Float temp = convertTemperature(tempC)
            processedValue = temp
            formattedValue = String.format("%.1f", temp)
            break
            
        case 'offset':
            if (unit.contains('°')) {  // Temperature offset
                Float offsetC = rawValue as Float
                Float offset = convertTemperature(offsetC) - convertTemperature(0)
                processedValue = offset
                formattedValue = String.format("%.1f", offset)
            } else {  // Other offsets (humidity, etc.)
                processedValue = rawValue as Float
                formattedValue = String.format("%.1f", processedValue)
                
                // Special case: Sync offset preferences with ESPHome values
                if (objectId == 'air_humidity_offset') {
                    Float currentPref = settings.airHumidityOffset as Float
                    if (currentPref != processedValue) {
                        device.updateSetting('airHumidityOffset', processedValue)
                        if (txtEnable && shouldReportDiagnostic(objectId)) {
                            log.info "Air humidity offset preference synced from ESPHome to ${processedValue}%"
                        }
                    }
                } else if (objectId == 'air_temperature_offset') {
                    Float currentPref = settings.airTemperatureOffset as Float
                    if (currentPref != processedValue) {
                        device.updateSetting('airTemperatureOffset', processedValue)
                        if (txtEnable && shouldReportDiagnostic(objectId)) {
                            log.info "Air temperature offset preference synced from ESPHome to ${processedValue}°"
                        }
                    }
                }
            }
            break
            
        case 'switch':
            boolean switchState = rawValue as Boolean
            processedValue = switchState ? "on" : "off"
            formattedValue = processedValue
            break
            
        case 'sensor':
            processedValue = rawValue as Float
            formattedValue = String.format("%.2f", processedValue)
            break
            
        case 'config':
            if (unit.contains('°')) {  // Temperature config
                Float tempC = rawValue as Float
                Float temp = convertTemperature(tempC)
                processedValue = temp
                formattedValue = String.format("%.1f", temp)
            } else {
                processedValue = rawValue as Float
                formattedValue = String.format("%.2f", processedValue)
            }
            break
            
        case 'signal':
            processedValue = rawValue as Integer
            formattedValue = processedValue.toString()
            break
            
        case 'status':
            if (objectId == 'uptime') {
                Long uptime = rawValue as Long
                int days = uptime / 86400
                int hours = (uptime % 86400) / 3600
                int minutes = (uptime % 3600) / 60
                int seconds = uptime % 60
                processedValue = "${days}d ${hours}h ${minutes}m ${seconds}s"
                formattedValue = processedValue
            } else {
                processedValue = rawValue
                formattedValue = processedValue.toString()
            }
            break
            
        default:
            processedValue = rawValue
            formattedValue = processedValue.toString()
            break
    }
    
    // Send event if diagnostic reporting allows it
    if (shouldReportDiagnostic(objectId)) {
        Map eventData = [
            name: attributeName,
            value: (formattedValue ?: processedValue),
            descriptionText: "${description} is ${formattedValue} ${unit}".trim()
        ]
        
        if (unit) {
            eventData.unit = unit
        }
        
        sendEvent(eventData)
    }
    
    // Only log if text logging is enabled AND diagnostic reporting allows it
    if (txtEnable && shouldReportDiagnostic(objectId)) {
        log.info "${description}: ${formattedValue} ${unit}".trim()
    }
}

 /**
 * Check if the specified value is null or empty
 * @param value value to check
 * @return true if the value is null or empty, false otherwise
 */
private static boolean isNullOrEmpty(final Object value) {
    return value == null || (value as String).trim().isEmpty()
}

void setRgbLight(String value) {
    def lightKey = state.rgbLightKey
    
    if (lightKey == null) {
        log.warn "RGB light entity not found"
        return
    }
    
    if (value == 'on') {
        if (txtEnable) { log.info "${device} RGB light on" }
        espHomeLightCommand(key: lightKey, state: true)
    } else if (value == 'off') {
        if (txtEnable) { log.info "${device} RGB light off" }
        espHomeLightCommand(key: lightKey, state: false)
    } else {
        log.warn "Unsupported RGBlight value: ${value}"
    }
}

void setAccessoryPower(String value) {
    def powerKey = state.accessoryPowerKey
    
    if (powerKey == null) {
        log.warn "Accessory power entity not found"
        return
    }
    
    if (value == 'on') {
        if (txtEnable) { log.info "${device} Accessory power on" }
        espHomeSwitchCommand(key: powerKey, state: true)
    } else if (value == 'off') {
        if (txtEnable) { log.info "${device} Accessory power off" }
        espHomeSwitchCommand(key: powerKey, state: false)
    } else {
        log.warn "Unsupported accessory power value: ${value}"
    }
}

private void handleAccessoryPowerState(Map message) {
    // For switch entities, check for 'state' directly since they don't use 'hasState'
    if (message.state != null) {
        def powerState = message.state as Boolean
        sendEvent(name: "accessoryPower", value: powerState ? 'on' : 'off', descriptionText: "Accessory Power is ${powerState ? 'on' : 'off'}")
        if (txtEnable) { log.info "Accessory Power is ${powerState ? 'on' : 'off'}" }
    } else {
        if (logEnable) { log.warn "Accessory power message does not contain state: ${message}" }
    }
}

private void handleRgbLightState(Map message) {
    // For light entities, check for 'state' directly since they don't use 'hasState'
    if (message.state != null) {
        def rgbLightState = message.state as Boolean
        sendEvent(name: "rgbLight", value: rgbLightState ? 'on' : 'off', descriptionText: "RGB Light is ${rgbLightState ? 'on' : 'off'}")
        if (txtEnable) { log.info "RGB Light is ${rgbLightState ? 'on' : 'off'}" }
    } else {
        if (logEnable) { log.warn "RGB light message does not contain state: ${message}" }
    }
}

private void handlePreventSleepState(Map message) {
    // For switch entities, check for 'state' directly since they don't use 'hasState'
    if (message.state != null) {
        def sleepState = message.state as Boolean
        if (shouldReportDiagnostic('prevent_sleep')) {
            sendEvent(name: "preventSleep", value: sleepState ? 'on' : 'off', descriptionText: "Prevent Sleep is ${sleepState ? 'on' : 'off'}")
        }
        if (txtEnable && shouldReportDiagnostic('prevent_sleep')) { 
            log.info "Prevent Sleep is ${sleepState ? 'on' : 'off'}" 
        }
    } else {
        if (logEnable) { log.warn "Prevent sleep message does not contain state: ${message}" }
    }
}

/**
 * Handle temperature sensor entities (soil_temperature and air_temperature)
 * @param message state message from ESPHome
 * @param entity entity information from state.entities
 */
private void handleTemperatureState(Map message, Map entity) {
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
    
    // Get the previous individual sensor temperature value
    def currentSensorState = device.currentState(attributeName)
    String previousSensorValue = currentSensorState?.value
    
    // Send individual sensor events only when Debug logging is enabled AND value has changed
    if (settings.logEnable && previousSensorValue != tempStr) {
        sendEvent(name: attributeName, value: tempStr, unit: unit, descriptionText: "${description} is ${tempStr} ${unit}")
        log.info "${description} is ${tempStr} ${unit}"
    }
    
    // Update main temperature attribute based on temperature preference
    String selectedSensorType = (objectId == 'soil_temperature') ? 'Soil' : 'Air'
    String userPreference = settings.temperaturePreference ?: 'Air'  // Default to Air if not set
    if (userPreference == selectedSensorType) {
        // Get the previous main temperature value
        def currentMainTempState = device.currentState("temperature")
        String previousMainValue = currentMainTempState?.value
        
        // Only update main temperature if the value has changed
        if (previousMainValue != tempStr) {
            String mainDescription = "Temperature is ${tempStr} ${unit}"
            sendEvent(name: "temperature", value: tempStr, unit: unit, descriptionText: mainDescription)
            if (txtEnable) { 
                log.info "${mainDescription}" 
            }
        }
        else {
            if (logEnable) { 
                log.debug "Main temperature already at ${tempStr} ${unit}, no update needed" 
            }
        }
    }
}

private void handleSelectSensorState(Map message) {
    if (message.hasState) {
        def selectedSensor = message.state as String
        
        if (shouldReportDiagnostic('select_sensor')) {
            sendEvent(name: "selectedSensor", value: selectedSensor, descriptionText: "Selected sensor is ${selectedSensor}")
        }
        
        // Only log if diagnostic reporting allows it
        if (txtEnable && shouldReportDiagnostic('select_sensor')) { 
            log.info "ESPHome selected sensor changed to: ${selectedSensor}" 
        }
        
        // Sync the preference setting with ESPHome selection (avoid loops)
        if (settings.temperaturePreference != selectedSensor) {
            device.updateSetting('temperaturePreference', selectedSensor)
            if (txtEnable && shouldReportDiagnostic('select_sensor')) { 
                log.info "Temperature preference synced from ESPHome to ${selectedSensor}" 
            }
        }
        
        if (txtEnable && shouldReportDiagnostic('select_sensor')) { 
            log.info "Selected sensor is ${selectedSensor}" 
        }
    } else {
        if (logEnable) { log.warn "Select sensor message does not have state: ${message}" }
    }
}

/**
 * Handle humidity sensor entity (air_humidity)
 * @param message state message from ESPHome
 * @param entity entity information from state.entities
 */
private void handleHumidityState(Map message, Map entity) {
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
    
    // Get the previous humidity value from current state
    def currentHumidityState = device.currentState(attributeName)
    String previousValue = currentHumidityState?.value
    
    // Only send event and log if the value has changed
    if (previousValue != humidityStr) {
        // Always send humidity event (air_humidity is isDiag: false)
        sendEvent(name: attributeName, value: humidityStr, unit: unit, descriptionText: "${description} is ${humidityStr} ${unit}")
        
        // Always log humidity (it's not a diagnostic attribute)
        if (txtEnable) { 
            log.info "${description} is ${humidityStr} ${unit}" 
        }
    }
}

/**
 * Handle illuminance sensor entity (ltr390_light)
 * @param message state message from ESPHome
 * @param entity entity information from state.entities
 */
private void handleIlluminanceState(Map message, Map entity) {
    if (!message.hasState) {
        return
    }
    
    String objectId = entity.objectId
    def entityInfo = getEntityInfo(objectId)
    if (!entityInfo) {
        if (logEnable) { log.warn "No entity info found for objectId: ${objectId}" }
        return
    }
    
    Float illuminance = message.state as Float
    Integer illuminanceInt = Math.round(illuminance) as Integer
    String attributeName = entityInfo.attr
    String description = entityInfo.description
    String unit = "lx"  // Standard unit for illuminance
    
    // Get the previous illuminance value from current state
    def currentIlluminanceState = device.currentState(attributeName)
    String previousValue = currentIlluminanceState?.value
    
    // Only send event and log if the value has changed
    if (previousValue != illuminanceInt.toString()) {
        // Always send illuminance event (ltr390_light is isDiag: false)
        sendEvent(name: attributeName, value: illuminanceInt, unit: unit, descriptionText: "${description} is ${illuminanceInt} ${unit}")
        
        // Always log illuminance (it's not a diagnostic attribute)
        if (txtEnable) { 
            log.info "${description} is ${illuminanceInt} ${unit}" 
        }
    }
}

/**
 * Handle UV index sensor entity (ltr390_uv_index)
 * @param message state message from ESPHome
 * @param entity entity information from state.entities
 */
private void handleUVIndexState(Map message, Map entity) {
    if (!message.hasState) {
        return
    }
    
    String objectId = entity.objectId
    def entityInfo = getEntityInfo(objectId)
    if (!entityInfo) {
        if (logEnable) { log.warn "No entity info found for objectId: ${objectId}" }
        return
    }
    
    Float uvIndex = message.state as Float
    String uvIndexStr = String.format("%.5f", uvIndex)  // More precision for UV index
    String attributeName = entityInfo.attr
    String description = entityInfo.description
    String unit = "UVI"  // UV Index unit
    
    // Get the previous UV index value from current state
    def currentUVState = device.currentState(attributeName)
    String previousValue = currentUVState?.value
    
    // Only send event and log if the value has changed
    if (previousValue != uvIndexStr) {
        // Always send UV index event (ltr390_uv_index is isDiag: false)
        sendEvent(name: attributeName, value: uvIndexStr, unit: unit, descriptionText: "${description} is ${uvIndexStr} ${unit}")
        
        // Always log UV index (it's not a diagnostic attribute)
        if (txtEnable) { 
            log.info "${description} is ${uvIndexStr} ${unit}" 
        }
    }
}

/**
 * Handle soil moisture sensor entity
 * @param message state message from ESPHome
 * @param entity entity information from state.entities
 */
private void handleSoilMoistureState(Map message, Map entity) {
    if (!message.hasState) {
        return
    }
    
    String objectId = entity.objectId
    def entityInfo = getEntityInfo(objectId)
    if (!entityInfo) {
        if (logEnable) { log.warn "No entity info found for objectId: ${objectId}" }
        return
    }
    
    Float moisture = message.state as Float
    String moistureStr = String.format("%.1f", moisture)
    String attributeName = entityInfo.attr
    String description = entityInfo.description
    String unit = "%"  // Percentage unit for soil moisture
    
    // Get the previous moisture value from current state
    def currentMoistureState = device.currentState(attributeName)
    String previousValue = currentMoistureState?.value
    
    // Only send event and log if the value has changed
    if (previousValue != moistureStr) {
        // Always send moisture event (soil_moisture is isDiag: false)
        sendEvent(name: attributeName, value: moistureStr, unit: unit, descriptionText: "${description} is ${moistureStr} ${unit}")
        
        // Always log moisture (it's not a diagnostic attribute)
        if (txtEnable) { 
            log.info "${description} is ${moistureStr} ${unit}" 
        }
    }
}

/**
 * Convert temperature based on hub's temperature scale setting
 * @param tempC temperature in Celsius
 * @return temperature in the hub's preferred scale
 */
private def convertTemperature(Float tempC) {
    if (location.temperatureScale == 'F') {
        return (tempC * 9/5) + 32
    }
    return tempC
}

/**
 * Get temperature unit based on hub's temperature scale setting
 * @return temperature unit string
 */
private String getTemperatureUnit() {
    return location.temperatureScale == 'F' ? '°F' : '°C'
}

private String driverVersionAndTimeStamp() { 
    String debugSuffix = _DEBUG ? ' (debug version!)' : ''
    return "${DRIVER_VERSION} ${DATE_TIME_STAMP} ${debugSuffix} (${getHubVersion()} ${location.hub.firmwareVersionString})"
}

//@CompileStatic
public void checkDriverVersion() {
    if (state.driverVersion == null || driverVersionAndTimeStamp() != state.driverVersion) {
        if (txtEnable) { log.info "checkDriverVersion: updating the settings from the current driver version ${state.driverVersion} to the new version ${driverVersionAndTimeStamp()}" }
        //sendInfoEvent("Updated to version ${driverVersionAndTimeStamp()}")
        //logInfo("Updated to version ${driverVersionAndTimeStamp()}")
        state.driverVersion = driverVersionAndTimeStamp()
    }
}

// Put this line at the end of the driver to include the ESPHome API library helper

#include esphome.espHomeApiHelperKKmod

