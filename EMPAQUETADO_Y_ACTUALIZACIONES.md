# 📦 Sistema de Empaquetado y Actualizaciones de Disciplina

Este documento describe cómo generar versiones de Disciplina para distribuir a los usuarios, incluyendo el sistema de actualizaciones automáticas.

## 📋 Requisitos Previos

- **Java Development Kit (JDK)**: JDK 21 o superior (recomendado tener una copia portable en `C:\runtime\jdk-21-jre`)
- **Launch4j**: Instalado en `C:\Program Files (x86)\Launch4j\`
- **Inno Setup 6**: Instalado en `C:\Program Files (x86)\Inno Setup 6\`
- **PowerShell**: 5.0 o superior

## 📝 Archivos Clave

### `version.properties`
Archivo de configuración centralizado en la raíz del proyecto. **Edítalo antes de compilar.**

```properties
# Versión de la aplicación (IMPORTANTE: debe coincidir con pom.xml)
app.version=1.0.0

# Notas del parche que verán los usuarios
app.release.notes=Descripción de los cambios

# Versión mínima recomendada para los usuarios
app.min.supported=1.0.0
```

**Este archivo controla:**
- ✅ Número de versión
- ✅ Notas de cambio del parche
- ✅ Requisito mínimo de versión
- ✅ Sincronización automática con `pom.xml`

### Scripts de Empaquetado

#### 1. `build-win.ps1`
Script principal que construye todo el paquete:

**Ejecución:**
```powershell
C:\Users\sebas\Documents\GitHub\Disciplina\scripts\packaging\build-win.ps1 `
  -RuntimePath "C:\runtime\jdk-21-jre"
```

**Qué hace:**
1. Lee `version.properties` automáticamente
2. Sincroniza `pom.xml` con la versión en `version.properties`
3. Compila el proyecto con Maven
4. Copia dependencias y base de datos
5. Genera EXE con Launch4j
6. Genera instalador con Inno Setup
7. Crea el archivo `manifest.json` con hash SHA-256

**Salida:**
- `dist/app/` → EXE portátil + dependencias
- `dist/installer/` → Instalador EXE
- `updates/` (en raíz) → Manifiesto para el sistema de updates

#### 2. `prepare-release.ps1`
Script que prepara la carpeta final para distribuir al usuario:

**Ejecución:**
```powershell
C:\Users\sebas\Documents\GitHub\Disciplina\scripts\packaging\prepare-release.ps1 `
  -RuntimePath "C:\runtime\jdk-21-jre"
```

**Qué hace:**
1. Ejecuta `build-win.ps1` automáticamente
2. Crea `dist/release_to_user/`
3. Organiza los archivos de actualización
4. Recalcula hashes SHA-256
5. Genera instrucciones para el usuario

**Salida:**
- `dist/release_to_user/updates/` → Carpeta lista para entregarle al usuario

#### 3. `update-manifest.ps1`
Script auxiliar que genera el archivo `manifest.json`:

```powershell
& "$UpdateManifestScript" `
    -Version "1.0.0" `
    -ArtifactPath "C:\Disciplina\updates\Disciplina-Setup.exe" `
    -Notes "Descripción del parche" `
    -MinSupportedVersion "1.0.0"
```

**Genera JSON como:**
```json
{
  "channel": "stable",
  "publishedAt": "2026-05-17T12:00:00Z",
  "latestVersion": "1.0.0",
  "minSupportedVersion": "1.0.0",
  "artifactPath": "C:\\Disciplina\\updates\\Disciplina-Setup.exe",
  "sha256": "a1b2c3d4e5f6...",
  "notes": "Descripción del parche"
}
```

## 🚀 Flujo Completo de Lanzamiento

### Paso 1: Editar `version.properties`

```properties
app.version=1.0.1
app.release.notes=Correcciones de seguridad y mejoras de rendimiento
app.min.supported=1.0.0
```

### Paso 2: Ejecutar `prepare-release.ps1`

Abre PowerShell como Administrador y ejecuta:

```powershell
cd "C:\Users\sebas\Documents\GitHub\Disciplina"

.\scripts\packaging\prepare-release.ps1 -RuntimePath "C:\Program Files\Java\jdk-21"
```

**Salida esperada:**
```
===================================================
   PROCESO DE LANZAMIENTO (RELEASE) - DISCIPLINA
