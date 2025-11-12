#!/bin/bash
# Script para generar reporte HTML de pruebas en Linux/Mac
# Ejecuta las pruebas y genera un dashboard visual

echo "============================================================"
echo "Generador de Reporte HTML de Pruebas - Linux/Mac"
echo "============================================================"
echo ""

# Verificar si Python está instalado
if ! command -v python3 &> /dev/null; then
    if ! command -v python &> /dev/null; then
        echo "[ERROR] Python no está instalado o no está en el PATH"
        echo "Por favor, instala Python 3.6 o superior"
        exit 1
    else
        PYTHON_CMD="python"
    fi
else
    PYTHON_CMD="python3"
fi

# Verificar si el script Python existe
if [ ! -f "generate_test_report.py" ]; then
    echo "[ERROR] No se encontró el archivo generate_test_report.py"
    exit 1
fi

# Hacer el script ejecutable si no lo es
chmod +x generate_test_report.py 2>/dev/null

echo "Ejecutando generador de reportes..."
echo ""

# Ejecutar el script Python
$PYTHON_CMD generate_test_report.py

# Verificar el código de salida
EXIT_CODE=$?
if [ $EXIT_CODE -ne 0 ]; then
    echo ""
    echo "[ADVERTENCIA] El proceso terminó con errores (código: $EXIT_CODE)"
    exit $EXIT_CODE
fi

echo ""
echo "============================================================"
echo "Proceso completado exitosamente"
echo "============================================================"

