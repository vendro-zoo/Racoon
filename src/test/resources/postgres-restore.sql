DROP TABLE IF EXISTS cat CASCADE;
DROP TABLE IF EXISTS dog CASCADE;
DROP TABLE IF EXISTS owner CASCADE;
DROP TYPE IF EXISTS dog_size;
DROP TYPE IF EXISTS dog_color;

CREATE TABLE owner
(
    id      SERIAL PRIMARY KEY,
    name    VARCHAR(255),
    surname VARCHAR(255)
);

CREATE TYPE dog_size AS ENUM ('SMALL', 'MEDIUM', 'LARGE');
CREATE TYPE dog_color AS ENUM ('LIGHT', 'DARK');

CREATE TABLE dog
(
    id    SERIAL PRIMARY KEY,
    name  VARCHAR(255),
    size  dog_size NOT NULL,
    color dog_color
);

CREATE TABLE cat
(
    id       SERIAL PRIMARY KEY,
    name     VARCHAR(255),
    age      INTEGER,
    owner_id INTEGER,
    FOREIGN KEY (owner_id) REFERENCES owner (id)
);

INSERT INTO owner (name, surname)
VALUES ('Albert', 'White'),
       ('John', 'Doe'),
       ('Jane', 'Doe');

INSERT INTO cat (name, age, owner_id)
VALUES ('Garfield', 10, 1),
       ('Fido', 4, 1),
       ('Tom', 10, 2),
       ('Garfield', 4, 3),
       ('Jim', 5, NULL);