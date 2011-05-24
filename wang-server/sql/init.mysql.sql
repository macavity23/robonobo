CREATE TABLE user_account (
	id INTEGER PRIMARY KEY AUTO_INCREMENT UNIQUE,
	friendly_name VARCHAR(256),
	email VARCHAR(256),
	password VARCHAR(256),
	balance DOUBLE
);
CREATE INDEX ua_email_idx ON user_account(email);

CREATE TABLE denomination (
	id INTEGER PRIMARY KEY,
	denom INTEGER,
	generator TEXT,
	prime TEXT,
	public_key TEXT,
	private_key TEXT
);

CREATE TABLE doublespend (
	coin_hash BIGINT PRIMARY KEY
);
