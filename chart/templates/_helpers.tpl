{{- define "omar-prestager.imagePullSecret" }}
{{- printf "{\"auths\": {\"%s\": {\"auth\": \"%s\"}}}" .Values.global.imagePullSecret.registry (printf "%s:%s" .Values.global.imagePullSecret.username .Values.global.imagePullSecret.password | b64enc) | b64enc }}
{{- end }}

{{/* Template for env vars */}}
{{- define "omar-prestager.envVars" -}}
  {{- range $key, $value := .Values.envVars }}
  - name: {{ $key | quote }}
    value: {{ $value | quote }}
  {{- end }}
{{- end -}}


{{/* Templates for the volumeMounts section */}}

{{- define "omar-prestager.volumeMounts.configmaps" -}}
{{- range $configmap := .Values.configmaps}}
- name: {{ $configmap.internalName | quote }}
  mountPath: {{ $configmap.mountPath | quote }}
  {{- if $configmap.subPath }}
  subPath: {{ $configmap.subPath | quote }}
  {{- end }}
{{- end -}}
{{- end -}}

{{- define "omar-prestager.volumeMounts.pvcs" -}}
{{- range $volumeName := .Values.volumeNames }}
{{- $volumeDict := index $.Values.global.volumes $volumeName }}
- name: {{ $volumeName }}
  mountPath: {{ $volumeDict.mountPath }}
  {{- if $volumeDict.subPath }}
  subPath: {{ $volumeDict.subPath | quote }}
  {{- end }}
{{- end -}}
{{- end -}}


{{- define "omar-prestager.volumeMounts.existing" -}}
{{- range $existingVolumeName, $existingVolumeDict := .Values.existingVolumes }}
- name: {{ $existingVolumeName }}
  mountPath: {{ $existingVolumeDict.mountPath }}
{{- end -}}
{{- end -}}

{{- define "omar-prestager.volumeMounts" -}}
{{- include "omar-prestager.volumeMounts.configmaps" . -}}
{{- include "omar-prestager.volumeMounts.pvcs" . -}}
{{- include "omar-prestager.volumeMounts.existing" . -}}
{{- end -}}





{{/* Templates for the volumes section */}}

{{- define "omar-prestager.volumes.configmaps" -}}
{{- range $configmap := .Values.configmaps}}
- name: {{ $configmap.internalName | quote }}
  configMap:
    name: {{ $configmap.name | quote }}
{{- end -}}
{{- end -}}

{{- define "omar-prestager.volumes.pvcs" -}}
{{- range $volumeName := .Values.volumeNames }}
{{- $volumeDict := index $.Values.global.volumes $volumeName }}
- name: {{ $volumeName }}
  persistentVolumeClaim:
    claimName: "{{ $.Values.appName }}-{{ $volumeName }}-pvc"
{{- end -}}
{{- end -}}

{{- define "omar-prestager.volumes.existing" -}}
{{- range $existingVolumeName, $existingVolumeDict := .Values.existingVolumes }}
- name: {{ $existingVolumeName }}
  {{- if eq $existingVolumeDict.type "empty" }}
  emptyDir: {}
  {{- end }} 
{{- end -}}
{{- end -}}

{{- define "omar-prestager.volumes" -}}
{{- include "omar-prestager.volumes.configmaps" . -}}
{{- include "omar-prestager.volumes.pvcs" . -}}
{{- include "omar-prestager.volumes.existing" . -}}
{{- end -}}
