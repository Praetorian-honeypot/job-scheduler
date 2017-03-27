from django.conf.urls import url
from . import views

urlpatterns = [
    url(r'^$', views.index, name='index'),
    url(r'^servers/', views.servers, name='servers'),
    url(r'^serverDetail/(?P<serverID>[0-9]+)/$', views.serverDetail, name='serverDetail'),
    url(r'^serverDetail/(?P<serverID>[0-9]+)/loadMeasurements.csv$', views.loadMeasurementsCSV, name='loadMeasurementsCSV'),
    url(r'^jobs/', views.jobs, name='jobs'),
]
