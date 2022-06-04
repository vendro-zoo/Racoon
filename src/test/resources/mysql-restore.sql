DROP TABLE IF EXISTS cat;
DROP TABLE IF EXISTS dog;
DROP TABLE IF EXISTS owner;

CREATE TABLE owner
(
    id      INT AUTO_INCREMENT PRIMARY KEY,
    name    VARCHAR(255) NULL,
    surname VARCHAR(255) NULL
);

CREATE TABLE dog
(
    id    INT AUTO_INCREMENT PRIMARY KEY,
    name  VARCHAR(255)                      NULL,
    size  ENUM ('SMALL', 'MEDIUM', 'LARGE') NOT NULL,
    color ENUM ('LIGHT', 'DARK')            NULL
);

CREATE TABLE cat
(
    id       INT AUTO_INCREMENT PRIMARY KEY,
    name     VARCHAR(255) NULL,
    age      INT          NULL,
    owner_id INT          NULL,
    CONSTRAINT cat_owner_id_fk
        FOREIGN KEY (owner_id) REFERENCES owner (id)
);

INSERT INTO owner (id, name, surname)
VALUES (1, 'Albert', 'White'),
       (2, 'John', 'Doe'),
       (3, 'Jane', 'Doe');

INSERT INTO cat (id, name, age, owner_id)
VALUES (1, 'Garfield', 10, 1),
       (2, 'Fido', 4, 1),
       (3, 'Tom', 10, 2),
       (4, 'Garfield', 4, 3),
       (5, 'Jim', 5, NULL);