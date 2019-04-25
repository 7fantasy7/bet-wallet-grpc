DROP TABLE IF EXISTS wallet;

CREATE TABLE wallet(
    id serial,
    user_id bigint,
    currency character varying(5),
    amount numeric(12,2),
    CONSTRAINT user_id_currency_unq UNIQUE (user_id, currency) /* todo index? */
);
