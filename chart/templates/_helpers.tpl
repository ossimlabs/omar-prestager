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

{{/* Template to populate the database connection info in the groovy configmap */}}
{{- define "omar-prestager.dbInfo" -}}
database:
  name: {{ .Values.database.name }}
  username: {{ .Values.database.user }}
  password: {{ .Values.database.password }}
  host: {{ .Values.database.host }}
  port: {{ .Values.database.port }}
{{- end -}}

{{/* Templates for the configMap mounts section */}}

{{- define "omar-prestager.mountBuckets" -}}
{{- range $volumeName := .Values.volumeNames }}
{{- $volumeDict := index $.Values.global.volumes $volumeName }}
- bucket: {{ $volumeDict.mountPath | replace "/" "" }}
  ingestDirectory: ingest
  archiveDirectory: archive
  unzipDirectory: unzipped
{{- end -}}
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

{{- define "omar-prestager.volumeMounts" -}}
{{- include "omar-prestager.volumeMounts.configmaps" . -}}
{{- include "omar-prestager.volumeMounts.pvcs" . -}}
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

{{- define "omar-prestager.volumes" -}}
{{- include "omar-prestager.volumes.configmaps" . -}}
{{- include "omar-prestager.volumes.pvcs" . -}}
{{- end -}}
