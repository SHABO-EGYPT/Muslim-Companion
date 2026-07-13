@echo off
echo Starting Emulator (if not already running)...
:: We use 'start /B' to run the emulator in the background so it doesn't block the script
start /B "" "%LOCALAPPDATA%\Android\Sdk\emulator\emulator" -avd Pixel_9 -no-snapshot-save -no-boot-anim

echo Waiting for device to boot...
"%LOCALAPPDATA%\Android\Sdk\platform-tools\adb" wait-for-device

echo Building and installing the app...
call gradlew installDebug

echo Launching the app...
"%LOCALAPPDATA%\Android\Sdk\platform-tools\adb" shell am start -n com.muslim.companion/com.example.MainActivity

echo Done!
