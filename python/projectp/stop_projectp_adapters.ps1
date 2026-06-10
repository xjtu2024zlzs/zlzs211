$ErrorActionPreference = "Continue"

$Root = Split-Path -Parent $MyInvocation.MyCommand.Path
$RunDir = Join-Path $Root "run"
$Ports = @(9711, 9712, 9713, 9714, 9715)

function Get-ListeningPidByPort {
    param([int]$Port)
    $pattern = "^\s*TCP\s+\S+:$Port\s+\S+\s+LISTENING\s+(\d+)\s*$"
    netstat -ano | ForEach-Object {
        if ($_ -match $pattern) {
            [int]$Matches[1]
        }
    } | Sort-Object -Unique
}

foreach ($port in $Ports) {
    $pids = @(Get-ListeningPidByPort -Port $port)
    if ($pids.Count -eq 0) {
        Write-Host "No listener found on port $port"
        continue
    }

    foreach ($pidValue in $pids) {
        try {
            Stop-Process -Id $pidValue -Force -ErrorAction Stop
            Write-Host "Stopped listener PID $pidValue on port $port"
        } catch {
            Write-Host "Failed to stop PID $pidValue on port ${port}: $($_.Exception.Message)"
        }
    }
}

if (Test-Path $RunDir) {
    Get-ChildItem -Path $RunDir -Filter "*.pid" -ErrorAction SilentlyContinue | ForEach-Object {
        $pidText = Get-Content -Path $_.FullName -ErrorAction SilentlyContinue | Select-Object -First 1
        if ($pidText -match "^\d+$") {
            try {
                Stop-Process -Id ([int]$pidText) -Force -ErrorAction SilentlyContinue
            } catch {
                Write-Host "Wrapper PID $pidText was already stopped"
            }
        }
        Remove-Item -Force -Path $_.FullName -ErrorAction SilentlyContinue
    }
}
