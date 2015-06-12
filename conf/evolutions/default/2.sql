# --- !Ups

CREATE TABLE orders (
    id INTEGER AUTO_INCREMENT PRIMARY KEY,
    ticket_block_id INTEGER,
    customer_name VARCHAR,
    customer_email VARCHAR,
    ticket_quantity INTEGER,
    timestamp DATETIME,
    FOREIGN KEY (ticket_block_id) REFERENCES ticket_blocks(id)
);

# --- !Downs

DROP TABLE IF EXISTS orders;