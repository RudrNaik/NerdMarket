@echo off
set SERVER_USER=cjhoy
set SERVER_HOST=coms-3090-022.class.las.iastate.edu
set REMOTE_DIR=/home/cjhoy/app
set WEBHOOK_URL=https://discord.com/api/webhooks/1472370317266784432/uwDG7fBRBd4kCupHkEA3X4hPUJxVaLB4JVBIva66GxaDaJRq98L3miOxQXh3fWnYPOph

echo Building...
call mvn clean package -DskipTests
if %ERRORLEVEL% neq 0 (
    curl -s -H "Content-Type: application/json" -d "{\"embeds\": [{\"title\": \":rocket: n3rd m@rket Deploy\", \"description\": \":x: Build failed\", \"color\": 15158332}]}" %WEBHOOK_URL%
    exit /b 1
)

echo Zipping...
powershell -Command "Compress-Archive -Path 'target\demo-0.0.1-SNAPSHOT.jar' -DestinationPath '%TEMP%\deploy.zip' -Force"

curl -s -H "Content-Type: application/json" -d "{\"embeds\": [{\"title\": \":rocket: n3rd m@rket Deploy\", \"description\": \":outbox_tray: Uploading...\", \"color\": 16776960}]}" %WEBHOOK_URL%

echo Uploading...
scp "%TEMP%\deploy.zip" %SERVER_USER%@%SERVER_HOST%:%REMOTE_DIR%/deploy.zip

if %ERRORLEVEL% neq 0 (
    curl -s -H "Content-Type: application/json" -d "{\"embeds\": [{\"title\": \":rocket: n3rd m@rket Deploy\", \"description\": \":x: Upload failed\", \"color\": 15158332}]}" %WEBHOOK_URL%
    exit /b 1
)

ssh %SERVER_USER%@%SERVER_HOST% "cd %REMOTE_DIR% && ./redeploy.sh"
del "%TEMP%\deploy.zip" 2>nul
echo Done!