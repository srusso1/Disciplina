param(
    [Parameter(Mandatory=$true)]
    [string]$RuntimePath,
    [string]$Launch4jExe = "C:\Program Files (x86)\Launch4j\launch4jc.exe",
    [string]$InnoExe     = "C:\Program Files (x86)\Inno Setup 6\ISCC.exe",
    [string]$Version,
    [string]$Notes = "Actualizacion generada por build-win.ps1",
    [string]$ManifestPath,
    [string]$MinSupportedVersion = "1.0.0",
    [string]$Channel = "stable",
    [string]$PublishUpdatesDir = "C:\Disciplina\updates"
)

$ErrorActionPreference = "Stop"

# Rutas base
$ProjectRoot     = (Resolve-Path "$PSScriptRoot\..\..").Path
$VersionPropsPath = Join-Path $ProjectRoot "version.properties"
$DistApp         = Join-Path $ProjectRoot "dist\app"
$RuntimeDest     = Join-Path $DistApp "runtime"
$DependencyTarget = Join-Path $ProjectRoot "target\dependency"
$DistInstaller   = Join-Path $ProjectRoot "dist\installer"

# Cargar valores desde version.properties si existe
if (Test-Path $VersionPropsPath) {
    Write-Host "[info] Cargando configuracion desde version.properties..." -ForegroundColor Gray
    $props = ConvertFrom-StringData (Get-Content $VersionPropsPath -Raw)

    $propVersion = $props.'app.version'
    if ($propVersion) {
        $Version = $propVersion

        # Sincronizar pom.xml con la version del properties
        $PomPath = Join-Path $ProjectRoot "pom.xml"
        if (Test-Path $PomPath) {
            [xml]$xml = Get-Content $PomPath
            if ($xml.project.version -ne $Version) {
                Write-Host "[info] Sincronizando pom.xml con la nueva version: $Version" -ForegroundColor Gray
                $xml.project.version = $Version
                $xml.Save($PomPath)
            }
        }
    }

    if ($props.'app.release.notes' -and ($Notes -eq "Actualizacion generada por build-win.ps1")) { $Notes = $props.'app.release.notes' }
    if ($props.'app.min.supported' -and ($MinSupportedVersion -eq "1.0.0")) { $MinSupportedVersion = $props.'app.min.supported' }
}

# JAR: maven-assembly o shade genera "disciplina-<version>-jar-with-dependencies.jar"
# o bien el jar simple "disciplina-<version>.jar" / "disciplina.jar"
# Intentamos primero el original (sin dependencias embebidas, junto con lib/)
$JarPrimary      = Join-Path $ProjectRoot "target\original-disciplina.jar"
$JarFallback     = Join-Path $ProjectRoot "target\disciplina.jar"

# DB: la app la busca en src\main\java\database o en src\main\resources\database
$DbSourcePrimary  = Join-Path $ProjectRoot "src\main\java\database\DisciplinaDB.db"
$DbSourceFallback = Join-Path $ProjectRoot "src\main\resources\database\DisciplinaDB.db"

$Launch4jConfig  = Join-Path $ProjectRoot "scripts\packaging\launch4j\disciplina.xml"
$InnoScript      = Join-Path $ProjectRoot "scripts\packaging\inno\Disciplina.iss"
$UpdateManifestScript = Join-Path $ProjectRoot "scripts\packaging\update-manifest.ps1"
$ExePath         = Join-Path $DistApp "Disciplina.exe"
$PublishedInstallerPath = Join-Path $PublishUpdatesDir "Disciplina-Setup.exe"

# Helpers
function Get-EffectiveVersion {
    param([string]$RequestedVersion, [string]$PomFile)
    if (-not [string]::IsNullOrWhiteSpace($RequestedVersion)) { return $RequestedVersion }
    if (-not (Test-Path $PomFile)) { throw "No se encontro pom.xml para inferir version. Usa -Version." }
    [xml]$pom = Get-Content $PomFile
    $v = $pom.project.version
    if ($v -and -not [string]::IsNullOrWhiteSpace($v)) { return $v.Trim() }
    throw "No se pudo inferir la version desde pom.xml. Usa -Version."
}

