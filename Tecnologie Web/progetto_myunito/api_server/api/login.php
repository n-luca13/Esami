<?php
    session_start();
    //stabilisco i permessi di lettura del file (anyone)
    header("Access-Control-Allow-Origin: *");
    // definisco il formato della risposta (json)
    header("Content-Type: application/json; charset=UTF-8");
    // definisco il metodo consentito per la request
    header("Access-Control-Allow-Methods: POST");
    
    // includo le classi per la gestione dei dati
    include_once '../dataMgr/Database.php';
    include_once '../dataMgr/User.php';

    // creo una connessione al DBMS
    $database = new Database();
    $db = $database->getConnection();
    // creo un'istanza della classe User
    $user = new User($db);
    // leggo i dati provenienti dal FORM
    $loginData = json_decode(file_get_contents("php://input"));
    
    if(!empty($loginData->username) && !empty($loginData->pass)){ // se username e password non sono vuoti...
        $user->username = $loginData->username; // inserisco l'username nella variabile di user 
        // invoco il metodo getUser che modifica l'oggetto su cui viene invocato, inserendo le info dell'utente
        $user->getUser();
        if($user->pass != null){ // Se la variabile d'istanza pass non è null... quindi l'utente esiste nel db...
            if($user->pass == $loginData->pass){ // .. controllo che sia corretta
                // dunque imposto le variabili di sessione contenenti le info anagrafiche dell'utente
                $_SESSION['username'] = $user->username;
                $_SESSION['nome'] = $user->nome;
                $_SESSION['cognome'] = $user->cognome;
                $_SESSION['genere'] = $user->genere;
                $_SESSION['data_nascita'] = $user->data_nascita;
                $_SESSION['nazione_residenza'] = $user->nazione_residenza;
                $_SESSION['indirizzo_residenza'] = $user->indirizzo_residenza;
                $_SESSION['citta_residenza'] = $user->citta_residenza;
                $_SESSION['cap_residenza'] = $user->cap_residenza;
                $_SESSION['email'] = $user->email;
                $_SESSION['data_nascita'] = $user->data_nascita;
                $_SESSION['telefono'] = $user->telefono;
                echo json_encode(array("message" => "Login effettuato"));
                http_response_code(200); // 200 : OK - Richiesta HTTP andata a buon fine
            }else{
                echo json_encode(array("error" => "Password errata"));
                http_response_code(401); // 401 : Unauthorized - Autenticazione fallita; l'utente può modificare la richiesta e riprovare
            }
        }else{
            echo json_encode(array("error" => "Username inesistente"));
            http_response_code(401); // 401 : Unauthorized - Autenticazione fallita; l'utente può modificare la richiesta e riprovare
        }
        
    }else{
        echo json_encode(array("error" => "Inserire username e password"));
        http_response_code(400); // response code 400 = bad request
    }
?>