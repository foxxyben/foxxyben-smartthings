/**
 *  SimpliSafe Alarm State revision 6
 *  2-16-2016
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
			input "alarmsystem", "capability.alarm", multiple: false, required: true
		}
        section("Set SimpliSafe to Off when mode matches") {
			input "modealarmoff", "mode", title: "Select mode for 'Disarmed'", multiple: true, required: false
        }
		section("Set SimpliSafe to Away when mode matches") {
			input "modealarmaway", "mode", title: "Select mode for 'Armed Away'", multiple: true, required: false  
        }
		section("Set SimpliSafe to Home when mode matches") {
			input "modealarmhome", "mode", title: "Select mode for 'Armed Home'", multiple: true, required: false
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
	init()
}

def updated() {
    unsubscribe()
    unschedule()
    init()
}
  
def init() {
    subscribe(location, "mode", modeaction)
    subscribe(alarmsystem, "alarm", alarmstate)
}
  
def modeaction(evt) {
	state.locationmode = evt.value

	if(evt.value in modealarmoff && state.alarmstate !="off") {
    	log.debug("Location mode: $state.locationmode")
    	setalarmoff()
    } else if(evt.value in modealarmaway && state.alarmstate !="away") {
		log.debug("Location mode: $state.locationmode")
    	setalarmaway()
  	} else if(evt.value in modealarmhome && state.alarmstate !="home") {
		log.debug("Location mode: $state.locationmode")
        setalarmhome()
	} else {
		log.debug("No actions set for location mode ${state.locationmode} or SimpliSafe already set to ${state.alarmstate} - aborting")
	}
}

def setalarmoff() {
    def message = "SimpliSafe is DISARMED"
    log.info(message)
    send(message)
    alarmsystem.off()
}
  
def setalarmaway() {
    def message = "SimpliSafe is Armed AWAY"
    log.info(message)
    send(message)
    alarmsystem.away()
}
  
def setalarmhome() {
    def message = "SimpliSafe is Armed HOME"
    log.info(message)
    send(message)
    alarmsystem.home()
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
