# Disciplina

Descripción
-----------
Disciplina es una aplicación Java orientada a la gestión disciplinaria académica: registro de faltas, consultas por orientadores, paneles administrativos para Rectoría y generación de informes y estadísticas. El proyecto está organizado por roles y empaquetado con Maven.

Características principales
--------------------------
- Registro y gestión de faltas disciplinarias.
- Consultas y registro por parte de orientadores.
- Paneles de administración y estadísticas en Rectoría.
- Generación de reportes y exportaciones.
- Scripts auxiliares para empaquetado y actualizaciones.

Stack tecnológico
-----------------
- Lenguaje principal: Java
- Herramienta de build: Maven (wrapper `mvnw`/`mvnw.cmd` incluido)
- Scripts: PowerShell
- Estilos: CSS
- Instalador opcional: Inno Setup (.iss)

Requisitos
---------
- JDK 11 o superior (ajustar según `pom.xml`)
- Maven 3.6+ (o usar el wrapper incluido)
- (Opcional) Inno Setup para generadores de instalador Windows

Instalación y ejecución
-----------------------
1. Clonar el repositorio

```bash
git clone https://github.com/srusso1/Disciplina.git
cd Disciplina
```

2. Construir con el wrapper (Linux/macOS)

```bash
./mvnw clean package
```

En Windows:

```powershell
mvnw.cmd clean package
```

3. Ejecutar la aplicación

```bash
java -jar target/disciplina-<versión>.jar
```

4. Ejecutar tests

```bash
./mvnw test
```

Ejecución desde el IDE
----------------------
- Importar el proyecto como Maven y ejecutar la clase principal `application.App`.

Estructura del proyecto
-----------------------
```
.mvn/
mvnw, mvnw.cmd
pom.xml
version.properties
src/
  main/
    java/
      application/      # App.java, Launcher.java
      controllers/      # Login, Orientador, Rectoria, Dashboard
      database/         # Persistencia
      models/           # Entidades del dominio
      reports/          # Generación de reportes
      utils/            # Utilidades comunes
    resources/          # Configuración y plantillas
scripts/               # Scripts y empaquetado
reportes/              # Plantillas y recursos de reportes (PDF, XLS, etc.)
```

Puntos clave del código
-----------------------
- `src/main/java/application/App.java` — arranque de la aplicación.
- `src/main/java/controllers/Orientador/RegistrarFaltaController.java` — lógica para registrar faltas.
- `src/main/java/controllers/Rectoria/InformesController.java` — generación y descarga de informes.
- `src/main/java/controllers/Login/LoginController.java` — autenticación.

Configuración
-------------
- Revisa `src/main/resources` para los archivos de configuración (`.properties` o `.yml`).
- Variables comunes: `APP_PORT`, `DB_URL`, `DB_USER`, `DB_PASSWORD`.

Empaquetado y actualizaciones
----------------------------
- El repositorio incluye `EMPAQUETADO_Y_ACTUALIZACIONES.md` y scripts en `scripts/` para procesos de empaquetado y actualización.
- Para crear instaladores Windows, usa Inno Setup si está disponible:

```powershell
"C:\Program Files (x86)\Inno Setup 6\ISCC.exe" installer\disciplina.iss
```

Pruebas y calidad de código
---------------------------
- Ejecuta `./mvnw test`.
- Considera añadir JaCoCo y Checkstyle en `pom.xml` para cobertura y calidad.

Contribuir
----------
1. Fork del repositorio.
2. Crear rama: `git checkout -b feature/nombre-descriptivo`.
3. Commit y push.
4. Abrir Pull Request con descripción y pruebas.

Licencia
--------
Añade un archivo `LICENSE` con la licencia preferida (MIT, Apache-2.0) si aún no existe.

Contacto
--------
Para soporte, abre un issue indicando pasos para reproducir, logs y versión de Java.
