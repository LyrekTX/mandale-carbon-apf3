$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot
$dependencyRoot = Join-Path $env:USERPROFILE ".m2\repository"
$javadoc = Get-Command javadoc.exe -ErrorAction SilentlyContinue

if (-not $javadoc) {
    throw "No se encontró javadoc.exe. Instala un JDK 21 o superior y agrega su carpeta bin al PATH."
}
if (-not (Test-Path $dependencyRoot)) {
    throw "No se encontró el repositorio local de Maven. Ejecuta mvn test primero para descargar las dependencias."
}

$jars = Get-ChildItem $dependencyRoot -Recurse -Filter *.jar | ForEach-Object { $_.FullName }
$classpath = (@((Join-Path $projectRoot "target\classes")) + $jars) -join ";"
$classpath = $classpath.Replace('\', '/')
$output = (Join-Path $projectRoot "docs\javadoc").Replace('\', '/')
$argumentFile = Join-Path $projectRoot "target\javadoc-args.txt"
$sourceFiles = Get-ChildItem (Join-Path $projectRoot "src\main\java") -Recurse -Filter *.java |
    ForEach-Object { "`"$($_.FullName.Replace('\', '/'))`"" }

New-Item -ItemType Directory -Force (Split-Path $argumentFile) | Out-Null
New-Item -ItemType Directory -Force $output | Out-Null
@(
    "--release 21"
    "-encoding UTF-8"
    "-charset UTF-8"
    "-notimestamp"
    "-Xdoclint:none"
    "-d `"$output`""
    "-classpath `"$classpath`""
    $sourceFiles
) | Set-Content -LiteralPath $argumentFile -Encoding utf8

Push-Location $projectRoot
try {
    & $javadoc.Source "@$argumentFile"
    if ($LASTEXITCODE -ne 0) {
        throw "Falló la generación de Javadoc (código $LASTEXITCODE). Revisa los errores anteriores."
    }
}
finally {
    Pop-Location
}

Write-Host "Javadoc generado en $output\index.html"
