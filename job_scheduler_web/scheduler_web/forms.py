from django import forms

class AddServerForm(forms.Form):
    displayName = forms.CharField(label='Name', max_length=32)
    address = forms.CharField(label='Host Address', max_length=32)
    port = forms.IntegerField(label='Host Port')

class AddJobForm(forms.Form):
    command = forms.CharField(label='Command', max_length = 256)
    priority = forms.IntegerField(label='Priority')
