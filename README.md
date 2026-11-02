# Bonapharm

Aplicación web para gestionar el catálogo y consultar existencias de productos farmacéuticos. Incluye acceso por sesión, registro, consulta, modificación y deshabilitación lógica de productos. El proyecto está en desarrollo.

## Tecnologías y versiones

Versiones verificadas en `pom.xml`, el BOM de Spring Boot y los recursos del proyecto:

| Componente | Versión / uso |
| --- | --- |
| Java | 25 (`java.version`); JDK 25, compilación 25+37, disponible en la preparación |
| Spring Boot | 4.1.0 |
| Spring Framework | 7.0.8, MVC |
| Spring Data JPA / Hibernate | Dependencias administradas por Boot / Hibernate 7.4.1.Final |
| Thymeleaf | 3.1.5.RELEASE |
| MySQL Connector/J | 9.7.0; esta es la versión del controlador, no del servidor |
| Lombok | 1.18.46 |
| Spring Security Crypto | 7.1.0, BCrypt; no incluye la cadena de seguridad de Spring Security |
| Maven | 3.9.16 mediante wrapper; wrapper 3.3.4 |
| Bootstrap / Bootstrap Icons | 5.3.8 / 1.11.3, cargados desde jsDelivr |

## Requisitos

- JDK 25 con `JAVA_HOME` apuntando a su instalación. Java 17 no cumple el objetivo de compilación actual.
- MySQL 8.0.16 o posterior, conforme al esquema y sus restricciones `CHECK`. La versión real del servidor no se verificó durante esta preparación.
- Acceso a Maven Central para descargar Maven y dependencias en la primera ejecución, y a jsDelivr para los estilos e iconos.
- Cliente de MySQL o MySQL Workbench. No se necesita instalar Maven si se utiliza el wrapper.

## Preparar MySQL

1. Con una cuenta que pueda crear bases y tablas, ejecutar **una sola vez en una base nueva**:

   ```sql
   SOURCE /ruta/al/proyecto/sql/bonapharm.sql;
   ```

   En Workbench, abrir el archivo y ejecutarlo. Crea `bd_bonapharm`, las tablas y cinco productos ficticios de demostración. No contiene usuarios, contraseñas ni hashes de acceso. Los productos de ejemplo no son registros privados. El script no es una migración idempotente: repetirlo puede producir duplicados de SKU.

2. Crear una cuenta exclusiva de la aplicación desde una sesión administrativa de MySQL. Sustituir la clave ilustrativa por una propia y no guardar el comando real en Git:

   ```sql
   CREATE USER 'bonapharm_app'@'localhost' IDENTIFIED BY '<CLAVE_PROPIA_DE_MYSQL>';
   GRANT SELECT, INSERT, UPDATE, DELETE ON bd_bonapharm.* TO 'bonapharm_app'@'localhost';
   ```

   Ajustar el host de la cuenta si la aplicación se conecta desde otra máquina.

3. Crear un usuario de acceso al sistema. Primero generar un hash BCrypt local, según la sección siguiente; después ejecutar desde una sesión local de MySQL:

   ```sql
   USE bd_bonapharm;
   INSERT INTO usuarios (usuario, nombre, contrasena_hash, rol, estado)
   VALUES ('<USUARIO_LOCAL>', '<NOMBRE_LOCAL>', '<HASH_BCRYPT_GENERADO>', 'ADMIN', 1);
   ```

   Los valores entre `<...>` son marcadores que deben sustituirse. No hay una contraseña predeterminada. La cuenta de MySQL y el usuario de acceso web son independientes. No publicar el hash ni datos personales del usuario creado.

`sql/actualizacion_guia_lote.sql` sirve únicamente para una base antigua cuya tabla `productos` todavía no tenga `numero_guia_remision` y `numero_lote`. **No ejecutarlo después del esquema nuevo**, que ya incluye ambas columnas. Revisar y respaldar una base existente antes de aplicar cambios.

### Generar un hash de acceso

Desde la raíz del proyecto, copiar la dependencia necesaria a una carpeta generada:

```powershell
.\mvnw.cmd dependency:copy-dependencies "-DincludeArtifactIds=spring-security-crypto" "-DoutputDirectory=target/hash-libs"
java --class-path "target/hash-libs/*" scripts/GenerarHash.java
```

En Linux/macOS, sustituir `.\mvnw.cmd` por `./mvnw`. Utilizar el ejecutable `java` del JDK 25. La utilidad solicita y confirma la contraseña sin mostrarla; imprime un hash BCrypt para el `INSERT` anterior. Ejecutarla en una terminal interactiva, sin redirigir la entrada. No guardar ni publicar el hash generado.

## Configurar el entorno

`src/main/resources/application.properties` obtiene las credenciales de `DB_USERNAME` y `DB_PASSWORD`, sin valores predeterminados. `.env.example` documenta las variables con marcadores públicos. **Spring Boot no carga `.env` automáticamente**: exportar las variables en la terminal de ejecución o configurarlas en el IDE.

Ejemplo en PowerShell:

```powershell
$env:JAVA_HOME = 'C:\ruta\al\jdk-25'
$env:DB_URL = 'jdbc:mysql://localhost:3306/bd_bonapharm?serverTimezone=America/Lima'
$env:DB_USERNAME = 'bonapharm_app'
$claveDb = Read-Host 'Contraseña de MySQL' -AsSecureString
$env:DB_PASSWORD = [System.Net.NetworkCredential]::new('', $claveDb).Password
$env:THYMELEAF_CACHE = 'false'
```

Ejemplo en Bash:

