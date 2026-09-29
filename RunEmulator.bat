@echo off
echo Starting Emulator (if not already running)...
:: We use 'start' to run the emulator in a separate window so it opens visibly and doesn't block the script
start "" "%LOCALAPPDATA%\Android\Sdk\emulator\emulator" -avd Pixel_9 -no-boot-anim

echo Waiting for device to boot...
"%LOCALAPPDATA%\Android\Sdk\platform-tools\adb" wait-for-device

echo Building and installing the app...
call gradlew installDebug

echo Launching the app...
"%LOCALAPPDATA%\Android\Sdk\platform-tools\adb" shell am start -n com.companion.muslim.app/com.example.MainActivity

echo Done!
