/**
 *  ESPHome Apollo AIR-1 Driver for Hubitat Elevation
 *
 *  https://community.hubitat.com/t/beta-apollo-automation-air-1-driver/155982
 *
 *  MIT License
 *  Copyright 2025 Krassimir Kossev
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
 *  ver. 1.0.0  2025-07-12 kkossev  - first beta version based on PLT-1B driver, using the common apollo library.
 *  ver. 1.0.1  2025-08-10 kkossev  - added thresholds for sensor changes; code optimizations
 * 
 *                         TODO:
*/

import groovy.transform.Field

@Field static final Boolean _DEBUG = false
@Field static final String DRIVER_VERSION =  '1.0.1'
@Field static final String DATE_TIME_STAMP = '08/10/2025 5:21 PM'

metadata {
    definition(
        name: 'ESPHome Apollo AIR-1',
        namespace: 'apollo',
        author: 'Krassimir Kossev',
        singleThreaded: true,
        importUrl: 'https://raw.githubusercontent.com/kkossev/Hubitat-ESPHome-Apollo/refs/heads/main/Apollo%20AIR-1.groovy') {

        capability 'AirQuality'
        capability 'CarbonDioxideMeasurement'
        capability 'RelativeHumidityMeasurement'
        capability 'TemperatureMeasurement'
        capability 'PressureMeasurement'
        capability 'Sensor'
        capability 'Refresh'
        capability 'SignalStrength'
        capability 'Initialize'

        // attribute populated by ESPHome API Library automatically
        
        // Primary air quality sensors
        attribute 'sen55Voc', 'number'                  // SEN55 VOC index
        attribute 'sen55Nox', 'number'                  // SEN55 NOX index
        attribute 'vocQuality', 'string'                // VOC quality text rating
        
        // Particulate matter sensors
        attribute 'pm1', 'number'                       // PM <1µm Weight concentration
        attribute 'pm25', 'number'                      // PM <2.5µm Weight concentration
        attribute 'pm4', 'number'                       // PM <4µm Weight concentration
        attribute 'pm10', 'number'                      // PM <10µm Weight concentration
        
        // Detailed PM size ranges (disabled by default in ESPHome)
        attribute 'pm03To1', 'number'                   // PM 0.3 To 1 µm
        attribute 'pm1To25', 'number'                   // PM 1 To 2.5 µm
        attribute 'pm25To4', 'number'                   // PM 2.5 To 4 µm
        attribute 'pm4To10', 'number'                   // PM 4 To 10 µm
        
        // Gas sensors (ENS160)
        attribute 'ammonia', 'number'                   // Ammonia (NH₃)
        attribute 'carbonMonoxide', 'number'            // Carbon Monoxide (CO)
        attribute 'ethanol', 'number'                   // Ethanol (C₂H₅OH)
        attribute 'hydrogen', 'number'                  // Hydrogen (H₂)
        attribute 'methane', 'number'                   // Methane (CH₄)
        attribute 'nitrogenDioxide', 'number'           // Nitrogen Dioxide (NO₂)
        
        // Configuration and calibration
        attribute 'sen55TemperatureOffset', 'number'   // SEN55 temperature calibration offset
        attribute 'sen55HumidityOffset', 'number'      // SEN55 humidity calibration offset
        
        // Controls and status
        // Diagnostic attributes

        command 'calibrateScd40', [[name:'Calibrate CO2 to 420ppm', type: 'ENUM', constraints: ['calibrate']]]
        command 'cleanSen55', [[name:'Clean SEN55 sensor', type: 'ENUM', constraints: ['clean']]]
        command 'refreshSensors', [[name:'Refresh Sensors Only', type: 'ENUM', constraints: ['refresh']]]
    }

    preferences {
        input name: 'logEnable', type: 'bool', title: 'Enable Debug Logging', required: false, defaultValue: false    // if enabled the library will log debug details
        input name: 'txtEnable', type: 'bool', title: 'Enable descriptionText logging', required: false, defaultValue: true
        input name: 'ipAddress', type: 'text', title: 'Device IP Address', required: true    // required setting for API library
        input name: 'sen55TemperatureOffset', type: 'decimal', title: 'SEN55 Temperature Offset (°)', required: false, defaultValue: 0.0, range: '-70..70', description: 'Calibration offset for SEN55 temperature sensor'
        input name: 'sen55HumidityOffset', type: 'decimal', title: 'SEN55 Humidity Offset (%)', required: false, defaultValue: 0.0, range: '-70..70', description: 'Calibration offset for SEN55 humidity sensor'

        input name: 'maxReportingInterval', type: 'number', title: 'Maximum Reporting Interval (seconds)', required: false, defaultValue: 1800, range: '60..14400', description: 'Maximum time between sensor reports, even if threshold not met (default: 30 minutes)'
        input name: 'temperatureChangeThreshold', type: 'decimal', title: 'Temperature Change Threshold (°)', required: false, defaultValue: 0.3, range: '0..2', description: 'Minimum temperature change to report (default: 0.3°)'
        input name: 'humidityChangeThreshold', type: 'decimal', title: 'Humidity Change Threshold (%)', required: false, defaultValue: 1.0, range: '0..5', description: 'Minimum humidity change to report (default: 1.0%)'
        input name: 'pmChangeThreshold', type: 'decimal', title: 'PM Sensor Change Threshold (µg/m³)', required: false, defaultValue: 0.5, range: '0..2', description: 'Minimum PM concentration change to report (default: 0.5 µg/m³)'
        input name: 'co2ChangeThreshold', type: 'number', title: 'CO₂ Change Threshold (ppm)', required: false, defaultValue: 10, range: '5..50', description: 'Minimum CO₂ change to report (default: 10 ppm)'
        input name: 'pressureChangeThreshold', type: 'decimal', title: 'Pressure Change Threshold (hPa)', required: false, defaultValue: 0.5, range: '0..2', description: 'Minimum pressure change to report (default: 0.5 hPa)'
        input name: 'vocNoxChangeThreshold', type: 'number', title: 'VOC/NOx Index Change Threshold', required: false, defaultValue: 2, range: '1..10', description: 'Minimum VOC/NOx index change to report (default: 2)'
        input name: 'gasChangeThreshold', type: 'decimal', title: 'Gas Sensor Change Threshold (ppb)', required: false, defaultValue: 5.0, range: '1..20', description: 'Minimum gas concentration change to report (default: 5 ppb)'
        input name: 'advancedOptions', type: 'bool', title: '<b>Advanced Options</b>', description: 'Flip to see or hide the advanced options', defaultValue: false

        if (advancedOptions == true) {
            input name: 'password', type: 'text', title: 'Device Password <i>(if required)</i>', required: false
            input name: 'diagnosticsReporting', type: 'bool', title: 'Enable Diagnostic Attributes', required: false, defaultValue: false, description: 'Enable reporting of technical diagnostic attributes (advanced users only)'
            input name: 'sleepDuration', type: 'number', title: 'Sleep Duration (minutes)', required: false, defaultValue: 5, range: '0..800', description: 'Time between measurements when in sleep mode'
            input name: 'logWarnEnable', type: 'bool', title: 'Enable warning logging', required: false, defaultValue: false, description: '<i>Enables API Library warnings and info logging.</i>'
            
        }
    }
}

