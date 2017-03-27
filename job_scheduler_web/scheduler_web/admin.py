from django.contrib import admin

# Register your models here.

from .models import Job, Server, LoadMeasurement, ServerGroup, JobSchedulingEvent

admin.site.register(Job)
admin.site.register(Server)
admin.site.register(LoadMeasurement)
admin.site.register(ServerGroup)
admin.site.register(JobSchedulingEvent)
