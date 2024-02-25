CREATE TABLE user_table (
  userid BIGINT PRIMARY KEY,
  email VARCHAR(255) NOT NULL,
  socialmediaacc VARCHAR(50) NOT NULL,
  balance DOUBLE PRECISION NOT NULL,
  password VARCHAR(255) NOT NULL
);

CREATE INDEX idx_email ON user_table (email);

CREATE TABLE transaction_table (
  transactionid BIGINT PRIMARY KEY,
  amount DOUBLE PRECISION NOT NULL,
  timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  description TEXT,
  fee DOUBLE PRECISION NOT NULL,
  sender_userid BIGINT NOT NULL,
  receiver_userid BIGINT NOT NULL,
  FOREIGN KEY (sender_userid) REFERENCES user_table(userid),
  FOREIGN KEY (receiver_userid) REFERENCES user_table(userid)
);

CREATE INDEX idx_sender_userid ON transaction_table (sender_userid);
CREATE INDEX idx_receiver_userid ON transaction_table (receiver_userid);
