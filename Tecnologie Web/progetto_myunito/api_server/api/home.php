<?php
    session_start();
    //stabilisco i permessi di lettura del file (anyone)
    header("Access-Control-Allow-Origin: *");
    // definisco il formato della risposta (json)
    header("Content-Type: application/json; charset=UTF-8");
    // definisco il metodo consentito per la request
    header("Access-Control-Allow-Methods: GET");
    // includo le classi per la gestione dei dati
    include_once '../dataMgr/Database.php';
    include_once '../dataMgr/IdentificationNumber.php';
    include_once '../dataMgr/Course.php';
    include_once '../dataMgr/Booklet.php';
    include_once '../dataMgr/Round.php';

    // creo una connessione al DBMS
    $database = new Database();
    $db = $database->getConnection();
    
    if(isset($_GET['username_studente'])){ // Solo se ricevo una variabile superglobale $_GET['username_studente] ...
        // creo un'istanza della classe IdentificationNumber (MATRICOLA)
        $matricola = new IdentificationNumber($db);
        $matricola->username_studente = htmlspecialchars($_GET['username_studente']);
        // $matricola->getMatricolaFromUsername();
        if($matricola->getMatricolaFromUsername()){ // Se all'username corrisponde una matricola...
            $corso = new Course($db); // creo un'istanza della classe Course (CORSO DI LAUREA)
            $corso->codice_corso = $matricola->cod_corso;
            if($row = $corso->getCorso()){ // Se a quel codice corso cirrisponde un corso di laurea...
                $nome_corso = $row['nome_corso'];
                $tipologia_corso = $row['tipologia'];
                $libretto = new Booklet($db, $matricola->matricola); // creo un'istanza della classe Booklet (LIBRETTO), passando come parametro ulteriore la matricola
                if($libretto->getBookletExt()){ // Se a quella matricola corrisponde un libretto..
                    $arrayMaterie = $libretto->materie_studente; // popolo un array con le materie di cui al libretto dello studente
                    if($_SESSION["http_referer"] == "libretto"){ // Se la richiesta proviene da libretto.php..
                        // creo un oggetto a partire da un array multidimensionale contenente tutte le informazioni su matricola e libretto necessarie per popolare il front-end
                        $num_materie_scelte = 0;
                        $appello = new Round($db);
                        foreach($arrayMaterie as $key => $materia){
                            if($materia['materia']['scelta_studente'] == TRUE){
                                $num_materie_scelte++;
                            };
                            $appello->cod_materia = $materia['materia']['cod_materia'];
                            $prenotazione_materia = false;
                            if($appelli = $appello->getRoundsByTeaching()){
                                while($row = $appelli->fetch(PDO::FETCH_ASSOC)){
                                    $appello->id_appello = $row['id_appello'];
                                    if($appello->getPrenotazione($matricola->matricola)){
                                        $prenotazione_materia = true;
                                    };
                                };
                                
                            };
                            $arrayMaterie[$key]['materia']['prenotazione'] = $prenotazione_materia;
                        };
                        $infoMatricola = (object) array_merge((array) $matricola, array("tipologia_corso" => $tipologia_corso), array("num_materie_scelte" => $num_materie_scelte), array("materie" => $arrayMaterie));
                    };
                    if($_SESSION["http_referer"] == "homepage"){ // Se la richiesta proviene da homepageStudente.php
                        $crediti_tot = 0; // inizializzo due variabili per i crediti totali e i crediti conseguiti
                        $crediti_ok = 0;
                        foreach($arrayMaterie as $key => $materia){ // ciclo per ogni materia del libretto, al fine di determinare i crediti
                            $crediti_tot += $materia['materia']['crediti_materia'];
                            if($materia['materia']['voto_materia'] != null){
                                $crediti_ok += $materia['materia']['crediti_materia'];
                            };
                        };
                        // creo un oggetto a partire da un array multidimensionale contenente tutte le informazioni corrispondenti alla matricola (necessarie per popolare il front-end)
                        $infoMatricola = (object) array_merge((array) $matricola, array("nome_corso" => $nome_corso), array("tipologia_corso" => $tipologia_corso), array("crediti_totali" => $crediti_tot), array("crediti_conseguiti" => $crediti_ok));
                    }
                    echo json_encode($infoMatricola);
                    http_response_code(200); // 200 : OK - Richiesta HTTP andata a buon fine
                }else{ // se a quella matricola non corrisponde un libretto, inserisco nell'array multidimensionale un array con chiave 'error' e valore contenente un messaggio
                    $infoMatricola = (object) array_merge((array) $matricola, array("nome_corso" => $nome_corso), array("tipologia_corso" => $tipologia_corso), array("error" => "Impossibile identificare il libretto. Contattare l'assistenza"));
                    echo json_encode($infoMatricola);
                    http_response_code(200);
                };
            }else{ // se al codice corso della matricola non corrisponde un codice corso nella tabella dedicata ai corsi di laurea, inserisco nell'array multidimensionale un array con chiave 'error' e valore contenente un messaggio
                $infoMatricola = (object) array_merge((array) $matricola, array("error" => "Impossibile identificare il corso di laurea. Contattare l'assistenza."));
                echo json_encode($infoMatricola);
                http_response_code(200);
            }
        }else{ // Se all'username non corrisponde una matricola...
            echo json_encode(array("message" => "Utente non immatricolato"));
            http_response_code(200); // 204 : No Content - Il server ha processato con successo la richiesta e non restituirà nessun contenuto
        }
    }else{ // Se non ricevo una variabile superglobale $_GET['username_studente]...
        echo json_encode(array("message" => "Utente non loggato"));
        http_response_code(403); // 403: Forbidden : il client non ha i diritti di accesso, pertanto il server si rifiuta di fornire un'opportuna risposta
    }
?>