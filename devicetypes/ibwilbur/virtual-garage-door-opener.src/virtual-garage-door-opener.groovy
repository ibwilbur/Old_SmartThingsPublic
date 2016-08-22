/**
 *  Virtual Garage Door
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
	definition (name: "Virtual Garage Door Opener", namespace: "ibwilbur", author: "Will Shelton") {
		capability "Actuator"
		capability "Door Control"
        capability "Garage Door Control"
		capability "Refresh"
		capability "Sensor"
        
        command "update"
	}

	simulator {
		
	}

	tiles {
		standardTile("toggle", "device.door", width: 2, height: 2) {
			state("closed", label:'${name}', action:"door control.open", icon:"st.doors.garage.garage-closed", backgroundColor:"#79b821", nextState:"opening")
			state("open", label:'${name}', action:"door control.close", icon:"st.doors.garage.garage-open", backgroundColor:"#ffa81e", nextState:"closing")
			state("opening", label:'${name}', icon:"st.doors.garage.garage-closed", backgroundColor:"#ffe71e")
			state("closing", label:'${name}', icon:"st.doors.garage.garage-open", backgroundColor:"#ffe71e")			
		}
		standardTile("open", "device.door", inactiveLabel: false, decoration: "flat") {
			state "default", label:'open', action:"door control.open", icon:"st.doors.garage.garage-opening"
		}
		standardTile("close", "device.door", inactiveLabel: false, decoration: "flat") {
			state "default", label:'close', action:"door control.close", icon:"st.doors.garage.garage-closing"
		}
		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat") {
			state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
		}        

		main "toggle"
		details(["toggle", "open", "close", "refresh"])
	}
}

def parse(String description) {
	log.trace "parse($description)"
}

def refresh() {
	def rnd = Calendar.getInstance().getTimeInMillis()
	sendEvent(name: "door", value: "refresh.$rnd")
}

def open() {
	if (device.currentValue("door") != "open") {
    	sendEvent(name: "door", value: "opening")
    }
}

def close() {
	if (device.currentValue("door") != "closed") {
    	sendEvent(name: "door", value: "closing")
    }
}

def update(newStatus) {
    log.debug "Garage door status: $newStatus"
	sendEvent(name: "door", value: newStatus)
}