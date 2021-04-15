import os
import os.path
import json
from json import JSONEncoder
import uuid

import paho

from commands import DittoCommand, DittoResponse
from device_info import DeviceInfo


class Feature:

    def __init__(self, name, version, feature_id, mqtt_client: paho.mqtt.client.Client, device_info: DeviceInfo):
        """
        Create a new Feature for being present on the respective device thing.

        :param name: Name of the feature
        :param version: Version information
        :param feature_id: The id to be used
        :param mqtt_client: A connected MQTT-client
        :param device_info: Registration information
        """

        self.name = name
        self.version = version
        self.featureId = feature_id
        self.__mqttClient = mqtt_client
        self.__deviceInfo = device_info

    def to_json(self):
        return json.dumps(self, default=lambda o: o.__dict__, sort_keys=True, indent=4,
                          skipkeys=['__mqttClient', '__deviceInfo'])

    def register(self):
        """Registers this as a feature in IoT-THings
    
        Parameters
        ----------
        mqtt_client : paho.mqtt.client
            The mqtt client that has the connection to the local mosquitto provided by the edge device.
        device_info : DeviceInfo
            Information of this device in the context of its subscription.
        """

        ditto_rsp_topic = "{}/{}/things/twin/commands/modify".format(self.__deviceInfo.namespace,
                                                                     self.__deviceInfo.deviceId)
        value = {
            "definition": ["org.example:PerformanceTest:2.0.0"],
            "properties": {
                "status": {
                    "agentName": self.name,
                    "agentVersion": self.version
                }
            }
        }
        path = "/features/" + self.featureId
        rsp = DittoResponse(ditto_rsp_topic, path, None)

        rsp.value = value
        self.__mqttClient.publish("e", rsp.to_json(), qos=1)

    def acknowledge(self, command: DittoCommand):
        if command.response_required():
            mosquitto_topic = "command///res/" + str(command.get_request_id()) + "/200"
            self.__mqttClient.publish(mosquitto_topic, command.get_response().to_json())
            print("======== Acknowledgement sent on topic " + mosquitto_topic + " =============")

    def handle(self, command: DittoCommand):
        """Handles a DittoCommand"""
        if self.featureId != command.featureId:
            print("Not matching feature ID: {}".format(command.featureId))
            return

        self.acknowledge(command)

        if command.mqttTopic == "command///req//start":
            print("Start sending messages...")
            count = command.value.get('message_count', 100)
            method = command.value.get('method', 'SUITE')
            respSubject = 'meter.event.response';
            respPath = "/features/{}/outbox/messages/{}".format(command.featureId, respSubject)
            print("Expected response message count = {} method = {}".format(count, method))

            for i in range(count):
                event = {
                    # 'topic': command.dittoTopic,
                    'topic': self.__deviceInfo.namespace + "/" + self.__deviceInfo.deviceId + "/things/live/messages/" + respSubject, 
                    'path': respPath,
                    'headers': {
                        "response-required": False,
                        "content-type": "application/json",
                        "correlation-id": "dont-care",
                    },
                    'value': {
                        "expected": count,
                        "current": i
                    }
                }
                if i == count-1:
                    print("Sending {}".format(json.dumps(event)))
                self.__mqttClient.publish('t',json.dumps(event),qos=0)