# Validaciones previas
if (-not (Test-Path $Launch4jExe)) { throw "No se encontro Launch4j en: $Launch4jExe" }
if (-not (Test-Path $InnoExe))     { throw "No se encontro Inno Setup en: $InnoExe" }

$RuntimePathResolved = (Resolve-Path $RuntimePath).Path

Write-Host "========================" -ForegroundColor Cyan
Write-Host "CONSTRUCCION DE DISCIPLINA" -ForegroundColor Cyan
Write-Host "========================" -ForegroundColor Cyan

# [1/6] Compilar
Write-Host "[1/6] Limpiando y compilando jar..." -ForegroundColor Yellow
Set-Location $ProjectRoot
Write-Host "Ejecutando Maven..." -ForegroundColor Gray

# Ejecutar Maven sin redireccionamiento para evitar conflictos con warnings de Java
cmd /c ".\mvnw.cmd -DskipTests clean package"

if ($LASTEXITCODE -ne 0) {
    throw "Maven falló durante clean package"
}

cmd /c ".\mvnw.cmd -DskipTests dependency:copy-dependencies -DincludeScope=runtime -DoutputDirectory=""$DependencyTarget"""

if ($LASTEXITCODE -ne 0) {
    throw "Maven falló durante dependency:copy-dependencies"
}

Write-Host "Maven completado exitosamente." -ForegroundColor Gray

# Determinar JAR final
$JarPath = $null
if (Test-Path $JarPrimary) {
    $JarPath = $JarPrimary
} elseif (Test-Path $JarFallback) {
    $JarPath = $JarFallback
} else {
    # Buscar cualquier jar que coincida con el artifactId
    $found = Get-ChildItem -Path (Join-Path $ProjectRoot "target") -Filter "disciplina*.jar" -File -ErrorAction SilentlyContinue |
            Where-Object { $_.Name -notmatch "sources|javadoc|tests" } |
            Sort-Object LastWriteTime -Descending |
            Select-Object -First 1
    if ($found) { $JarPath = $found.FullName }
}

if (-not (Test-Path $JarPath)) {
    throw "No se encontro el jar en target/. Revisa que Maven haya compilado correctamente."
}

# [2/6] Preparar dist\app
Write-Host "[2/6] Preparando carpeta dist\app..." -ForegroundColor Yellow

if (Test-Path $DistApp) {
    try {
        Remove-Item $DistApp -Recurse -Force
    }
    catch {
        throw "No se pudo limpiar dist\app. Cierra Disciplina.exe (y cualquier javaw.exe del paquete) y reintenta."
    }
}

New-Item -ItemType Directory -Path $DistApp | Out-Null

# [3/6] Copiar artefactos
Write-Host "[3/6] Copiando artefactos..." -ForegroundColor Yellow

# JAR principal → Disciplina.jar (nombre fijo que espera disciplina.xml)
Copy-Item $JarPath (Join-Path $DistApp "Disciplina.jar") -Force

# Carpeta lib con dependencias runtime
New-Item -ItemType Directory -Path (Join-Path $DistApp "lib") | Out-Null

$DepJars = Get-ChildItem -Path $DependencyTarget -Filter "*.jar" -ErrorAction SilentlyContinue

if (-not $DepJars) {
    throw "No se encontraron dependencias runtime en target\dependency."
}

Copy-Item (Join-Path $DependencyTarget "*.jar") (Join-Path $DistApp "lib") -Force

# Base de datos inicial
New-Item -ItemType Directory -Path (Join-Path $DistApp "database") | Out-Null

$DbSource = $null

if (Test-Path $DbSourcePrimary)  {
    $DbSource = $DbSourcePrimary
}
elseif (Test-Path $DbSourceFallback) {
    $DbSource = $DbSourceFallback
}

