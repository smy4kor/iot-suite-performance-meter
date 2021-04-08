import paho
import paho.mqtt.client as mqtt
import time
import json
import argparse

from commands import DittoCommand
# from commands import MeasurementData
from device_info import DeviceInfo
# from ditto_response import DittoResponse
from feature import Feature

deviceInfo = None
meter_feature = None
DEVICE_INFO_TOPIC = "edge/thing/response"
MQTT_TOPIC = [(DEVICE_INFO_TOPIC, 0), ("command///req/#", 0)]


def on_connect(client, userdata, flags, rc):
    print("Connected with result code " + str(rc))
    # Subscribing to a topic that sends install or download command
    client.subscribe(MQTT_TOPIC)
    client.publish('edge/thing/request', '', qos=1);
    # hint: to register as agent or operation status, use "e".


def on_publish(client, userdata, result):
    print("data published: " + str(result))


# The callback when a install or download command is received
# msg is of type MQTTMessage


def on_message(client, userdata, msg):
    print("received message on mqtt topic: " + msg.topic)
    # try-catch will ensure that subscription is not broken in case of any unhandled exception.
    try:
        process_event(msg)
    except Exception as err:
        print(err)


def process_event(msg: paho.mqtt.client.MQTTMessage):
    """Will process the mqtt message based on the use case. Use case is determined by the message topic."""
    payload_str = str(msg.payload.decode("utf-8", "ignore"))
    print(payload_str)
    payload = json.loads(payload_str)
    feature_id = payload.get('headers', {}).get('ditto-message-feature-id', None)

    if msg.topic == DEVICE_INFO_TOPIC:
        global deviceInfo, meter_feature
        deviceInfo = DeviceInfo(payload)
        meter_feature = Feature("performanceTest", "2.0.0", 'measure-performance-feature', mqtt_client, deviceInfo)
        meter_feature.register()
        print("======== Feature is ready =============")
    # elif msg.topic == "command///req//modified":
    #     handle_measurement_request(cmd)
    elif feature_id and feature_id == meter_feature.featureId:
        cmd = DittoCommand(payload, msg.topic)
        cmd.print_info()
        meter_feature.handle(cmd)


# def handle_measurement_request(cmd):
#     # todo: also check
#     if cmd.featureId == meter_feature.featureId and cmd.path.endswith('status/request'):
#         print("processing request with ditto req id: " + str(cmd.getRequestId()))
#         acknowledge(cmd)
#         send_response(cmd)
#     else:
        print("Ignoring the message received for feature: " + str(cmd.featureId))
        # else, from cache file stored with cmd.featureId and execute the scripts stored there


# see https://wiki.bosch-si.com/pages/viewpage.action?spaceKey=MBSIOTSDK&title=Things+Protocol+Patterns
# def send_response(cmd):
#     mr = cmd.get_measurement_data()
#     print("Responding to : {},{}".format(mr.id, mr.serialNumber))
#     pth = "/features/{}/properties/status/response".format(meter_feature.featureId)
#
#     ditto_rsp_topic = "{}/{}/things/twin/commands/modify".format(deviceInfo.namespace, deviceInfo.deviceId)
#     rsp = DittoResponse(ditto_rsp_topic, pth)
#     rsp.prepareMeasurementResponse(mr)
#     print("publishing response: " + rsp.toJson())
#     mqtt_client.publish("e", rsp.toJson(), qos=1)
#
#
# def acknowledge(cmd, value=None):
#     status = 200
#     mosquitto_topic = "command///res/" + str(cmd.getRequestId()) + "/" + str(status)
#     # print("======== Sending acknowledgement for ditto requestId: %s =============" %(cmd.getRequestId()))
#     akn_path = cmd.path.replace("inbox", "outbox")  # # "/features/manually-created-lua-agent/outbox/messages/install"
#     rsp = DittoResponse(cmd.dittoTopic, akn_path, status)
#     rsp.prepareAknowledgement(cmd.dittoCorrelationId)
#     if value:
#         rsp.value = value
#
#     mqtt_client.publish(mosquitto_topic, rsp.toJson())
#     print("======== Acknowledgement sent on topic " + mosquitto_topic + " =============")


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("-mh", "--mqtt_host", metavar='PORT', help="Host of the MQTT broker", default='localhost')
    parser.add_argument("-mp", "--mqtt_port", metavar='PORT', help="Port of the MQTT broker", type=int, default=1883)
    args = parser.parse_args()

    print("MQTT connecting to {}:{}".format(args.mqtt_host, args.mqtt_port))
    mqtt_client = mqtt.Client()
    mqtt_client.on_connect = on_connect
    mqtt_client.on_publish = on_publish
    mqtt_client.on_message = on_message

    mqtt_client.connect(args.mqtt_host, args.mqtt_port, 60)

    mqtt_client.loop_forever()
