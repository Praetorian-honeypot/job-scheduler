from django.contrib import admin

# Register your models here.

from .models import Job, Server, LoadMeasurement, JobSchedulingEvent

admin.site.register(Job)
admin.site.register(Server)
admin.site.register(LoadMeasurement)
admin.site.register(JobSchedulingEvent)
