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
* Run the client as spring boot application.
* Use the end points to trigger performance measurement.
