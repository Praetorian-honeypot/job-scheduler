import json

from django.db import models
from .models import Server, Job, LoadMeasurement

class apiConnection:
    serverAddress = "0.0.0.0"
    loggedIn = False

    def login():
        pass

    def getServers(beginDate=0,endDate=2147483647):
        response = requests.get("{}/servers?begin={}&end={}".format(self.serverAddress,beginDate,endDate))

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

    def getJobs(beginDate=0,endDate=2147483647,server=""):
        if server != "":
            response = requests.get("{}/jobs?begin={}&end={}&server={}".format(self.serverAddress,beginDate,endDate,server))
        else:
            response = requests.get("{}/jobs?begin={}&end={}".format(self.serverAddress,beginDate,endDate))
        j = response.json()

        jobs = []
        for job in j:
            obj = Jobs(command = job['command'],
                        priority = job['priority'],
                        deadline = job['deadline'])

            jobs.append(obj)

        return jobs

    def getJobSchedulingEvents(beginDate=0,endDate=2147483647,job=""):
        if job != "":
            response = requests.get("{}/jobsSchedulingEvents?begin={}&end={}&job={}".format(self.serverAddress,beginDate,endDate,job))
        else:
            response = requests.get("{}/jobsSchedulingEvents?begin={}&end={}".format(self.serverAddress,beginDate,endDate))
        j = response.json()

        scheds = []
        for sched in j:
            obj = JobSchedulingEvent(job = Job.objects.get(pk=sched['job']),
                        eventDate = sched['date'],
                        schedStatus = sched['status'])

            scheds.append(obj)

        return scheds

    def getLoadMeasurements(beginDate=0,endDate=2147483647,server=""):
        if server != "":
            response = requests.get("{}/jobsSchedulingEvents?begin={}&end={}&server={}".format(self.serverAddress,beginDate,endDate,job))
        else:
            response = requests.get("{}/jobsSchedulingEvents?begin={}&end={}".format(self.serverAddress,beginDate,endDate))
        j = response.json()

        measurements = []
        for meas in j:
            obj = JobSchedulingEvent(server = Server.objects.get(pk=meas['server']),
                        date = meas['date'],
                        cpuLoad = meas['cpuload'],
                        memoryLoad = meas['memoryused'])

            measurements.append(obj)

        return scheds
