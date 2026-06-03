CREATE TABLE users (id INT, name STRING, age INT, active BOOLEAN);
INSERT INTO users VALUES (1, "Ido", 17, true);
INSERT INTO users VALUES (2, "Dana", 18, false);
SELECT * FROM users;
SELECT name, age FROM users WHERE age > 17;
UPDATE users SET active = true WHERE id = 2;
DELETE FROM users WHERE id = 1;
