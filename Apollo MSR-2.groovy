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
 *  ver. 1.0.0  2022-07-01 kkossev  - first beta version
 *  ver. 1.2.0  2025-08-11 kkossev  - major refactoring using AIR-1 pattern; added thresholds; common library integration
 * 
 *                         TODO:
*/

import groovy.transform.Field

@Field static final Boolean _DEBUG = true
@Field static final String DRIVER_VERSION =  '1.2.0'
@Field static final String DATE_TIME_STAMP = '08/11/2025 7:59 PM'


metadata {
    definition(
        name: 'ESPHome Apollo MSR-2',
        namespace: 'apollo',
        author: 'Krassimir Kossev',
        singleThreaded: true,
        importUrl: 'https://raw.githubusercontent.com/kkossev/Hubitat-ESPHome-Apollo/refs/heads/main/Apollo%20MSR-2.groovy') {

        capability 'IlluminanceMeasurement'
        capability 'MotionSensor'
        capability 'PressureMeasurement'
        capability 'TemperatureMeasurement'
        capability 'Sensor'
        capability 'Refresh'
        capability 'SignalStrength'
        capability 'Initialize'

        // Primary motion and environmental sensors
        attribute 'mmwave', 'enum', ['active', 'not active']
        attribute 'pir', 'enum', ['active', 'not active']
        attribute 'radarTarget', 'string'
        attribute 'radarStillTarget', 'string'
        
        // Radar zones
        attribute 'radarZone1Occupancy', 'enum', ['active', 'inactive']
        attribute 'radarZone2Occupancy', 'enum', ['active', 'inactive']
        attribute 'radarZone3Occupancy', 'enum', ['active', 'inactive']
        
        // Distance sensors (diagnostic)
        attribute 'radarMovingDistance', 'number'
        attribute 'radarStillDistance', 'number'
        
        // Device controls
        attribute 'rgbLight', 'enum', ['on', 'off']
        
        // Diagnostic attributes
        attribute 'boardTemperature', 'number'
        attribute 'espTemperature', 'number'
        attribute 'uptime', 'string'

        command 'restart', [[name:'Restart device', type: 'ENUM', constraints: ['restart']]]
    }

    preferences {
        input name: 'logEnable', type: 'bool', title: 'Enable Debug Logging', required: false, defaultValue: false
        input name: 'txtEnable', type: 'bool', title: 'Enable descriptionText logging', required: false, defaultValue: true
        input name: 'ipAddress', type: 'text', title: 'Device IP Address', required: true
        
        input name: 'maxReportingInterval', type: 'number', title: 'Maximum Reporting Interval (seconds)', required: false, defaultValue: 1800, range: '60..14400', description: 'Maximum time between sensor reports, even if threshold not met (default: 30 minutes)'
        input name: 'temperatureChangeThreshold', type: 'decimal', title: 'Temperature Change Threshold (°)', required: false, defaultValue: 0.5, range: '0..2', description: 'Minimum temperature change to report (default: 0.5°)'
        input name: 'pressureChangeThreshold', type: 'decimal', title: 'Pressure Change Threshold (hPa)', required: false, defaultValue: 0.5, range: '0..2', description: 'Minimum pressure change to report (default: 0.5 hPa)'
        input name: 'illuminanceChangeThreshold', type: 'number', title: 'Illuminance Change Threshold (lx)', required: false, defaultValue: 10, range: '1..100', description: 'Minimum illuminance change to report (default: 10 lx)'
        input name: 'distanceChangeThreshold', type: 'number', title: 'Distance Change Threshold (cm)', required: false, defaultValue: 5, range: '1..50', description: 'Minimum distance change to report (default: 5 cm)'
        
        input name: 'advancedOptions', type: 'bool', title: '<b>Advanced Options</b>', description: 'Flip to see or hide the advanced options', defaultValue: false

        if (advancedOptions == true) {
            input name: 'password', type: 'text', title: 'Device Password <i>(if required)</i>', required: false
            input name: 'diagnosticsReporting', type: 'bool', title: 'Enable Diagnostic Attributes', required: false, defaultValue: false, description: 'Enable reporting of technical diagnostic attributes (advanced users only)'
            input name: 'distanceReporting', type: 'bool', title: 'Distance Reporting', required: false, defaultValue: false, description: 'Enable distance reporting from radar sensors (disable if not used in automations)'
            input name: 'logWarnEnable', type: 'bool', title: 'Enable warning logging', required: false, defaultValue: false, description: '<i>Enables API Library warnings and info logging.</i>'
        }
    }
}

