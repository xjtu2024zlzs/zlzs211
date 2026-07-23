$ErrorActionPreference = "Stop"

$PyRoot = Split-Path -Parent $PSCommandPath
if (-not $PyRoot) {
    $PyRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
}

$ProjectRoot = Split-Path -Parent $PyRoot
$LogDir = Join-Path $ProjectRoot "logs"
$RunDir = Join-Path $ProjectRoot "run"

New-Item -ItemType Directory -Force -Path $LogDir | Out-Null
New-Item -ItemType Directory -Force -Path $RunDir | Out-Null

function Resolve-CondaExe {
    if ($env:CONDA_EXE -and (Test-Path $env:CONDA_EXE)) {
        return $env:CONDA_EXE
    }

    $cmd = Get-Command conda -ErrorAction SilentlyContinue
    if ($cmd) {
        return $cmd.Source
    }

    $candidates = @(
        "D:\software\Anaconda3\Scripts\conda.exe",
        "D:\Anaconda3\Scripts\conda.exe",
        "D:\software\miniconda3\Scripts\conda.exe",
        "D:\miniconda3\Scripts\conda.exe"
    )

    foreach ($candidate in $candidates) {
        if (Test-Path $candidate) {
            return $candidate
        }
    }

    throw "conda.exe not found. Please check Anaconda path."
}

function Get-ListeningPidByPort {
    param([int]$Port)

    $result = @()

    netstat -ano | ForEach-Object {
        $line = $_.Trim()
        if ($line -like "TCP*" -and $line -like "*LISTENING*") {
            $parts = $line -split "\s+"
            if ($parts.Length -ge 5) {
                $localAddress = $parts[1]
                $pidText = $parts[4]

                if ($localAddress -match (":" + $Port + "$")) {
                    $result += [int]$pidText
                }
            }
        }
    }

    return $result | Sort-Object -Unique
}

