import paho.mqtt.client as mqtt
import time
import json
import argparse

from commands import DittoCommand
from commands import MeasurementData
from device_info import DeviceInfo
from ditto_response import DittoResponse
from agent import Agent

agent = Agent("performanceTest", "2.0.0", 'measure-performance-feature')
deviceInfo = DeviceInfo()
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


def process_event(msg):
    '''Will process the mqtt message based on the use case. Usecase is determined by the message topic.'''
    payloadStr = str(msg.payload.decode("utf-8", "ignore"))
    print(payloadStr)
    payload = json.loads(payloadStr)

    if msg.topic == DEVICE_INFO_TOPIC:
        deviceInfo.compute(payload)
        agent.register(client, deviceInfo)
        print("======== Agent is ready =============")
    elif msg.topic == "command///req//modified":
        cmd = DittoCommand(payload, msg.topic)
        handle_measurement_request(cmd)
        cmd.printInfo()


def handle_measurement_request(cmd):
    # todo: also check
    if cmd.featureId == agent.featureId and cmd.path.endswith('status/request'):
        print("processing request with ditto req id: " + str(cmd.getRequestId()))
        acknowledge(cmd)
        send_response(cmd)
    else:
        print("Ignoring the message received for feature: " + str(cmd.featureId))
        # else, from cache file stored with cmd.featureId and execute the scripts stored there


# see https://wiki.bosch-si.com/pages/viewpage.action?spaceKey=MBSIOTSDK&title=Things+Protocol+Patterns
def send_response(cmd):
    mr = cmd.getMeasurementData()
    print("Responding to : {},{}".format(mr.id, mr.serialNumber))
    pth = "/features/{}/properties/status/response".format(agent.featureId)

    dittoRspTopic = "{}/{}/things/twin/commands/modify".format(deviceInfo.namespace, deviceInfo.deviceId)
    rsp = DittoResponse(dittoRspTopic, pth)
    rsp.prepareMeasurementResponse(mr)
    print("publishing response: " + rsp.toJson())
    client.publish("e", rsp.toJson(), qos=1)


def acknowledge(cmd, value=None):
    status = 200
    mosquittoTopic = "command///res/" + str(cmd.getRequestId()) + "/" + str(status)
    # print("======== Sending aknowledgement for ditto requestId: %s =============" %(cmd.getRequestId()))
    aknPath = cmd.path.replace("inbox", "outbox")  # # "/features/manually-created-lua-agent/outbox/messages/install"
    rsp = DittoResponse(cmd.dittoTopic, aknPath, status)
    rsp.prepareAknowledgement(cmd.dittoCorrelationId)
    if value:
        rsp.value = value

    client.publish(mosquittoTopic, rsp.toJson())
    print("======== Aknowledgement sent on topic " + mosquittoTopic + " =============")


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("-mh", "--mqtt_host", metavar='PORT', help="Host of the MQTT broker", default='localhost')
    parser.add_argument("-mp", "--mqtt_port", metavar='PORT', help="Port of the MQTT broker", type=int, default=1883)
    args = parser.parse_args()

    port = args.mqtt_port

    print("MQTT connect to {}:{}".format(args.mqtt_host, args.mqtt_port))
    client = mqtt.Client()
    client.on_connect = on_connect
    client.on_publish = on_publish
    client.on_message = on_message

    client.connect(args.mqtt_host, args.mqtt_port, 60)

    client.loop_forever()
