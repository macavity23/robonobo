Name "robonobo"
OutFile "target\dist\robonobo-${VERSION}-installer.exe"
InstallDir "$PROGRAMFILES\robonobo"
DirText "This will install robonobo on your computer."

!define PRODUCT_NAME "robonobo"
!define JRE_VERSION "6.0"
!define JRE_URL "http://javadl.sun.com/webapps/download/AutoDL?BundleId=41308"
!define JAVAEXE "javaw.exe"

RequestExecutionLevel user

!include "FileFunc.nsh"
!insertmacro GetFileVersion
!insertmacro GetParameters
!include "WordFunc.nsh"
!insertmacro VersionCompare

Section ""
Call GetJRE
Pop $R0
; MessageBox MB_ICONSTOP "Got java: $R0" 
SetOutPath $INSTDIR
File target\dist\robonobo-${VERSION}.exe
CreateShortCut "$SMPROGRAMS\robonobo.lnk" "$INSTDIR\robonobo-${VERSION}.exe"
WriteUninstaller $INSTDIR\uninstall.exe
WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\robonobo" "DisplayName" "robonobo - a music sharing application"
WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\robonobo" "UninstallString" "$\"$INSTDIR\uninstall.exe$\""
WriteRegStr HKCR "rbnb" "URL Protocol" ""
WriteRegStr HKCR "rbnb" "(Default)" "URL: robonobo playlist or action"
WriteRegStr HKCR "rbnb\DefaultIcon" "(Default)" "$INSTDIR\robonobo-${VERSION}.exe,1"
WriteRegStr HKCR "rbnb\shell\open\command" "(Default)" "$INSTDIR\robonobo-${VERSION}.exe %1"
SectionEnd

Section "uninstall"
Delete $INSTDIR\uninstall.exe
Delete $INSTDIR\robonobo-${VERSION}.exe
Delete "$SMPROGRAMS\robonobo.lnk"
RMDir $INSTDIR
DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\robonobo"
DeleteRegKey HKCR "rbnb"
SectionEnd

;  JRE stuff taken from http://nsis.sourceforge.net/Java_Launcher_with_automatic_JRE_installation 
;  returns the full path of a valid java.exe
;  looks in:
;  1 - .\jre directory (JRE Installed with application)
;  2 - JAVA_HOME environment variable
;  3 - the registry
;  4 - hopes it is in current dir or PATH
Function GetJRE
    Push $R0
    Push $R1
    Push $2
 
  ; 1) Check local JRE
  CheckLocal:
    ClearErrors
    StrCpy $R0 "$EXEDIR\jre\bin\${JAVAEXE}"
    IfFileExists $R0 JreFound
 
  ; 2) Check for JAVA_HOME
  CheckJavaHome:
    ClearErrors
    ReadEnvStr $R0 "JAVA_HOME"
    StrCpy $R0 "$R0\bin\${JAVAEXE}"
    IfErrors CheckRegistry     
    IfFileExists $R0 0 CheckRegistry
    Call CheckJREVersion
    IfErrors CheckRegistry JreFound
 
  ; 3) Check for registry
  CheckRegistry:
    ClearErrors
    ReadRegStr $R1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
    ReadRegStr $R0 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$R1" "JavaHome"
    StrCpy $R0 "$R0\bin\${JAVAEXE}"
    IfErrors DownloadJRE
    IfFileExists $R0 0 DownloadJRE
    Call CheckJREVersion
    IfErrors DownloadJRE JreFound
 
  DownloadJRE:
    Call ElevateToAdmin
    MessageBox MB_ICONINFORMATION "${PRODUCT_NAME} uses Java Runtime Environment ${JRE_VERSION}, it will now be downloaded and installed."
    StrCpy $2 "$TEMP\Java Runtime Environment.exe"
    nsisdl::download /TIMEOUT=30000 ${JRE_URL} $2
    Pop $R0 ;Get the return value
    StrCmp $R0 "success" +3
      MessageBox MB_ICONSTOP "Download failed: $R0"
      Abort
    ExecWait $2
    Delete $2
 
    ReadRegStr $R1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
    ReadRegStr $R0 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$R1" "JavaHome"
    StrCpy $R0 "$R0\bin\${JAVAEXE}"
    IfFileExists $R0 0 GoodLuck
    Call CheckJREVersion
    IfErrors GoodLuck JreFound
 
  ; 4) wishing you good luck
  GoodLuck:
    StrCpy $R0 "${JAVAEXE}"
    ; MessageBox MB_ICONSTOP "Cannot find appropriate Java Runtime Environment."
    ; Abort
 
  JreFound:
    Pop $2
    Pop $R1
    Exch $R0
FunctionEnd

; Attempt to give the UAC plug-in a user process and an admin process.
Function ElevateToAdmin
  UAC_Elevate:
    UAC::RunElevated
    StrCmp 1223 $0 UAC_ElevationAborted ; UAC dialog aborted by user?
    StrCmp 0 $0 0 UAC_Err ; Error?
    StrCmp 1 $1 0 UAC_Success ;Are we the real deal or just the wrapper?
    Quit
 
  UAC_ElevationAborted:
    # elevation was aborted, run as normal?
    MessageBox MB_ICONSTOP "This installer requires admin access, aborting!"
    Abort
 
  UAC_Err:
    MessageBox MB_ICONSTOP "Unable to elevate, error $0"
    Abort
 
  UAC_Success:
    StrCmp 1 $3 +4 ;Admin?
    StrCmp 3 $1 0 UAC_ElevationAborted ;Try again?
    MessageBox MB_ICONSTOP "This installer requires admin access, try again"
    goto UAC_Elevate 
FunctionEnd

; Pass the "javaw.exe" path by $R0
Function CheckJREVersion
    Push $R1
 
    ; Get the file version of javaw.exe
    ${GetFileVersion} $R0 $R1
    ${VersionCompare} ${JRE_VERSION} $R1 $R1
 
    ; Check whether $R1 != "1"
    ClearErrors
    StrCmp $R1 "1" 0 CheckDone
    SetErrors
 
  CheckDone:
    Pop $R1
FunctionEnd