@Field static final Map<String, Map<String, Object>> ALL_ENTITIES = [
    // Primary sensors (always enabled)
    'ammonia':                             [attr: 'ammonia',                        isDiag: false, type: 'sensor',      description: 'Ammonia (NH₃) gas sensor',                      thresholdPref: 'gasChangeThreshold',        dataType: 'float'],
    'carbon_monoxide':                     [attr: 'carbonMonoxide',                 isDiag: false, type: 'sensor',      description: 'Carbon Monoxide (CO) gas sensor',               thresholdPref: 'gasChangeThreshold',        dataType: 'float'],
    'co2':                                 [attr: 'carbonDioxide',                  isDiag: false, type: 'sensor',      description: 'Carbon Dioxide (CO₂) sensor (SCD40)',           thresholdPref: 'co2ChangeThreshold',        dataType: 'integer'],
    'dps310_pressure':                     [attr: 'pressure',                       isDiag: false, type: 'pressure',    description: 'DPS310 air pressure sensor',                    thresholdPref: 'pressureChangeThreshold',   dataType: 'float'],
    'ethanol':                             [attr: 'ethanol',                        isDiag: false, type: 'sensor',      description: 'Ethanol (C₂H₅OH) gas sensor',                   thresholdPref: 'gasChangeThreshold',        dataType: 'float'],
    'hydrogen':                            [attr: 'hydrogen',                       isDiag: false, type: 'sensor',      description: 'Hydrogen (H₂) gas sensor',                      thresholdPref: 'gasChangeThreshold',        dataType: 'float'],
    'methane':                             [attr: 'methane',                        isDiag: false, type: 'sensor',      description: 'Methane (CH₄) gas sensor',                      thresholdPref: 'gasChangeThreshold',        dataType: 'float'],
    'nitrogen_dioxide':                    [attr: 'nitrogenDioxide',                isDiag: false, type: 'sensor',      description: 'Nitrogen Dioxide (NO₂) gas sensor',             thresholdPref: 'gasChangeThreshold',        dataType: 'float'],
    'pm__1_m_weight_concentration':        [attr: 'pm1',                            isDiag: false, type: 'sensor',      description: 'PM <1µm weight concentration',                   thresholdPref: 'pmChangeThreshold',         dataType: 'float'],
    'pm__2_5_m_weight_concentration':      [attr: 'pm25',                           isDiag: false, type: 'sensor',      description: 'PM <2.5µm weight concentration',                 thresholdPref: 'pmChangeThreshold',         dataType: 'float'],
    'pm__4_m_weight_concentration':        [attr: 'pm4',                            isDiag: false, type: 'sensor',      description: 'PM <4µm weight concentration',                   thresholdPref: 'pmChangeThreshold',         dataType: 'float'],
    'pm__10_m_weight_concentration':       [attr: 'pm10',                           isDiag: false, type: 'sensor',      description: 'PM <10µm weight concentration',                  thresholdPref: 'pmChangeThreshold',         dataType: 'float'],
    'sen55_humidity':                      [attr: 'humidity',                       isDiag: false, type: 'sensor',      description: 'SEN55 humidity sensor',                         thresholdPref: 'humidityChangeThreshold',   dataType: 'float'],
    'sen55_nox':                           [attr: 'sen55Nox',                       isDiag: false, type: 'sensor',      description: 'SEN55 NOX index',                               thresholdPref: 'vocNoxChangeThreshold',     dataType: 'integer'],
    'sen55_temperature':                   [attr: 'temperature',                    isDiag: false, type: 'temperature', description: 'SEN55 temperature sensor',                      thresholdPref: 'temperatureChangeThreshold', dataType: 'float'],
    'sen55_voc':                           [attr: 'sen55Voc',                       isDiag: false, type: 'sensor',      description: 'SEN55 VOC index',                               thresholdPref: 'vocNoxChangeThreshold',     dataType: 'integer'],
    'voc_quality':                         [attr: 'vocQuality',                     isDiag: false, type: 'text',        description: 'VOC quality rating'],
    
    // Detailed PM sensors (disabled by default in ESPHome)
    'pm_0_3_to_1__m':                      [attr: 'pm03To1',                        isDiag: true,  type: 'sensor',      description: 'PM 0.3 to 1 µm concentration',                  thresholdPref: 'pmChangeThreshold',         dataType: 'float'],
    'pm_1_to_2_5__m':                      [attr: 'pm1To25',                        isDiag: true,  type: 'sensor',      description: 'PM 1 to 2.5 µm concentration',                  thresholdPref: 'pmChangeThreshold',         dataType: 'float'],
    'pm_2_5_to_4__m':                      [attr: 'pm25To4',                        isDiag: true,  type: 'sensor',      description: 'PM 2.5 to 4 µm concentration',                  thresholdPref: 'pmChangeThreshold',         dataType: 'float'],
    'pm_4_to_10__m':                       [attr: 'pm4To10',                        isDiag: true,  type: 'sensor',      description: 'PM 4 to 10 µm concentration',                   thresholdPref: 'pmChangeThreshold',         dataType: 'float'],
    
    // Controls
    'rgb_light':                           [attr: 'rgbLight',                       isDiag: false, type: 'light',       description: 'RGB status light control'],
    
    // Configuration entities
    'calibrate_scd40_to_420ppm':           [attr: 'calibrateScd40',                 isDiag: true,  type: 'button',      description: 'Calibrate SCD40 CO₂ to 420ppm'],
    'clean_sen55':                         [attr: 'cleanSen55',                     isDiag: true,  type: 'button',      description: 'Clean SEN55 sensor'],
    'esp_reboot':                          [attr: 'espReboot',                      isDiag: true,  type: 'button',      description: 'ESP device reboot button'],
    'factory_reset_esp':                   [attr: 'factoryResetEsp',                isDiag: true,  type: 'button',      description: 'Factory reset ESP device button'],
    'prevent_sleep':                       [attr: 'preventSleep',                   isDiag: true,  type: 'switch',      description: 'Prevent device sleep mode switch'],
    'sen55_humidity_offset':               [attr: 'sen55HumidityOffset',            isDiag: true,  type: 'offset',      description: 'SEN55 humidity calibration offset'],
    'sen55_temperature_offset':            [attr: 'sen55TemperatureOffset',         isDiag: true,  type: 'offset',      description: 'SEN55 temperature calibration offset'],
    'sleep_duration':                      [attr: 'sleepDuration',                  isDiag: true,  type: 'config',      description: 'Device sleep duration between measurements'],
    
    // Diagnostic entities
    'esp_temperature':                     [attr: 'espTemperature',                 isDiag: true,  type: 'temperature', description: 'ESP32 chip internal temperature'],
    'online':                              [attr: 'online',                         isDiag: true,  type: 'status',      description: 'Device online status'],
    'rssi':                                [attr: 'rssi',                           isDiag: true,  type: 'signal',      description: 'WiFi signal strength indicator'],
    'uptime':                              [attr: 'uptime',                         isDiag: true,  type: 'status',      description: 'Device uptime since last restart']
]





