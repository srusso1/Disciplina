param(
    [Parameter(Mandatory=$true)]
    [string]$RuntimePath,
    [string]$Launch4jExe = "C:\Program Files (x86)\Launch4j\launch4jc.exe",
    [string]$InnoExe     = "C:\Program Files (x86)\Inno Setup 6\ISCC.exe",
    [string]$Version
)

$ErrorActionPreference = "Stop"

# ── Rutas base ─────────────────────────────────────────────────────────────────
$ProjectRoot     = (Resolve-Path "$PSScriptRoot\..\..").Path
$DistApp         = Join-Path $ProjectRoot "dist\app"
$RuntimeDest     = Join-Path $DistApp "runtime"
$DependencyTarget = Join-Path $ProjectRoot "target\dependency"
$DistInstaller   = Join-Path $ProjectRoot "dist\installer"

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
$ExePath         = Join-Path $DistApp "Disciplina.exe"

# ── Helpers ────────────────────────────────────────────────────────────────────
function Get-EffectiveVersion {
    param([string]$RequestedVersion, [string]$PomFile)
    if (-not [string]::IsNullOrWhiteSpace($RequestedVersion)) { return $RequestedVersion }
    if (-not (Test-Path $PomFile)) { throw "No se encontro pom.xml para inferir version. Usa -Version." }
    [xml]$pom = Get-Content $PomFile
    $v = $pom.project.version
    if ($v -and -not [string]::IsNullOrWhiteSpace($v)) { return $v.Trim() }
    throw "No se pudo inferir la version desde pom.xml. Usa -Version."
}

# ── Validaciones previas ───────────────────────────────────────────────────────
if (-not (Test-Path $Launch4jExe)) { throw "No se encontro Launch4j en: $Launch4jExe" }
if (-not (Test-Path $InnoExe))     { throw "No se encontro Inno Setup en: $InnoExe" }

$RuntimePathResolved = (Resolve-Path $RuntimePath).Path

# ── [1/6] Compilar ─────────────────────────────────────────────────────────────
Write-Host "[1/6] Limpiando y compilando jar..." -ForegroundColor Cyan
Set-Location $ProjectRoot
.\mvnw.cmd -q -DskipTests clean package
.\mvnw.cmd -q -DskipTests dependency:copy-dependencies -DincludeScope=runtime -DoutputDirectory="$DependencyTarget"

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
if (-not $JarPath -or -not (Test-Path $JarPath)) {
    throw "No se encontro el jar en target/. Revisa que Maven haya compilado correctamente."
}

# ── [2/6] Preparar dist\app ────────────────────────────────────────────────────
Write-Host "[2/6] Preparando carpeta dist\app..." -ForegroundColor Cyan
if (Test-Path $DistApp) {
    try { Remove-Item $DistApp -Recurse -Force }
    catch { throw "No se pudo limpiar dist\app. Cierra Disciplina.exe (y cualquier javaw.exe del paquete) y reintenta." }
}
New-Item -ItemType Directory -Path $DistApp | Out-Null

# ── [3/6] Copiar artefactos ────────────────────────────────────────────────────
Write-Host "[3/6] Copiando artefactos..." -ForegroundColor Cyan

# JAR principal → Disciplina.jar (nombre fijo que espera disciplina.xml)
Copy-Item $JarPath (Join-Path $DistApp "Disciplina.jar") -Force

# Carpeta lib con dependencias runtime
New-Item -ItemType Directory -Path (Join-Path $DistApp "lib") | Out-Null
$DepJars = Get-ChildItem -Path $DependencyTarget -Filter "*.jar" -ErrorAction SilentlyContinue
if (-not $DepJars) { throw "No se encontraron dependencias runtime en target\dependency." }
Copy-Item (Join-Path $DependencyTarget "*.jar") (Join-Path $DistApp "lib") -Force

# Base de datos inicial
New-Item -ItemType Directory -Path (Join-Path $DistApp "database") | Out-Null
$DbSource = $null
if (Test-Path $DbSourcePrimary)  { $DbSource = $DbSourcePrimary }
elseif (Test-Path $DbSourceFallback) { $DbSource = $DbSourceFallback }
if (-not $DbSource) { throw "No se encontro DisciplinaDB.db en src\main\java\database ni en src\main\resources\database." }
Copy-Item $DbSource (Join-Path $DistApp "database\DisciplinaDB.db") -Force

# ── [4/6] Runtime embebido ─────────────────────────────────────────────────────
Write-Host "[4/6] Copiando runtime embebido desde: $RuntimePathResolved" -ForegroundColor Cyan
Copy-Item $RuntimePathResolved $RuntimeDest -Recurse -Force

# ── [5/6] Launch4j → Disciplina.exe ───────────────────────────────────────────
Write-Host "[5/6] Generando EXE con Launch4j..." -ForegroundColor Cyan
$Launch4jConfigTemp = Join-Path $env:TEMP "disciplina_launch4j_tmp.xml"
$projectRootForward = $ProjectRoot.Replace("\\", "/")
(Get-Content $Launch4jConfig -Raw) -replace "DISCIPLINA_ROOT", $projectRootForward |
    Set-Content $Launch4jConfigTemp -Encoding UTF8

& "$Launch4jExe" "$Launch4jConfigTemp"
if ($LASTEXITCODE -ne 0 -or -not (Test-Path $ExePath)) {
    throw "Launch4j no genero Disciplina.exe correctamente."
}
Remove-Item $Launch4jConfigTemp -Force -ErrorAction SilentlyContinue

# ── [6/6] Inno Setup → instalador ─────────────────────────────────────────────
$EffectiveVersion = Get-EffectiveVersion -RequestedVersion $Version -PomFile (Join-Path $ProjectRoot "pom.xml")

Write-Host "[6/6] Generando instalador con Inno Setup (v$EffectiveVersion)..." -ForegroundColor Cyan
if (-not (Test-Path $DistInstaller)) { New-Item -ItemType Directory -Path $DistInstaller | Out-Null }
& "$InnoExe" "/dAppVersion=$EffectiveVersion" "$InnoScript"
if ($LASTEXITCODE -ne 0) { throw "Inno Setup no genero el instalador correctamente." }

# ── Resumen ────────────────────────────────────────────────────────────────────
$Installer = Get-ChildItem -Path $DistInstaller -Filter "Disciplina-Setup*.exe" -File |
             Sort-Object LastWriteTime -Descending | Select-Object -First 1

Write-Host ""
Write-Host "============================================================" -ForegroundColor Green
Write-Host " Build completado exitosamente." -ForegroundColor Green
Write-Host "  EXE portátil : $ExePath" -ForegroundColor Green
if ($Installer) {
Write-Host "  Instalador   : $($Installer.FullName)" -ForegroundColor Green
}
Write-Host "============================================================" -ForegroundColor Green
