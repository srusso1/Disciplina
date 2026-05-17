; Inno Setup script para Disciplina
[Setup]
AppId={{A1B2C3D4-E5F6-7890-ABCD-EF1234567890}
AppName=Disciplina
AppVersion=1.0.0
AppPublisher=Disciplina
SetupIconFile=..\..\..\src\main\resources\images\iconAppReal.ico
DefaultDirName={code:GetInstallDir}
DefaultGroupName=Disciplina
DisableProgramGroupPage=yes
OutputDir=..\..\..\dist\installer
OutputBaseFilename=Disciplina-Setup-{#AppVersion}
Compression=lzma
SolidCompression=yes
ArchitecturesInstallIn64BitMode=x64compatible
PrivilegesRequired=admin
WizardStyle=modern

[Languages]
Name: "spanish"; MessagesFile: "compiler:Languages\Spanish.isl"

[Tasks]
Name: "desktopicon"; Description: "Crear acceso directo en escritorio"; GroupDescription: "Accesos directos:"

[Files]
; Copia todo el contenido de dist\app (exe, jar, lib, runtime)
Source: "..\..\..\dist\app\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs
; Copia la base de datos inicial a AppData solo si no existe aun
Source: "..\..\..\dist\app\database\DisciplinaDB.db"; DestDir: "{localappdata}\Disciplina\data"; Flags: ignoreversion onlyifdoesntexist uninsneveruninstall

[Dirs]
Name: "{app}\updates"
Name: "{app}\updates\win-x64\stable"

[Icons]
Name: "{autoprograms}\Disciplina"; Filename: "{app}\Disciplina.exe"
Name: "{autodesktop}\Disciplina"; Filename: "{app}\Disciplina.exe"; Tasks: desktopicon

[Run]
Filename: "{app}\Disciplina.exe"; Description: "Abrir Disciplina"; Flags: nowait postinstall skipifsilent

[Code]
function GetInstallDir(Param: String): String;
begin
  Result := ExpandConstant('{sd}\Disciplina');
end;