// Lifecycle methods

public void updated() {
    checkDriverVersion(DRIVER_VERSION, DATE_TIME_STAMP, _DEBUG)
    log.info "${device} driver configuration updated"
    
    // Delete diagnostic attribute states if diagnostics reporting is disabled
    if (settings.diagnosticsReporting == false) {
        ALL_ENTITIES.each { entityId, entityInfo ->
            def attributeName = entityInfo.attr
            // Skip networkStatus and online as they're important for connection status
            if (attributeName != 'networkStatus' && entityInfo.isDiag == true) {
                device.deleteCurrentState(attributeName)
            }
        }
    }
    
    // Sync SEN55 temperature offset preference with ESPHome
    if (settings.sen55TemperatureOffset != null) {
        syncSen55TemperatureOffset()
    }
    
    // Sync SEN55 humidity offset preference with ESPHome
    if (settings.sen55HumidityOffset != null) {
        syncSen55HumidityOffset()
    }
    
    // Sync sleep duration preference with ESPHome
    if (settings.sleepDuration != null) {
        syncSleepDuration()
    }
    
    initialize()
}

private void syncSen55TemperatureOffset() {
    // Find the sen55_temperature_offset entity key
    def offsetKey = null
    state.entities?.each { key, entity ->
        if (entity.objectId == 'sen55_temperature_offset') {
            offsetKey = key as Long
        }
    }
    
    if (offsetKey == null) {
        if (logEnable) { 
            log.warn "SEN55 temperature offset entity not found - available entities: ${state.entities?.values()?.collect { it.objectId }}" 
        }
        return
    }
    
    Float offset = settings.sen55TemperatureOffset as Float
    if (txtEnable) { log.info "${device} syncing SEN55 temperature offset to ${offset}° (key: ${offsetKey})" }
    
    // Send the offset to ESPHome
    espHomeNumberCommand(key: offsetKey, state: offset)
}

