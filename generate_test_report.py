#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Generador de Reporte HTML de Pruebas Unitarias
Ejecuta las pruebas de Maven y genera un dashboard HTML visual
"""

import os
import sys
import subprocess
import xml.etree.ElementTree as ET
from datetime import datetime
from pathlib import Path
import json

# Colores para el HTML
COLORS = {
    'success': '#28a745',
    'failure': '#dc3545',
    'error': '#ffc107',
    'skipped': '#6c757d',
    'primary': '#007bff',
    'background': '#f8f9fa',
    'text': '#212529'
}

def run_maven_tests():
    """Ejecuta las pruebas de Maven y retorna el código de salida"""
    print("🔍 Ejecutando pruebas con Maven...")
    print("-" * 50)
    
    # Determinar el comando Maven según el sistema operativo
    if os.name == 'nt':  # Windows
        mvn_cmd = 'mvnw.cmd' if os.path.exists('mvnw.cmd') else 'mvn'
    else:  # Linux/Mac
        mvn_cmd = './mvnw' if os.path.exists('mvnw') else 'mvn'
    
    try:
        result = subprocess.run(
            [mvn_cmd, 'clean', 'test'],
            capture_output=True,
            text=True,
            check=False
        )
        
        if result.returncode != 0:
            print("⚠️  Algunas pruebas fallaron, pero continuando con el reporte...")
        
        return result.returncode
    except FileNotFoundError:
        print(f"❌ Error: No se encontró {mvn_cmd}")
        print("   Asegúrate de tener Maven instalado o usar mvnw")
        sys.exit(1)

def parse_junit_xml(xml_file):
    """Parsea un archivo XML de JUnit y extrae información de las pruebas"""
    try:
        tree = ET.parse(xml_file)
        root = tree.getroot()
        
        # JUnit puede tener diferentes formatos
        testsuite = root.find('testsuite')
        if testsuite is None:
            testsuite = root
        
        test_cases = []
        for testcase in testsuite.findall('testcase'):
            test_name = testcase.get('name', 'Unknown')
            class_name = testcase.get('classname', 'Unknown')
            time = float(testcase.get('time', 0))
            
            # Determinar el estado de la prueba
            status = 'success'
            error_msg = None
            error_type = None
            
            if testcase.find('failure') is not None:
                status = 'failure'
                failure = testcase.find('failure')
                error_msg = failure.text if failure.text else failure.get('message', '')
                error_type = failure.get('type', '')
            elif testcase.find('error') is not None:
                status = 'error'
                error = testcase.find('error')
                error_msg = error.text if error.text else error.get('message', '')
                error_type = error.get('type', '')
            elif testcase.find('skipped') is not None:
                status = 'skipped'
            
            test_cases.append({
                'name': test_name,
                'class': class_name,
                'status': status,
                'time': time,
                'error_msg': error_msg,
                'error_type': error_type
            })
        
        return {
            'name': testsuite.get('name', 'Unknown'),
            'tests': int(testsuite.get('tests', 0)),
            'failures': int(testsuite.get('failures', 0)),
            'errors': int(testsuite.get('errors', 0)),
            'skipped': int(testsuite.get('skipped', 0)),
            'time': float(testsuite.get('time', 0)),
            'test_cases': test_cases
        }
    except Exception as e:
        print(f"⚠️  Error al parsear {xml_file}: {e}")
        return None

def collect_test_reports():
    """Recopila todos los reportes XML de las pruebas"""
    reports_dir = Path('target/surefire-reports')
    
    if not reports_dir.exists():
        print("❌ No se encontró el directorio target/surefire-reports")
        print("   Asegúrate de ejecutar las pruebas primero")
        return []
    
    xml_files = list(reports_dir.glob('*.xml'))
    
    if not xml_files:
        print("⚠️  No se encontraron archivos XML de reportes")
        return []
    
    reports = []
    for xml_file in xml_files:
        report = parse_junit_xml(xml_file)
        if report:
            reports.append(report)
    
    return reports

def generate_html_dashboard(reports):
    """Genera el HTML del dashboard con los resultados de las pruebas"""
    
    # Calcular estadísticas totales
    total_tests = sum(r['tests'] for r in reports)
    total_failures = sum(r['failures'] for r in reports)
    total_errors = sum(r['errors'] for r in reports)
    total_skipped = sum(r['skipped'] for r in reports)
    total_time = sum(r['time'] for r in reports)
    total_passed = total_tests - total_failures - total_errors - total_skipped
    
    success_rate = (total_passed / total_tests * 100) if total_tests > 0 else 0
    
    # Recopilar todos los casos de prueba
    all_test_cases = []
    for report in reports:
        for test_case in report['test_cases']:
            test_case['suite'] = report['name']
            all_test_cases.append(test_case)
    
    # Ordenar por estado (fallos primero)
    all_test_cases.sort(key=lambda x: (
        x['status'] == 'success',
        x['status'] == 'skipped',
        x['status'] == 'error',
        x['status'] == 'failure'
    ))
    
    # Generar HTML
    html = f"""<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard de Pruebas - Users API</title>
    <style>
        * {{
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }}
        
        body {{
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
            background: {COLORS['background']};
            color: {COLORS['text']};
            line-height: 1.6;
            padding: 20px;
        }}
        
        .container {{
            max-width: 1400px;
            margin: 0 auto;
        }}
        
        header {{
            background: linear-gradient(135deg, {COLORS['primary']} 0%, #0056b3 100%);
            color: white;
            padding: 30px;
            border-radius: 10px;
            margin-bottom: 30px;
            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
        }}
        
        header h1 {{
            font-size: 2.5em;
            margin-bottom: 10px;
        }}
        
        header .timestamp {{
            opacity: 0.9;
            font-size: 0.9em;
        }}
        
        .stats-grid {{
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
            gap: 20px;
            margin-bottom: 30px;
        }}
        
        .stat-card {{
            background: white;
            padding: 25px;
            border-radius: 10px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
            border-left: 5px solid;
            transition: transform 0.2s;
        }}
        
        .stat-card:hover {{
            transform: translateY(-5px);
            box-shadow: 0 4px 8px rgba(0,0,0,0.15);
        }}
        
        .stat-card.total {{
            border-color: {COLORS['primary']};
        }}
        
        .stat-card.passed {{
            border-color: {COLORS['success']};
        }}
        
        .stat-card.failed {{
            border-color: {COLORS['failure']};
        }}
        
        .stat-card.skipped {{
            border-color: {COLORS['skipped']};
        }}
        
        .stat-card.time {{
            border-color: #17a2b8;
        }}
        
        .stat-card .value {{
            font-size: 2.5em;
            font-weight: bold;
            margin: 10px 0;
        }}
        
        .stat-card .label {{
            color: #666;
            font-size: 0.9em;
            text-transform: uppercase;
            letter-spacing: 1px;
        }}
        
        .stat-card.total .value {{
            color: {COLORS['primary']};
        }}
        
        .stat-card.passed .value {{
            color: {COLORS['success']};
        }}
        
        .stat-card.failed .value {{
            color: {COLORS['failure']};
        }}
        
        .stat-card.skipped .value {{
            color: {COLORS['skipped']};
        }}
        
        .stat-card.time .value {{
            color: #17a2b8;
        }}
        
        .progress-bar {{
            background: white;
            padding: 25px;
            border-radius: 10px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
            margin-bottom: 30px;
        }}
        
        .progress-bar h2 {{
            margin-bottom: 15px;
            color: {COLORS['text']};
        }}
        
        .progress-container {{
            background: #e9ecef;
            border-radius: 10px;
            height: 30px;
            overflow: hidden;
            position: relative;
        }}
        
        .progress-fill {{
            background: linear-gradient(90deg, {COLORS['success']} 0%, #20c997 100%);
            height: 100%;
            transition: width 0.5s ease;
            position: relative;
            z-index: 1;
        }}
        
        .progress-text {{
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            text-align: center;
            line-height: 30px;
            font-weight: bold;
            color: {COLORS['text']};
            z-index: 10;
            pointer-events: none;
            display: flex;
            align-items: center;
            justify-content: center;
        }}
        
        .tests-table {{
            background: white;
            border-radius: 10px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
            overflow: hidden;
        }}
        
        .tests-table h2 {{
            padding: 20px 25px;
            background: {COLORS['primary']};
            color: white;
            margin: 0;
        }}
        
        table {{
            width: 100%;
            border-collapse: collapse;
        }}
        
        thead {{
            background: #f8f9fa;
        }}
        
        th {{
            padding: 15px;
            text-align: left;
            font-weight: 600;
            color: {COLORS['text']};
            border-bottom: 2px solid #dee2e6;
        }}
        
        td {{
            padding: 12px 15px;
            border-bottom: 1px solid #dee2e6;
        }}
        
        tr:hover {{
            background: #f8f9fa;
        }}
        
        .status-badge {{
            display: inline-block;
            padding: 5px 12px;
            border-radius: 20px;
            font-size: 0.85em;
            font-weight: 600;
            text-transform: uppercase;
        }}
        
        .status-badge.success {{
            background: {COLORS['success']};
            color: white;
        }}
        
        .status-badge.failure {{
            background: {COLORS['failure']};
            color: white;
        }}
        
        .status-badge.error {{
            background: {COLORS['error']};
            color: {COLORS['text']};
        }}
        
        .status-badge.skipped {{
            background: {COLORS['skipped']};
            color: white;
        }}
        
        .test-name {{
            font-weight: 500;
            color: {COLORS['text']};
        }}
        
        .test-class {{
            color: #6c757d;
            font-size: 0.9em;
            font-family: 'Courier New', monospace;
        }}
        
        .test-time {{
            color: #6c757d;
            font-family: 'Courier New', monospace;
        }}
        
        .error-details {{
            margin-top: 10px;
            padding: 10px;
            background: #fff3cd;
            border-left: 4px solid {COLORS['error']};
            border-radius: 4px;
            font-family: 'Courier New', monospace;
            font-size: 0.85em;
            white-space: pre-wrap;
            word-break: break-word;
        }}
        
        .error-type {{
            color: {COLORS['failure']};
            font-weight: 600;
            margin-bottom: 5px;
        }}
        
        .summary {{
            background: white;
            padding: 25px;
            border-radius: 10px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
            margin-bottom: 30px;
        }}
        
        .summary h2 {{
            margin-bottom: 15px;
            color: {COLORS['text']};
        }}
        
        .summary-item {{
            padding: 10px 0;
            border-bottom: 1px solid #dee2e6;
        }}
        
        .summary-item:last-child {{
            border-bottom: none;
        }}
        
        .summary-label {{
            font-weight: 600;
            color: #6c757d;
        }}
        
        .suites-container {{
            background: white;
            border-radius: 10px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
            margin-bottom: 30px;
            overflow: hidden;
        }}
        
        .suites-header {{
            padding: 20px 25px;
            background: {COLORS['primary']};
            color: white;
            margin: 0;
            display: flex;
            align-items: center;
            gap: 10px;
        }}
        
        .suite-card {{
            border-bottom: 1px solid #dee2e6;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            margin: 0;
        }}
        
        .suite-card:last-child {{
            border-bottom: none;
        }}
        
        .suite-header {{
            padding: 20px 25px;
            cursor: pointer;
            display: flex;
            align-items: center;
            justify-content: space-between;
            transition: background 0.2s;
            color: white;
        }}
        
        .suite-header:hover {{
            background: rgba(255, 255, 255, 0.1);
        }}
        
        .suite-info {{
            display: flex;
            align-items: center;
            gap: 20px;
            flex: 1;
        }}
        
        .suite-name {{
            font-weight: 600;
            font-size: 1.1em;
            flex: 1;
            font-family: 'Courier New', monospace;
        }}
        
        .suite-stats {{
            display: flex;
            align-items: center;
            gap: 15px;
        }}
        
        .stat-item {{
            display: flex;
            align-items: center;
            gap: 5px;
            font-size: 0.9em;
        }}
        
        .stat-item.success {{
            color: #90EE90;
        }}
        
        .stat-item.failure {{
            color: #FFB6C1;
        }}
        
        .stat-item.error {{
            color: #FFD700;
        }}
        
        .stat-item.skipped {{
            color: #D3D3D3;
        }}
        
        .stat-item.time {{
            color: #E0E0E0;
        }}
        
        .chevron {{
            transition: transform 0.3s ease;
            font-size: 1.2em;
            color: white;
        }}
        
        .suite-card.expanded .chevron {{
            transform: rotate(180deg);
        }}
        
        .suite-content {{
            max-height: 0;
            overflow: hidden;
            transition: max-height 0.3s ease;
            background: white;
        }}
        
        .suite-card.expanded .suite-content {{
            max-height: 5000px;
        }}
        
        .suite-tests-table {{
            width: 100%;
            border-collapse: collapse;
        }}
        
        .suite-tests-table thead {{
            background: #f8f9fa;
        }}
        
        .suite-tests-table th {{
            padding: 12px 20px;
            text-align: left;
            font-weight: 600;
            color: {COLORS['text']};
            border-bottom: 2px solid #dee2e6;
        }}
        
        .suite-tests-table td {{
            padding: 10px 20px;
            border-bottom: 1px solid #dee2e6;
        }}
        
        .suite-tests-table tr:hover {{
            background: #f8f9fa;
        }}
        
        @media (max-width: 768px) {{
            .stats-grid {{
                grid-template-columns: 1fr;
            }}
            
            table {{
                font-size: 0.9em;
            }}
            
            th, td {{
                padding: 8px;
            }}
            
            .suite-header {{
                flex-direction: column;
                align-items: flex-start;
                gap: 15px;
            }}
            
            .suite-info {{
                flex-direction: column;
                align-items: flex-start;
                gap: 10px;
                width: 100%;
            }}
            
            .suite-stats {{
                flex-wrap: wrap;
            }}
        }}
    </style>
</head>
<body>
    <div class="container">
        <header>
            <h1>📊 Dashboard de Pruebas Unitarias</h1>
            <div class="timestamp">Generado el {datetime.now().strftime('%d/%m/%Y %H:%M:%S')}</div>
        </header>
        
        <div class="stats-grid">
            <div class="stat-card total">
                <div class="label">Total de Pruebas</div>
                <div class="value">{total_tests}</div>
            </div>
            <div class="stat-card passed">
                <div class="label">Exitosas</div>
                <div class="value">{total_passed}</div>
            </div>
            <div class="stat-card failed">
                <div class="label">Fallidas</div>
                <div class="value">{total_failures + total_errors}</div>
            </div>
            <div class="stat-card skipped">
                <div class="label">Omitidas</div>
                <div class="value">{total_skipped}</div>
            </div>
            <div class="stat-card time">
                <div class="label">Tiempo Total</div>
                <div class="value">{total_time:.2f}s</div>
            </div>
        </div>
        
        <div class="progress-bar">
            <h2>Tasa de Éxito</h2>
            <div class="progress-container">
                <div class="progress-fill" style="width: {success_rate}%"></div>
                <div class="progress-text">{success_rate:.1f}%</div>
            </div>
        </div>
        
        <div class="summary">
            <h2>📈 Resumen de Ejecución</h2>
            <div class="summary-item">
                <span class="summary-label">Tiempo Total:</span> {total_time:.3f} segundos
            </div>
            <div class="summary-item">
                <span class="summary-label">Suites de Pruebas:</span> {len(reports)}
            </div>
            <div class="summary-item">
                <span class="summary-label">Pruebas Exitosas:</span> {total_passed} ({total_passed/total_tests*100:.1f}%)
            </div>
            <div class="summary-item">
                <span class="summary-label">Pruebas Fallidas:</span> {total_failures + total_errors} ({(total_failures + total_errors)/total_tests*100:.1f}%)
            </div>
        </div>
        
        <div class="suites-container">
            <h2 class="suites-header">
                <span>📄</span>
                <span>Suites de Pruebas ({len(reports)})</span>
            </h2>
"""
    
    # Agrupar pruebas por suite
    for report in reports:
        suite_passed = report['tests'] - report['failures'] - report['errors'] - report['skipped']
        suite_id = f"suite-{report['name'].replace('.', '-').replace(' ', '-')}"
        
        html += f"""
            <div class="suite-card" id="{suite_id}">
                <div class="suite-header" onclick="toggleSuite('{suite_id}')">
                    <div class="suite-info">
                        <div class="suite-name">{report['name']}</div>
                        <div class="suite-stats">
                            <div class="stat-item success">
                                <span>✓</span>
                                <span>{suite_passed}</span>
                            </div>
                            <div class="stat-item failure">
                                <span>✗</span>
                                <span>{report['failures']}</span>
                            </div>
                            <div class="stat-item error">
                                <span>⚠</span>
                                <span>{report['errors']}</span>
                            </div>
                            <div class="stat-item skipped">
                                <span>⊘</span>
                                <span>{report['skipped']}</span>
                            </div>
                            <div class="stat-item time">
                                <span>⏱</span>
                                <span>{report['time']:.2f}s</span>
                            </div>
                        </div>
                    </div>
                    <div class="chevron">▼</div>
                </div>
                <div class="suite-content">
                    <table class="suite-tests-table">
                        <thead>
                            <tr>
                                <th>Estado</th>
                                <th>Nombre de Prueba</th>
                                <th>Tiempo (s)</th>
                            </tr>
                        </thead>
                        <tbody>
"""
        
        # Agregar pruebas de esta suite
        for test_case in report['test_cases']:
            status_class = test_case['status']
            status_text = {
                'success': '✓ Exitoso',
                'failure': '✗ Fallido',
                'error': '⚠ Error',
                'skipped': '⊘ Omitido'
            }.get(status_class, status_class)
            
            html += f"""
                            <tr>
                                <td><span class="status-badge {status_class}">{status_text}</span></td>
                                <td>
                                    <div class="test-name">{test_case['name']}</div>
                                </td>
                                <td class="test-time">{test_case['time']:.3f}</td>
                            </tr>
"""
            
            # Agregar detalles de error si existe
            if test_case['error_msg']:
                error_display = test_case['error_msg']
                if len(error_display) > 500:
                    error_display = error_display[:500] + "..."
                html += f"""
                            <tr>
                                <td colspan="3">
                                    <div class="error-details">
                                        <div class="error-type">{test_case['error_type'] or 'Error'}</div>
                                        {error_display}
                                    </div>
                                </td>
                            </tr>
"""
        
        html += """
                        </tbody>
                    </table>
                </div>
            </div>
"""
    
    html += """
        </div>
    </div>
    
    <script>
        function toggleSuite(suiteId) {
            const suite = document.getElementById(suiteId);
            suite.classList.toggle('expanded');
        }
        
        // Expandir automáticamente las suites con fallos
        document.addEventListener('DOMContentLoaded', function() {
            const suites = document.querySelectorAll('.suite-card');
            suites.forEach(suite => {
                const failureEl = suite.querySelector('.stat-item.failure span:last-child');
                const errorEl = suite.querySelector('.stat-item.error span:last-child');
                if (failureEl && errorEl) {
                    const failureCount = parseInt(failureEl.textContent) || 0;
                    const errorCount = parseInt(errorEl.textContent) || 0;
                    if (failureCount > 0 || errorCount > 0) {
                        suite.classList.add('expanded');
                    }
                }
            });
        });
    </script>
</body>
</html>
"""
    
    return html

def main():
    """Función principal"""
    print("=" * 60)
    print("🚀 Generador de Reporte HTML de Pruebas")
    print("=" * 60)
    print()
    
    # Ejecutar pruebas
    exit_code = run_maven_tests()
    print()
    
    # Recopilar reportes
    print("📄 Recopilando reportes de pruebas...")
    reports = collect_test_reports()
    
    if not reports:
        print("❌ No se encontraron reportes para generar el dashboard")
        sys.exit(1)
    
    print(f"✓ Se encontraron {len(reports)} suite(s) de pruebas")
    print()
    
    # Generar HTML
    print("🎨 Generando dashboard HTML...")
    html_content = generate_html_dashboard(reports)
    
    # Guardar archivo
    output_file = 'test-report.html'
    with open(output_file, 'w', encoding='utf-8') as f:
        f.write(html_content)
    
    print(f"✅ Dashboard generado exitosamente: {output_file}")
    print()
    print("=" * 60)
    print("📌 Para ver el reporte:")
    print(f"   Abre {output_file} en tu navegador web")
    print("=" * 60)
    
    # Intentar abrir automáticamente en el navegador
    try:
        import webbrowser
        file_path = os.path.abspath(output_file)
        webbrowser.open(f'file://{file_path}')
        print("🌐 Abriendo reporte en el navegador...")
    except:
        pass
    
    return exit_code

if __name__ == '__main__':
    sys.exit(main())