```bash
export JAVA_HOME=/ruta/al/jdk-25
export DB_URL='jdbc:mysql://localhost:3306/bd_bonapharm?serverTimezone=America/Lima'
export DB_USERNAME=bonapharm_app
read -rsp 'Contraseña de MySQL: ' DB_PASSWORD; echo
export DB_PASSWORD
export THYMELEAF_CACHE=false
```

| Variable | Comportamiento |
| --- | --- |
| `DB_URL` | Por defecto, MySQL local en puerto 3306, base `bd_bonapharm` y zona `America/Lima` |
| `DB_USERNAME`, `DB_PASSWORD` | Obligatorias; credenciales de la cuenta de MySQL |
| `SERVER_PORT` | Opcional; 8080 por defecto |
| `THYMELEAF_CACHE` | Opcional; `true` por defecto, `false` para desarrollo |

Hibernate usa `ddl-auto=none`: no crea ni actualiza el esquema. La configuración permite `application-local.*` y `application-secrets.*` privados, excluidos de Git; sus perfiles requieren activación explícita mediante `SPRING_PROFILES_ACTIVE`. No guardar secretos en el archivo base ni en los ejemplos.

## Compilar y ejecutar

En Windows, desde la raíz:

```powershell
.\mvnw.cmd clean verify
.\mvnw.cmd spring-boot:run
```

En Linux/macOS:

```bash
./mvnw clean verify
./mvnw spring-boot:run
```

También se puede ejecutar el paquete después de compilar:

```bash
java -jar target/bonapharm-0.0.1-SNAPSHOT.jar
```

Abrir http://localhost:8080/bonapharm/ e iniciar sesión con el usuario creado en MySQL. Mantener las variables de entorno en la terminal que inicia el proceso.

## Funcionalidades implementadas

- Inicio de sesión con usuarios activos y comprobación BCrypt; cierre de sesión e interceptor que exige sesión para las rutas internas.
- Resumen de productos activos, unidades en stock y productos con stock bajo.
- Búsqueda de productos activos por nombre o SKU.
- Registro con categoría, precio, stock inicial, descripción, número de lote y guía de remisión.
- Generación de SKU por prefijo de categoría y correlativo; cálculo del stock mínimo como `ceil(consumo diario × (7 + 2))`. Usa consumos fijos por categoría: 6 para analgésicos, 3 para antibióticos, 2 para antialérgicos y vitaminas, 1,5 para cuidado personal y 2 por defecto. No calcula consumo a partir de ventas.
- Modificación de nombre, descripción, categoría, guía, lote y precio; recalcula el mínimo. No cambia el SKU ni el stock actual.
- Deshabilitación lógica de productos, excluyéndolos de las consultas de activos.
- Consulta de stock con indicadores de agotado, stock bajo (`stock <= stockMinimo`) y disponible.

El SQL contiene tablas de lotes, movimientos, clientes, ventas y detalles de ventas, pero no existen controladores, servicios ni pantallas operativas para esos módulos. El número de lote del producto no equivale a gestión de lotes con vencimientos.

## Pendientes y limitaciones

- Implementar entradas y salidas, ventas, clientes, reportes, gestión de lotes y vencimientos. Las tarjetas de estos módulos están desactivadas; algunos textos promocionales de la pantalla de acceso describen el alcance futuro.
- Añadir pruebas automatizadas unitarias, de integración y de interfaz. El proyecto recibido no incluye `src/test`; compilar no demuestra que sus flujos funcionen contra MySQL.
- Verificar la importación SQL, el inicio de sesión y los flujos de productos contra un servidor MySQL local.
- Completar validaciones, manejo de errores de persistencia y control de concurrencia de la generación de SKU.
- Incorporar autorización por roles, protección CSRF, renovación del identificador de sesión al autenticar y cambios de estado mediante POST. El rol se guarda en sesión, pero actualmente no limita operaciones; la deshabilitación y el cierre de sesión usan GET.
- Preparar configuración de despliegue con HTTPS, cookies seguras y conexión TLS a MySQL según el entorno. La configuración actual corresponde al desarrollo local.

## Verificación de esta preparación

Verificación realizada el 2 de noviembre de 2026 con JDK 25 y Maven 3.9.16, usando dependencias locales en modo offline:

- `verify -Dmaven.compiler.fork=true` terminó con `BUILD SUCCESS` y generó `target/bonapharm-0.0.1-SNAPSHOT.jar`. Maven indicó `No tests to run`.
- Esa ejecución también imprimió una excepción de acceso a un JAR al cerrar el compilador (`java.nio.file.AccessDeniedException`). La ejecución normal de `clean verify` quedó bloqueada por restricciones de acceso del entorno de preparación. Por ello, **queda pendiente confirmar una compilación limpia sin excepciones en una terminal local** con `.\mvnw.cmd clean verify`.
- No se ejecutaron pruebas funcionales contra MySQL ni pruebas de interfaz. No se afirma que el sistema esté validado para producción.

Se corrigió en `mvnw.cmd` el acceso a `Target[0]` cuando la carpeta de Maven no es un enlace, que impedía iniciar el wrapper en Windows.

## Archivos que se publican

Código `src/`, esquema y actualización SQL revisados, utilidad `scripts/GenerarHash.java`, `pom.xml`, wrapper `mvnw`, `mvnw.cmd` y `.mvn/wrapper/maven-wrapper.properties`, `.gitignore`, `.gitattributes`, `.env.example` y este README.

Se excluyen `target/`, binarios, registros, configuraciones personales de IDE, `.env` y sus variantes privadas, configuraciones locales, claves, certificados privados, copias de seguridad y directorios de datos. Antes de cada commit, revisar `git status --short`, `git diff --cached` y `git ls-files`; `.gitignore` no protege secretos que se añadan a archivos públicos ni elimina archivos ya versionados.
