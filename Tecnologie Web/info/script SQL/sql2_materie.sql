create table materie
(
	codice_materia char(7) not null primary key,
	nome_materia varchar(60) not null,
	crediti tinyint not null,
	cod_corso char(7),
	opzionale tinyint(1) NOT NULL DEFAULT 0,
	foreign key (cod_corso) references corsi_laurea(codice_corso)
);

insert into materie 
values
('CPS0175','Diritto delle ICT e dei media','6','030421'),
('CPS0548','Ecnomia di Internet','6','030421'),
('CPS0666','Metodi digitali per la ricerca sociale','6','030421', 1),
('CPS0089','Innovazione sociale','9','030421'),
('CPS0560','Interazione uomo-macchina: approcci avanzati','9','030421'),
('CPS0090','Sociologia della conoscenza e delle reti','9','030421'),
('CPS0567','Tecnologie web: approcci avanzati','9','030421'),
('CPS0557','Design of interactive systems','6','030421'),
('CPS0098','Economia dei mercati globali','6','030421'),
('CPS0392','Economia dell''innovazione','6','030421'),
('CPS0549','Green Economy e tecnologie digitali','6','030421');

insert into materie
values
('CPS0690','Analisi dei media','9','009506'),
('SCP0338','Comunicazione pubblica','9','009506'),
('SCP0256','Opinione pubblica e comunicazione politica','9','009506'),
('SCP0398','Sistemi mediali e ICT','9','009506'),
('SPS0689','Sondaggi, media e opinione pubblica','6','009506', 1),
('CPS0578','Sociologia del turismo','6','009506'),
('SCP0344','Storia del giornalismo e della comunicazione','6','009506'),
('CPS0689','Comunicare l''Europa','9','009506'),
('CPS0526','Imprenditori, reti sociali e innovazione','9','009506'),
('CPS0527','Territorio, economia e società','9','009506'),
('CPS0296','Marketing politico e comunicazione elettorale','9','009506');

insert into materie 
values
('CPS0256','Programmazione e gestione dei servizi sociali','9','009505'),
('CPS0101','Servizio sociale e innovazione professionale','9','009505'),
('CPS0048','Disuguaglianze sociali, vulnerabilità e politiche pubbliche','6','009505'),
('CPS0671','Valutazioni delle politiche','6','009505'),
('CPS0254','Fonti, mtodi per lo studio delle politiche sociali','6','009505', 1),
('SCP0010','Diritto dei lavori e delle occupazioni','6','009505'),
('GIU0707','Diritto dell''immigrazione','6','009505'),
('CPS0157','Antropologia dell''infanzia','6','009505'),
('SCP0005','Storia della marginalità e dell''assistenza','6','009505'),
('SCP0012','Cittadinanza, diritti sociali, giustizia','9','009505'),
('CPS0545','Culture dell''infanzia e diritti dei bambini','9','009505');