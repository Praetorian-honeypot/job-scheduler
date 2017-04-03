from __future__ import unicode_literals

from django.db import models
from jsonfield import JSONField

# Create your models here.

class LoadMeasurement(models.Model):
    date = models.DateTimeField('Measurement date/time')
    server = models.ForeignKey('Server', on_delete = models.CASCADE)
    cpuLoad = models.FloatField()
    memoryLoad = models.FloatField()

    def cpuLoadPercentage(self):
        return 100.0 * self.cpuLoad / self.server.cpuCores
    def memoryLoadPercentage(self):
        return 100.0 * self.memoryLoad / self.server.memoryAmount

class Server(models.Model):
    hostname = models.CharField(max_length = 32)
    hostport = models.IntegerField(default = 8901)
    address= models.CharField(max_length = 32)
    displayName = models.CharField(max_length = 32)
    cpuName = models.CharField(max_length = 32)
    cpuCores = models.IntegerField(default = 1, blank=True)
    memoryAmount = models.FloatField()
    serverGroup = models.ForeignKey('ServerGroup', on_delete = models.SET_NULL, null=True, blank=True)

    idOnServer = models.IntegerField(default = -1)

    def __str__(self):
        return self.hostname

    def groupPath(self):
        if self.serverGroup is None:
            return ""
        else:
            return self.serverGroup.groupPath() + self.serverGroup.groupName + "/"

class ServerGroup(models.Model):
    groupName = models.CharField(max_length = 32)
    superGroup = models.ForeignKey('ServerGroup', on_delete = models.SET_NULL, null=True, blank=True)

    def __str__(self):
        return self.groupName

    def groupPath(self):
        if self.superGroup is None:
            return ""
        else:
            return self.superGroup.groupPath() + self.superGroup.groupName + "/"


class Job(models.Model):
    command = models.CharField(max_length=256)
    priority = models.IntegerField(default = 1)
    deadline = models.DateTimeField('Requested deadline')

    idOnServer = models.IntegerField(default = -1)

    def schedStatus(self):
        jobSchedulingEvents = JobSchedulingEvent.objects.filter(job = self)

        if len(jobSchedulingEvents) > 0:
            return jobSchedulingEvents.order_by('-eventDate')[0]
        else:
            return None


    def __str__(self):
        stat = self.schedStatus()
        if stat is not None:
            if(len(self.command) < 32):
                return "[%d] %s (%s)" % (self.priority,self.command,stat.get_schedStatus_display())
            else:
                return "[%d] %s... (%s)" % (self.priority,self.command[:32],stat.get_schedStatus_display())
        else:
            if(len(self.command) < 32):
                return "[%d] %s (%s)" % (self.priority,self.command,None)
            else:
                return "[%d] %s... (%s)" % (self.priority,self.command[:32],None)

"""
JobSchedulingEvent:
some event causes the job scheduling status of a job to change.

Maybe a job was entered by the user, or it was scheduled,
or it was cancelled/killed, or it was executed and it either ran sucessfully or failed.

"""

class JobSchedulingEvent(models.Model):
    job = models.ForeignKey('Job', on_delete=models.CASCADE)

    eventDate = models.DateTimeField('Date of event')
    schedStatus = models.IntegerField(choices = ((0,'entered'),(1,'scheduled'),
        (2,'running'),(3,'finished'),(4,'failed'),(5,'cancelled'),(6,'killed')))

    server = models.ForeignKey('Server', on_delete = models.SET_NULL, null=True, blank=True)

    def __str__(self):
        return "[%s] %s (%s)" % (self.eventDate, self.job.command, self.get_schedStatus_display())
