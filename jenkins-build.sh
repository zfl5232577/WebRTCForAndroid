#!/bin/sh

echo "Upload the remote file downloader."
appDir="/repo/maven-snapshots"
cp -r ${WORKSPACE}/maven-snapshots/* ${appDir};

echo "Pull repo."
cd ${appDir};
git pull origin master;
echo "Push file to maven."
git add -A;
git commit -m "Push file to maven.";
git push origin master;


#echo ======================Execute sonarqube======================
#gradle sonarqube;
#/usr/local/gradle/gradle-4.1/bin/gradle sonarqube;


echo ======================jenkins-build finish======================