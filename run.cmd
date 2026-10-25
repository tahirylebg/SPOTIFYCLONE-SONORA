@echo off
setlocal
rem Usage : run.cmd [backend|web|desktop]   (desktop par défaut)
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0run.ps1" %*
