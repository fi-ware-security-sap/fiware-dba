DROP TABLE IF EXISTS `results`;
CREATE TABLE `results` (
  `idresults` int(11) NOT NULL AUTO_INCREMENT,
  `GID` bigint(20) NOT NULL,
  `result` text NOT NULL,
  `computed` tinyint(1) NOT NULL,
  PRIMARY KEY (`idresults`)
);

CREATE TABLE `deepsearchsave` ( 
 `GID` bigint(20) NOT NULL,
 `columncounter` int NOT NULL,
 `columnrisk` float NOT NULL,
 PRIMARY KEY (`GID`,`columncounter`)
);

CREATE TABLE `bootstrapsave` ( 
 `GID` bigint(20) NOT NULL,
 `columncounter` int NOT NULL,
 `columnrisk` float NOT NULL,
 PRIMARY KEY (`GID`,`columncounter`)
);

CREATE TABLE `riskcolumnsave` ( 
 `GID` bigint(20) NOT NULL,
 `columncounter` int NOT NULL,
 `columnrisk` float NOT NULL,
  PRIMARY KEY (`GID`,`columncounter`)
);

CREATE TABLE `policies` ( 
 `GID` bigint(20) NOT NULL,
 `columncounter` int NOT NULL,
 `columnname` text NOT NULL,
 `columntype` text NOT NULL,
 `hidden` boolean NOT NULL,	
 PRIMARY KEY (`GID`,`columncounter`)
);

