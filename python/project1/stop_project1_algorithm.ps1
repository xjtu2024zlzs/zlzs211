$ErrorActionPreference = "Stop"

$Root = Split-Path -Parent $MyInvocation.MyCommand.Path
$Port = if ($env:PROJECT1_ALGORITHM_PORT) { [int]$env:PROJECT1_ALGORITHM_PORT } else { 9701 }
$RunDir = Join-Path $Root "run"
$PidFile = Join-Path $RunDir "project1-algorithm.wrapper.pid"

function Get-ListeningPidByPort {
    param([int]$Port)
    $pattern = "^\s*TCP\s+\S+:$Port\s+\S+\s+LISTENING\s+(\d+)\s*$"
    netstat -ano | ForEach-Object {
        if ($_ -match $pattern) {
            [int]$Matches[1]
        }
    } | Sort-Object -Unique
}

$pids = @()
if (Test-Path $PidFile) {
    $value = Get-Content -LiteralPath $PidFile -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($value -match '^\d+$') {
        $pids += [int]$value
    }
}
$pids += @(Get-ListeningPidByPort -Port $Port)
$pids = $pids | Sort-Object -Unique

if ($pids.Count -eq 0) {
    Write-Host "Project1 algorithm API is not listening on port $Port."
    exit 0
}

foreach ($pidValue in $pids) {
    try {
        Stop-Process -Id $pidValue -Force -ErrorAction Stop
        Write-Host "Stopped PID $pidValue"
    }
    catch {
        Write-Host "Failed to stop PID $pidValue: $($_.Exception.Message)"
    }
}

if (Test-Path $PidFile) {
    Remove-Item -Force -LiteralPath $PidFile
}
