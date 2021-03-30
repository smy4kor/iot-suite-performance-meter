import json

class DittoResponse:
    """A utility class that is responsible for generating response messages according to the ditto protocol."""
    
    def __init__(self,topic,path,responseCode=None):
        self.topic = topic
        self.path = path.replace("inbox","outbox") ## "/features/manually-created-lua-agent/outbox/messages/install"
        
        if responseCode:
            self.status = responseCode
            
    def prepareAknowledgement(self,dittoCorrelationId):
        self.value = {}
        self.headers = {
            "response-required": False,
            "correlation-id": dittoCorrelationId,
            "content-type": "application/json"
        }
    def preparePongResponse(self,measurement):
        self.headers = {
            "response-required": False,
            "content-type": "application/json"
        }
        self.value = {
            serialNumber: {}
        }
        self.value[measurement.id] = {
                "response": "pong" + measurement.serialNumber
        }
            
        
    def toJson(self):
        return json.dumps(self, default=lambda o: o.__dict__, sort_keys=True, indent=4)