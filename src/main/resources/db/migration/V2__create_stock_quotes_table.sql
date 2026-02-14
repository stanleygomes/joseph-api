CREATE TABLE stock_quotes (
    id UUID PRIMARY KEY,
    stock_id UUID NOT NULL,
    quote_date DATE NOT NULL,
    open_price NUMERIC(19, 4),
    high_price NUMERIC(19, 4),
    low_price NUMERIC(19, 4),
    close_price NUMERIC(19, 4) NOT NULL,
    volume BIGINT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE,

    CONSTRAINT fk_stock FOREIGN KEY (stock_id) REFERENCES stocks(id),
    CONSTRAINT uk_stock_quote_date UNIQUE (stock_id, quote_date)
);