private void syncSen55HumidityOffset() {
    // Find the sen55_humidity_offset entity key
    def offsetKey = null
    state.entities?.each { key, entity ->
        if (entity.objectId == 'sen55_humidity_offset') {
            offsetKey = key as Long
        }
    }
    
    if (offsetKey == null) {
        if (logEnable) { 
            log.warn "SEN55 humidity offset entity not found - available entities: ${state.entities?.values()?.collect { it.objectId }}" 
        }
        return
    }
    
    Float offset = settings.sen55HumidityOffset as Float
    if (txtEnable) { log.info "${device} syncing SEN55 humidity offset to ${offset}% (key: ${offsetKey})" }
    
    // Send the offset to ESPHome
    espHomeNumberCommand(key: offsetKey, state: offset)
}

private void syncSleepDuration() {
    // Find the sleep_duration entity key
    def durationKey = null
    state.entities?.each { key, entity ->
        if (entity.objectId == 'sleep_duration') {
            durationKey = key as Long
        }
    }
    
    if (durationKey == null) {
        if (logEnable) { 
            log.warn "Sleep duration entity not found - available entities: ${state.entities?.values()?.collect { it.objectId }}" 
        }
        return
    }
    
    Integer duration = settings.sleepDuration as Integer
    if (txtEnable) { log.info "${device} syncing sleep duration to ${duration} minutes (key: ${durationKey})" }
    
    // Send the duration to ESPHome
    espHomeNumberCommand(key: durationKey, state: duration)
}

