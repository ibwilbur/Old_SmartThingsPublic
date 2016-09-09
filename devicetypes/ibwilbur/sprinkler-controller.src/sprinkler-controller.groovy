/**
 *  Sprinkler Controller
 *
 *  Copyright 2016 Will Shelton
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
    
	definition (name: "Sprinkler Controller", namespace: "ibwilbur", author: "Will Shelton") {
		capability "Momentary"
		capability "Switch"

		attribute "effect", "string"

		command "setRuntimeParams"
		command "RelayOn1"
		command "RelayOn1For"
		command "RelayOff1"
		command "RelayOn2"
		command "RelayOn2For"
		command "RelayOff2"        
	}


	simulator {
		// TODO: define status and reply messages here
	}

	tiles {
        standardTile("allZonesTile", "device.switch", width: 1, height: 1, canChangeIcon: true, canChangeBackground: true) {
            state "off", label: 'Start', action: "switch.on", icon: "st.Outdoor.outdoor12", backgroundColor: "#ffffff", nextState: "starting"
            state "on", label: 'Running', action: "switch.off", icon: "st.Health & Wellness.health7", backgroundColor: "#53a7c0", nextState: "stopping"
            state "starting", label: 'Starting...', action: "switch.off", icon: "st.Health & Wellness.health7", backgroundColor: "#53a7c0"
            state "stopping", label: 'Stopping...', action: "switch.off", icon: "st.Health & Wellness.health7", backgroundColor: "#53a7c0"
            state "rainDelayed", label: 'Rain Delay', action: "switch.off", icon: "st.Weather.weather10", backgroundColor: "#fff000", nextState: "off"
        	state "warning", label: 'Issue',  icon: "st.Health & Wellness.health7", backgroundColor: "#ff000f", nextState: "off"
        }
        
        standardTile("zoneOneTile", "device.zoneOne", width: 1, height: 1, canChangeIcon: true, canChangeBackground: true) {
            state "off1", label: 'One', action: "RelayOn1", icon: "st.Outdoor.outdoor12", backgroundColor: "#ffffff",nextState: "sending1"
            state "sending1", label: 'sending', action: "RelayOff1", icon: "st.Health & Wellness.health7", backgroundColor: "#cccccc"
            state "q1", label: 'One', action: "RelayOff1",icon: "st.Outdoor.outdoor12", backgroundColor: "#c0a353", nextState: "sending1"
            state "on1", label: 'One', action: "RelayOff1",icon: "st.Outdoor.outdoor12", backgroundColor: "#53a7c0", nextState: "sending1"
        }
        
		standardTile("zoneTwoTile", "device.zoneTwo", width: 1, height: 1, canChangeIcon: true, canChangeBackground: true) {
            state "off2", label: 'Two', action: "RelayOn2", icon: "st.Outdoor.outdoor12", backgroundColor: "#ffffff", nextState: "sending2"
            state "sending2", label: 'sending', action: "RelayOff2", icon: "st.Health & Wellness.health7", backgroundColor: "#cccccc"
            state "q2", label: 'Two', action: "RelayOff2",icon: "st.Outdoor.outdoor12", backgroundColor: "#c0a353", nextState: "sending2"
            state "on2", label: 'Two', action: "RelayOff2",icon: "st.Outdoor.outdoor12", backgroundColor: "#53a7c0", nextState: "sending2"
        }
        
		standardTile("refreshTile", "device.refresh", width: 1, height: 1, canChangeIcon: true, canChangeBackground: true, decoration: "flat") {
            state "ok", label: "", action: "update", icon: "st.secondary.refresh", backgroundColor: "#ffffff"
        }
        
        standardTile("scheduleEffect", "device.effect", width: 1, height: 1) {
            state("noEffect", label: "Normal", action: "skip", icon: "st.Office.office7", backgroundColor: "#ffffff")
            state("skip", label: "Skip 1X", action: "expedite", icon: "st.Office.office7", backgroundColor: "#c0a353")
            state("expedite", label: "Expedite", action: "onHold", icon: "st.Office.office7", backgroundColor: "#53a7c0")
            state("onHold", label: "Pause", action: "noEffect", icon: "st.Office.office7", backgroundColor: "#bc2323")
        }  
        
		main "allZonesTile"
		details(["zoneOneTile","zoneTwoTile","scheduleEffect","refreshTile"])
	}
}

// parse events into attributescon
def parse(String description) {
	log.debug "Parsing '${description}'"
	// TODO: handle 'switch' attribute
	// TODO: handle 'effect' attribute
}

def setRuntimeParams(controlUrl, serviceType, targetHost) {
	state.controlurl = controlUrl
    log.trace "Control URL set to: ${state.controlurl}"
    
    state.serviceType = serviceType
    log.trace "ServiceType set to: ${state.serviceType}"
    
    state.targetHost = targetHost
    log.trace "TargetHost set to: ${state.targetHost}"
}

def RelayOn1() {
    log.info "Executing 'on,1'"
    doAction("turnOnZone", [zone: 1])
}

def RelayOn1For(value) {
    value = checkTime(value)
    log.info "Executing 'on,1,$value'"
    sendCommand("on", 1, value)
}

def RelayOff1() {
    log.info "Executing 'off,1'"
    doAction("turnOffZone", [zone: 1])
}

def RelayOn2() {
    log.info "Executing 'on,2'"
    doAction("turnOnZone", [zone: 2])
}

def RelayOn2For(value) {
    value = checkTime(value)
    log.info "Executing 'on,2,$value'"
    sendCommand("on", 2, value)
}

def RelayOff2() {
    log.info "Executing 'off,2'"
    doAction("turnOffZone", [zone: 2])
}

def doAction(action, body) {
	def result = new physicalgraph.device.HubSoapAction(
    	path:		"${state.controlurl}",
        urn:		"${state.serviceType}",
        action:		action,
        body:		body,
        headers:	[HOST: "${state.targetHost}:8080", CONNECTION: "close"]
    )
    //log.debug result
    return result
}

def on() {
    log.info "Executing 'allOn'"
    zigbee.smartShield(text: "allOn,${oneTimer ?: 0},${twoTimer ?: 0},${threeTimer ?: 0},${fourTimer ?: 0},${fiveTimer ?: 0},${sixTimer ?: 0},${sevenTimer ?: 0},${eightTimer ?: 0}").format()
}

def OnWithZoneTimes(value) {
    log.info "Executing 'allOn' with zone times [$value]"
    def evt = createEvent(name: "switch", value: "starting", displayed: true)
    sendEvent(evt)
    
	def zoneTimes = [:]
    for(z in value.split(",")) {
    	def parts = z.split(":")
        zoneTimes[parts[0].toInteger()] = parts[1]
        log.info("Zone ${parts[0].toInteger()} on for ${parts[1]} minutes")
    }
    zigbee.smartShield(text: "allOn,${checkTime(zoneTimes[1]) ?: 0},${checkTime(zoneTimes[2]) ?: 0},${checkTime(zoneTimes[3]) ?: 0},${checkTime(zoneTimes[4]) ?: 0},${checkTime(zoneTimes[5]) ?: 0},${checkTime(zoneTimes[6]) ?: 0},${checkTime(zoneTimes[7]) ?: 0},${checkTime(zoneTimes[8]) ?: 0}").format()
}

def off() {
    log.info "Executing 'allOff'"
    zigbee.smartShield(text: "allOff").format()
}

def checkTime(t) {
	def time = (t ?: 0).toInteger()
    time > 60 ? 60 : time
}

def update() {
    log.info "Executing refresh"
    zigbee.smartShield(text: "update").format()
}

def rainDelayed() {
    log.info "rain delayed"
    if(device.currentValue("switch") != "on") {
        sendEvent (name:"switch", value:"rainDelayed", displayed: true)
    }
}

def warning() {
    log.info "Warning: Programmed Irrigation Did Not Start"
    if(device.currentValue("switch") != "on") {
        sendEvent (name:"switch", value:"warning", displayed: true)
    }
}

def push() {
    log.info "advance to next zone"
    zigbee.smartShield(text: "advance").format()  //turn off currently running zone and advance to next
}

def	skip() {
    def evt = createEvent(name: "effect", value: "skip", displayed: true)
    log.info("Sending: $evt")
    sendEvent(evt)
}

// over-ride rain delay and water even if it rains
def	expedite() {
    def evt = createEvent(name: "effect", value: "expedite", displayed: true)
    log.info("Sending: $evt")
    sendEvent(evt)
}

// schedule operates normally
def	noEffect() {
    def evt = createEvent(name: "effect", value: "noEffect", displayed: true)
    log.info("Sending: $evt")
    sendEvent(evt)
}

// turn schedule off indefinitely
def	onHold() {
    def evt = createEvent(name: "effect", value: "onHold", displayed: true)
    log.info("Sending: $evt")
    sendEvent(evt)
}

private getCallbackAddress() {
    return device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")
}

/* Helper functions to get the network device ID */
private String NetworkDeviceId(){
    def iphex = convertIPtoHex(settings.ip).toUpperCase()
    def porthex = convertPortToHex(settings.port)
    log.debug "${iphex}:${porthex}.irrigation"
    return "${iphex}:${porthex}.irrigation" 
}

private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
    //log.debug "IP address entered is $ipAddress and the converted hex code is $hex"
    return hex

}

private String convertPortToHex(port) {
    String hexport = port.toString().format( '%04x', port.toInteger() )
    //log.debug hexport
    return hexport
}
