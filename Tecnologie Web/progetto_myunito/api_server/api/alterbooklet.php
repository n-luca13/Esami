<?php
    //stabilisco i permessi di lettura del file (anyone)
    header("Access-Control-Allow-Origin: *");
    // definisco il formato della risposta (json)
    header("Content-Type: application/json; charset=UTF-8");
    // definisco il metodo consentito per la request
    header("Access-Control-Allow-Methods: GET, POST");
    // includo le classi per la gestione dei dati
    include_once '../dataMgr/Database.php';
    include_once '../dataMgr/Course.php';
    include_once '../dataMgr/Teaching.php';
    include_once '../dataMgr/Booklet.php';
    include_once '../dataMgr/IdentificationNumber.php';
    // creo una connessione al DBMS
    $database = new Database();
    $db = $database->getConnection();

    if(isset($_GET['tipologia_corso'])){ // Se ricevo in get la tipologia del corso cui è iscritto lo studente --> POPOLARE SELECT CORSI DI LAUREA
        $tipologia = htmlspecialchars($_GET['tipologia_corso']);
        // creo un'istanza della classe 'COURSE' (CORSO DI LAUREA)
        $course = new Course($db);
        // getAllCourses() restituisce un recordset contenete tutti i campi della tabella 'corsi_laurea'
        $stmt = $course->getAllCourses();
        if($stmt->rowCount()>0){ // se il recordset non è vuoto...
            // creo un array contenente un array con chiave "corsi_laurea"
            $array_corsi["corsi_laurea"] = array();
            while($row = $stmt->fetch(PDO::FETCH_ASSOC)){ // per ogni record...
                if($tipologia == "magistrale"){ // se la tipologia del corso di laurea dello studente è magistrale ...
                    if($row['tipologia'] == "magistrale"){ // fornire solo corsi magistrali
                        $array_corso = array( // creo un array del singolo corso 
                            'codice_corso' => $row['codice_corso'],
                            'nome_corso' => $row['nome_corso'],
                        );
                        // inserisco l'array all'interno di array_corsi
                        array_push($array_corsi["corsi_laurea"], $array_corso);
                    };
                }else{
                    $array_corso = array(
                        'codice_corso' => $row['codice_corso'],
                        'nome_corso' => $row['nome_corso'],
                    );
                    array_push($array_corsi["corsi_laurea"], $array_corso);
                };
            };
            http_response_code(200); // 200: richiesta HTTP andata a buon fine
            echo json_encode($array_corsi); // invio un JSON contenente l'array
        }else{
            http_response_code(503); // 503: service unavailable
            echo json_encode(array("message" => "Service unavailable"));
        }
    }elseif(file_get_contents("php://input")){ // Se ricevo dei dati nel body della request ...
        $userData = json_decode(file_get_contents("php://input")); // inserisco in una variabile il json ricevuto dal form
        if(!empty($userData->corso_selezionato) || !empty($userData->ricerca_materia)){ // Se almeno uno dei campi non è vuoto..
            if(!empty($userData->matricola_studente)){ // Verifico di aver ricevuto la matricola
                // N.B. Avrei dovuto omologare e - dunque - accorpare i controlli sulla matricola
                $matricola = htmlspecialchars(strip_tags($userData->matricola_studente));
            }else{
                http_response_code(401); // 401: Unauthorized : autenticazione fallita
                header("location: ../../loginPage.php");
            };
            $materia = new Teaching($db); // nuova istanza della classe per MATERIA
            $materia->cod_corso = $userData->corso_selezionato; // modifico il valore della variabile d'istanza 'cod_corso'
            $keywords = strtolower(htmlspecialchars(strip_tags($userData->ricerca_materia)));
            // Invoco sull'istanza 'materia' della classe Teaching la funzione SEARCHTEACHINGS(),
            // passando come parametro una stringa contenente le parole digitate dall'utente
            // La funzione restituisce tutte le materie che corrispondono alla RICERCA DELL'UTENTE
            if($stmt = $materia->searchTeachings($keywords, $matricola)){ // Se ricevo un recordset di insegnamenti
                $array_materie["materie"] = array(); // Creo un array per le materie ricevute
                $libretto = new Booklet($db, $matricola); // Nuova istanza di 'BOOKLET' (LIBRETTO)
                // La funzione GETBOOKLET restituisce solamente i campi della tabella libretti corrispondenti alla matricola X
                // in breve: MATERIE DEL LIBRETTO STUDENTE
                if($stmtLibretto = $libretto->getBooklet()){ // Se ricevo un recordset (= il libretto esiste)
                    $array_libretto["materie-libretto"] = array(); // inizializzo un array per le materie del libretto
                    while($row = $stmtLibretto->fetch(PDO::FETCH_ASSOC)){ // popolo l'array
                        array_push($array_libretto["materie-libretto"], $row['cod_materia']);
                    };
                    while($row = $stmt->fetch(PDO::FETCH_ASSOC)){ // per ogni MATERIA fornita dalla RICERCA DELL'UTENTE (!)
                        $exitFlag = false;
                        foreach($array_libretto["materie-libretto"] as $value){ // ciclo sull'array libretto
                            if($value == $row['codice_materia']){ // fintanto che non trovo quella materia
                                // Se trovo quella materia nel libretto dello studente
                                $exitFlag = true; // TRUE
                                break; // interrompo il ciclo foreach
                            };
                        };
                        // SE LA MATERIA FORNITA DALLA RICERCA NON è PRESENTE NEL LIBRETTO DELLO STUDENTE..
                        if($exitFlag != true){
                            $corso = new Course($db);
                            $corso->codice_corso = $row['cod_corso']; 
                            if($row_corso = $corso->getCorso()){ // <-- per ottenere il nome del corso di laurea cui appartiene la materia con codice X
                                $nome_corso = $row_corso['nome_corso'];
                            };
                            // Creo un array per ciascuna materia della ricerca
                            $array_materia = array(
                                'codice_materia' => $row['codice_materia'],
                                'nome_materia' => $row['nome_materia'],
                                'crediti_materia' => $row['crediti'],
                                'cod_corso' => $row['cod_corso'],
                                'nome_corso' => $nome_corso,
                            );
                            // e lo inserisco nell'array delle materie
                            array_push($array_materie["materie"], $array_materia);
                        };
                    };
                    http_response_code(200); // OK: richiesta HTTP andata a buon fine
                    echo json_encode($array_materie); // Restituisco un JSON contentente l'array delle materie (può essere vuoto!)
                }else{ // se a quella matricola non corrisponde un libretto
                    echo json_encode(array("error" => "Libretto non trovato"));
                    http_response_code(204); // 204 : No Content - Il server ha processato con successo la richiesta e non restituirà nessun contenuto
                };
            }else{
                http_response_code(503); // 503: service unavailable
                echo json_encode(array("message" => "Servizio non disponibile"));
            };
        }elseif(!empty($userData->cod_materia) && !empty($userData->username)){ // Se ricevo tali dati nel body della request --> MODIFICARE LIBRETTO
            $matricola = new IdentificationNumber($db);
            $matricola->username_studente = htmlspecialchars(strip_tags($userData->username));
            if($matricola->getMatricolaFromUsername()){ // A partire dall'USERNAME ricavo la MATRICOLA
                $libretto = new Booklet($db, $matricola->matricola);
                if($libretto->getBookletExt()){ // A partire dalla matricola ricavo il LIBRETTO
                    $cod_materia = htmlspecialchars(strip_tags($userData->cod_materia)); // sanifico il contenuto delle variabili ricevute dalla chiamata ajax
                    $action = htmlspecialchars(strip_tags($userData->action));
                    if($action == "add-link"){
                        if($libretto->addToBooklet($cod_materia)){ // AGGIUNGERE MATERIA A SCELTA AL LIBRETTO
                            echo json_encode(array("message" => "Materia inserita nel libretto"));
                            http_response_code(200);
                        }else{
                            echo json_encode(array("error" => "Impossibile inserire la materia: limite massimo raggiunto"));
                            http_response_code(200);
                        };
                    }elseif($action == "remove-link"){
                        if($libretto->removeFromBooklet($cod_materia)){ // RIMUOVERE MATERIA A SCELTA
                            echo json_encode(array("message" => "Materia rimossa dal libretto"));
                            http_response_code(200);
                        }else{
                            echo json_encode(array("error" => "Impossibile rimuovere la materia"));
                            http_response_code(200);
                        };
                    }else{
                        http_response_code(400); // 400: bad request (sintassi invalida)
                        echo json_encode(array("error" => "Errore: dati non pervenuti"));
                    }
                }else{ // se a quella matricola non corrisponde un libretto
                    echo json_encode(array("error" => "Libretto non trovato"));
                    http_response_code(204); // 204 : No Content - Il server ha processato con successo la richiesta e non restituirà nessun contenuto
                };
            }else{ // Se all'username non corrisponde una matricola...
                echo json_encode(array("error" => "Utente non immatricolato"));
                http_response_code(204); 
            };
        }else{
            http_response_code(400); // 400: bad request (sintassi invalida)
            echo json_encode(array("error" => "Errore: dati non pervenuti"));
        };
    }else{
        http_response_code(405); //  405: method not allowed (il metodo richiesto non può essere usato)
        // in alternativa, si potrebbe usare (e preferire) 403 Forbidden:
        // il client non ha i diritti di accesso, non è cioè autorizzato, pertanto il server si rifiuta di fornire un'opportuna risposta
        header("location: ../../loginPage.php");
    };

?>