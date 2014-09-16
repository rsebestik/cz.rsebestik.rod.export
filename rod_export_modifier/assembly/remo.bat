@REM THIS FILE IS FOR CONVINIENT USAGE
@ECHO off

echo ---- Application needs Java6
java -version

echo ---- Starting...
java -jar "%~dp0\${project.artifactId}-${project.version}\${project.artifactId}-${project.version}.jar" %*
 
echo ---- End.
@ECHO on