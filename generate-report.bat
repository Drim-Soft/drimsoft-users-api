@echo off
REM Script para generar reporte HTML de pruebas en Windows
REM Ejecuta las pruebas y genera un dashboard visual

echo ============================================================
echo Generador de Reporte HTML de Pruebas - Windows
echo ============================================================
echo.

REM Verificar si Python está instalado
python --version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Python no está instalado o no está en el PATH
    echo Por favor, instala Python 3.6 o superior
    pause
    exit /b 1
)

REM Verificar si el script Python existe
if not exist "generate_test_report.py" (
    echo [ERROR] No se encontró el archivo generate_test_report.py
    pause
    exit /b 1
)

echo Ejecutando generador de reportes...
echo.

REM Ejecutar el script Python
python generate_test_report.py

REM Verificar el código de salida
if errorlevel 1 (
    echo.
    echo [ADVERTENCIA] El proceso terminó con errores
    pause
    exit /b 1
)

echo.
echo ============================================================
echo Proceso completado
echo ============================================================
pause

