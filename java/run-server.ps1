[CmdletBinding()]
param(
    [ValidateRange(1, 65535)]
    [int]$Port = 8081,
    [string[]]$AllowedOrigins = @()
)

$ErrorActionPreference = 'Stop'
$javaRoot = $PSScriptRoot
$sourceRoot = Join-Path $javaRoot 'src'
$buildRoot = Join-Path $javaRoot 'build\classes'

$javac = Get-Command javac -ErrorAction Stop
$java = Get-Command java -ErrorAction Stop
$listener = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue |
    Select-Object -First 1
if ($listener) {
    $owner = Get-CimInstance Win32_Process -Filter "ProcessId=$($listener.OwningProcess)" `
        -ErrorAction SilentlyContinue
    $ownerName = if ($owner) { $owner.Name } else { 'unknown process' }
    throw "Port $Port is already in use by PID $($listener.OwningProcess) ($ownerName)."
}

$sources = Get-ChildItem -Path $sourceRoot -Recurse -File -Filter '*.java' |
    ForEach-Object { $_.FullName }
if (-not $sources) {
    throw "No Java sources found under $sourceRoot."
}

New-Item -ItemType Directory -Force -Path $buildRoot | Out-Null
Write-Host "Compiling Java sources with $($javac.Source)..."
& $javac.Source -encoding UTF-8 -d $buildRoot $sources
if ($LASTEXITCODE -ne 0) {
    throw "Java compilation failed with exit code $LASTEXITCODE."
}
Write-Host "Compilation successful. Starting on 127.0.0.1:$Port (Ctrl+C to stop gracefully)."

$previousPort = $env:SMART_HOSPITAL_PORT
$previousOrigins = $env:SMART_HOSPITAL_ALLOWED_ORIGINS
try {
    $env:SMART_HOSPITAL_PORT = [string]$Port
    $env:SMART_HOSPITAL_ALLOWED_ORIGINS = $AllowedOrigins -join ','
    & $java.Source -cp $buildRoot server.SmartHospitalServer
    if ($LASTEXITCODE -ne 0) {
        throw "Java server exited with code $LASTEXITCODE."
    }
} finally {
    if ($null -eq $previousPort) {
        Remove-Item Env:\SMART_HOSPITAL_PORT -ErrorAction SilentlyContinue
    } else {
        $env:SMART_HOSPITAL_PORT = $previousPort
    }
    if ($null -eq $previousOrigins) {
        Remove-Item Env:\SMART_HOSPITAL_ALLOWED_ORIGINS -ErrorAction SilentlyContinue
    } else {
        $env:SMART_HOSPITAL_ALLOWED_ORIGINS = $previousOrigins
    }
}
