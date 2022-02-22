<?php
    //stabilisco i permessi di lettura del file (anyone)
    header("Access-Control-Allow-Origin: *");
    // definisco il formato della risposta (json)
    header("Content-Type: application/json; charset=UTF-8");
    // definisco il metodo consentito per la request
    header("Access-Control-Allow-Methods: POST, DELETE");
    // includo le classi per la gestione dei dati
    include_once '../dataMgr/Database.php';
    include_once '../dataMgr/Round.php';
    include_once '../dataMgr/IdentificationNumber.php';

    // creo una connessione al DBMS
    $database = new Database();
    $db = $database->getConnection();

    if(file_get_contents("php://input")){ // Se ricevo qualcosa nel body della request...
        $userData = json_decode(file_get_contents("php://input"));
        if(!empty($userData->username)){ // Mi assicuro di ricevere l'username
            $matricola = new IdentificationNumber($db); // Creo un'istanza della classe matricola
            $username = htmlspecialchars(strip_tags($userData->username));
            $matricola->username_studente = $username; // modifico il valore della variabile d'istanza 'username_studente'
            if($matricola->getMatricolaFromUsername()){ // Se a quell'username corrisponde una matricola (UTENTE IMMATRICOLATO)
                $matricola_studente = $matricola->matricola;
                if(!empty($userData->id_appello)){ // Mi assicuro di ricevere l'id dell'appello su cui intervenire
                    $appello = new Round($db); // Creo un'istanza della classe appello
                    $appello->id_appello = htmlspecialchars(strip_tags($userData->id_appello)); // modifico il valore dell'a variabile d'istanza id_appello
                    if($userData->action == "insert"){ // PRENOTAZIONE (INSERT)
                        if($appello->createPrenotazione($matricola_studente)){
                            http_response_code(200);
                            echo json_encode(array("message" => "Prenotazione effettuata"));
                        }else{
                            http_response_code(404); // 404: Not Found
                            echo json_encode(array("error" => "Appello non trovato"));
                        };
                    }elseif($userData->action == "delete"){ // CANCELLAZIONE PRENOTAZIONE (DELETE)
                        if($appello->deletePrenotazione($matricola_studente)){
                            http_response_code(200);
                            echo json_encode(array("message" => "Prenotazione cancellata"));
                        }else{
                            http_response_code(404); // 404: Not Found
                            echo json_encode(array("error" => "Appello non trovato"));
                        }
                    }else{
                        http_response_code(400); // 400: Bad Request
                        echo json_encode(array("error" => "Bad Request"));
                    };
                };
            }else{ // Se all'username non corrisponde una matricola...
                http_response_code(403); //  403: Forbidden:
                // il client non ha i diritti di accesso, non è cioè autorizzato, pertanto il server si rifiuta di fornire un'opportuna risposta
                header("location: ../../homepageStudente.php");
            }
        }else{ // Se non ricevo lo username
            http_response_code(403); //  403: forbidden
            // il client non ha i diritti di accesso, non è cioè autorizzato, pertanto il server si rifiuta di fornire un'opportuna risposta
            header("location: ../../loginPage.php");
        }
    }else{ // Se non ricevo nulla nel body della request
        http_response_code(405); //  405: method not allowed (il metodo richiesto non può essere usato)
        header("location: ../../loginPage.php");
    }
?>