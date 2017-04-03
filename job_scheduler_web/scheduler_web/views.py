from django.shortcuts import render
from django.http import HttpResponse
from django.template import loader
from django import forms

from time import strftime

import csv

from .models import Server, Job, LoadMeasurement, JobSchedulingEvent
from .forms import AddServerForm, AddJobForm

import rest

import requests
# Create your views here.

from .JSONserializer import ServerSerializer

def index(request):
    context = {}
    template = loader.get_template('scheduler_web/index.html')
    return HttpResponse(template.render(context,request))

def servers(request):

    server_list = Server.objects.order_by('hostname')
    context = {'server_list': server_list}
    template = loader.get_template('scheduler_web/servers.html')
    return HttpResponse(template.render(context,request))

def serverDetail(request, serverID):
    server = Server.objects.get(pk=serverID)
    latestLoad = LoadMeasurement.objects.filter(server=server).order_by('-date').first()

    context = {
        'server':server,
        'load':latestLoad,
    }

    template = loader.get_template('scheduler_web/serverDetail.html')
    return HttpResponse(template.render(context,request))

def jobs(request):
    job_list = Job.objects.order_by('-priority')
    context = {'job_list': job_list}
    template = loader.get_template('scheduler_web/jobs.html')
    return HttpResponse(template.render(context,request))

def jobDetail(request, jobID):
    job = Job.objects.get(pk=jobID)
    schedEvents = JobSchedulingEvent.objects.filter(job=job)
    latestEvent = schedEvents.order_by('-eventDate').first()

    context = {
        'job':job,
        'latestEvent':latestEvent,
        'schedEvents':schedEvents,
    }

    template = loader.get_template('scheduler_web/jobDetail.html')
    return HttpResponse(template.render(context,request))

def addServer(request):
    template = loader.get_template('scheduler_web/addServer.html')

    if request.method == 'POST':
        form = AddServerForm(request.POST)

        if form.is_valid():
            #send API request to server
            return HttpResponseRedirect('/servers/')
    else:
        form = AddServerForm()

        context = {'form' : form}
    return HttpResponse(template.render(context,request))

def addJob(request):
    template = loader.get_template('scheduler_web/addJob.html')

    if request.method == 'POST':
        form = AddJobForm(request.POST)

        if form.is_valid():
            payload = {'command':request.POST['command'],'priority':request.POST['priority']}
            requests.post('http://localhost:8080/jobservice/addjob')
            return HttpResponseRedirect('/jobs/')
    else:
        form = AddJobForm()

        context = {'form' : form}
    return HttpResponse(template.render(context,request))


def loadMeasurementsCSV(request,serverID):
    server = Server.objects.get(pk=serverID)
    loadMeasurements = LoadMeasurement.objects.filter(server=server).order_by('-date')

    response = HttpResponse(content_type='text/html')
    #response['Content-Disposition'] = 'attachment; filename="loadMeasurements_'+server.hostname+'.csv"'

    writer = csv.writer(response)
    writer.writerow(["date","cpuLoad","memoryLoad"])
    for meas in loadMeasurements:
        writer.writerow([meas.date.strftime("%Y-%m-%d %H:%M"),meas.cpuLoad,meas.memoryLoad])

    return response
