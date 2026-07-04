param(
    [string]$Database = "project3_e2e_verify",
    [int]$PythonPort = 19741,
    [int]$Project3Port = 19303,
    [int]$GatewayPort = 18088,
    [switch]$WithGateway
)

$ErrorActionPreference = "Stop"

$required = @("MYSQL_HOST", "MYSQL_PORT", "MYSQL_USERNAME", "MYSQL_PASSWORD")
foreach ($name in $required) {
    if ([string]::IsNullOrWhiteSpace([Environment]::GetEnvironmentVariable($name))) {
        throw "Missing required environment variable: $name"
    }
}

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$mysql = (Get-Command mysql -ErrorAction Stop).Source
$env:MYSQL_PWD = $env:MYSQL_PASSWORD
$connectionArgs = @(
    "--host=$env:MYSQL_HOST",
    "--port=$env:MYSQL_PORT",
    "--user=$env:MYSQL_USERNAME",
    "--default-character-set=utf8mb4"
)
$started = @()

function Invoke-MySql([string]$Sql, [string]$TargetDatabase = "") {
    $args = @($connectionArgs)
    if ($TargetDatabase) { $args += "--database=$TargetDatabase" }
    $args += "--batch"
    $args += "--skip-column-names"
    $args += "--execute=$Sql"
    $output = & $mysql @args
    if ($LASTEXITCODE -ne 0) { throw "mysql command failed" }
    return $output
}

function Wait-Http([string]$Url, [int]$Seconds = 90) {
    $deadline = (Get-Date).AddSeconds($Seconds)
    do {
        try {
            return Invoke-RestMethod -Uri $Url -Method Get -TimeoutSec 5
        }
        catch {
            Start-Sleep -Seconds 2
        }
    } while ((Get-Date) -lt $deadline)
    throw "Timed out waiting for $Url"
}

function Invoke-JsonPost([string]$Url, [object]$Body) {
    $json = $Body | ConvertTo-Json -Depth 20
    return Invoke-RestMethod -Uri $Url -Method Post -Body $json -ContentType "application/json" -TimeoutSec 30
}

function Wait-TaskStatus([string]$BaseUrl, [string]$TaskId, [string[]]$Expected, [int]$Seconds = 120) {
    $deadline = (Get-Date).AddSeconds($Seconds)
    do {
        Start-Sleep -Seconds 2
        $state = Invoke-RestMethod -Uri "$BaseUrl/service/preventive-maintenance/tasks/$TaskId" -Method Get -TimeoutSec 10
        $status = $state.data.status
        if ($Expected -contains $status) { return $state }
    } while ((Get-Date) -lt $deadline)
    throw "Timed out waiting for task $TaskId to reach $($Expected -join ','); last status=$status"
}

function Write-PythonStatus([string]$TaskId, [string]$Status, [int]$Version = 2) {
    $dir = Join-Path $env:PYTHON_TASK_ROOT $TaskId
    New-Item -ItemType Directory -Force -Path $dir | Out-Null
    @{
        success = $true
        taskId = $TaskId
        algorithmType = "PREVENTIVE_MAINTENANCE"
        status = $Status
        version = $Version
        progress = 100
        message = "seeded by e2e"
        result = @{
            recommended = @{ Cu = 1.23; A = 0.95; minR = 0.75 }
            paretoResults = @(@{ Cu = 1.23 })
        }
        error = ""
    } | ConvertTo-Json -Depth 20 | Set-Content -Path (Join-Path $dir "status.json") -Encoding UTF8
}

function Start-HiddenProcess([string]$FilePath, [string]$Arguments, [string]$WorkingDirectory) {
    $process = Start-Process -FilePath $FilePath -ArgumentList $Arguments -WorkingDirectory $WorkingDirectory -WindowStyle Hidden -PassThru
    $script:started += $process
    return $process
}

