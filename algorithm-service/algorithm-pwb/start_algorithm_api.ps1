$ErrorActionPreference = "Stop"

$Root = Split-Path -Parent $MyInvocation.MyCommand.Path
$CondaEnv = if ($env:ALGORITHM_API_CONDA_ENV) { $env:ALGORITHM_API_CONDA_ENV } else { "py310-magneto" }
$Port = if ($env:ALGORITHM_API_PORT) { [int]$env:ALGORITHM_API_PORT } else { 9204 }
$HostAddress = if ($env:ALGORITHM_API_HOST) { $env:ALGORITHM_API_HOST } else { "127.0.0.1" }
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

$existing = @(Get-ListeningPidByPort -Port $Port)
if ($existing.Count -gt 0) {
    Write-Host "Algorithm API skipped: port $Port is already listening, PID(s): $($existing -join ', ')"
    exit 0
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

$stdout = Join-Path $LogDir "algorithm-api-stdout.log"
$stderr = Join-Path $LogDir "algorithm-api-stderr.log"
if (Test-Path $stdout) { Remove-Item -Force -LiteralPath $stdout }
if (Test-Path $stderr) { Remove-Item -Force -LiteralPath $stderr }

$escapedConda = $CondaCommand.Replace("'", "''")
$command = "& '$escapedConda' run -n '$CondaEnv' python -m uvicorn algorithm_api.main:app --host $HostAddress --port $Port --log-level info"
$process = Start-Process `
    -FilePath "powershell.exe" `
    -ArgumentList @("-NoProfile", "-ExecutionPolicy", "Bypass", "-Command", $command) `
    -WorkingDirectory $Root `
    -RedirectStandardOutput $stdout `
    -RedirectStandardError $stderr `
    -WindowStyle Hidden `
    -PassThru

Set-Content -Path (Join-Path $RunDir "algorithm-api.wrapper.pid") -Value $process.Id -Encoding ASCII
Write-Host "Algorithm API starting on http://$HostAddress`:$Port, wrapper PID $($process.Id)"
Write-Host "Health: http://$HostAddress`:$Port/api/health"
