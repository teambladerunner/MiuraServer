CREATE SCHEMA MIURA;
COMMIT;

DROP TABLE MIURA.NASDAQUPDATE;
CREATE TABLE MIURA.NASDAQUPDATE
(
   TIMEPK BIGINT NOT NULL,
   SYMBOL VARCHAR(50) NOT NULL,
   LASTSALE DECIMAL(24,3) NOT NULL,
   MARKETCAP DECIMAL(24,3),
   SECTOR VARCHAR(250),
   INDUSTRY VARCHAR(250)
);

ALTER TABLE MIURA.NASDAQUPDATE
   ADD CONSTRAINT PK_NASDAQUPDATE PRIMARY KEY (TIMEPK);

COMMIT;

delete from MIURA.NASDAQUPDATE;
commit;

drop table MIURA.USERPORTFOLIO;
CREATE TABLE MIURA.USERPORTFOLIO
(
   TIMEPK BIGINT NOT NULL,
   QUOTEID VARCHAR(50) NOT NULL,
   SYMBOL VARCHAR(50) NOT NULL,
   UNITS DECIMAL(24,3) NOT NULL,
	 PRICE DECIMAL(24,3) NOT NULL,
   BUYSELL VARCHAR(1) NOT NULL,
   USERID VARCHAR(250) NOT NULL,

);

ALTER TABLE MIURA.USERPORTFOLIO
   ADD CONSTRAINT PK_USERPORTFOLIO PRIMARY KEY (TIMEPK);

DROP TABLE MIURA.NASDAQUPLOADLOG;
CREATE TABLE MIURA.MIURAENV
(
   PROPKEY VARCHAR(250) NOT NULL,
   PROPVALUE VARCHAR(250) NOT NULL
);

ALTER TABLE MIURA.MIURAENV
   ADD CONSTRAINT PK_MIURAENV PRIMARY KEY (PROPKEY);


DROP TABLE MIURA.GAMEUSER;
CREATE TABLE MIURA.GAMEUSER
(
   USERIDPK VARCHAR(50) NOT NULL,
   USERPASSWORD VARCHAR(256) NOT NULL,
	 FIRSTNAME VARCHAR(100) NOT NULL,
	 LASTNAME VARCHAR(100) NOT NULL,
	 EMAIL VARCHAR(100) NOT NULL,
	 TWITTERHANDLE VARCHAR(100),
	 FACEBOOKHANDLE VARCHAR(100),
	 GOOGLEHANDLE VARCHAR(100),
	 LINKEDINHANDLE VARCHAR(100),
	 LOCALEID VARCHAR(10),
	 AVATARID VARCHAR(10),
	 CASH DECIMAL(24,3) NOT NULL,
	 LEVEL INTEGER NOT NULL,
	 JOINDATE TIMESTAMP
);

ALTER TABLE MIURA.GAMEUSER
   ADD CONSTRAINT PK_GAMEUSER PRIMARY KEY (USERIDPK);

INSERT INTO MIURA.GAMEUSER
(
  USERIDPK,
  USERPASSWORD,
  FIRSTNAME,
  LASTNAME,
  EMAIL,
  TWITTERHANDLE,
  FACEBOOKHANDLE,
  GOOGLEHANDLE,
  LINKEDINHANDLE,
  LOCALEID,
  AVATARID,
  CASH,
  LEVEL
)
VALUES
(
  'x',
  'x',
  'zubin',
  'kavarana',
  'zubin',
  NULL,
  NULL,
  NULL,
  NULL,
  'en_us',
  NULL,
  25000.000,
  1
);


COMMIT;