try {
    Invoke-MySql "DROP DATABASE IF EXISTS ``$Database``; CREATE DATABASE ``$Database`` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
    Invoke-MySql "source $((Join-Path $repoRoot 'sql/project3_data.sql').Replace('\', '/'));" $Database

    $env:PYTHON_SERVICE_URL = "http://127.0.0.1:$PythonPort"
    $env:PYTHON_TASK_ROOT = Join-Path $repoRoot "python/project3/runtime/tasks"
    $env:MYSQL_DATABASE = $Database
    $env:PROJECT3_TASK_SYNC_ENABLED = "true"
    $env:PROJECT3_TASK_SYNC_FIXED_DELAY_MS = "3000"
    $env:PROJECT3_TASK_SYNC_ACTIVE_TIMEOUT_MINUTES = "1"
    $env:PROJECT3_TASK_SYNC_BATCH_SIZE = "5"
    $env:PROJECT3_TASK_SYNC_MAX_RETRY_COUNT = "1"

    Start-HiddenProcess "python" "-m uvicorn main:app --host 127.0.0.1 --port $PythonPort" (Join-Path $repoRoot "python/project3") | Out-Null
    Wait-Http "http://127.0.0.1:$PythonPort/health" 90 | Out-Null

    $project3Jar = Join-Path $repoRoot "ruoyi-modules/project3/target/project3.jar"
    if (!(Test-Path $project3Jar)) { throw "Missing project3 jar: $project3Jar" }
    $project3Config = (Join-Path $repoRoot "nacos/DEFAULT_GROUP/project3-dev.yml").Replace("\", "/")
    $project3Args = "-jar `"$project3Jar`" --server.port=$Project3Port --spring.config.on-not-found=ignore --spring.config.additional-location=file:///$project3Config"
    $project3Process = Start-HiddenProcess "java" $project3Args $repoRoot
    Wait-Http "http://127.0.0.1:$Project3Port/test" 120 | Out-Null

    $baseUrl = "http://127.0.0.1:$Project3Port"
    if ($WithGateway) {
        $env:GATEWAY_PORT = "$GatewayPort"
        $env:PROJECT3_GATEWAY_ROUTE_URI = "http://127.0.0.1:$Project3Port"
        $gatewayJar = Join-Path $repoRoot "ruoyi-gateway/target/ruoyi-gateway.jar"
        if (!(Test-Path $gatewayJar)) { throw "Missing gateway jar: $gatewayJar" }
        $gatewayConfig = (Join-Path $repoRoot "nacos/DEFAULT_GROUP/ruoyi-gateway-dev.yml").Replace("\", "/")
        $gatewayArgs = "-jar `"$gatewayJar`" --server.port=$GatewayPort --spring.config.on-not-found=ignore --spring.config.additional-location=file:///$gatewayConfig"
        Start-HiddenProcess "java" $gatewayArgs $repoRoot | Out-Null
        Wait-Http "http://127.0.0.1:$GatewayPort/project3/test" 120 | Out-Null
        $baseUrl = "http://127.0.0.1:$GatewayPort"
    }

    $payload = @{
        T1 = 90; T2 = 80; T3 = 70
        N1 = 2; N2 = 2; N3 = 2
        sampleCount = 10
        population = 6
        iterations = 2
        Rm = 0.75
        attackThreshold = 0.3
        taskNo = "E2E-$([DateTimeOffset]::Now.ToUnixTimeSeconds())"
    }
    # Gateway/direct success scenario.
    $submit = Invoke-JsonPost "$baseUrl/service/preventive-maintenance/tasks" $payload
    $taskId = $submit.data.taskId
    if ([string]::IsNullOrWhiteSpace($taskId)) { throw "taskId was not returned" }
    $state = Wait-TaskStatus $baseUrl $taskId @("SUCCESS")
    if ($state.data.status -ne "SUCCESS") { throw "Expected SUCCESS, got $($state.data.status)" }
    $dbStatus = Invoke-MySql "SELECT status FROM t3_algorithm_task_results WHERE task_id='$taskId';" $Database
    if ($dbStatus -ne "SUCCESS") { throw "Database status mismatch: $dbStatus" }

    # Gateway/direct failure scenario: invalid monotonic T values should be rejected by Java validation.
    try {
        $invalid = Invoke-JsonPost "$baseUrl/service/preventive-maintenance/tasks" @{
            T1 = 60; T2 = 70; T3 = 50
            N1 = 2; N2 = 2; N3 = 2
            sampleCount = 10; population = 6; iterations = 2
            Rm = 0.75; attackThreshold = 0.3
        }
        if ($invalid.data.taskId -or $invalid.code -eq 200) {
            throw "Invalid request was unexpectedly accepted"
        }
    }
    catch {
        if ($_.Exception.Message -eq "Invalid request was unexpectedly accepted") { throw }
    }

    # Gateway/direct cancel scenario.
    $cancelPayload = $payload.Clone()
    $cancelPayload["iterations"] = 100
    $cancelPayload["population"] = 40
    $cancelPayload["sampleCount"] = 2000
    $cancelSubmit = Invoke-JsonPost "$baseUrl/service/preventive-maintenance/tasks" $cancelPayload
    $cancelTaskId = $cancelSubmit.data.taskId
    if ([string]::IsNullOrWhiteSpace($cancelTaskId)) { throw "cancel taskId was not returned" }
    Invoke-JsonPost "$baseUrl/service/preventive-maintenance/tasks/$cancelTaskId/cancel" @{} | Out-Null
    $cancelState = Wait-TaskStatus $baseUrl $cancelTaskId @("CANCELED", "SUCCESS", "FAILED") 60
    if ($cancelState.data.status -ne "CANCELED") { throw "Expected CANCELED, got $($cancelState.data.status)" }

    # Reconciliation: DB RUNNING + Python SUCCESS.
    $syncSuccessId = "PYE2ESYNC$([DateTimeOffset]::Now.ToUnixTimeMilliseconds())"
    Write-PythonStatus $syncSuccessId "SUCCESS" 3
    Invoke-MySql "INSERT INTO t3_algorithm_task_results(task_id,task_type,task_name,status,status_version,sync_retry_count,created_at,updated_at) VALUES('$syncSuccessId','PREVENTIVE_MAINTENANCE','e2e sync success','RUNNING',0,0,NOW(),NOW());" $Database
    Start-Sleep -Seconds 8
    $syncSuccessStatus = Invoke-MySql "SELECT status FROM t3_algorithm_task_results WHERE task_id='$syncSuccessId';" $Database
    if ($syncSuccessStatus -ne "SUCCESS") { throw "Expected reconciled SUCCESS, got $syncSuccessStatus" }

    # Reconciliation: DB RUNNING + missing Python files -> retry max exceeded -> FAILED.
    $missingId = "PYE2EMISS$([DateTimeOffset]::Now.ToUnixTimeMilliseconds())"
    Invoke-MySql "INSERT INTO t3_algorithm_task_results(task_id,task_type,task_name,status,status_version,sync_retry_count,created_at,updated_at) VALUES('$missingId','PREVENTIVE_MAINTENANCE','e2e missing python','RUNNING',0,0,NOW(),NOW());" $Database
    Start-Sleep -Seconds 8
    $missingStatus = Invoke-MySql "SELECT status FROM t3_algorithm_task_results WHERE task_id='$missingId';" $Database
    if ($missingStatus -ne "FAILED") { throw "Expected missing Python task FAILED, got $missingStatus" }

    # Reconciliation: retry already exceeded -> FAILED without Python call.
    $retryId = "PYE2ERETRY$([DateTimeOffset]::Now.ToUnixTimeMilliseconds())"
    Invoke-MySql "INSERT INTO t3_algorithm_task_results(task_id,task_type,task_name,status,status_version,sync_retry_count,created_at,updated_at) VALUES('$retryId','PREVENTIVE_MAINTENANCE','e2e retry exceeded','RUNNING',0,1,NOW(),NOW());" $Database
    Start-Sleep -Seconds 8
    $retryStatus = Invoke-MySql "SELECT status FROM t3_algorithm_task_results WHERE task_id='$retryId';" $Database
    if ($retryStatus -ne "FAILED") { throw "Expected retry exceeded FAILED, got $retryStatus" }

    # Java restart active-task reconciliation.
    $restartId = "PYE2ERESTART$([DateTimeOffset]::Now.ToUnixTimeMilliseconds())"
    Write-PythonStatus $restartId "SUCCESS" 3
    Invoke-MySql "INSERT INTO t3_algorithm_task_results(task_id,task_type,task_name,status,status_version,sync_retry_count,created_at,updated_at) VALUES('$restartId','PREVENTIVE_MAINTENANCE','e2e restart sync','RUNNING',0,0,NOW(),NOW());" $Database
    Stop-Process -Id $project3Process.Id -Force -ErrorAction SilentlyContinue
    Start-Sleep -Seconds 3
    $project3Process = Start-HiddenProcess "java" $project3Args $repoRoot
    Wait-Http "http://127.0.0.1:$Project3Port/test" 120 | Out-Null
    Start-Sleep -Seconds 8
    $restartStatus = Invoke-MySql "SELECT status FROM t3_algorithm_task_results WHERE task_id='$restartId';" $Database
    if ($restartStatus -ne "SUCCESS") { throw "Expected restart reconciled SUCCESS, got $restartStatus" }

    Write-Output "PROJECT3_E2E_OK success=$taskId cancel=$cancelTaskId gateway=$WithGateway"
}
finally {
    foreach ($process in $started) {
        try {
            if ($process -and !$process.HasExited) { Stop-Process -Id $process.Id -Force -ErrorAction SilentlyContinue }
        }
        catch {}
    }
    try { Invoke-MySql "DROP DATABASE IF EXISTS ``$Database``;" | Out-Null } catch {}
    Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue
}
