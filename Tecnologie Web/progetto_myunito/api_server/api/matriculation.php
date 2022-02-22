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
    include_once '../dataMgr/IdentificationNumber.php';
    include_once '../dataMgr/Booklet.php';
    // creo una connessione al DBMS
    $database = new Database();
    $db = $database->getConnection();
    
    // Se ricevo una variabile superglobale di tipo GET con parametro 'tipologia' --> FORNIRE CORSI DI LAUREA
    if(isset($_GET['tipologia'])){
        // salvo in una variabile il valore di $_GET['tipologia]
        // htmlspecialchars converte caratteri speciali in entità HTML
        $tipologia = htmlspecialchars($_GET['tipologia']);
        // creo un'istanza della classe Course
        $course = new Course($db);
        // getAllCourses() restituisce un recordset contenete tutti i campi della tabella 'corsi_laurea'
        $stmt = $course->getAllCourses();
        if($stmt->rowCount()>0){ // se il recordset non è vuoto...
            // creo un array contenente un array con chiave "corsi_laurea"
            $array_corsi["corsi_laurea"] = array();
            while($row = $stmt->fetch(PDO::FETCH_ASSOC)){ // per ogni record...
                if($row['tipologia'] == $tipologia){ // se il campo tipologia è quello richiesto dall'utente...
                    $array_corso = array( // creo un array del singolo corso 
                        'codice_corso' => $row['codice_corso'],
                        'nome_corso' => $row['nome_corso'],
                    );
                    // e inserisco l'array all'interno di array_corsi
                    array_push($array_corsi["corsi_laurea"], $array_corso);
                };
            };
            http_response_code(200); // 200: richiesta HTTP andata a buon fine
            echo json_encode($array_corsi); // invio un JSON contenente l'array
        }else{
            http_response_code(503); // 503: service unavailable
            echo json_encode(array("message" => "Service unavailable"));
        }
    }elseif(file_get_contents("php://input")){ // Se ricevo dei dati nel body della request (in questo caso attraverso un form - POST) --> IMMATRICOLAZIONE
        $matricola = new IdentificationNumber($db); // nuova istanza della classe per Matricola
        $userData = json_decode(file_get_contents("php://input")); // inserisco in una variabile il json ricevuto dal form
        if(!empty($userData->username) && !empty($userData->codice_corso)){ // Se i dati ci sono...
            // popolo le proprietà username e codice corso della classe
            $matricola->username_studente = $userData->username;
            $matricola->cod_corso = $userData->codice_corso;
            if(!$matricola->getMatricolaFromUsername()){ // Controllo che l'utente NON sia gia immatricolato
                if($matricola->createMatricola()){ // Se l'operazione di creazione della matricola va a buon fine...
                    // popolo la proprietà matricola; sprintf serve per aggiungere gli zero laddove necessari (es. 11 -> 000011)
                    $matricola->matricola = sprintf("%06d",$db->lastInsertId());
                    $libretto = new Booklet($db, $matricola->matricola);
                    if($libretto->createBooklet($matricola->cod_corso)){
                        echo json_encode($matricola); // invio un json contenente l'oggetto matricola
                        http_response_code(201); // 201: created
                    }else{
                        http_response_code(503); // 503: service unavailable
                        echo json_encode(array("message" => "Service unavailable"));
                    };
                    
                }else{
                    http_response_code(503); // 503: service unavailable
                    echo json_encode(array("message" => "Service unavailable"));
                } 
            }else{
                http_response_code(200); // 200: OK
                echo json_encode(array("message" => "Utente già immatricolato"));
            }
        }else{ // Se per qualche ragione mancano dei dati...
            http_response_code(400); // 400: bad request (sintassi invalida)
            echo json_encode(array("message" => "Errore: dati non pervenuti. Riprova"));
        }
    }else{
        http_response_code(405); //  405: method not allowed (il metodo richiesto non può essere usato)
        // in alternativa, si potrebbe usare (e preferire) 403 Forbidden:
        // il client non ha i diritti di accesso, non è cioè autorizzato, pertanto il server si rifiuta di fornire un'opportuna risposta
        header("location: ../../loginPage.php");
    };
?>