// the parse method is invoked by the API library when messages are received
void parse(final Map message) {
    if (logEnable) { log.debug "parseAIR: ${message}" }

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
    if (message.objectId == 'calibrate_scd40_to_420ppm') {
        state.calibrateScd40Key = key
    }
    if (message.objectId == 'clean_sen55') {
        state.cleanSen55Key = key
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
        case 'prevent_sleep':
            handlePreventSleepState(message)
            break
        case 'sen55_temperature':
            handleTemperatureState(message, entity, ALL_ENTITIES)
            break
        case 'sen55_humidity':
            handleHumidityState(message, entity, ALL_ENTITIES)
            break
        case 'dps310_pressure':
            handlePressureState(message, entity)
            break
        case 'co2': 
            handleCO2State(message, entity)
            break
        case 'voc_quality': 
            handleVocQualityState(message, entity)
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
                
                // Special case: Sync offset preferences with ESPHome values
                if (objectId == 'sen55_humidity_offset') {
                    Float currentPref = settings.sen55HumidityOffset as Float
                    if (currentPref != processedValue) {
                        device.updateSetting('sen55HumidityOffset', processedValue)
                        if (txtEnable && shouldReportDiagnostic(ALL_ENTITIES, objectId, settings.diagnosticsReporting)) { 
                            log.info "SEN55 humidity offset preference synced from ESPHome to ${processedValue}%" 
                        }
                    }
                } else if (objectId == 'sen55_temperature_offset') {
                    Float currentPref = settings.sen55TemperatureOffset as Float
                    if (currentPref != processedValue) {
                        device.updateSetting('sen55TemperatureOffset', processedValue)
                        if (txtEnable && shouldReportDiagnostic(ALL_ENTITIES, objectId, settings.diagnosticsReporting)) { 
                            log.info "SEN55 temperature offset preference synced from ESPHome to ${processedValue}°" 
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
            
            // Apply universal threshold filtering
            Map reportResult = shouldReportValue(objectId, attributeName, processedValue)
            boolean shouldReport = reportResult.shouldReport
            String reportReason = reportResult.reason
            
            // Format value based on unit type
            if (unit == 'µg/m³') {  // PM sensors
                formattedValue = String.format("%.1f", processedValue)
            } else if (unit == 'ppm' && objectId != 'co2') {  // Gas sensors (not CO2)
                formattedValue = String.format("%.2f", processedValue)
            } else if (objectId.contains('voc') || objectId.contains('nox')) {  // VOC/NOx indices
                Integer indexValue = Math.round(processedValue) as Integer
                formattedValue = indexValue.toString()
            } else if (objectId.contains('humidity')) {  // Humidity sensors
                formattedValue = String.format("%.1f", processedValue)
            } else {
                formattedValue = String.format("%.0f", processedValue)  // For other indices
            }
            
            // Only send event if threshold criteria met
            if (shouldReport && shouldReportDiagnostic(ALL_ENTITIES, objectId, settings.diagnosticsReporting)) {
                String suffix = ''
                if (reportReason == 'max_interval') {
                    suffix = ' [MaxReportingInterval]'
                } else if (reportReason == 'refresh') {
                    suffix = ' [Refresh]'
                }
                
                Map eventData = [
                    name: attributeName,
                    value: (formattedValue ?: processedValue),
                    descriptionText: "${description} is ${formattedValue} ${unit}${suffix}".trim()
                ]
                
                if (unit) {
                    eventData.unit = unit
                }
                
                // Force state change when reporting due to max interval or refresh
                if (reportReason == 'max_interval' || reportReason == 'refresh') {
                    eventData.isStateChange = true
                }
                
                sendEvent(eventData)
                
                // Only log if text logging is enabled
                if (txtEnable) {
                    log.info "${description}: ${formattedValue} ${unit}${suffix}".trim()
                }
            }
            return  // Exit early to avoid duplicate processing
            
        case 'config':
            if (unit.contains('°')) {  // Temperature config
                Float tempC = rawValue as Float
                Float temp = convertTemperature(tempC)
                processedValue = temp
                formattedValue = String.format("%.1f", temp)
            } else {
                processedValue = rawValue
                formattedValue = String.format("%.0f", processedValue)
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

void calibrateScd40(String value) {
    def calibrateKey = state.calibrateScd40Key
    
    if (calibrateKey == null) {
        log.warn "Calibrate SCD40 entity not found"
        return
    }
    
    if (value == 'calibrate') {
        if (txtEnable) { log.info "${device} calibrating SCD40 CO2 sensor to 420ppm" }
        espHomeButtonCommand(key: calibrateKey)
    } else {
        log.warn "Unsupported calibrate SCD40 value: ${value}"
    }
}

void cleanSen55(String value) {
    def cleanKey = state.cleanSen55Key
    
    if (cleanKey == null) {
        log.warn "Clean SEN55 entity not found"
        return
    }
    
    if (value == 'clean') {
        if (txtEnable) { log.info "${device} cleaning SEN55 sensor" }
        espHomeButtonCommand(key: cleanKey)
    } else {
        log.warn "Unsupported clean SEN55 value: ${value}"
    }
}

private void handlePreventSleepState(Map message) {
    // For switch entities, check for 'state' directly since they don't use 'hasState'
    if (message.state != null) {
        def sleepState = message.state as Boolean
        if (shouldReportDiagnostic(ALL_ENTITIES, 'prevent_sleep', settings.diagnosticsReporting)) {
            sendEvent(name: "preventSleep", value: sleepState ? 'on' : 'off', descriptionText: "Prevent Sleep is ${sleepState ? 'on' : 'off'}")
        }
        if (txtEnable && shouldReportDiagnostic(ALL_ENTITIES, 'prevent_sleep', settings.diagnosticsReporting)) { 
            log.info "Prevent Sleep is ${sleepState ? 'on' : 'off'}" 
        }
    } else {
        if (logEnable) { log.warn "Prevent sleep message does not contain state: ${message}" }
    }
}

/**
 * Handle DPS310 pressure sensor
 * @param message state message from ESPHome
 * @param entity entity information from state.entities
 */
private void handlePressureState(Map message, Map entity) {
    if (!message.hasState) {
        return
    }
    
    String objectId = entity.objectId
    def entityInfo = getEntityInfo(ALL_ENTITIES, objectId)
    if (!entityInfo) {
        if (logEnable) { log.warn "No entity info found for objectId: ${objectId}" }
        return
    }
    
    Float pressure = message.state as Float
    String pressureStr = String.format("%.1f", pressure)
    String description = entityInfo.description
    String unit = "hPa"
    
    // Only report if threshold exceeded using universal method
    Map reportResult = shouldReportValue(objectId, 'pressure', pressure)
    if (reportResult.shouldReport) {
        String suffix = ''
        if (reportResult.reason == 'max_interval') {
            suffix = ' [MaxReportingInterval]'
        } else if (reportResult.reason == 'refresh') {
            suffix = ' [Refresh]'
        }
        boolean forceStateChange = (reportResult.reason == 'max_interval' || reportResult.reason == 'refresh')
        
        // Send pressure event for Hubitat capability compatibility
        Map eventData = [name: "pressure", value: pressureStr, unit: unit, descriptionText: "Pressure is ${pressureStr} ${unit}${suffix}"]
        if (forceStateChange) eventData.isStateChange = true
        sendEvent(eventData)
        
        if (txtEnable) { 
            log.info "${description}: ${pressureStr} ${unit}${suffix}".trim()
        }
    }
}

/**
 * Handle CO2 sensor (SCD40)
 * @param message state message from ESPHome
 * @param entity entity information from state.entities
 */
private void handleCO2State(Map message, Map entity) {
    if (!message.hasState) {
        return
    }
    
    String objectId = entity.objectId
    def entityInfo = getEntityInfo(ALL_ENTITIES, objectId)
    if (!entityInfo) {
        if (logEnable) { log.warn "No entity info found for objectId: ${objectId}" }
        return
    }
    
    Integer co2 = Math.round(message.state as Float) as Integer
    String attributeName = entityInfo.attr
    String description = entityInfo.description
    String unit = "ppm"
    
    // Only report if threshold exceeded using universal method
    Map reportResult = shouldReportValue(objectId, attributeName, co2)
    if (reportResult.shouldReport) {
        String suffix = ''
        if (reportResult.reason == 'max_interval') {
            suffix = ' [MaxReportingInterval]'
        } else if (reportResult.reason == 'refresh') {
            suffix = ' [Refresh]'
        }
        boolean forceStateChange = (reportResult.reason == 'max_interval' || reportResult.reason == 'refresh')
        
        // Send individual sensor event
        Map eventData1 = [name: attributeName, value: co2, unit: unit, descriptionText: "${description} is ${co2} ${unit}${suffix}"]
        if (forceStateChange) eventData1.isStateChange = true
        sendEvent(eventData1)
        
        // Also update main carbonDioxide attribute for Hubitat capability compatibility
        Map eventData2 = [name: "carbonDioxide", value: co2, unit: unit, descriptionText: "Carbon Dioxide is ${co2} ${unit}${suffix}"]
        if (forceStateChange) eventData2.isStateChange = true
        sendEvent(eventData2)
        
        if (txtEnable) { 
            log.info "${description}: ${co2} ${unit}${suffix}".trim()
        }
    }
}

/**
 * Handle VOC quality text sensor
 * @param message state message from ESPHome
 * @param entity entity information from state.entities
 */
private void handleVocQualityState(Map message, Map entity) {
    if (!message.hasState) {
        return
    }
    
    String objectId = entity.objectId
    def entityInfo = getEntityInfo(ALL_ENTITIES, objectId)
    if (!entityInfo) {
        if (logEnable) { log.warn "No entity info found for objectId: ${objectId}" }
        return
    }
    
    String vocQuality = message.state as String
    String attributeName = entityInfo.attr
    String description = entityInfo.description
    
    // Get the previous VOC quality value
    def currentVocState = device.currentState(attributeName)
    String previousValue = currentVocState?.value
    
    // Only send event and log if the value has changed
    if (previousValue != vocQuality) {
        sendEvent(name: attributeName, value: vocQuality, descriptionText: "${description} is ${vocQuality}")
        
        if (txtEnable) { 
            log.info "${description} is ${vocQuality}" 
        }
    }
}

// Add these methods before the existing handler methods:

/**
 * Universal threshold checking method that uses preference names from ALL_ENTITIES
 * @param objectId The ESPHome object ID to get threshold info for
 * @param attributeName The Hubitat attribute name
 * @param newValue The new sensor value
 * @return Map with 'shouldReport' boolean and 'reason' string
 */
private Map shouldReportValue(String objectId, String attributeName, def newValue) {
    // Check if we're in refresh mode - if so, always report
    if (isInRefreshMode()) {
        if (logEnable) log.debug "shouldReportValue(${objectId}): Refresh mode active"
        return [shouldReport: true, reason: 'refresh']
    }
    
    def entityInfo = ALL_ENTITIES[objectId]
    if (!entityInfo?.thresholdPref) {
        if (logEnable) log.debug "shouldReportValue(${objectId}): No threshold defined"
        return [shouldReport: true, reason: 'no_threshold']  // No threshold defined, always report
    }
    
    def currentState = device.currentState(attributeName)
    if (!currentState?.value) {
        if (logEnable) log.debug "shouldReportValue(${objectId}): First value"
        return [shouldReport: true, reason: 'first_value']  // No previous value, always report first value
    }
    
    // Get threshold from preferences using the preference name
    def threshold = settings[entityInfo.thresholdPref]
    if (!threshold) {
        if (logEnable) log.debug "shouldReportValue(${objectId}): No threshold configured"
        return [shouldReport: true, reason: 'no_threshold_set']  // No threshold set, always report
    }
    
    def previousValue = currentState.value
    def change
    
    // Calculate change based on data type
    if (entityInfo.dataType == 'integer') {
        Integer newVal = Math.round(newValue as Float) as Integer
        Integer prevVal = previousValue as Integer
        change = Math.abs(newVal - prevVal)
        threshold = threshold as Integer
        //if (logEnable) log.debug "shouldReportValue(${objectId}): Δ${change} vs threshold ${threshold} (${newVal}←${prevVal})"
    } else {
        Float newVal = newValue as Float
        Float prevVal = previousValue as Float
        change = Math.abs(newVal - prevVal)
        threshold = threshold as Float
        //if (logEnable) log.debug "shouldReportValue(${objectId}): Δ${String.format('%.3f', change)} vs threshold ${threshold} (${String.format('%.3f', newVal)}←${String.format('%.3f', prevVal)})"
    }
    
    boolean thresholdMet = change >= threshold
    boolean maxTimeMet = hasMaxTimeElapsed(attributeName)
    
    // Report if threshold met OR maximum time elapsed
    boolean shouldReport = thresholdMet || maxTimeMet
    String reason
    
    if (shouldReport) {
        if (thresholdMet) {
            reason = 'threshold'
        } else if (maxTimeMet) {
            reason = 'max_interval'
        }
    }
    
    // Only log when reporting or when explicitly debugging thresholds
    if (logEnable && (shouldReport || change > (threshold * 0.5))) {
        log.debug "shouldReportValue(${objectId}): ${shouldReport ? 'REPORTING' : 'suppressed'} - ${reason ?: 'below threshold'}"
    }
    
    return [shouldReport: shouldReport, reason: reason]
}

/**
 * Check if maximum time has elapsed for an attribute (force reporting even if threshold not met)
 */
private boolean hasMaxTimeElapsed(String attributeName) {
    if (!state.lastReported) state.lastReported = [:]
    
    Long lastTime = state.lastReported[attributeName] as Long
    Long maxInterval = (settings.maxReportingInterval ?: 1800) * 1000
    Long currentTime = now()
    
    if (!lastTime || (currentTime - lastTime) >= maxInterval) {
        //if (logEnable) log.debug "hasMaxTimeElapsed(${attributeName}): Max time reached (${Math.round((currentTime - (lastTime ?: 0))/1000)}s)"
        state.lastReported[attributeName] = currentTime
        return true
    }
    // Only log when close to max time (last 10 seconds) to reduce spam
    if (logEnable && (currentTime - lastTime) > (maxInterval - 10000)) {
        //log.debug "hasMaxTimeElapsed(${attributeName}): ${Math.round((maxInterval - (currentTime - lastTime))/1000)}s remaining"
    }
    return false
}

// Put this line at the end of the driver to include the ESPHome API library helper

#include esphome.espHomeApiHelperKKmod
#include apollo.espHomeApolloLibraryCommon
