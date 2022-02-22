create table libretti
(
	matricola_studente int(6) zerofill not null references matricole(matricola),
	cod_materia char(7) not null references materie(codice_materia),
	voto_materia char(2),
	scelta_studente boolean NOT NULL DEFAULT 0,
	primary key (matricola_studente, cod_materia)
);

create table appelli 
(
	id_appello int(255) not null auto_increment primary key,
	data_appello date not null,
	cod_materia char(7) not null,
	foreign key (cod_materia) references materie(codice_materia)
);

insert into appelli (data_appello, cod_materia)
values
('2021-01-01','CPS0089'),
('2021-01-15','CPS0089'),
('2021-02-03','CPS0089'),
('2021-01-02','CPS0567'),
('2021-01-16','CPS0567'),
('2021-02-04','CPS0567'),
('2021-01-03','CPS0090'),
('2021-01-17','CPS0090'),
('2021-02-05','CPS0090');

create table prenotazioni
(
	matricola_studente int(6) zerofill not null references matricole(matricola),
	appello_id int(255) not null references appelli(id_appello),
	primary key (matricola_studente, appello_id)
);

insert into prenotazioni values ('000001','2');