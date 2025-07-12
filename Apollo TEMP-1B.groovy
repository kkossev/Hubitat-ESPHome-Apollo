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
 *  ver. 1.0.0  2022-07-04 kkossev  - first beta version
 *  ver. 1.0.1  2022-07-12 kkossev  - use a Common library for ESPHome Apollo drivers
 * 
 *                         TODO: 
*/

import groovy.transform.Field

@Field static final Boolean _DEBUG = true
@Field static final String DRIVER_VERSION =  '1.0.1'
@Field static final String DATE_TIME_STAMP = '07/12/2025 9:58 PM'

metadata {
    definition(
        name: 'ESPHome Apollo TEMP-1(B)',
        namespace: 'apollo',
        author: 'Krassimir Kossev',
        singleThreaded: true,
        importUrl: 'https://raw.githubusercontent.com/kkossev/Hubitat-ESPHome-Apollo/refs/heads/main/Apollo%20TEMP-1B.groovy') {

        capability 'Sensor'
        capability 'Refresh'
        capability 'RelativeHumidityMeasurement'
        capability 'SignalStrength'
        capability 'TemperatureMeasurement'
        capability 'Battery'
        capability 'Initialize'

        // attribute populated by ESPHome API Library automatically
        attribute "boardTemperature", "number"
        attribute 'temperatureProbe', 'number'  // Add this line
        attribute 'foodProbe', 'number'
        attribute 'alarmOutsideTempRange', 'enum', ['on', 'off']
        attribute 'tempProbeOffset', 'number'
        attribute 'foodProbeOffset', 'number'
        attribute 'boardTemperatureOffset', 'number'
        attribute 'boardHumidityOffset', 'number'
        attribute 'notifyOnlyOutsideTempDifference', 'enum', ['on', 'off']
        attribute 'selectedProbe', 'string'
        attribute 'probeTempDifferenceThreshold', 'number'
        attribute 'minProbeTemp', 'number'
        attribute 'maxProbeTemp', 'number'

    }

    preferences {
        input name: 'logEnable', type: 'bool', title: 'Enable Debug Logging', required: false, defaultValue: false    // if enabled the library will log debug details
        input name: 'txtEnable', type: 'bool', title: 'Enable descriptionText logging', required: false, defaultValue: true
        input name: 'ipAddress', type: 'text', title: 'Device IP Address', required: true    // required setting for API library
        input name: 'selectedProbe', type: 'enum', title: 'Temperature Sensor Selection', required: false, options: ['Temperature', 'Food'], defaultValue: 'Temperature', description: 'Select which sensor to use for main temperature attribute'    // allows the user to select which sensor to use for temperature
        input name: 'boardHumidityOffset', type: 'decimal', title: 'Board Humidity Offset (%)', required: false, defaultValue: 0.0, range: '-50..50', description: 'Calibration offset for board humidity sensor'
        input name: 'advancedOptions', type: 'bool', title: '<b>Advanced Options</b>', description: 'Flip to see or hide the advanced options', defaultValue: false
        if (advancedOptions == true) {
            input name: 'password', type: 'text', title: 'Device Password <i>(if required)</i>', required: false     // optional setting for API library
            input name: 'diagnosticsReporting', type: 'bool', title: 'Enable Diagnostic Attributes', required: false, defaultValue: false, description: 'Enable reporting of technical diagnostic attributes (advanced users only)'
            input name: 'logWarnEnable', type: 'bool', title: 'Enable warning logging', required: false, defaultValue: true, description: '<i>Enables API Library warnings and info logging.</i>'
        }
    }
}