@Field static final Map<String, Map<String, Object>> ALL_ENTITIES = [
    // Primary sensors (always enabled)
    'dps310_pressure':                     [attr: 'pressure',                       isDiag: false, type: 'pressure',    description: 'DPS310 air pressure sensor',                    thresholdPref: 'pressureChangeThreshold',   dataType: 'float'],
    'dps310_temperature':                  [attr: 'boardTemperature',               isDiag: true,  type: 'temperature', description: 'DPS310 board temperature sensor',               thresholdPref: 'temperatureChangeThreshold', dataType: 'float'],
    'ltr390_light':                        [attr: 'illuminance',                    isDiag: false, type: 'sensor',      description: 'LTR390 illuminance sensor',                     thresholdPref: 'illuminanceChangeThreshold', dataType: 'integer'],
    'radar_detection_distance':            [attr: 'radarMovingDistance',            isDiag: true,  type: 'sensor',      description: 'Radar moving target detection distance',        thresholdPref: 'distanceChangeThreshold',   dataType: 'integer'],
    'radar_still_distance':                [attr: 'radarStillDistance',             isDiag: true,  type: 'sensor',      description: 'Radar still target detection distance',         thresholdPref: 'distanceChangeThreshold',   dataType: 'integer'],
    'radar_target':                        [attr: 'radarTarget',                    isDiag: false, type: 'text',        description: 'Radar moving target detection status'],
    'radar_still_target':                  [attr: 'radarStillTarget',               isDiag: false, type: 'text',        description: 'Radar still target detection status'],
    'radar_zone_1_occupancy':              [attr: 'radarZone1Occupancy',            isDiag: false, type: 'binary',      description: 'Radar zone 1 occupancy detection'],
    'radar_zone_2_occupancy':              [attr: 'radarZone2Occupancy',            isDiag: false, type: 'binary',      description: 'Radar zone 2 occupancy detection'],
    'radar_zone_3_occupancy':              [attr: 'radarZone3Occupancy',            isDiag: false, type: 'binary',      description: 'Radar zone 3 occupancy detection'],
    
    // Controls
    'rgb_light':                           [attr: 'rgbLight',                       isDiag: false, type: 'light',       description: 'RGB status light control'],
    
    // Configuration entities
    'restart':                             [attr: 'restart',                        isDiag: true,  type: 'button',      description: 'Device restart button'],
    
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
    
    // Delete distance attributes if distance reporting is disabled
    if (settings.distanceReporting == false) {
        device.deleteCurrentState('radarMovingDistance')
        device.deleteCurrentState('radarStillDistance')
    }
    
    initialize()
}

// called from updated() method after 5 seconds
void configure() {
    if (logEnable) { log.debug "${device} configure()" }
}

// the parse method is invoked by the API library when messages are received
void parse(final Map message) {
    if (logEnable) { log.debug "parseMSR: ${message}" }

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
    if (message.objectId == 'restart') {
        state.restartKey = key
    }
}

