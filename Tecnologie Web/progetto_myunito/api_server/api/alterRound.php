<?php
    //stabilisco i permessi di lettura del file (anyone)
    header("Access-Control-Allow-Origin: *");
    // definisco il formato della risposta (json)
    header("Content-Type: application/json; charset=UTF-8");
    // definisco il metodo consentito per la request
    header("Access-Control-Allow-Methods: POST");
    // includo le classi per la gestione dei dati
    include_once '../dataMgr/Database.php';
    include_once '../dataMgr/Round.php';
    include_once '../dataMgr/Booklet.php';
    include_once '../dataMgr/IdentificationNumber.php';
    // creo una connessione al DBMS
    $database = new Database();
    $db = $database->getConnection();

    if(file_get_contents("php://input")){ // Verifico di ricevere dei dati nel body della request
        $userData = json_decode(file_get_contents("php://input"));
        if(!empty($userData->username)){ // Se ricevo lo username...
            $appello = new Round($db); // Creo un'istanza di 'ROUND' (APPELLO)
            $array_appelli["appelli"] = array(); // Creo un array per tutti gli appelli delle eventuali materie
            $matricola = new IdentificationNumber($db); // Creo un'istanza di 'IDENTIFICATION NUMBER' (MATRICOLA)
            $username = htmlspecialchars(strip_tags($userData->username)); // sanifico
            $matricola->username_studente = $username; // modifico il valore della variabile d'istanza 'username_studente'
            if($matricola->getMatricolaFromUsername()){ // Se a quell'username corrisponde una MATRICOLA
                $libretto = new Booklet($db, $matricola->matricola); // Creo un'istanza di 'BOOKLET' (LIBRETTO)
                if($libretto->getBookletExt()){ // Se ricevo il recordset contenente il libretto dello studente...
                    $arrayLibretto = $libretto->materie_studente; // Creo un array contenente le materie di cui al libretto
                    if(!empty($userData->cod_materia)){ // SE ricevo un CODICE MATERIA --> FORNIRE APPELLI PER QUELLA SOLA MATERIA
                        $appello->cod_materia = htmlspecialchars(strip_tags($userData->cod_materia)); // modifico il valore della variabile d'istanza 'cod_materia'
                        foreach($arrayLibretto as $key => $materia){ // ciclo su tutte le materie del libretto per trovare quella richiesta
                            if($materia['materia']['cod_materia'] == $appello->cod_materia){ 
                                $nome_materia = $materia['materia']['nome_materia'];
                                $crediti_materia = $materia['materia']['crediti_materia'];
                                $voto_materia = $materia['materia']['voto_materia'];
                                break; // quando la trovo, interrompo il foreach
                            };
                        };
                        if($voto_materia == NULL){ // Se la variabile 'voto_materia' è NULL --> Esame non ancora sostenuto --> Mostrare appelli
                            $stmtAppelli = $appello->getRoundsByTeaching();
                            if($stmtAppelli->rowCount()>0){ // Verifico ci siano appelli per quella materia
                                while($row = $stmtAppelli->fetch(PDO::FETCH_ASSOC)){ // Per ogni record (appello)..
                                    $appello->id_appello = $row['id_appello'];
                                    $prenotazione = $appello->getPrenotazione($matricola->matricola); // per sapere se lo studente si è già prenotato o meno
                                    $array_appello = array(
                                        'nome_materia' => $nome_materia,
                                        'crediti_materia' => $crediti_materia,
                                        'cod_materia' => $row['cod_materia'],
                                        'id_appello' => $row['id_appello'],
                                        'data_appello' => date("d/m/Y", strtotime($row['data_appello'])),
                                        'prenotazione' => $prenotazione // true/false
                                    );
                                    array_push($array_appelli["appelli"], $array_appello); // Inserisco l'array del singolo appello nell'array contenente tutti gli appelli
                                };
                                http_response_code(200);
                                echo json_encode($array_appelli);
                            }else{
                                http_response_code(200);
                                echo json_encode(array("message" => "Nessun appello disponibile per la seguente materia:<hr/><b>$nome_materia</b>"));
                            };
                        }else{
                            http_response_code(200);
                            echo json_encode(array("message" => "Nessun appello disponibile per la seguente materia:<hr/><b>$nome_materia<hr/>Esame superato con voto $voto_materia/30 </b>"));
                        };
                            
                    }else{ // SE non ricevo uno specifico codice materia --> FORNIRE APPELLI DI TUTTE LE MATERIE DEL LIBRETTO (sempre che non siano state già sostenute)
                        foreach($arrayLibretto as $key => $materia){ // Ciclo su tutte le materie del libretto
                            if($materia['materia']['voto_materia'] == NULL){ // Se l'esame non è stato già superato...
                                $appello->cod_materia = $materia['materia']['cod_materia'];
                                $stmtAppelli = $appello->getRoundsByTeaching();
                                if($stmtAppelli->rowCount()>0){
                                    while($row = $stmtAppelli->fetch(PDO::FETCH_ASSOC)){
                                        $appello->id_appello = $row['id_appello'];
                                        $prenotazione = $appello->getPrenotazione($matricola->matricola);
                                        $array_appello = array(
                                            'nome_materia' => $materia['materia']['nome_materia'],
                                            'crediti_materia' => $materia['materia']['crediti_materia'],
                                            'cod_materia' => $row['cod_materia'],
                                            'id_appello' => $row['id_appello'],
                                            'data_appello' => date("d/m/Y", strtotime($row['data_appello'])),
                                            'prenotazione' => $prenotazione
                                        );
                                        array_push($array_appelli["appelli"], $array_appello);
                                    };
                                };
                            }   
                        };
                        http_response_code(200);
                        echo json_encode($array_appelli);
                    };
                }else{ // se a quella matricola non corrisponde un libretto
                    echo json_encode(array("message" => "Libretto non trovato.<hr/>Contattare l'assistenza se il problema persiste."));
                    http_response_code(400);
                };
            }else{ // Se all'username non corrisponde una matricola...
                http_response_code(403); //  403: forbidden:
                // il client non ha i diritti di accesso, non è cioè autorizzato, pertanto il server si rifiuta di fornire un'opportuna risposta
                header("location: ../../homepageStudente.php");
            };
        }else{
            http_response_code(403); //  403: forbidden:
            // il client non ha i diritti di accesso, non è cioè autorizzato, pertanto il server si rifiuta di fornire un'opportuna risposta
            header("location: ../../loginPage.php");
        };
    }else{
        http_response_code(405); //  405: method not allowed (il metodo richiesto non può essere usato)
        // in alternativa, si potrebbe usare (e preferire) 403 Forbidden:
        // il client non ha i diritti di accesso, non è cioè autorizzato, pertanto il server si rifiuta di fornire un'opportuna risposta
        header("location: ../../loginPage.php");
    };
?>