@Field static final Map<String, Map<String, Object>> ALL_ENTITIES = [
    'alarm_outside_temp_range':            [attr: 'alarmOutsideTempRange',          isDiag: true,  type: 'switch',      description: 'Temperature range alarm switch'],
    'battery_level':                       [attr: 'battery',                        isDiag: false, type: 'sensor',      description: 'Battery charge level percentage'],
    'battery_voltage':                     [attr: 'batteryVoltage',                 isDiag: false, type: 'sensor',      description: 'Battery voltage measurement'],
    'board_humidity':                      [attr: 'humidity',                       isDiag: false, type: 'sensor',      description: 'Humidity'],                                   // Internal board humidity sensor reading
    'board_humidity_offset':               [attr: 'boardHumidityOffset',            isDiag: true,  type: 'offset',      description: 'Board humidity sensor calibration offset'],
    'board_temperature':                   [attr: 'boardTemperature',               isDiag: true,  type: 'temperature', description: 'Internal board temperature sensor reading'],
    'board_temperature_offset':            [attr: 'boardTemperatureOffset',         isDiag: true,  type: 'offset',      description: 'Board temperature sensor calibration offset'],
    'esp_reboot':                          [attr: 'espReboot',                      isDiag: true,  type: 'button',      description: 'ESP device reboot button'],
    'esp_temperature':                     [attr: 'espTemperature',                 isDiag: true,  type: 'temperature', description: 'ESP32 chip internal temperature'],
    'factory_reset_esp':                   [attr: 'factoryResetEsp',                isDiag: true,  type: 'button',      description: 'Factory reset ESP device button'],
    'food_probe':                          [attr: 'foodProbe',                      isDiag: true,  type: 'temperature', description: 'External food probe temperature reading'],
    'food_probe_offset':                   [attr: 'foodProbeOffset',                isDiag: true,  type: 'offset',      description: 'Food probe calibration offset'],
    'max_probe_temp':                      [attr: 'maxProbeTemp',                   isDiag: true,  type: 'config',      description: 'Maximum valid probe temperature threshold'],
    'min_probe_temp':                      [attr: 'minProbeTemp',                   isDiag: true,  type: 'config',      description: 'Minimum valid probe temperature threshold'],
    'notify_only_outside_temp_difference': [attr: 'notifyOnlyOutsideTempDifference',isDiag: true,  type: 'switch',      description: 'Notify only when outside temperature difference threshold'],
    'online':                              [attr: 'networkStatus',                  isDiag: true,  type: 'status',      description: 'Network connection status'],
    'prevent_sleep':                       [attr: 'preventSleep',                   isDiag: true,  type: 'switch',      description: 'Prevent device sleep mode switch'],
    'probe_temp_difference_threshold':     [attr: 'probeTempDifferenceThreshold',   isDiag: true,  type: 'config',      description: 'Temperature difference threshold for notifications'],
    'rgb_light':                           [attr: 'rgbLight',                       isDiag: false, type: 'light',       description: 'RGB status light control'],
    'rssi':                                [attr: 'rssi',                           isDiag: true,  type: 'signal',      description: 'WiFi signal strength indicator'],
    'select_probe':                        [attr: 'selectedProbe',                  isDiag: true,  type: 'selector',    description: 'Active temperature probe selection'],
    'sleep_duration':                      [attr: 'sleepDuration',                  isDiag: true,  type: 'config',      description: 'Device sleep duration between measurements'],
    'temperature_probe':                   [attr: 'temperatureProbe',               isDiag: true,  type: 'temperature', description: 'Primary external temperature probe reading'],
    'temp_probe_offset':                   [attr: 'tempProbeOffset',                isDiag: true,  type: 'offset',      description: 'Temperature probe calibration offset'],
    'uptime':                              [attr: 'uptime',                         isDiag: true,  type: 'status',      description: 'Device uptime since last restart']
]

/**
/**
 * Get the unit for a specific entity from state.entities, with fallback to ALL_ENTITIES
 * @param objectId ESPHome entity objectId
 * @return unit string with temperature scale applied
 */




// Lifecycle methods

public void updated() {
    checkDriverVersion(DRIVER_VERSION, DATE_TIME_STAMP, _DEBUG)
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
    
    // Sync temperature preference with ESPHome select_probe entity
    if (settings.selectedProbe) {
        syncTemperatureSelection()
    }
    
    // Sync board humidity offset preference with ESPHome
    if (settings.boardHumidityOffset != null) {
        syncBoardHumidityOffset()
    }
    
    initialize()
}

private void syncTemperatureSelection() {
    // Find the select_probe entity key
    def selectKey = null
    state.entities?.each { key, entity ->
        if (entity.objectId == 'select_probe') {
            selectKey = key as Long
        }
    }
    
    if (selectKey == null) {
        if (logEnable) { 
            log.warn "Select probe entity not found - available entities: ${state.entities?.values()?.collect { it.objectId }}" 
        }
        return
    }
    
    String selectedProbe = settings.selectedProbe
    if (txtEnable) { log.info "${device} syncing selected probe to ${selectedProbe} (key: ${selectKey})" }
    
    // Send the selection to ESPHome
    espHomeSelectCommand(key: selectKey, state: selectedProbe)
}

private void syncBoardHumidityOffset() {
    // Find the board_humidity_offset entity key
    def offsetKey = null
    state.entities?.each { key, entity ->
        if (entity.objectId == 'board_humidity_offset') {
            offsetKey = key as Long
        }
    }
    
    if (offsetKey == null) {
        if (logEnable) { 
            log.warn "Board humidity offset entity not found - available entities: ${state.entities?.values()?.collect { it.objectId }}" 
        }
        return
    }
    
    Float offset = settings.boardHumidityOffset as Float
    if (txtEnable) { log.info "${device} syncing board humidity offset to ${offset}% (key: ${offsetKey})" }
    
    // Send the offset to ESPHome
    espHomeNumberCommand(key: offsetKey, state: offset)
}

// the parse method is invoked by the API library when messages are received
void parse(final Map message) {
    if (logEnable) { log.debug "parseTEMP: ${message}" }

    switch (message.type) {
        case 'device':
            // Device information - also handle ping response
            handlePingResponse()
            break

        case 'entity':
            parseKeys(message)
            break

        case 'state':
            parseState(message)
    }
}

/**
 * parseKeys - Process entity registration from ESPHome
 */