if (-not $DbSource) {
    throw "No se encontro DisciplinaDB.db en src\main\java\database ni en src\main\resources\database."
}

Copy-Item $DbSource (Join-Path $DistApp "database\DisciplinaDB.db") -Force

# [4/6] Runtime embebido
Write-Host "[4/6] Copiando runtime embebido desde: $RuntimePathResolved" -ForegroundColor Yellow
Copy-Item $RuntimePathResolved $RuntimeDest -Recurse -Force

# [5/6] Launch4j → Disciplina.exe
Write-Host "[5/6] Generando EXE con Launch4j..." -ForegroundColor Yellow

$Launch4jConfigTemp = Join-Path $env:TEMP "disciplina_launch4j_tmp.xml"
$projectRootForward = $ProjectRoot.Replace("\\", "/")

(Get-Content $Launch4jConfig -Raw) -replace "DISCIPLINA_ROOT", $projectRootForward |
        Set-Content $Launch4jConfigTemp -Encoding UTF8

& "$Launch4jExe" "$Launch4jConfigTemp"

if ($LASTEXITCODE -ne 0 -or -not (Test-Path $ExePath)) {
    throw "Launch4j no genero Disciplina.exe correctamente."
}

Remove-Item $Launch4jConfigTemp -Force -ErrorAction SilentlyContinue

# [6/6] Inno Setup → instalador
$EffectiveVersion = Get-EffectiveVersion -RequestedVersion $Version -PomFile (Join-Path $ProjectRoot "pom.xml")

Write-Host "[6/6] Generando instalador con Inno Setup (v$EffectiveVersion)..." -ForegroundColor Yellow

if (-not (Test-Path $DistInstaller)) {
    New-Item -ItemType Directory -Path $DistInstaller | Out-Null
}

& "$InnoExe" "/dAppVersion=$EffectiveVersion" "$InnoScript"

if ($LASTEXITCODE -ne 0) {
    throw "Inno Setup no genero el instalador correctamente."
}

if (-not (Test-Path $UpdateManifestScript)) {
    throw "No se encontro el script de manifest: $UpdateManifestScript"
}

$InstallerArtifact = Get-ChildItem -Path $DistInstaller -Filter "Disciplina-Setup*.exe" -File -ErrorAction SilentlyContinue |
        Sort-Object LastWriteTime -Descending |
        Select-Object -First 1

if (-not $InstallerArtifact) {
    throw "No se encontro el instalador generado en dist\installer."
}

if (-not (Test-Path $PublishUpdatesDir)) {
    New-Item -ItemType Directory -Path $PublishUpdatesDir -Force | Out-Null
}

Copy-Item $InstallerArtifact.FullName $PublishedInstallerPath -Force

if ([string]::IsNullOrWhiteSpace($ManifestPath)) {
    $ManifestPath = Join-Path $PublishUpdatesDir "win-x64\$Channel\manifest.json"
}

Write-Host "[post] Actualizando manifiesto de updates..." -ForegroundColor Yellow

$ManifestParams = @{
    Version = $EffectiveVersion
    ArtifactPath = $PublishedInstallerPath
    Channel = $Channel
    MinSupportedVersion = $MinSupportedVersion
    Notes = $Notes
    ManifestPath = $ManifestPath
}

& "$UpdateManifestScript" @ManifestParams

# Resumen
$Installer = Get-ChildItem -Path $DistInstaller -Filter "Disciplina-Setup*.exe" -File |
        Sort-Object LastWriteTime -Descending |
        Select-Object -First 1

Write-Host ""
Write-Host "============================================================" -ForegroundColor Green
Write-Host "Build completado exitosamente (v$EffectiveVersion)." -ForegroundColor Green
Write-Host "  EXE portatil : $ExePath" -ForegroundColor Green

if ($Installer) {
    Write-Host "  Instalador   : $($Installer.FullName)" -ForegroundColor Green
}

Write-Host "  Updates      : $PublishUpdatesDir" -ForegroundColor Green
Write-Host "============================================================" -ForegroundColor Green