from django.shortcuts import render
from django.http import HttpResponse
from django.template import loader
from django import forms

from time import strftime

import csv

from .models import Server, Job, LoadMeasurement

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

"""
def submitServer(request):
    errors = []

    if 'serverHostname' in request.POST and request.POST['serverHostname'] != '':
        hostname = request.POST['serverHostname']
    else:
        errors.append("No Hostname specified.")

    if not errors:
        serverURL = "0.0.0.0" #TODO: move server URL to settings file
"""

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
