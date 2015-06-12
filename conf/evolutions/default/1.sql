# --- !Ups

CREATE TABLE events (
    id INTEGER AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR,
    start DATETIME,
    end DATETIME,
    address VARCHAR,
    city VARCHAR,
    state VARCHAR,
    country CHAR(2)
);

CREATE TABLE ticket_blocks (
    id INTEGER AUTO_INCREMENT PRIMARY KEY,
    event_id INTEGER,
    name VARCHAR,
    product_code VARCHAR(40),
    price DECIMAL,
    initial_size INTEGER,
    sale_start DATETIME,
    sale_end DATETIME,
    FOREIGN KEY (event_id) REFERENCES events(id)
);

# --- !Downs

DROP TABLE IF EXISTS ticket_blocks;
DROP TABLE IF EXISTS events;
