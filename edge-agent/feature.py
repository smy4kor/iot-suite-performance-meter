import os
import os.path
import json
from json import JSONEncoder
import uuid

import paho

from commands import DittoCommand, DittoResponse, MeasurementData
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

        count = command.value.get('count', 100)
        request_id = command.value.get('id')
        
        if command.mqttTopic == "command///req//start":
            self.respondUsingEvents(command,count,request_id)
        elif command.mqttTopic == "command///req//modified" or command.mqttTopic == "command///req//created":
            self.respondUsingFeature(command,count,request_id)

    def respondUsingFeature(self, command: DittoCommand, count, request_id):
        print("Responding as feature update")
        dittoRspTopic = "{}/{}/things/twin/commands/modify".format(self.__deviceInfo.namespace, self.__deviceInfo.deviceId)
        for i in range(count):
            event = {
                # 'topic': command.dittoTopic,
                'topic': dittoRspTopic,
                'path': "/features/{}/properties/status/response".format(command.featureId),
                'headers': {
                    "response-required": False,
                    "content-type": "application/json"
                },
                'value': MeasurementData(request_id,count,i).__dict__
            }
            if i == count - 1:
                print("Sending {}".format(json.dumps(event)))
            self.__mqttClient.publish('t', json.dumps(event), qos=0)
                        
    def respondUsingEvents(self, command: DittoCommand, count, request_id):
        print("Start sending messages...")
        resp_headers = command.value.get('responseHeaders',{
                    "response-required": False,
                    "content-type": "application/json",
                    "correlation-id": "dont-care",
                })
        
        resp_subject = 'meter.event.response';
        resp_path = "/features/{}/outbox/messages/{}".format(command.featureId, resp_subject)
        print("Expected response message count = {}, headers={}, request id = {} ".format(count,resp_headers,request_id))

        for i in range(count):
            event = {
                # 'topic': command.dittoTopic,
                'topic': self.__deviceInfo.namespace + "/" + self.__deviceInfo.deviceId + "/things/live/messages/" + resp_subject,
                'path': resp_path,
                'headers': resp_headers,
                'value': MeasurementData(request_id,count,i).__dict__
            }
            if i == count - 1:
                print("Sending {}".format(json.dumps(event)))
            self.__mqttClient.publish('t', json.dumps(event), qos=0)
