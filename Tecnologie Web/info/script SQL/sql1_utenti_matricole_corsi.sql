create table utenti 
(
	username varchar(30) not null primary key,
	pass varchar(30) not null,
	nome varchar(20) not null,
	cognome varchar(20) not null,
	genere enum('m','f') not null,
	data_nascita date not null,
	nazione_nascita varchar(20) not null,
	nazione_residenza varchar(20) not null,
	indirizzo_residenza varchar(50) not null,
	cap_residenza int(5) not null,
	citta_residenza varchar(30) not null,
	email varchar(40) not null,
	telefono char(10)
);

insert into utenti 
values
('luca_13', 'luca_13', 'Luca', 'Nuzzo', 'm', '1995-12-13', 'Italia', 'Italia', 
'Via Sannicandro, 60', '70020', 'Cassano delle Murge', 'lucaluca@luca.it', '3343333233');

create table corsi_laurea
(
	codice_corso char(7) NOT NULL primary key,
	nome_corso varchar(60) NOT NULL,
	tipologia varchar(10) NOT NULL
);

insert into corsi_laurea 
values 
('030421','Comunicazione, ICT e Media', 'magistrale'),
('009506','Comunicazione Pubblica e Politica', 'magistrale'),
('009505', 'Politiche e Servizi Sociali', 'magistrale'),
('031031', 'Innovazione Sociale, Comunicazione, Nuove Tecnologie', 'triennale'),
('005706', 'Comunicazione Interculturale', 'triennale'),
('009710', 'Scienze Politiche e Sociali', 'triennale')
;

create table matricole
(
	matricola int(6) zerofill not null auto_increment primary key, 
	/* ho preferito determinare una lunghezza massima della matricola per questioni di spazio, 
	 * nonostante ciò renda necessario un futuro una modifica al database */
	username_studente varchar(30) NOT NULL,
	cod_corso char(7) NOT NULL,
	foreign key (username_studente) references utenti(username),
	foreign key (cod_corso) references corsi_laurea(codice_corso)
);

insert into matricole (username_studente, cod_corso)
values
('luca_13', '030421');