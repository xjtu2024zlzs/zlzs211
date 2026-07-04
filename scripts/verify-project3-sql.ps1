param(
    [string]$Database = "project3_sql_verify"
)

$ErrorActionPreference = "Stop"

$required = @("MYSQL_HOST", "MYSQL_PORT", "MYSQL_USERNAME", "MYSQL_PASSWORD")
foreach ($name in $required) {
    if ([string]::IsNullOrWhiteSpace([Environment]::GetEnvironmentVariable($name))) {
        throw "Missing required environment variable: $name"
    }
}

$mysql = (Get-Command mysql -ErrorAction Stop).Source
$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$sqlDir = Join-Path $repoRoot "sql"
$env:MYSQL_PWD = $env:MYSQL_PASSWORD
$connectionArgs = @(
    "--host=$env:MYSQL_HOST",
    "--port=$env:MYSQL_PORT",
    "--user=$env:MYSQL_USERNAME",
    "--default-character-set=utf8mb4"
)

function Invoke-MySql([string]$Sql, [string]$TargetDatabase = "") {
    $args = @($connectionArgs)
    if ($TargetDatabase) {
        $args += "--database=$TargetDatabase"
    }
    $args += "--batch"
    $args += "--skip-column-names"
    $args += "--execute=$Sql"
    $output = & $mysql @args
    if ($LASTEXITCODE -ne 0) {
        throw "mysql command failed"
    }
    return $output
}

Push-Location $sqlDir
try {
    Invoke-MySql "DROP DATABASE IF EXISTS ``$Database``; CREATE DATABASE ``$Database`` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
    Invoke-MySql "source project3_data.sql; source project3_data.sql;" $Database

    $setup = @"
CREATE TABLE sys_menu(
  menu_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  menu_name VARCHAR(100), parent_id BIGINT, order_num INT, path VARCHAR(200),
  component VARCHAR(255), query VARCHAR(255), route_name VARCHAR(100),
  is_frame CHAR(1), is_cache CHAR(1), menu_type CHAR(1), visible CHAR(1),
  status CHAR(1), perms VARCHAR(200), icon VARCHAR(100), create_by VARCHAR(64),
  create_time DATETIME, update_by VARCHAR(64), update_time DATETIME, remark VARCHAR(500)
);
CREATE TABLE sys_role(role_id BIGINT PRIMARY KEY, role_name VARCHAR(100));
CREATE TABLE sys_role_menu(role_id BIGINT, menu_id BIGINT, PRIMARY KEY(role_id, menu_id));
INSERT INTO sys_menu(menu_name,parent_id,order_num,path,component,route_name,is_frame,is_cache,menu_type,visible,status,create_by,create_time)
VALUES('Project 3',0,1,'project_3','Layout','Project3Menu','1','0','M','0','0','ci',NOW());
INSERT INTO sys_role(role_id, role_name) VALUES(2, 'project3');
"@
    Invoke-MySql $setup $Database
    Invoke-MySql "source preventiveMaintenance_menu.sql; source preventiveMaintenance_menu.sql;" $Database

    $tableCount = Invoke-MySql "SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA='$Database' AND TABLE_NAME='t3_algorithm_task_results';"
    $statusComment = Invoke-MySql "SELECT COLUMN_COMMENT FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='$Database' AND TABLE_NAME='t3_algorithm_task_results' AND COLUMN_NAME='status';"
    $syncColumns = Invoke-MySql "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='$Database' AND TABLE_NAME='t3_algorithm_task_results' AND COLUMN_NAME IN ('status_version','last_sync_at','last_sync_error','sync_retry_count');"
    $imageColumns = Invoke-MySql "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='$Database' AND TABLE_NAME='t3_part_instances' AND COLUMN_NAME='image_url';"
    $menuCount = Invoke-MySql "SELECT COUNT(*) FROM sys_menu WHERE perms='project3:preventiveMaintenance:view';" $Database
    $roleMenuCount = Invoke-MySql "SELECT COUNT(*) FROM sys_role_menu WHERE role_id=2;" $Database

    if ([int]$tableCount -ne 1) { throw "t3_algorithm_task_results was not created" }
    if ($statusComment -notmatch "CANCELED") { throw "status comment does not contain CANCELED" }
    if ([int]$syncColumns -ne 4) { throw "task synchronization columns are incomplete" }
    if ([int]$imageColumns -ne 1) { throw "image_url migration is not idempotent" }
    if ([int]$menuCount -ne 1) { throw "preventive maintenance menu is not idempotent" }
    if ([int]$roleMenuCount -ne 1) { throw "role menu grant is not idempotent" }

    Write-Output "PROJECT3_SQL_VERIFY_OK"
}
finally {
    try {
        Invoke-MySql "DROP DATABASE IF EXISTS ``$Database``;" | Out-Null
    }
    finally {
        Pop-Location
        Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue
    }
}
