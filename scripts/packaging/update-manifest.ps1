param(
    [Parameter(Mandatory = $true)]
    [string]$Version,

    [Parameter(Mandatory = $true)]
    [string]$ArtifactPath,

    [string]$ManifestPath,
    [string]$Channel = "stable",
    [string]$MinSupportedVersion = "1.0.0",
    [string]$Notes = "Actualización de mantenimiento"
)

$ErrorActionPreference = "Stop"

$projectRoot = (Resolve-Path "$PSScriptRoot\..\..").Path
if ([string]::IsNullOrWhiteSpace($ManifestPath)) {
    $ManifestPath = Join-Path $projectRoot "updates\win-x64\stable\manifest.json"
}

if (-not (Test-Path $ArtifactPath)) {
    throw "No existe el artefacto indicado: $ArtifactPath"
}

$artifactResolved = (Resolve-Path $ArtifactPath).Path
$sha256 = (Get-FileHash -Path $artifactResolved -Algorithm SHA256).Hash.ToLowerInvariant()
$publishedAt = (Get-Date).ToString("yyyy-MM-ddTHH:mm:ssK")

$manifest = [ordered]@{
    channel = $Channel
    publishedAt = $publishedAt
    latestVersion = $Version
    minSupportedVersion = $MinSupportedVersion
    artifactPath = "C:\Disciplina\updates\Disciplina-Setup.exe"
    sha256 = $sha256
    notes = $Notes
}

$manifestJson = $manifest | ConvertTo-Json -Depth 5
$manifestDir = Split-Path -Path $ManifestPath -Parent
if (-not (Test-Path $manifestDir)) {
    New-Item -ItemType Directory -Path $manifestDir -Force | Out-Null
}

$utf8NoBom = New-Object System.Text.UTF8Encoding($false)
[System.IO.File]::WriteAllText($ManifestPath, $manifestJson + [Environment]::NewLine, $utf8NoBom)

Write-Host "Manifest actualizado: $ManifestPath"
Write-Host "Version: $Version"
Write-Host "SHA-256: $sha256"

