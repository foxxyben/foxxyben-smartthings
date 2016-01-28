/**
 *  Ecobee Set Comfort Profile rev 2
 *  1-24-2016
 *
 *  Copyright 2016 Ben Fox
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
 *  For use with SimpliSafe device type found here: https://community.smartthings.com/t/simplisafe-alarm-integration-cloud-to-cloud/8473
 */
definition(
	name: "Ecobee Set Comfort Profile",
	namespace: "foxxyben",
	author: "Ben Fox",
	description: "Automatically sets the Ecobee3 to one of the standard comfort profiles based on the Smartthings mode. For use with the StrykerSKS ecobee device type.",
	category: "My Apps",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Partner/ecobee.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Partner/ecobee.png@2x"
)

preferences {
	page(name: "selectProgram", title: "Ecobee3 Set Comfort Profile", install: false, uninstall: true,
    			nextPage: "Notifications") {
		section("Use this Thermostat...") {
			input "ecobeeThermostat", "capability.thermostat", multiple: false, required: true
		}
        section("Change to this comfort profile...") {
			input "comfortProfile","enum", options: ["Away", "Home", "Sleep"], required: true
		}
		section("When SmartThings changes to this mode... (optional)") {
            input "smartthingsMode","mode",multiple:true, required: false
		}
	}
    page(name: "Notifications", title: "Notifications Options", install: true, uninstall: true) {
		section("Notifications") {
			input "sendPushMessage", "enum", title: "Send a push notification?", options: ["Yes", "No"], 
            		required: false
			input "phone", "phone", title: "Send a Text Message?", required: false
		}
        section([mobileOnly:true]) {
			label title: "Assign a name", required: false
		}
	}
}

def installed() {
	subscribe(location, changeMode)
	subscribe(app, changeMode)
}

def updated() {
	unsubscribe()
	subscribe(location, changeMode)
	subscribe(app, changeMode)
}

def changeMode(evt) {
    def currentProfile = ecobeeThermostat.currentState("currentProgramId")?.value
    log.debug "Current Ecobee comfort profile is: ${currentProfile}"
    if ( comfortProfile.toLowerCase() == currentProfile.toLowerCase() ) {
    	log.debug "No change to comfort profiles needed"
    }
    if ( comfortProfile.toLowerCase() != currentProfile.toLowerCase() ) {
        def message

        Boolean foundMode=false        
        smartthingsMode.each {
            if (it==location.mode) {
                foundMode=true            
            }            
        }        

        if ((smartthingsMode != null) && (!foundMode)) {
            log.debug "changeMode>location.mode= $location.mode, smartthingsMode=${smartthingsMode},foundMode=${foundMode}, not doing anything"
            return			
        }

	try {
	        if (comfortProfile=="Home") {
	            ecobeeThermostat.setThermostatProgram("home")
	            message = "$ecobeeThermostat.name is set to HOME"
	        }
	
	        if (comfortProfile=="Away") {
	            ecobeeThermostat.setThermostatProgram("away")
	            message = "$ecobeeThermostat.name is set to AWAY"
	        }
	
	        if (comfortProfile=="Sleep") {
	            ecobeeThermostat.setThermostatProgram("sleep")
	            message = "$ecobeeThermostat.name is set to SLEEP"
	        }
	} catch (all) {
		message = "Something broke and the comfort profile was not changed!"
		log.error "$message"
	}
	
        send(message)
    }
}

private send(msg) {
	if (sendPushMessage != "No") {
		log.debug("sending push message")
		sendPush(msg)
	}
	if (phone) {
		log.debug("sending text message")
		sendSms(phone, msg)
	}
    
	log.debug msg
}
