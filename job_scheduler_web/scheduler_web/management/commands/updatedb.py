from django.core.management.base import BaseCommand, CommandError
from scheduler_web.models import Server, Job, JobSchedulingEvent, LoadMeasurement
import requests


class Command(BaseCommand):
    help = 'Updates the internal db state'

    def handle(self, *args, **options):
        fetchAPI()

def fetchAPI():
    fetchServers()
    fetchJobs()
    fetchServerReports()
    fetchJobSchedules()

def fetchServers():
    Server.objects.all().delete()

    r = requests.get("http://localhost:8080/api/clientservice")

    json = r.json()

    i = 0
    while(True):
        try:
            server = Server(idOnServer = json[str(i)][0],
                    hostname = json[str(i)][6],
                    hostport = json[str(i)][7],
                    address = json[str(i)][1],
                    displayName = json[str(i)][9],
                    cpuName = json[str(i)][2],
                    cpuCores = json[str(i)][3],
                    memoryAmount = json[str(i)][4])
            server.save()
            i += 1
        except:
            break

def fetchJobs():
    Job.objects.all().delete()

    r = requests.get("http://localhost:8080/api/jobservice")

    json = r.json()

    i = 0
    while(True):
        try:
            job = Job(idOnServer = json[str(i)][0],
                    deadline = json[str(i)][1],
                    command = json[str(i)][2],
                    priority = json[str(i)][3],)
            job.save()
            i += 1
        except:
            break

def fetchServerReports():
    LoadMeasurement.objects.all().delete()

    r = requests.get("http://localhost:8080/api/reportservice")

    json = r.json()

    i = 0
    while(True):
        try:
            server = Server.objects.get(adress=json[str(i)][0])

            lm = LoadMeasurement(date = json[str(i)][4],
                cpuLoad = json[str(i)][1],
                memoryLoad = json[str(i)][2],
                server = server)

            lm.save()
            i += 1
        except:
            break

def fetchJobSchedules():
    JobSchedulingEvent.objects.all().delete()

    r = requests.get("http://localhost:8080/api/jobscheduleservice")

    json = r.json()

    i = 0
    while(True):
        try:
            server = Server.objects.get(idOnServer=json[str(i)][3])
            job = Job.objects.get(idOnServer=json[str(i)][0])

            schedEvent = JobSchedulingEvent(eventDate = json[str(i)][1],
                status = json[str(i)][2],
                server = server)

            schedEvent.save()
            i += 1
        except:
            break
