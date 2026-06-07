$ErrorActionPreference = "Stop"

$Root = Split-Path -Parent $MyInvocation.MyCommand.Path
$Port = if ($env:ALGORITHM_API_PORT) { [int]$env:ALGORITHM_API_PORT } else { 9204 }
$RunDir = Join-Path $Root "run"

function Get-ListeningPidByPort {
    param([int]$Port)
    $pattern = "^\s*TCP\s+\S+:$Port\s+\S+\s+LISTENING\s+(\d+)\s*$"
    netstat -ano | ForEach-Object {
        if ($_ -match $pattern) {
            [int]$Matches[1]
        }
    } | Sort-Object -Unique
}

$pids = @(Get-ListeningPidByPort -Port $Port)
foreach ($pidValue in $pids) {
    try {
        Stop-Process -Id $pidValue -Force -ErrorAction Stop
        Write-Host "Stopped Algorithm API listener PID $pidValue on port $Port"
    } catch {
        Write-Host "Failed to stop PID $pidValue: $($_.Exception.Message)"
    }
}

if (Test-Path $RunDir) {
    Get-ChildItem -LiteralPath $RunDir -Filter "algorithm-api*.pid" | Remove-Item -Force
}

if ($pids.Count -eq 0) {
    Write-Host "No Algorithm API listener found on port $Port"
}
