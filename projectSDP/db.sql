drop table if exists delivery;
drop table if exists item;

CREATE TABLE IF NOT EXISTS item (
	iditem serial PRIMARY KEY,
	name varchar(20) NOT NULL,
	quantity INTEGER NOT NULL DEFAULT 0,
	description varchar(100) DEFAULT 'No description'
);

CREATE TABLE IF NOT EXISTS delivery (
	iddelivery serial PRIMARY KEY,
	iditemdelivery INTEGER NOT NULL,
	name varchar(20) NOT NULL,
	quantity INTEGER NOT NULL,
	location varchar(100) NOT NULL DEFAULT 'No location',
	FOREIGN KEY (iditemdelivery) REFERENCES item (iditem)
);
