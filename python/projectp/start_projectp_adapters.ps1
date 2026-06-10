param(
    [string]$CondaEnv = $(if ($env:PROJECTP_CONDA_ENV) { $env:PROJECTP_CONDA_ENV } else { "py310-magneto" }),
    [string]$HostAddress = $(if ($env:PROJECTP_API_HOST) { $env:PROJECTP_API_HOST } else { "127.0.0.1" })
)

$ErrorActionPreference = "Stop"

$Root = Split-Path -Parent $MyInvocation.MyCommand.Path
$LogDir = Join-Path $Root "logs"
$RunDir = Join-Path $Root "run"

New-Item -ItemType Directory -Force -Path $LogDir | Out-Null
New-Item -ItemType Directory -Force -Path $RunDir | Out-Null

function Get-ListeningPidByPort {
    param([int]$Port)
    $pattern = "^\s*TCP\s+\S+:$Port\s+\S+\s+LISTENING\s+(\d+)\s*$"
    netstat -ano | ForEach-Object {
        if ($_ -match $pattern) {
            [int]$Matches[1]
        }
    } | Sort-Object -Unique
}

$CondaCommand = $env:CONDA_EXE
if (-not $CondaCommand) {
    $cmd = Get-Command conda -ErrorAction SilentlyContinue
    if ($cmd) {
        $CondaCommand = $cmd.Source
    }
}
if (-not $CondaCommand) {
    throw "conda was not found. Start this script from Anaconda Prompt, or set CONDA_EXE."
}

$Services = @(
    @{ Key = "plm"; Label = "PLM"; Port = 9711 },
    @{ Key = "erp"; Label = "ERP"; Port = 9712 },
    @{ Key = "mes"; Label = "MES"; Port = 9713 },
    @{ Key = "qms"; Label = "QMS"; Port = 9714 },
    @{ Key = "mro"; Label = "MRO"; Port = 9715 }
)

foreach ($svc in $Services) {
    $port = [int]$svc.Port
    $existing = @(Get-ListeningPidByPort -Port $port)
    if ($existing.Count -gt 0) {
        Write-Host "$($svc.Label) projectp adapter skipped: port $port is already listening, PID(s): $($existing -join ', ')"
        continue
    }

    $stdout = Join-Path $LogDir "$($svc.Key)-stdout.log"
    $stderr = Join-Path $LogDir "$($svc.Key)-stderr.log"
    if (Test-Path $stdout) { Remove-Item -Force -Path $stdout }
    if (Test-Path $stderr) { Remove-Item -Force -Path $stderr }

    $escapedConda = $CondaCommand.Replace("'", "''")
    $command = "`$env:PROJECTP_SYSTEM_KEY='$($svc.Key)'; & '$escapedConda' run -n '$CondaEnv' python -m uvicorn app.main:app --host '$HostAddress' --port $port --log-level info"
    $process = Start-Process `
        -FilePath "powershell.exe" `
        -ArgumentList @("-NoProfile", "-ExecutionPolicy", "Bypass", "-Command", $command) `
        -WorkingDirectory $Root `
        -RedirectStandardOutput $stdout `
        -RedirectStandardError $stderr `
        -WindowStyle Hidden `
        -PassThru

    Set-Content -Path (Join-Path $RunDir "$($svc.Key).wrapper.pid") -Value $process.Id -Encoding ASCII
    Write-Host "$($svc.Label) projectp adapter starting on http://$HostAddress`:$port, wrapper PID $($process.Id)"
}

Write-Host ""
Write-Host "Health check URLs:"
foreach ($svc in $Services) {
    Write-Host "  $($svc.Label): http://$HostAddress`:$($svc.Port)/api/health"
}