// Driver-specific entity state handler
void handleEntityState(Map message, Map entity, String objectId) {
    // Handle special cases that need custom logic
    switch (objectId) {
        case 'rgb_light':
            handleRgbLightState(message)
            break
        case 'dps310_temperature':
            handleBoardTemperatureState(message, entity)
            break
        case 'dps310_pressure':
            handlePressureState(message, entity)
            break
        case 'ltr390_light':
            handleIlluminanceState(message, entity)
            break
        case 'radar_target':
            handleRadarTargetState(message, entity)
            break
        case 'radar_still_target':
            handleRadarStillTargetState(message, entity)
            break
        case 'radar_detection_distance':
        case 'radar_still_distance':
            handleDistanceState(message, entity, objectId)
            break
        case 'radar_zone_1_occupancy':
        case 'radar_zone_2_occupancy':
        case 'radar_zone_3_occupancy':
            handleZoneOccupancyState(message, entity, objectId)
            break
        default:
            // Use common handler for most entities
            handleGenericEntityState(message, entity)
            break
    }
}

// ========================================
// Commands
// ========================================

/**
 * Clear the refresh request flag
 */
private void clearRefreshFlag() {
    state.remove('refreshRequested')
}

/**
 * Restart the ESPHome device
 */
void restart() {
    log.info "Restarting ESPHome device"
    
    // Construct restart message  
    Map message = [:]
    message.put("restart_request", [:])
    
    try {
        sendMessage(message)
        if (txtEnable) { log.info "Restart command sent to device" }
    } catch (Exception e) {
        log.error "Failed to send restart command: ${e.message}"
    }
}

// ========================================
// Threshold Management
// ========================================

/**
 * Universal threshold checking for all sensors
 * @param objectId sensor object ID
 * @param attributeName attribute name for the sensor
 * @param currentValue current sensor value
 * @return Map with shouldReport (boolean) and reason (string)
 */
private Map shouldReportValue(String objectId, String attributeName, def currentValue) {
    // Always report during refresh
    if (state.refreshRequested) {
        return [shouldReport: true, reason: 'refresh']
    }
    
    // Get entity configuration
    def entityConfig = ALL_ENTITIES[objectId]
    if (!entityConfig) {
        if (logEnable) { log.warn "No configuration found for objectId: ${objectId}" }
        return [shouldReport: true, reason: 'no_config']
    }
    
    // Get threshold preference name
    String thresholdPref = entityConfig.thresholdPref
    if (!thresholdPref) {
        if (logEnable) { log.warn "No threshold preference configured for objectId: ${objectId}" }
        return [shouldReport: true, reason: 'no_threshold']
    }
    
    // Get threshold value from settings
    def thresholdValue = settings[thresholdPref]
    if (thresholdValue == null) {
        if (logEnable) { log.debug "Threshold preference ${thresholdPref} not set, reporting all changes for ${objectId}" }
        return [shouldReport: true, reason: 'threshold_not_set']
    }
    
    // Initialize tracking maps if they don't exist
    if (!state.lastValues) state.lastValues = [:]
    if (!state.lastRefreshTimes) state.lastRefreshTimes = [:]
    
    // Get last value and time
    def lastValue = state.lastValues[objectId]
    Long lastTime = state.lastRefreshTimes[objectId] ?: 0
    Long currentTime = now()
    
    // Check if we should report based on max interval (in minutes)
    boolean maxTimeElapsed = hasMaxTimeElapsed(objectId, currentTime, lastTime)
    
    boolean shouldReport = false
    String reason = ''
    
    if (lastValue == null) {
        // First reading
        shouldReport = true
        reason = 'first_reading'
    } else if (maxTimeElapsed) {
        // Max time elapsed
        shouldReport = true
        reason = 'max_interval'
    } else {
        // Check threshold
        def valueDiff = Math.abs((currentValue as Double) - (lastValue as Double))
        if (valueDiff >= (thresholdValue as Double)) {
            shouldReport = true
            reason = 'threshold_exceeded'
        }
    }
    
    // Update tracking if we're reporting
    if (shouldReport) {
        state.lastValues[objectId] = currentValue
        state.lastRefreshTimes[objectId] = currentTime
    }
    
    if (logEnable && !shouldReport) {
        log.debug "Threshold not met for ${objectId}: current=${currentValue}, last=${lastValue}, threshold=${thresholdValue}, diff=${Math.abs((currentValue as Double) - (lastValue as Double))}"
    }
    
    return [shouldReport: shouldReport, reason: reason]
}

