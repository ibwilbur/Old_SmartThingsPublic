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
		capability "Actuator"
        capability "Refresh"
        capability "Switch"
        
        command "Zone1On"
        command "Zone1Off"
        command "Zone1OnFor"
        command "Zone2On"
        command "Zone2Off"
        command "Zone2OnFor"        
	}

	preferences {
		section("Configuration") {
			input "ip", "text", "title": "IP Address", multiple: false, required: false
			input "port", "text", "title": "Port", multiple: false, required: false
            input "uuid", "text", "title": "UUID", multiple: false, required: false
		}   
	}
    
	simulator {}

	tiles {
        standardTile("allZonesTile", "device.switch", width: 1, height: 1, canChangeIcon: true, canChangeBackground: true) {
            state "off", label: 'Start', action: "switch.on", icon: "st.Outdoor.outdoor12", backgroundColor: "#ffffff", nextState: "starting"
            state "on", label: 'Running', action: "switch.off", icon: "st.Health & Wellness.health7", backgroundColor: "#53a7c0", nextState: "stopping"
            state "starting", label: 'Starting', action: "switch.off", icon: "st.Health & Wellness.health7", backgroundColor: "#53a7c0"
            state "stopping", label: 'Stopping', action: "switch.off", icon: "st.Health & Wellness.health7", backgroundColor: "#53a7c0"
            state "rainDelayed", label: 'Rain Delay', action: "switch.off", icon: "st.Weather.weather10", backgroundColor: "#fff000", nextState: "off"
        	state "warning", label: 'Issue',  icon: "st.Health & Wellness.health7", backgroundColor: "#ff000f", nextState: "off"
        }
        
        standardTile("zoneOneTile", "device.zoneOne", width: 1, height: 1, canChangeIcon: true, canChangeBackground: true) {
            state "off", label: 'One', action: "Zone1On", icon: "st.Outdoor.outdoor12", backgroundColor: "#ffffff", nextState: "sending"
            state "sending", label: 'sending', action: "Zone1Off", icon: "st.Health & Wellness.health7", backgroundColor: "#cccccc"
            state "queued", label: 'One', action: "Zone1Off",icon: "st.Outdoor.outdoor12", backgroundColor: "#c0a353", nextState: "sending"
            state "on", label: 'One', action: "Zone1Off",icon: "st.Outdoor.outdoor12", backgroundColor: "#53a7c0", nextState: "sending"
        }
        
		standardTile("zoneTwoTile", "device.zoneTwo", width: 1, height: 1, canChangeIcon: true, canChangeBackground: true) {
            state "off", label: 'Two', action: "Zone2On", icon: "st.Outdoor.outdoor12", backgroundColor: "#ffffff", nextState: "sending"
            state "sending", label: 'sending', action: "Zone2Off", icon: "st.Health & Wellness.health7", backgroundColor: "#cccccc"
            state "queued", label: 'Two', action: "Zone2Off",icon: "st.Outdoor.outdoor12", backgroundColor: "#c0a353", nextState: "sending"
            state "on", label: 'Two', action: "Zone2Off",icon: "st.Outdoor.outdoor12", backgroundColor: "#53a7c0", nextState: "sending"
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

// parse events into attributes
def parse(String description) {
    log.debug "Parsing '${description}'"
    def msg = parseLanMessage(description)
    log.debug "data ${msg.data}"
    log.debug "headers ${msg.headers}"
}

def installed() {
	//upnpSubscribe("/${device.deviceNetworkId}")
    upnpSubscribe()
}

def updated() {
	//upnpSubscribe("/${device.deviceNetworkId}")
    upnpSubscribe()
}

def on() {

}

def off() {

}

def refresh() {
	sendCommand("refresh", null)
}

def Zone1On() {
	sendCommand("zoneOn", [zone: 1])
}

def Zone1Off() {
	sendCommand("zoneOff", [zone: 1])
}

def Zone1OnFor(value) {
	sendCommand("zoneOnFor", [zone: 1, time: value])
}

def Zone2On() {
	sendCommand("zoneOn", [zone: 2])
}

def Zone2Off() {
	sendCommand("zoneOff", [zone: 2])
}

def Zone2OnFor(value) {
	sendCommand("zoneOnFor", [zone: 2, time: value])
}

def sendCommand(action, body) {
	// controlUrl: /upnp/service/control?usn=6bd5eabd-b7c8-4f7b-ae6c-a30ccdeb5988::urn:schemas-upnp-org:service:Sprinkler:1
   
    log.trace "Sending ${action}: ${body}"
    
	def result = new physicalgraph.device.HubSoapAction(
    	path:		"/upnp/service/control?usn=${settings.uuid}::urn:schemas-upnp-org:service:Sprinkler:1",
        urn:		"urn:schemas-upnp-org:service:Sprinkler:1",
        action:		action,
        body:		body,
        headers:	[HOST: "${settings.ip}:${settings.port}", CONNECTION: "close"]
    )
    //log.debug "sendCommand: ${result}"
   	return result
}

private upnpSubscribe(callbackPath="") {
	// /upnp/service/events?usn=6bd5eabd-b7c8-4f7b-ae6c-a30ccdeb5988::urn:schemas-upnp-org:service:Sprinkler:1
	log.trace "Subscribing to UPNP events"
    
    def result = new physicalgraph.device.HubAction(
        method: 	"SUBSCRIBE",
        path: 		"/upnp/service/events?usn=${settings.uuid}::urn:schemas-upnp-org:service:Sprinkler:1",       
        headers: [
            HOST: 		getHostAddress(),
            CALLBACK: 	"<http://${getCallBackAddress()}/notify$callbackPath>",
            NT: 		"upnp:event",
            TIMEOUT: 	"Second-28800"
        ])
    //log.debug "subscribe: ${result}"
	return result
}

private getCallBackAddress() {
	return device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")
}

private Integer convertHexToInt(hex) {
 	Integer.parseInt(hex,16)
}

private String convertHexToIP(hex) {
 	[convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}

private getHostAddress() {
 	def ip = settings.ip
 	def port = settings.port
 	if (!ip || !port) {
 		def parts = device.deviceNetworkId.split(":")
 		if (parts.length == 2) {
 			ip = parts[0]
 			port = parts[1]
 		} else {
 			log.warn "Can't figure out ip and port for device: ${device.id}"
		 }
 	}
 	log.debug "Using ip: ${ip} and port: ${port} for device: ${device.id}"
 	return convertHexToIP(ip) + ":" + convertHexToInt(port)
}