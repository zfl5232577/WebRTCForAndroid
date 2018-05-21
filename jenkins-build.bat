@echo off

echo ======================Execute sonarqube======================
call gradle sonarqube
echo;

@echo off
echo ======================jenkins-build finish======================
echo;