/**
 * Check if maximum reporting interval has elapsed
 * @param objectId sensor object ID
 * @param currentTime current timestamp
 * @param lastTime last report timestamp
 * @return true if max interval elapsed
 */
private boolean hasMaxTimeElapsed(String objectId, Long currentTime, Long lastTime) {
    def maxInterval = settings.maxReportingInterval
    if (!maxInterval || maxInterval <= 0) {
        return false  // No max interval set
    }
    
    Long intervalMs = (maxInterval as Long) * 60 * 1000  // Convert minutes to milliseconds
    return (currentTime - lastTime) >= intervalMs
}

// ========================================
// Helper Functions  
// ========================================

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
            
        case 'sensor':
            processedValue = rawValue as Float
            
            // Apply universal threshold filtering for distance sensors only if distance reporting is enabled
            if (objectId.contains('distance') && !settings.distanceReporting) {
                if (logEnable) { log.warn "Distance reporting is disabled, ignoring ${objectId}" }
                return
            }
            
            // Apply universal threshold filtering
            Map reportResult = shouldReportValue(objectId, attributeName, processedValue)
            boolean shouldReport = reportResult.shouldReport
            String reportReason = reportResult.reason
            
            // Format value based on unit type
            if (unit == 'cm') {  // Distance sensors
                formattedValue = String.format("%.0f", processedValue)
            } else if (unit == 'lx') {  // Illuminance
                formattedValue = String.format("%.0f", processedValue)
            } else {
                formattedValue = String.format("%.1f", processedValue)
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

/**
 * Handle DPS310 board temperature
 * @param message state message from ESPHome
 * @param entity entity information from state.entities
 */
private void handleBoardTemperatureState(Map message, Map entity) {
    if (!message.hasState) return
    if (!shouldReportDiagnostic(ALL_ENTITIES, entity.objectId, settings.diagnosticsReporting)) return
    
    Float tempC = message.state as Float
    Float temp = convertTemperature(tempC)
    String tempStr = String.format("%.1f", temp)
    String unit = location.temperatureScale
    
    sendEvent(name: "boardTemperature", value: tempStr, unit: unit, descriptionText: "Board Temperature is ${tempStr}°${unit}")
    if (txtEnable) { log.info "Board Temperature: ${tempStr}°${unit}" }
}

/**
 * Handle DPS310 pressure sensor
 * @param message state message from ESPHome
 * @param entity entity information from state.entities
 */
private void handlePressureState(Map message, Map entity) {
    if (!message.hasState) return
    
    String objectId = entity.objectId
    def entityInfo = getEntityInfo(ALL_ENTITIES, objectId)
    if (!entityInfo) return
    
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
 * Handle LTR390 illuminance sensor
 * @param message state message from ESPHome
 * @param entity entity information from state.entities
 */
private void handleIlluminanceState(Map message, Map entity) {
    if (!message.hasState) return
    
    String objectId = entity.objectId
    def entityInfo = getEntityInfo(ALL_ENTITIES, objectId)
    if (!entityInfo) return
    
    Integer illuminance = message.state as Integer
    String description = entityInfo.description
    String unit = "lx"
    
    // Only report if threshold exceeded using universal method
    Map reportResult = shouldReportValue(objectId, 'illuminance', illuminance)
    if (reportResult.shouldReport) {
        String suffix = ''
        if (reportResult.reason == 'max_interval') {
            suffix = ' [MaxReportingInterval]'
        } else if (reportResult.reason == 'refresh') {
            suffix = ' [Refresh]'
        }
        boolean forceStateChange = (reportResult.reason == 'max_interval' || reportResult.reason == 'refresh')
        
        // Send illuminance event for Hubitat capability compatibility
        Map eventData = [name: "illuminance", value: illuminance, unit: unit, descriptionText: "Illuminance is ${illuminance} ${unit}${suffix}"]
        if (forceStateChange) eventData.isStateChange = true
        sendEvent(eventData)
        
        if (txtEnable) { 
            log.info "${description}: ${illuminance} ${unit}${suffix}".trim()
        }
    }
}

/**
 * Handle radar target detection
 * @param message state message from ESPHome
 * @param entity entity information from state.entities
 */
private void handleRadarTargetState(Map message, Map entity) {
    if (!message.hasState) return
    
    String target = message.state as String
    sendEvent(name: "radarTarget", value: target, descriptionText: "Radar moving target detection is ${target}")
    
    // Update motion sensor based on radar target
    String motionValue = (target == 'true') ? 'active' : 'inactive'
    sendEvent(name: "motion", value: motionValue, descriptionText: "Motion is ${motionValue}")
    sendEvent(name: "mmwave", value: motionValue, descriptionText: "mmWave is ${motionValue}")
    
    if (txtEnable) { 
        log.info "Radar moving target: ${target}, Motion: ${motionValue}"
    }
}

/**
 * Handle radar still target detection
 * @param message state message from ESPHome
 * @param entity entity information from state.entities
 */
private void handleRadarStillTargetState(Map message, Map entity) {
    if (!message.hasState) return
    
    String stillTarget = message.state as String
    sendEvent(name: "radarStillTarget", value: stillTarget, descriptionText: "Radar still target detection is ${stillTarget}")
    
    if (txtEnable) { 
        log.info "Radar still target: ${stillTarget}"
    }
}

/**
 * Handle distance sensors (moving and still)
 * @param message state message from ESPHome
 * @param entity entity information from state.entities
 * @param objectId the object ID for the sensor
 */
private void handleDistanceState(Map message, Map entity, String objectId) {
    if (!message.hasState) return
    if (!settings.distanceReporting) {
        if (logEnable) { log.warn "Distance reporting is disabled, ignoring ${objectId}" }
        return
    }
    
    // Use generic handler which includes threshold checking
    handleGenericEntityState(message, entity)
}

/**
 * Handle radar zone occupancy
 * @param message state message from ESPHome
 * @param entity entity information from state.entities
 * @param objectId the object ID for the zone
 */
private void handleZoneOccupancyState(Map message, Map entity, String objectId) {
    if (!message.hasState) return
    
    def entityInfo = getEntityInfo(ALL_ENTITIES, objectId)
    if (!entityInfo) return
    
    boolean occupancy = message.state as Boolean
    String attributeName = entityInfo.attr
    String description = entityInfo.description
    String value = occupancy ? 'active' : 'inactive'
    
    sendEvent(name: attributeName, value: value, descriptionText: "${description} is ${value}")
    
    if (txtEnable) { 
        log.info "${description}: ${value}"
    }
}


/**
 * Update the specified device attribute with the specified value and log if changed
 * @param attribute name of the attribute
 * @param value value of the attribute
 * @param unit unit of the attribute
 * @param type type of the attribute
 */
private void updateAttribute(final String attribute, final Object value, final String unit = null, final String type = null) {
    final String descriptionText = "${attribute} was set to ${value}${unit ?: ''}"
    if (device.currentValue(attribute) != value && settings.txtEnable) {
        if (txtEnable) { log.info descriptionText }
    }
    sendEvent(name: attribute, value: value, unit: unit, type: type, descriptionText: descriptionText)
}

void test() {
    log.info "${device} test command executed"
    def ssstate = state.parseEntities
    //log.trace "Test command: state.entities = ${state.entities}"
    Long key = 68806113 // Example key, replace with actual key if needed
    log.trace "Test command: key = ${key}"
    def entity = state.entities["$key"]
    if (entity) {
        log.info "Entity with key ${key} found: ${entity}"

    } else {
        log.warn "Entity with key ${key} not found"
    }

   

    if (!isNullOrEmpty(state.entities["$key"])) {
        log.info "Entity with key ${key} found: ${state.entities["$key"]}"
    } else {
        log.warn "Entity with key ${key} not found"
    }
}

// Put this line at the end of the driver to include the ESPHome API library helper
#include esphome.espHomeApiHelperKKmod
#include apollo.espHomeApolloLibraryCommon
