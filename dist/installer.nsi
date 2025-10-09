; NSIS installer script for the iSmart Schedule desktop build
; Bundles the shaded jar together with the bundled JRE so the user
; gets a one-click installer that works without a system-wide Java install

!define APPNAME            "iSmart Schedule"
!define COMPANYNAME        "YourCompany"
!define DESCRIPTION        "Smart Weekly Scheduler"
!define VERSION            "1.0.0"
!define JAR_NAME           "iSmart-Schedule.jar"
!define BUILD_JAR_PATH     "..\target\${JAR_NAME}"
!define DIST_LICENSE       "LICENSE.txt"
!define DIST_ICON          "calendar-blue.ico"
!define DIST_IMAGE         "calendar-blue.png"
!define JRE_SUBDIR         "jre\jdk-25+36-jre"
!define INSTALL_JRE_DIR    "$INSTDIR\jre"
!define INSTALL_LAUNCHER   "$INSTDIR\launch.bat"
!define UNINSTALLER        "$INSTDIR\uninst.exe"
!define INCLUDE_DEBUG_SHORTCUTS 0

SetCompress auto
SetCompressor /SOLID lzma

; Include license page at runtime
LicenseData "${DIST_LICENSE}"

; Installer output name lives alongside this script
OutFile "iSmart-Schedule-Setup-${VERSION}.exe"

; Default installation folder
InstallDir "$PROGRAMFILES\${APPNAME}"

; Request application privileges for Windows Vista+
RequestExecutionLevel admin

; Installer icon (optional)
Icon "${DIST_ICON}"

; Installer pages
Page license
Page instfiles

;--------------------------------

; The stuff to install
Section "Install"
    SetOutPath "$INSTDIR"
    
    ; Write the uninstaller
    WriteUninstaller "${UNINSTALLER}"
    
    ; Specify files
    File "${DIST_IMAGE}"
    File "${DIST_ICON}"
    File "${DIST_LICENSE}"
    File "${BUILD_JAR_PATH}"
    
    ; Copy JRE with directory structure
    SetOutPath "${INSTALL_JRE_DIR}"
    File /r "jre\*.*"
    
    ; Return to main install directory
    SetOutPath "$INSTDIR"
    
    ; Create batch file launcher
    FileOpen $0 "${INSTALL_LAUNCHER}" w
    FileWrite $0 '@echo off$\r$\n'
    FileWrite $0 'cd /d "%~dp0"$\r$\n'
    FileWrite $0 '"${JRE_SUBDIR}\bin\java.exe" -jar "${JAR_NAME}"$\r$\n'
    FileWrite $0 'if errorlevel 1 ($\r$\n'
    FileWrite $0 '  echo.$\r$\n'
    FileWrite $0 '  echo Application failed to start. Press any key to exit...$\r$\n'
    FileWrite $0 '  pause > nul$\r$\n'
    FileWrite $0 ')$\r$\n'
    FileClose $0

  ; Create shortcuts that launch javaw (no console) to run the jar
  ; Target: bundled javaw.exe, Parameters: -jar "$INSTDIR\${JAR_NAME}"
  CreateShortCut "$DESKTOP\${APPNAME}.lnk" "$INSTDIR\${JRE_SUBDIR}\bin\javaw.exe" "-jar $\"$INSTDIR\${JAR_NAME}$\"" "$INSTDIR\${DIST_ICON}" 0
  CreateDirectory "$SMPROGRAMS\${APPNAME}"
  CreateShortCut "$SMPROGRAMS\${APPNAME}\${APPNAME}.lnk" "$INSTDIR\${JRE_SUBDIR}\bin\javaw.exe" "-jar $\"$INSTDIR\${JAR_NAME}$\"" "$INSTDIR\${DIST_ICON}" 0

  !if ${INCLUDE_DEBUG_SHORTCUTS}
    ; Also create debug shortcuts that call the bundled batch file (keeps console open for troubleshooting)
    CreateShortCut "$DESKTOP\${APPNAME} (Debug).lnk" "${INSTALL_LAUNCHER}" "" "$INSTDIR\${DIST_ICON}" 0
    CreateShortCut "$SMPROGRAMS\${APPNAME}\${APPNAME} (Debug).lnk" "${INSTALL_LAUNCHER}" "" "$INSTDIR\${DIST_ICON}" 0
  !endif

  ; Register uninstall information
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}" "DisplayName" "${APPNAME}"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}" "UninstallString" "${UNINSTALLER}"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}" "DisplayVersion" "${VERSION}"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}" "Publisher" "${COMPANYNAME}"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}" "InstallLocation" "$INSTDIR"
SectionEnd

Section "Uninstall"
  ; Remove shortcuts
  Delete "$DESKTOP\${APPNAME}.lnk"
  Delete "$SMPROGRAMS\${APPNAME}\${APPNAME}.lnk"
  !if ${INCLUDE_DEBUG_SHORTCUTS}
    Delete "$DESKTOP\${APPNAME} (Debug).lnk"
    Delete "$SMPROGRAMS\${APPNAME}\${APPNAME} (Debug).lnk"
  !endif
  RMDir "$SMPROGRAMS\${APPNAME}"

  ; Remove files
  Delete "$INSTDIR\${JAR_NAME}"
  Delete "$INSTDIR\${DIST_IMAGE}"
  Delete "$INSTDIR\${DIST_LICENSE}"
  Delete "$INSTDIR\${DIST_ICON}"
  Delete "${INSTALL_LAUNCHER}"
  RMDir /r "${INSTALL_JRE_DIR}"
  Delete "${UNINSTALLER}"
  RMDir "$INSTDIR"

  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}"
SectionEnd
