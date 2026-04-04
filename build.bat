@echo off
cd /d "%~dp0"
apache-maven-3.9.12\bin\mvn.cmd package -DskipTests
