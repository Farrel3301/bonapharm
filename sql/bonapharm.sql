-- Bonapharm: script COMPLETO (BORRA y recrea la base bd_bonapharm)
DROP DATABASE IF EXISTS bd_bonapharm;
CREATE DATABASE bd_bonapharm CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE bd_bonapharm;

CREATE TABLE usuarios (
  id_usuario BIGINT AUTO_INCREMENT PRIMARY KEY,
  usuario VARCHAR(50) NOT NULL UNIQUE,
  nombre VARCHAR(100) NOT NULL,
  contrasena_hash VARCHAR(255) NOT NULL,
  rol VARCHAR(30) NOT NULL,                       -- ADMIN o VENDEDOR
  estado TINYINT(1) NOT NULL DEFAULT 1,
  fecha_registro DATETIME DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE catalogo_medicamentos (
  id_medicamento BIGINT AUTO_INCREMENT PRIMARY KEY,
  codigo_producto VARCHAR(30) NOT NULL UNIQUE,
  nombre VARCHAR(120) NOT NULL
);
CREATE TABLE catalogo_categorias (
  id_categoria BIGINT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(60) NOT NULL UNIQUE
);
CREATE TABLE catalogo_lotes (
  id_catalogo_lote BIGINT AUTO_INCREMENT PRIMARY KEY,
  codigo_lote VARCHAR(40) NOT NULL UNIQUE
);
CREATE TABLE catalogo_motivos_salida (
  id_motivo BIGINT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(60) NOT NULL UNIQUE
);
CREATE TABLE productos (
  id_producto BIGINT AUTO_INCREMENT PRIMARY KEY,
  codigo_producto VARCHAR(30) NOT NULL UNIQUE,
  nombre VARCHAR(120) NOT NULL,
  descripcion TEXT,
  categoria VARCHAR(60) NOT NULL,
  precio DECIMAL(10,2) NOT NULL CHECK (precio >= 0),
  stock INT NOT NULL DEFAULT 0 CHECK (stock >= 0),
  stock_minimo INT NOT NULL DEFAULT 5,
  estado TINYINT(1) NOT NULL DEFAULT 1,
  fecha_creacion DATE NOT NULL
);
CREATE TABLE lotes (
  id_lote BIGINT AUTO_INCREMENT PRIMARY KEY,
  id_producto BIGINT NOT NULL,
  codigo_lote VARCHAR(40) NOT NULL,
  fecha_vencimiento DATE NOT NULL,
  cantidad INT NOT NULL DEFAULT 0 CHECK (cantidad >= 0),
  UNIQUE (id_producto, codigo_lote),
  FOREIGN KEY (id_producto) REFERENCES productos(id_producto)
);
CREATE TABLE movimientos_inventario (
  id_movimiento BIGINT AUTO_INCREMENT PRIMARY KEY,
  id_producto BIGINT NOT NULL,
  id_lote BIGINT NULL,
  tipo VARCHAR(10) NOT NULL,                      -- ENTRADA, SALIDA, VENTA, ANULACION
  cantidad INT NOT NULL CHECK (cantidad > 0),
  guia_remision VARCHAR(30),
  motivo VARCHAR(60),
  usuario VARCHAR(50) NOT NULL,
  fecha DATETIME NOT NULL,
  FOREIGN KEY (id_producto) REFERENCES productos(id_producto),
  FOREIGN KEY (id_lote) REFERENCES lotes(id_lote)
);
CREATE TABLE clientes (
  id_cliente BIGINT AUTO_INCREMENT PRIMARY KEY,
  codigo_cliente VARCHAR(30) NOT NULL UNIQUE,
  ruc VARCHAR(11) NOT NULL UNIQUE,
  razon_social VARCHAR(150) NOT NULL,
  direccion_oficina VARCHAR(250) NOT NULL,
  direccion_almacen VARCHAR(250) NOT NULL,
  estado TINYINT(1) NOT NULL DEFAULT 1
);
CREATE TABLE ventas (
  id_venta BIGINT AUTO_INCREMENT PRIMARY KEY,
  id_cliente BIGINT NOT NULL,
  destinatario VARCHAR(150) NOT NULL,             -- copia de los datos del cliente al vender
  ruc_destinatario VARCHAR(11) NOT NULL,
  direccion_destinatario VARCHAR(250) NOT NULL,
  punto_llegada VARCHAR(250) NOT NULL,
  usuario VARCHAR(50) NOT NULL,
  fecha DATETIME NOT NULL,
  fecha_emision DATE NOT NULL,
  fecha_traslado DATE NOT NULL,
  estado VARCHAR(12) NOT NULL DEFAULT 'ACTIVA',   -- ACTIVA o CANCELADA
  subtotal DECIMAL(12,2) NOT NULL,
  igv DECIMAL(12,2) NOT NULL,                     -- 18%
  total DECIMAL(12,2) NOT NULL,
  FOREIGN KEY (id_cliente) REFERENCES clientes(id_cliente)
);
CREATE TABLE detalle_ventas (
  id_detalle BIGINT AUTO_INCREMENT PRIMARY KEY,
  id_venta BIGINT NOT NULL,
  id_producto BIGINT NOT NULL,
  id_lote BIGINT NOT NULL,
  cantidad INT NOT NULL CHECK (cantidad > 0),
  precio_unitario DECIMAL(10,2) NOT NULL,
  subtotal DECIMAL(12,2) NOT NULL,
  FOREIGN KEY (id_venta) REFERENCES ventas(id_venta),
  FOREIGN KEY (id_producto) REFERENCES productos(id_producto),
  FOREIGN KEY (id_lote) REFERENCES lotes(id_lote)
);

-- Catálogos genéricos de demostración. Sin usuarios ni datos de clientes, ventas o movimientos.
INSERT INTO catalogo_medicamentos (codigo_producto,nombre) VALUES
('PAR-500','Paracetamol 500 mg x100'),('IBU-400','Ibuprofeno 400 mg x50'),('AMO-500','Amoxicilina 500 mg x20'),
('LOR-010','Loratadina 10 mg x10'),('VIT-C1G','Vitamina C 1 g x30'),('NAP-550','Naproxeno 550 mg x20'),
('OME-020','Omeprazol 20 mg x30'),('AZI-500','Azitromicina 500 mg x6'),('DIC-050','Diclofenaco 50 mg x20'),
('CET-010','Cetirizina 10 mg x10'),('ASP-100','Aspirina 100 mg x30');

INSERT INTO catalogo_categorias (nombre) VALUES
('Analgésicos'),('Antibióticos'),('Antialérgicos'),('Vitaminas'),('Antiinflamatorios'),('Gastrointestinales'),('Cuidado personal');

INSERT INTO catalogo_lotes (codigo_lote) VALUES
('L2401'),('L2402'),('L2403'),('L2404'),('L2405'),('L2406'),('L2407'),('L2408'),('L2409'),('L2410');

INSERT INTO catalogo_motivos_salida (nombre) VALUES
('Producto vencido'),('Dañado o merma'),('Devolución al proveedor'),('Ajuste de inventario'),('Muestra o donación');
