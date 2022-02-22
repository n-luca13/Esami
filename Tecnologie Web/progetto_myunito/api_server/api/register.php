<?php
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
    $userData = json_decode(file_get_contents("php://input"));
    $arrayData = (array)$userData; // creo un array a partire dall'oggetto
    unset($arrayData["telefono"]); // rimuovo l'elemento con chiave "telefono"
   
    $emptyFields = []; // inizializzo un array per eventuali campi vuoti
    $errorsArray = []; // inizializzo un array per eventuali errori di username e email
    // popolo l'array "emptyFields" con le chiavi dei campi vuoti di "arrayData"
    foreach($arrayData as $key => $value){
        if(empty($value)){$emptyFields[] = $key;}
    }
    if(empty($emptyFields)){ // Se l'array è vuoto, vuol dire che tutti i campi sono stati compilati, pertanto...
        // inserisco i valori nelle variabili d'istanza dell'oggetto $user
        $user->nome = $userData->nome;
        $user->cognome = $userData->cognome;
        $user->username = $userData->username;
        $user->pass = $userData->pass;
        $user->genere = $userData->genere;
        $user->data_nascita = $userData->data_nascita;
        $user->nazione_nascita = $userData->nazione_nascita;
        $user->nazione_residenza = $userData->nazione_residenza;
        $user->indirizzo_residenza = $userData->indirizzo_residenza;
        $user->cap_residenza = $userData->cap_residenza;
        $user->citta_residenza = $userData->citta_residenza;
        $user->email = $userData->email;
        if(!empty($userData->telefono)){
            $user->telefono = $userData->telefono; 
        };

        if($user->doesUsernameExist()){
            $errorsArray[] = "username"; // Se l'username è già presente nel DB inserisco un valore nell'array
        }
        if($user->doesEmailExist()){
            $errorsArray[] = "email"; // Se la mail è già presente nel DB inserisco un valore nell'array
        }
        if(empty($errorsArray)){
            if($user->register()){ // se la registrazione va a buon fine...
                http_response_code(201); // response code 201 = created
                echo json_encode(array("message" => "Utente creato con successo"));
            }
            else{ // se la creazione è fallita...
                http_response_code(503); // response code 503 = service unavailable
                echo json_encode(array("message" => "Service unavailable"));
            }
        }else{ // Se l'array degli errori non è vuoto..
            http_response_code(409); // response code 409 = conflict
            echo json_encode(array("errorsArray" => $errorsArray)); // invio l'array al client
        }
    }
    else { // se i dati sono incompleti... 
        http_response_code(400); // response code 400 = bad request
        echo json_encode(array("emptyFields" => $emptyFields));
    }
?>