void parseKeys(final Map message) {
    if (state.entities == null) { state.entities = [:] }
    
    // Convert key to Long for consistency
    Long key = message.key as Long
    
    // Check if the message contains the required keys
    if (message.objectId && message.key) {
        // Store the entity using string representation of key for consistent map access
        state.entities["$key"] = message
        
        // Store specific entity keys for easy access
        storeSpecificEntityKeys(message, key)
        
        if (logEnable) { 
            log.debug "entity registered: ${message.objectId} (key=${key}, platform=${message.platform})" 
        }
    } else {
        if (logEnable) { 
            log.warn "Message does not contain required keys: ${message}" 
        }
    }
}

/**
 * parseState - Process state updates from ESPHome
 */
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

    // Route to appropriate handler
    handleEntityState(message, entity, objectId)
}

// Driver-specific entity key storage
void storeSpecificEntityKeys(Map message, Long key) {
    if (message.objectId == 'rgb_light') {
        state.rgbLightKey = key
    }
    if (message.objectId == 'esp_reboot') {
        state.espRebootKey = key
    }
}

// Driver-specific entity state handler
void handleEntityState(Map message, Map entity, String objectId) {
    // Handle special cases that need custom logic
    switch (objectId) {
        case 'rgb_light':
            handleRgbLightState(message)
            break
        case 'select_probe':
            handleSelectProbeState(message)
            break
        case 'food_probe':
        case 'temperature_probe':
            handleTemperatureState(message, entity, ALL_ENTITIES)
            break
        case 'board_humidity':
            handleHumidityState(message, entity, ALL_ENTITIES)
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
    def entityInfo = getEntityInfo(ALL_ENTITIES, objectId)
    if (!entityInfo) {
        if (logEnable) { log.warn "No entity info found for objectId: ${objectId}" }
        return
    }
    
    String attributeName = entityInfo.attr
    String description = entityInfo.description
    String unit = getEntityUnit(ALL_ENTITIES, objectId)
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
                
                // Special case: Sync board humidity offset preference
                if (objectId == 'board_humidity_offset') {
                    Float currentPref = settings.boardHumidityOffset as Float
                    if (currentPref != processedValue) {
                        device.updateSetting('boardHumidityOffset', processedValue)
                        if (txtEnable && shouldReportDiagnostic(ALL_ENTITIES, objectId, settings.diagnosticsReporting)) {
                            log.info "Board humidity offset preference synced from ESPHome to ${processedValue}%"
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
            if (rawValue instanceof Float) {
                processedValue = rawValue as Float
                formattedValue = String.format("%.1f", processedValue)
            } else {
                processedValue = rawValue as Integer
                formattedValue = processedValue.toString()
            }
            break
            
        case 'config':
            if (unit.contains('°')) {  // Temperature config
                Float tempC = rawValue as Float
                Float temp = convertTemperature(tempC)
                processedValue = temp
                formattedValue = String.format("%.1f", temp)
            } else if (rawValue instanceof Float) {
                processedValue = rawValue as Float
                formattedValue = String.format("%.1f", processedValue)
            } else {
                processedValue = rawValue as Integer
                formattedValue = processedValue.toString()
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
    if (shouldReportDiagnostic(ALL_ENTITIES, objectId, settings.diagnosticsReporting)) {
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
    if (txtEnable && shouldReportDiagnostic(ALL_ENTITIES, objectId, settings.diagnosticsReporting)) {
        log.info "${description}: ${formattedValue} ${unit}".trim()
    }
}

private void handleSelectProbeState(Map message) {
    if (message.hasState) {
        def selectedProbe = message.state as String
        
        if (shouldReportDiagnostic(ALL_ENTITIES, 'select_probe', settings.diagnosticsReporting)) {
            sendEvent(name: "selectedProbe", value: selectedProbe, descriptionText: "Selected probe is ${selectedProbe}")
        }
        
        // Only log if diagnostic reporting allows it
        if (txtEnable && shouldReportDiagnostic(ALL_ENTITIES, 'select_probe', settings.diagnosticsReporting)) { 
            log.info "ESPHome selected probe changed to: ${selectedProbe}" 
        }
        
        // Sync the preference setting with ESPHome selection (avoid loops)
        if (settings.selectedProbe != selectedProbe) {
            device.updateSetting('selectedProbe', selectedProbe)
            if (txtEnable && shouldReportDiagnostic(ALL_ENTITIES, 'select_probe', settings.diagnosticsReporting)) { 
                log.info "Selected probe preference synced from ESPHome to ${selectedProbe}" 
            }
        }
        
        if (txtEnable && shouldReportDiagnostic(ALL_ENTITIES, 'select_probe', settings.diagnosticsReporting)) { 
            log.info "Selected probe is ${selectedProbe}" 
        }
    } else {
        if (logEnable) { log.warn "Select probe message does not have state: ${message}" }
    }
}

// Put this line at the end of the driver to include the ESPHome API library helper

#include esphome.espHomeApiHelperKKmod
#include apollo.espHomeApolloLibraryCommon
