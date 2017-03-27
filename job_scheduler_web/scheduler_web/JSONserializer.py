from rest_framework import serializers
from.models import Job, Server, LoadMeasurement

class JobSerializer(serializers.ModelSerializer):
    class Meta:
        model = Job
        fields = '__all__'

class ServerSerializer(serializers.ModelSerializer):
    class Meta:
        model = Server
        fields = '__all__'

class LoadMeasurementSerializer(serializers.ModelSerializer):
    class Meta:
        model = LoadMeasurement
        fields = '__all__'