===================================================

[1/3] Leyendo versión desde version.properties...
  Versión: 1.0.1
  Notas: Correcciones de seguridad y mejoras de rendimiento
  Mínimo soportado: 1.0.0

[2/3] Ejecutando empaquetado y sincronización...
  [Ejecuta todo el build...]
  ✓ Build completado exitosamente (v1.0.1).

[3/3] Preparando carpeta para el usuario...

===================================================
✓ Proceso completado exitosamente.
===================================================

INSTRUCCIONES PARA ACTUALIZAR AL USUARIO:
────────────────────────────────────────
1. Localiza la carpeta 'updates' en:
   C:\Users\sebas\Documents\GitHub\Disciplina\dist\release_to_user
2. Cópiala completa (updates/) junto con Disciplina-Setup.exe
3. Entrega al usuario para que la ponga en: C:\Disciplina\updates
   (Quedando así: C:\Disciplina\updates\Disciplina-Setup.exe, etc.)
```

### Paso 3: Entregar al Usuario

Copia la carpeta `dist/release_to_user/updates/` al usuario. Él debe pegarla en:
```
C:\Disciplina\updates\
```

## 🔒 Sistema de Seguridad

### Validación SHA-256

Cada archivo de actualización se protege con un hash SHA-256:

1. **Al compilar**: `build-win.ps1` calcula el SHA-256 del instalador
2. **Al actualizar**: `UpdateService.java` valida el hash antes de ejecutar
3. Si el hash no coincide → ❌ Actualización cancelada por seguridad

### Estructura del Instalador (Inno Setup)

El archivo `Disciplina.iss` incluye:

```ini
[Dirs]
Name: "{app}\updates"
Name: "{app}\updates\win-x64\stable"
```

Esto garantiza que **siempre se crean las carpetas necessarias** para el sistema de updates.

## 🎯 Configuración de Actualizaciones en el Usuario

El usuario debe habilitar actualizaciones editando:
```
%LOCALAPPDATA%\Disciplina\config\updates.properties
```

Y establecer:
```properties
updates.enabled=true
updates.manifest.uri=file:///C:/Disciplina/updates/win-x64/stable/manifest.json
```

## 📂 Estructura de Carpetas Generada

```
dist/
├── app/
│   ├── Disciplina.exe          (EXE portátil)
│   ├── Disciplina.jar
│   ├── lib/                    (dependencias)
│   ├── runtime/                (JRE embebido)
│   └── database/               (DisciplinaDB.db)
│
├── installer/
│   └── Disciplina-Setup-1.0.1.exe
│
└── release_to_user/
    └── updates/
        ├── Disciplina-Setup.exe
        └── win-x64/
            └── stable/
                └── manifest.json
```

## 🐛 Troubleshooting

### Error: "No se encontró Launch4j"
Instala Launch4j en `C:\Program Files (x86)\Launch4j\` o edita la ruta en los scripts.

### Error: "No se encontró Inno Setup"
Instala Inno Setup 6 en `C:\Program Files (x86)\Inno Setup 6\` o edita la ruta en los scripts.

### Error: "No se encontró el runtime JRE"
Asegúrate de pasar la ruta correcta con `-RuntimePath`:
```powershell
.\scripts\packaging\prepare-release.ps1 -RuntimePath "C:\Tu\Ruta\A\JRE"
```

### El manifest.json no se genera correctamente
Verifica que la carpeta `C:\Disciplina\updates\` exista en tu sistema (no es necesaria para la compilación, pero se menciona en la ruta del manifest).

## ✅ Checklist Antes de Lanzar

- [ ] Editorial `version.properties` con la nueva versión
- [ ] Verificado que `pom.xml` será sincronizado automáticamente
- [ ] Ejecutado `prepare-release.ps1` sin errores
- [ ] Verificado que `dist/release_to_user/updates/manifest.json` existe
- [ ] Revisado el contenido del manifest.json
- [ ] Copias la carpeta `updates/` al servidor o medio de distribución
- [ ] Prueba manual de instalación con usuario de prueba
- [ ] Documenta los cambios en notas de lanzamiento

---

**Última actualización:** 17/05/2026
**Versión del sistema:** 1.0.0

