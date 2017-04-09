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

    print r.text

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

    print r.text

    i = 0
    while(True):
        try:
            job = Job(idOnServer = json[str(i)][0],
                    deadline = parse(json[str(i)][1]),
                    command = json[str(i)][2],
                    priority = json[str(i)][3],)
            job.save()
            i += 1
        except Exception as e:
            print e
            break


def fetchServerReports():
    LoadMeasurement.objects.all().delete()

    r = requests.get("http://localhost:8080/api/reportservice")

    json = r.json()

    print r.text

    i = 0
    while(True):
        try:
            server = Server.objects.get(address=json[str(i)][0])

            lm = LoadMeasurement(date = parse(json[str(i)][4]),
                cpuLoad = json[str(i)][1],
                memoryLoad = json[str(i)][2],
                server = server)

            lm.save()
            i += 1
        except Exception as e:
            print e
            break

def fetchJobSchedules():
    JobSchedulingEvent.objects.all().delete()

    for job in Job.objects.all():
        print job.idOnServer
        r = requests.get("http://localhost:8080/api/jobscheduleservice?job=%d" % job.idOnServer)

        print r.text
        try:
            json = r.json()
            i=0
            while(True):
                try:
                    if int(json[str(i)][3]) > 0:
                        server = Server.objects.get(idOnServer=json[str(i)][3])

                        schedEvent = JobSchedulingEvent(job=job,
                            eventDate = parse(json[str(i)][1]),
                            schedStatus = json[str(i)][2],
                            server = server)
                    else:
                        schedEvent = JobSchedulingEvent(job=job,
                            eventDate = parse(json[str(i)][1]),
                            schedStatus = json[str(i)][2])
                    i+=1
                    schedEvent.save()
                except Exception as e:
                    print e
                    break
        except Exception as e:
            print e
