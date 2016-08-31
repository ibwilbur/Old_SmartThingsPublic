/**
 *  Virtual Sprinkler
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
	definition (name: "Virtual Sprinkler", namespace: "ibwilbur", author: "Will Shelton") {
    	capability "Refresh"
		capability "Relay Switch"
	}
	simulator {}
    
	tiles {
		standardTile("switch", "device.switch", width: 2, height: 2) {
			state "on", label: '${name}', action: "off", icon: "st.Outdoor.outdoor12", backgroundColor: "#79b821"
			state "off", label: '${name}', action: "on", icon: "st.Outdoor.outdoor12", backgroundColor: "#ffffff"
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main "switch"
		details(["switch","refresh"])        
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
}

def installed() {
	initialize()
}

def updated() {
	initialize()
}

def initialize() {
	sendEvent(name: "switch", value: "off")
}

def refresh() {
	def rnd = Calendar.getInstance().getTimeInMillis()
	log.trace "Sending refresh for $device.deviceNetworkId"
	sendEvent(name: "refresh", value: device.deviceNetworkId + ".refresh.$rnd")
}

def on() {
	log.debug "Executing 'on'"
	sendEvent(name: "switch", value: "on")
}

def off() {
	log.debug "Executing 'off'"
	sendEvent(name: "switch", value: "off")
}