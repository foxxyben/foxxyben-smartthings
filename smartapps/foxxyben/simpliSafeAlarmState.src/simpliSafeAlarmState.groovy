/**
 *  SimpliSafe Alarm State revision 3
 *  12-25-2015
 *
 *  Copyright 2015 Ben Fox
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
	name: "SimpliSafe Alarm State",
	namespace: "foxxyben",
	author: "Ben Fox",
	description: "Automatically sets the SimpliSafe alarm state based on the Smartthings mode.",
	category: "My Apps",
	iconUrl: "https://pbs.twimg.com/profile_images/594250179215241217/LOjVA4Yf.jpg",
	iconX2Url: "https://pbs.twimg.com/profile_images/594250179215241217/LOjVA4Yf.jpg"
)

preferences {
	page(name: "selectProgram", title: "SimpliSafe Alarm State", install: false, uninstall: true,
    			nextPage: "Notifications") {
		section("Use this Alarm...") {
			input "simpliSafeAlarm", "capability.alarm", multiple: false, required: true
		}
        section("Change to this arming state...") {
			input "alarmState","enum", options: ["Away", "Home", "Off"], required: true
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
    def currentState = simpliSafeAlarm.currentState("alarm")
    log.debug "Current SimpliSafe state is: ${currentState.value}"
    if ( alarmState.toLowerCase() == currentState.value.toLowerCase() ) {
    	log.debug "No alarm state change needed"
    }
    if ( alarmState.toLowerCase() != currentState.value.toLowerCase() ) {
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

        if (alarmState=="Home") {
            simpliSafeAlarm.home()
            message = "SimpliSafe is Armed HOME"
        }

        if (alarmState=="Away") {
            simpliSafeAlarm.away()
            message = "SimpliSafe is Armed AWAY"
        }

        if (alarmState=="Off") {
            simpliSafeAlarm.off()
            message = "SimpliSafe is DISARMED"
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
