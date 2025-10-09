; NSIS script for SchedulerFX JavaFX app
; This script will create an installer that bundles your JAR and a JRE

!define APPNAME "iSmart Schedule"
!define COMPANYNAME "YourCompany"
!define DESCRIPTION "Smart Weekly Scheduler"
!define VERSION "1.0"
!define JRE_SUBDIR "jre\jdk-25+36-jre"

; Include license page
LicenseData "LICENSE.txt"

; Installer output name
OutFile "iSmart-Schedule-Setup.exe"

; Default installation folder
InstallDir "$PROGRAMFILES\${APPNAME}"

; Request application privileges for Windows Vista+
RequestExecutionLevel admin

; Installer icon (optional)
Icon "calendar-blue.ico"

; Installer pages
Page license
Page instfiles

;--------------------------------

; The stuff to install
Section "Install"
    SetOutPath "$INSTDIR"
    
    ; Write the uninstaller
    WriteUninstaller "$INSTDIR\uninst.exe"
    
    ; Specify files
  File "iSmart-Schedule.jar"
    File "calendar-blue.png"
    File "calendar-blue.ico"
    File "LICENSE.txt"
    
    ; Copy JRE with directory structure
    SetOutPath "$INSTDIR\jre"
    File /r "jre\*.*"
    
    ; Return to main install directory
    SetOutPath "$INSTDIR"
    
    ; Create batch file launcher
    FileOpen $0 "$INSTDIR\launch.bat" w
    FileWrite $0 '@echo off$\r$\n'
    FileWrite $0 'cd /d "%~dp0"$\r$\n'
  FileWrite $0 '"jre\jdk-25+36-jre\bin\java.exe" -jar "iSmart-Schedule.jar"$\r$\n'
    FileWrite $0 'if errorlevel 1 ($\r$\n'
    FileWrite $0 '  echo.$\r$\n'
    FileWrite $0 '  echo Application failed to start. Press any key to exit...$\r$\n'
    FileWrite $0 '  pause > nul$\r$\n'
    FileWrite $0 ')$\r$\n'
    FileClose $0

  ; Create shortcuts pointing to batch file
  CreateShortCut "$DESKTOP\${APPNAME}.lnk" "$INSTDIR\launch.bat" "" "$INSTDIR\calendar-blue.ico" 0
  CreateDirectory "$SMPROGRAMS\${APPNAME}"
  CreateShortCut "$SMPROGRAMS\${APPNAME}\${APPNAME}.lnk" "$INSTDIR\launch.bat" "" "$INSTDIR\calendar-blue.ico" 0
SectionEnd

Section "Uninstall"
  ; Remove shortcuts
  Delete "$DESKTOP\${APPNAME}.lnk"
  Delete "$SMPROGRAMS\${APPNAME}\${APPNAME}.lnk"
  RMDir "$SMPROGRAMS\${APPNAME}"

  ; Remove files
  Delete "$INSTDIR\iSmart-Schedule.jar"
  Delete "$INSTDIR\calendar-blue.png"
  Delete "$INSTDIR\launch.bat"
  RMDir /r "$INSTDIR\jre"
  Delete "$INSTDIR\calendar-blue.ico"
  Delete "$INSTDIR\LICENSE.txt"
  Delete "$INSTDIR\uninst.exe"
  RMDir "$INSTDIR"
SectionEnd
