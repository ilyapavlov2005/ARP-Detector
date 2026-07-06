; ARP Spoofing Detector - Professional Installer
; Inno Setup Script

[Setup]
AppName=ARP Spoofing Detector
AppVersion=1.0
AppPublisher=Your Name
AppPublisherURL=https://github.com/arpdetector
AppSupportURL=https://github.com/arpdetector
AppUpdatesURL=https://github.com/arpdetector
DefaultDirName={pf}\ARP Spoofing Detector
DefaultGroupName=ARP Spoofing Detector
AllowNoIcons=yes
LicenseFile=license.txt
OutputDir=output
OutputBaseFilename=ARPSpoofingDetector_Setup
Compression=lzma2
SolidCompression=yes
WizardStyle=modern
PrivilegesRequired=admin
ArchitecturesInstallIn64BitMode=x64
SetupIconFile=..\..\icon.ico
UninstallDisplayIcon={app}\ARPSpoofingDetector.exe
UninstallDisplayName=ARP Spoofing Detector

[Languages]
Name: "russian"; MessagesFile: "compiler:Languages\Russian.isl"
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked
Name: "startup"; Description: "Автоматический запуск при старте системы"; GroupDescription: "Дополнительные задачи"

[Files]
Source: "..\ARP_Detector_Portable\app\ARPSpoofingDetector.jar"; DestDir: "{app}\app"; Flags: ignoreversion
Source: "..\ARP_Detector_Portable\jre\*"; DestDir: "{app}\jre"; Flags: recursesubdirs ignoreversion
Source: "..\ARP_Detector_Portable\run.bat"; DestDir: "{app}"; Flags: ignoreversion
Source: "..\ARP_Detector_Portable\run.vbs"; DestDir: "{app}"; Flags: ignoreversion

[Icons]
Name: "{group}\ARP Spoofing Detector"; Filename: "{app}\run.bat"; IconFilename: "{app}\run.bat"; WorkingDir: "{app}"
Name: "{group}\{cm:UninstallProgram,ARP Spoofing Detector}"; Filename: "{uninstallexe}"
Name: "{commondesktop}\ARP Spoofing Detector"; Filename: "{app}\run.bat"; Tasks: desktopicon; IconFilename: "{app}\run.bat"; WorkingDir: "{app}"
Name: "{userstartup}\ARP Spoofing Detector"; Filename: "{app}\run.bat"; Tasks: startup; WorkingDir: "{app}"

[Run]
Filename: "{app}\run.bat"; Description: "{cm:LaunchProgram,ARP Spoofing Detector}"; Flags: postinstall nowait skipifsilent runascurrentuser

[UninstallDelete]
Type: filesandordirs; Name: "{app}\logs"
Type: dirifempty; Name: "{app}"

[Registry]
Root: HKLM; Subkey: "Software\Microsoft\Windows\CurrentVersion\Run"; ValueType: string; ValueName: "ARPSpoofingDetector"; ValueData: "{app}\run.bat"; Flags: uninsdeletevalue; Tasks: startup
Root: HKLM; Subkey: "Software\ARP Spoofing Detector"; ValueType: string; ValueName: "InstallPath"; ValueData: "{app}"; Flags: uninsdeletekey