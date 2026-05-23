-- ============================================================
-- Modulo: Factura / Comprobante de Compra de Entrada
-- Sprint: Facturacion
-- Descripcion: Tabla para registrar facturas generadas al
--              confirmar el pago de una entrada al Mundial 2026
-- ============================================================

CREATE TABLE IF NOT EXISTS factura_entrada (
    id              BIGSERIAL       PRIMARY KEY,
    id_entrada      BIGINT          NOT NULL REFERENCES entrada(id) ON DELETE CASCADE,
    numero_factura  VARCHAR(50)     NOT NULL UNIQUE,
    fecha_emision   TIMESTAMP       NOT NULL DEFAULT NOW(),
    subtotal        NUMERIC(12, 2)  NOT NULL,
    impuestos       NUMERIC(12, 2)  NOT NULL,
    total           NUMERIC(12, 2)  NOT NULL,
    enviada_correo  SMALLINT        NOT NULL DEFAULT 0,
    fecha_envio     TIMESTAMP,
    ruta_pdf        VARCHAR(300)
);

-- Indice para consultas por entrada
CREATE INDEX IF NOT EXISTS idx_factura_entrada_id_entrada
    ON factura_entrada(id_entrada);

-- Comentarios de columnas
COMMENT ON TABLE  factura_entrada                    IS 'Comprobantes de compra generados al confirmar pago de una entrada';
COMMENT ON COLUMN factura_entrada.id_entrada         IS 'FK a la entrada pagada';
COMMENT ON COLUMN factura_entrada.numero_factura     IS 'Numero unico de factura: FACT-2026-YYYYMMDD-XXXXXXXX';
COMMENT ON COLUMN factura_entrada.subtotal           IS 'Precio base sin IVA';
COMMENT ON COLUMN factura_entrada.impuestos          IS 'IVA 19%';
COMMENT ON COLUMN factura_entrada.total              IS 'Total pagado (subtotal + impuestos)';
COMMENT ON COLUMN factura_entrada.enviada_correo     IS '0 = no enviada, 1 = enviada al correo del titular';
COMMENT ON COLUMN factura_entrada.fecha_envio        IS 'Timestamp del ultimo envio por correo';
COMMENT ON COLUMN factura_entrada.ruta_pdf           IS 'Ruta opcional si se persiste el PDF en disco';
