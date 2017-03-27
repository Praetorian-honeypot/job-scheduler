import json

from django.db import models
from .models import Server, Job, LoadMeasurement

class apiConnection:
    serverAddress = "0.0.0.0"
    loggedIn = False

    def login():


    def getServers():
        response = requests.get(self.serverAddress + "/servers/")

        j = response.json()

        servers = []
        for server in j:
            obj = Server(hostname=server['hostname'],
                            logicalAddress= server['hostname'],
                            cpuName= server['cpuname'],
                            cpuCores = server['cpucores'],
                            memoryAmount = server['memoryamount'])

            servers.append(obj)

        return servers

    def getJobs():
        response = requests.get(self.serverAddress + "/jobs/")

        j = response.json()

        servers = []
        for server in j:
            obj = Server(hostname=server['hostname'],
                            logicalAddress= server['hostname'],
                            cpuName= server['cpuname'],
                            cpuCores = server['cpucores'],
                            memoryAmount = server['memoryamount'])

            servers.append(obj)

        return servers