function Start-FastApiService {
    param(
        [string]$Name,
        [string]$WorkingDirectory,
        [string]$CondaEnv,
        [string]$App,
        [string]$HostAddress,
        [int]$Port,
        [string[]]$ExtraArgs = @(),
        [hashtable]$EnvVars = @{}
    )

    if (-not (Test-Path $WorkingDirectory)) {
        Write-Warning ("[" + $Name + "] directory not found, skipped: " + $WorkingDirectory)
        return
    }

    $existing = @(Get-ListeningPidByPort -Port $Port)
    if ($existing.Count -gt 0) {
        Write-Host ("[" + $Name + "] port " + $Port + " already listening, skipped. PID: " + ($existing -join ", "))
        return
    }

    $stdout = Join-Path $LogDir ($Name + "-stdout.log")
    $stderr = Join-Path $LogDir ($Name + "-stderr.log")

    if (Test-Path $stdout) {
        Remove-Item -Force -LiteralPath $stdout
    }
    if (Test-Path $stderr) {
        Remove-Item -Force -LiteralPath $stderr
    }

    $condaExe = Resolve-CondaExe

    $argumentList = @(
        "run",
        "-n",
        $CondaEnv,
        "python",
        "-m",
        "uvicorn",
        $App,
        "--host",
        $HostAddress,
        "--port",
        [string]$Port,
        "--log-level",
        "info"
    )

    if ($ExtraArgs.Count -gt 0) {
        $argumentList += $ExtraArgs
    }

    $oldEnv = @{}

    foreach ($key in $EnvVars.Keys) {
        $oldEnv[$key] = [Environment]::GetEnvironmentVariable($key, "Process")
        [Environment]::SetEnvironmentVariable($key, [string]$EnvVars[$key], "Process")
    }

    try {
        $process = Start-Process `
            -FilePath $condaExe `
            -ArgumentList $argumentList `
            -WorkingDirectory $WorkingDirectory `
            -RedirectStandardOutput $stdout `
            -RedirectStandardError $stderr `
            -WindowStyle Hidden `
            -PassThru
    }
    finally {
        foreach ($key in $EnvVars.Keys) {
            [Environment]::SetEnvironmentVariable($key, $oldEnv[$key], "Process")
        }
    }

    Set-Content -Path (Join-Path $RunDir ($Name + ".wrapper.pid")) -Value $process.Id -Encoding ASCII

    $url = "http://" + $HostAddress + ":" + $Port
    Write-Host ("[" + $Name + "] started in background: " + $url + ", wrapper PID: " + $process.Id)
    Write-Host ("log: " + $stdout)
}

Write-Host "=================================================="
Write-Host "Starting all FastAPI services in background..."
Write-Host "=================================================="
Write-Host ""

# Project 1 - projectp adapters

Start-FastApiService `
    -Name "projectp-plm-9711" `
    -WorkingDirectory (Join-Path $PyRoot "projectp") `
    -CondaEnv "project1" `
    -App "app.main:app" `
    -HostAddress "127.0.0.1" `
    -Port 9711 `
    -EnvVars @{ PROJECTP_SYSTEM_KEY = "plm" }

Start-FastApiService `
    -Name "projectp-erp-9712" `
    -WorkingDirectory (Join-Path $PyRoot "projectp") `
    -CondaEnv "project1" `
    -App "app.main:app" `
    -HostAddress "127.0.0.1" `
    -Port 9712 `
    -EnvVars @{ PROJECTP_SYSTEM_KEY = "erp" }

Start-FastApiService `
    -Name "projectp-mes-9713" `
    -WorkingDirectory (Join-Path $PyRoot "projectp") `
    -CondaEnv "project1" `
    -App "app.main:app" `
    -HostAddress "127.0.0.1" `
    -Port 9713 `
    -EnvVars @{ PROJECTP_SYSTEM_KEY = "mes" }

Start-FastApiService `
    -Name "projectp-qms-9714" `
    -WorkingDirectory (Join-Path $PyRoot "projectp") `
    -CondaEnv "project1" `
    -App "app.main:app" `
    -HostAddress "127.0.0.1" `
    -Port 9714 `
    -EnvVars @{ PROJECTP_SYSTEM_KEY = "qms" }

Start-FastApiService `
    -Name "projectp-mro-9715" `
    -WorkingDirectory (Join-Path $PyRoot "projectp") `
    -CondaEnv "project1" `
    -App "app.main:app" `
    -HostAddress "127.0.0.1" `
    -Port 9715 `
    -EnvVars @{ PROJECTP_SYSTEM_KEY = "mro" }

# Project 1 - algorithm

Start-FastApiService `
    -Name "project1-algorithm-9701" `
    -WorkingDirectory (Join-Path $PyRoot "project1") `
    -CondaEnv "project1" `
    -App "app.main:app" `
    -HostAddress "127.0.0.1" `
    -Port 9701

# Project 2

Start-FastApiService `
    -Name "project2-9721" `
    -WorkingDirectory (Join-Path $PyRoot "project2") `
    -CondaEnv "project2" `
    -App "app.main:app" `
    -HostAddress "127.0.0.1" `
    -Port 9721

# Project 3

Start-FastApiService `
    -Name "project3-9741" `
    -WorkingDirectory (Join-Path $PyRoot "project3") `
    -CondaEnv "project3" `
    -App "main:app" `
    -HostAddress "0.0.0.0" `
    -Port 9741

# Project 4

Start-FastApiService `
    -Name "project4-9761" `
    -WorkingDirectory (Join-Path $PyRoot "project4") `
    -CondaEnv "project4" `
    -App "app:app" `
    -HostAddress "127.0.0.1" `
    -Port 9761 `
    -ExtraArgs @("--reload")

# Project 5 - code1

Start-FastApiService `
    -Name "project5-code1-9781" `
    -WorkingDirectory (Join-Path $PyRoot "project5\code1\pytorch_geometric_temporal\my") `
    -CondaEnv "topic5_code1" `
    -App "api_server:app" `
    -HostAddress "127.0.0.1" `
    -Port 9781

# Project 5 - code2

Start-FastApiService `
    -Name "project5-code2-9782" `
    -WorkingDirectory (Join-Path $PyRoot "project5\code2\transH\services") `
    -CondaEnv "topic5_code21" `
    -App "api_server:app" `
    -HostAddress "127.0.0.1" `
    -Port 9782

Write-Host ""
Write-Host "=================================================="
Write-Host "All start commands have been sent."
Write-Host "Services are running in background."
Write-Host "Check logs folder if any service failed."
Write-Host "=================================================="
Write-Host ""

Write-Host ("logs: " + $LogDir)
Write-Host ("run:  " + $RunDir)
Write-Host ""

Write-Host "Health or service URLs:"
Write-Host "projectp PLM: http://127.0.0.1:9711/api/health"
Write-Host "projectp ERP: http://127.0.0.1:9712/api/health"
Write-Host "projectp MES: http://127.0.0.1:9713/api/health"
Write-Host "projectp QMS: http://127.0.0.1:9714/api/health"
Write-Host "projectp MRO: http://127.0.0.1:9715/api/health"
Write-Host "project1:      http://127.0.0.1:9701/api/health"
Write-Host "project2:      http://127.0.0.1:9721"
Write-Host "project3:      http://127.0.0.1:9741"
Write-Host "project4:      http://127.0.0.1:9761"
Write-Host "project5-1:    http://127.0.0.1:9781"
Write-Host "project5-2:    http://127.0.0.1:9782"