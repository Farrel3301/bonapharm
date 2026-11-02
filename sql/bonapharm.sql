-- Bonapharm: base de datos MySQL 8
CREATE DATABASE IF NOT EXISTS bd_bonapharm CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE bd_bonapharm;

CREATE TABLE IF NOT EXISTS usuarios (
  id_usuario      BIGINT AUTO_INCREMENT PRIMARY KEY,
  usuario         VARCHAR(50)  NOT NULL UNIQUE,
  nombre          VARCHAR(100) NOT NULL,
  contrasena_hash VARCHAR(255) NOT NULL,
  rol             VARCHAR(30)  NOT NULL,           -- ADMIN, ALMACEN, VENDEDOR
  estado          TINYINT(1)   NOT NULL DEFAULT 1,
  fecha_registro  DATETIME     DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS productos (
  id_producto    BIGINT AUTO_INCREMENT PRIMARY KEY,
  sku            VARCHAR(30)   NOT NULL UNIQUE,
  nombre         VARCHAR(120)  NOT NULL,
  descripcion    TEXT,
  categoria      VARCHAR(60)   NOT NULL,
  numero_guia_remision VARCHAR(30) NOT NULL,
  numero_lote    VARCHAR(40)   NOT NULL,
  precio         DECIMAL(10,2) NOT NULL CHECK (precio >= 0),
  stock          INT           NOT NULL DEFAULT 0 CHECK (stock >= 0),
  stock_minimo   INT           NOT NULL DEFAULT 5,
  estado         TINYINT(1)    NOT NULL DEFAULT 1,
  fecha_creacion DATE          NOT NULL
);

CREATE TABLE IF NOT EXISTS lotes (                 -- HU-07, HU-08, HU-09, HU-10
  id_lote           BIGINT AUTO_INCREMENT PRIMARY KEY,
  id_producto       BIGINT      NOT NULL,
  codigo_lote       VARCHAR(40) NOT NULL,
  fecha_vencimiento DATE        NOT NULL,
  cantidad          INT         NOT NULL DEFAULT 0,
  FOREIGN KEY (id_producto) REFERENCES productos(id_producto)
);

CREATE TABLE IF NOT EXISTS movimientos_inventario ( -- HU-05, HU-06
  id_movimiento BIGINT AUTO_INCREMENT PRIMARY KEY,
  id_producto   BIGINT      NOT NULL,
  id_usuario    BIGINT      NOT NULL,
  tipo          ENUM('ENTRADA','SALIDA','VENTA') NOT NULL,
  cantidad      INT         NOT NULL CHECK (cantidad > 0),
  motivo        VARCHAR(150),
  fecha         DATETIME    DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (id_producto) REFERENCES productos(id_producto),
  FOREIGN KEY (id_usuario)  REFERENCES usuarios(id_usuario)
);

CREATE TABLE IF NOT EXISTS clientes (               -- HU-14, HU-15
  id_cliente BIGINT AUTO_INCREMENT PRIMARY KEY,
  documento  VARCHAR(20)  NOT NULL UNIQUE,
  nombre     VARCHAR(120) NOT NULL,
  telefono   VARCHAR(20),
  correo     VARCHAR(100),
  estado     TINYINT(1) NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS ventas (                 -- HU-11, HU-12, HU-13, HU-17
  id_venta   BIGINT AUTO_INCREMENT PRIMARY KEY,
  id_cliente BIGINT NULL,
  id_usuario BIGINT NOT NULL,
  fecha      DATETIME DEFAULT CURRENT_TIMESTAMP,
  total      DECIMAL(12,2) NOT NULL,
  FOREIGN KEY (id_cliente) REFERENCES clientes(id_cliente),
  FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
);

CREATE TABLE IF NOT EXISTS detalle_ventas (
  id_detalle      BIGINT AUTO_INCREMENT PRIMARY KEY,
  id_venta        BIGINT NOT NULL,
  id_producto     BIGINT NOT NULL,
  cantidad        INT NOT NULL CHECK (cantidad > 0),
  precio_unitario DECIMAL(10,2) NOT NULL,
  subtotal        DECIMAL(12,2) NOT NULL,
  FOREIGN KEY (id_venta)    REFERENCES ventas(id_venta),
  FOREIGN KEY (id_producto) REFERENCES productos(id_producto)
);

-- No se incluyen usuarios ni hashes de acceso. Consulta README.md para crear un usuario local.

-- Datos de ejemplo para la demo (puedes borrarlos):
INSERT INTO productos (sku,nombre,descripcion,categoria,numero_guia_remision,numero_lote,precio,stock,stock_minimo,fecha_creacion) VALUES
('PAR-500','Paracetamol 500 mg x100','Analgésico y antipirético','Analgésicos','T001-00001201','L2401',18.50,120,20,CURDATE()),
('IBU-400','Ibuprofeno 400 mg x50','Antiinflamatorio','Analgésicos','T001-00001202','L2402',22.00,8,15,CURDATE()),
('AMO-500','Amoxicilina 500 mg x20','Antibiótico (con receta)','Antibióticos','T001-00001203','L2403',35.90,60,10,CURDATE()),
('LOR-010','Loratadina 10 mg x10','Antialérgico','Antialérgicos','T001-00001204','L2404',9.80,0,10,CURDATE()),
('VIT-C1G','Vitamina C 1 g x30','Suplemento vitamínico','Vitaminas','T001-00001205','L2405',27.00,45,10,CURDATE());
