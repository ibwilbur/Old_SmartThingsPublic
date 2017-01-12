/**
 *  Temperature Monitor
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
definition(
    name: "Temperature Monitor",
    namespace: "ibwilbur",
    author: "Will Shelton",
    description: "Monitors temperatures and takes actions accordingly",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo-switch.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo-switch.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo-switch.png")


preferences {
	section("Raspberry Pi Setup") {
        input "ip", "text", "title": "IP Address", multiple: false, required: false
        input "port", "text", "title": "Port", multiple: false, required: false
	} 
    
	section("Choose a temperature sensor... "){
		input "temperature", "capability.temperatureMeasurement", title: "Temprature sensor", multiple: false, required: false
	}
	section("Select the fan to turn on... "){
		input "fan", "capability.switch", title: "Fan", multiple: false, required: false
	}
	section("When does the fan turn on?"){
		input "highsetpoint", "number", title: "Temperature?"
	}
	section("When does the fan turn off?"){
		input "lowsetpoint", "number", title: "Temperature?"
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"    
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	initialize()
}

def initialize() {
	subscribe(temperature, "temperature", temperatureHandler, [filterEvents: false])
    sendCommand("GET", "/devices/reload", [])
}

def temperatureHandler(e) {
	log.trace "temperatureHandler: DeviceID: $e.deviceId, Attribute: $e.name, Value: $e.value"
    
    if (e.doubleValue >= highsetpoint) {
    	fan.on()
    }
    else if (e.doubleValue <= lowsetpoint) {
    	fan.off()
    }
}

def sendCommand(method, path, data) {
	def hubAction = new physicalgraph.device.HubAction(
    	method: method,
        path: path,
        headers: [HOST: "$settings.ip:$settings.port"],
        body: data
    )
    sendHubCommand(hubAction)
}