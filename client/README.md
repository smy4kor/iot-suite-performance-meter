# Performance Measurement Tool

### Step 1: Start the agent on the edge device
* Install the following prerequisites.

```
sudo apt-get install python3 -y
sudo apt-get install lua5.1 -y
alias python=python3
sudo apt install python3-pip
pip3 install paho-mqtt python-etcd
pip3 install --upgrade requests
```

* Download the contents of repository on the edge device.
* Run `python3 edge-agent/start-agent.py`.

### Step 2: Run the client app
* Client is a spring boot application.
* To run, using following configurations. You can generate a clientId and secret from https://accounts.bosch-iot-suite.com/oauth2-clients/.

```
-Dauthentication.clientId=<generate-it-from-above-link>
-Dauthentication.clientSecret=
-Dauthentication.deviceId=<thing-id-including-namespace>
-Dauthentication.serviceInstanceId=<your-service-instance-id>
```
### Step 3: Start a measurement
Use the following end points to trigger performance measurement.
* **Using 100 feature update:** http://localhost:8080/api/v1/measure/using-feature/100
* **Using 100 event updates:** http://localhost:8080/api/v1/measure/using-events/100
* **Check the status:** http://localhost:8080/api/v1/measure/status/<request-id>
