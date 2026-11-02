USE bd_bonapharm;

ALTER TABLE productos
  ADD COLUMN numero_guia_remision VARCHAR(30) NOT NULL DEFAULT 'S/N' AFTER categoria,
  ADD COLUMN numero_lote          VARCHAR(40) NOT NULL DEFAULT 'S/N' AFTER numero_guia_remision;
