# Bonapharm

Aplicación web para gestionar productos farmacéuticos, lotes, existencias, clientes y ventas. Incluye movimientos de inventario, guías de remisión en PDF, reportes y administración de usuarios. Este repositorio incorpora la entrega `proyecto_bonapharm_9`; las funcionalidades descritas se identificaron en el código y siguen pendientes de validación funcional contra MySQL.

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
| OpenPDF | 1.3.30; generación de guías y reportes PDF |
| Apache POI | 5.3.0 (`poi-ooxml`); exportación Excel |
| Bootstrap / Bootstrap Icons | 5.3.8 / 1.11.3, cargados desde jsDelivr |

## Requisitos

- JDK 25 con `JAVA_HOME` apuntando a su instalación. Java 17 no cumple el objetivo de compilación actual.
- MySQL 8.0.16 o posterior, conforme al esquema y sus restricciones `CHECK`. La versión real del servidor no se verificó durante esta preparación.
- Acceso a Maven Central para descargar Maven y dependencias en la primera ejecución, y a jsDelivr para los estilos e iconos.
- Cliente de MySQL o MySQL Workbench. No se necesita instalar Maven si se utiliza el wrapper.

## Preparar MySQL

1. **El script de esta entrega contiene `DROP DATABASE IF EXISTS bd_bonapharm`: borra y recrea toda la base.** Usarlo solo en una instancia de desarrollo vacía o descartable. No ejecutarlo sobre una base con datos que se deban conservar. Para actualizar una instalación anterior, respaldar y preparar una migración; el proyecto no incluye una migración entre ambos esquemas.

   Con una cuenta que pueda crear y eliminar bases y tablas, ejecutar en ese entorno de desarrollo:

   ```sql
   SOURCE /ruta/al/proyecto/sql/bonapharm.sql;
   ```

   En Workbench, abrir el archivo y ejecutarlo. Crea las tablas del nuevo esquema y catálogos genéricos de medicamentos, categorías, lotes y motivos de salida. La versión publicada no incluye usuarios, hashes de acceso, clientes, ventas ni movimientos precargados. Tampoco incluye productos ni existencias de demostración; deben registrarse localmente. Solo se conservan semillas de catálogos genéricos.

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

`sql/actualizacion_guia_lote.sql` se conserva como referencia histórica de la entrega anterior. **No aplicarlo al esquema actual**: `numero_guia_remision` y `numero_lote` dejaron de ser columnas de `productos`; la información de lotes y movimientos tiene tablas propias. No sustituye una migración desde la primera versión.

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
| `EMPRESA_NOMBRE` | Nombre del emisor de las guías PDF; `Bonapharm` por defecto |
| `EMPRESA_DIRECCION` | Dirección del emisor; vacía por defecto |
| `EMPRESA_PUNTO_PARTIDA` | Dirección de salida de las guías; vacía por defecto |

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

## Funcionalidades presentes en esta entrega

- Acceso por sesión y BCrypt. El interceptor limita las rutas del rol `VENDEDOR` a ventas, cuenta, inicio, consulta de stock y cierre de sesión; los demás roles no tienen esa restricción.
- Administración de usuarios: registro con roles `ADMIN` o `VENDEDOR`, cambio de estado, restablecimiento de clave y cambio de contraseña propia.
- Productos: registro, búsqueda por nombre o código, modificación y deshabilitación lógica. El código y el stock mínimo ahora se ingresan en el formulario: ya no se generan automáticamente como en la primera entrega. Los catálogos alimentan las listas de selección.
- Inventario: entradas con producto, lote, vencimiento, cantidad y guía; salidas por lote con motivo. Los servicios actualizan existencias de producto y lote y registran movimientos dentro de transacciones.
- Consulta de lotes vencidos y próximos a vencer; indicadores en el menú principal.
- Clientes: registro de código, RUC, razón social y direcciones; consulta del historial de compras.
- Ventas con cliente, fechas de emisión y traslado, detalle de productos y lotes, validación de cantidades, descuento de stock, subtotal, IGV fijo del 18 % y total. Selección de lote o asignación automática por vencimiento más próximo (FEFO), excluyendo lotes vencidos.
- Historial de ventas, cancelación con devolución de existencias y guía de remisión PDF con datos del emisor configurables por entorno.
- Reportes de inventario, ventas y vencimientos con filtros y exportación PDF y Excel.

La existencia de controladores, servicios y plantillas no demuestra que todos los flujos funcionen correctamente en ejecución. Las guías PDF son documentos generados por la aplicación; no hay integración implementada con una autoridad tributaria.

## Pendientes y limitaciones

- Ejecutar pruebas funcionales con MySQL de acceso, roles, productos, lotes, movimientos, ventas, cancelaciones, usuarios, clientes y exportaciones. Añadir pruebas automatizadas: el ZIP no incluye `src/test`.
- Preparar una migración para bases anteriores; el script actual es una recreación destructiva, no una actualización incremental.
- Revisar concurrencia y bloqueo de existencias al registrar movimientos o ventas simultáneas y consistencia entre stock de producto y sus lotes.
- Fortalecer autorización: `VentasServiceImpl.obtener` no restringe el acceso por propietario al consultar una guía, aunque la cancelación sí comprueba quién creó la venta. Revisar también el historial de compras de clientes y el tratamiento de roles distintos de `VENDEDOR`.
- Incorporar protección CSRF, renovación de sesión al autenticar, políticas de contraseña más fuertes y uso de POST para todas las operaciones que cambian estado; la deshabilitación de productos y el cierre de sesión siguen usando GET.
- Completar el manejo de errores y la validación de formularios; comprobar el renderizado de PDF y Excel con datos reales solo en un entorno privado.
- Preparar HTTPS, cookies seguras y TLS a MySQL para el entorno de despliegue. Configurar localmente los datos del emisor antes de usar las guías.

## Verificación de la entrega actual

Preparación del 23 de noviembre de 2026: revisión estática del ZIP y comparación con `main`. Se conserva la configuración por variables de entorno, las exclusiones y la utilidad local para crear hashes. Se retiraron de la publicación cuentas, hashes, datos de clientes y operaciones, así como direcciones fijas del emisor.

Se intentó `clean verify` con Maven 3.9.16 y JDK 25, en modo offline con dependencias locales. Maven inició la compilación de los 51 archivos Java, pero terminó con `BUILD FAILURE` y un error de acceso a la dependencia `spring-web-7.0.8.jar` desde el compilador. No se considera una compilación validada. Queda pendiente ejecutar `.\mvnw.cmd clean verify` en una terminal local con acceso normal al JDK y a las dependencias. No se ejecutaron pruebas funcionales contra MySQL ni se verificaron visualmente los PDF o Excel. Los resultados de la entrega anterior no se consideran una validación de esta entrega.

## Archivos que se publican

Código `src/`, esquema y actualización SQL revisados, utilidad `scripts/GenerarHash.java`, `pom.xml`, wrapper `mvnw`, `mvnw.cmd` y `.mvn/wrapper/maven-wrapper.properties`, `.gitignore`, `.gitattributes`, `.env.example` y este README.

Se excluyen `target/`, binarios, registros, configuraciones personales de IDE, `.env` y sus variantes privadas, configuraciones locales, claves, certificados privados, copias de seguridad y directorios de datos. Antes de cada commit, revisar `git status --short`, `git diff --cached` y `git ls-files`; `.gitignore` no protege secretos que se añadan a archivos públicos ni elimina archivos ya versionados.
