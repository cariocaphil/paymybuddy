CREATE TABLE user_table (
  userid BIGINT PRIMARY KEY,
  email VARCHAR(255) NOT NULL,
  socialmediaacc VARCHAR(50) NOT NULL,
  balance DOUBLE PRECISION NOT NULL,
  password VARCHAR(255) NOT NULL
);

CREATE INDEX idx_email ON user_table (email);
