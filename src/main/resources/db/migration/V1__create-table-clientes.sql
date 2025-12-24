CREATE TABLE clientes (
    id BIGINT AUTO_INCREMENT,
    cliente_id INT NOT NULL,
    genero VARCHAR(20) NOT NULL,
    tiempo_meses INT NOT NULL,
    retrasos_pago INT NOT NULL,
    uso_mensual_hrs DOUBLE NOT NULL,
    plan VARCHAR(20) NOT NULL,
    soporte_tickets INT NOT NULL,
    cambio_plan VARCHAR(2) NOT NULL,
    pago_automatico VARCHAR(2) NOT NULL,
    edad INT NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT uk_cliente_cliente_id UNIQUE (cliente_id)
);
