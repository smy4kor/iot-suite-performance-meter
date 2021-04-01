import json
import re

class MeasurementData:
    def __init__(self,id,serialNumber):
        self.id = id
        self.serialNumber = serialNumber
    def toJson(self):
        return json.dumps(self, default=lambda o: o.__dict__, sort_keys=True, indent=4)

class DittoCommand:
    def __init__(self, payload,topic):
        self.payload = payload
        self.mqttTopic = topic
        self.dittoTopic = payload['topic']
        self.path = payload['path']
        self.dittoCorrelationId = payload['headers']["correlation-id"]
        self.dittoOriginator = payload['headers']["ditto-originator"]
        self.requestHeaders = payload['headers']
        self.featureId = self.getFeatureId()
    
    def getRequestId(self):
        pattern = "req/(.*)/" ## everything between req/ and /install is the request id. Ex topic: command///req/01fp-pdid6m-12i8u431qmpi1b-1m2zqv2replies/install
        x = re.search(pattern, self.mqttTopic)
        if x:
            return x.group(1)
        else:
            return None

    def getFeatureId(self):
        pattern = "features/(.*)/properties/" ## /features/measure-performance-feature/properties/status/request
        x = re.search(pattern, self.path)
        if x:
            return x.group(1)
        else:
            return None
    def getServiceInstanceId(self):
        pattern = "service-instance.(.*).iot-" 
        ## everything between 'service-instance.' and '.iot-'. 
        # Ex topic: iot-suite:useridhere/service-instance.abcde.iot-things@device-management
        x = re.search(pattern, self.dittoOriginator)
        if x:
            return x.group(1)
        else:
            return None   
    
    def printInfo(self):
        print("MQTT topic: " + self.mqttTopic)
        print('Ditto topic: ' + self.dittoTopic)
        print('Ditto originator: ' + self.dittoOriginator)
        print('Service instance id: ' + self.getServiceInstanceId())
        print('Path: ' + self.path)
        if self.featureId:
            print('Feature id: : ' + self.featureId)
        print("===")
        
    def getMeasurementData(self):
        lst = []
        if 'value' not in self.payload.keys():
            return null
        id = self.payload['value']['id']
        serialNumber = self.payload['value']['serialNumber']
        return MeasurementData(id,serialNumber)

