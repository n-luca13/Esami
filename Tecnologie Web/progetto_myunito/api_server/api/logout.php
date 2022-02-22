<?php
    session_start();
    //stabilisco i permessi di lettura del file (anyone)
    header("Access-Control-Allow-Origin: *");
    // definisco il formato della risposta (json)
    header("Content-Type: application/json; charset=UTF-8");
    // definisco il metodo consentito per la request
    header("Access-Control-Allow-Methods: GET");

    if(isset($_GET['username'])){ // Solo se ricevo una variabile superglobale $_GET['username] ...
        if(isset($_SESSION['username'])){ // Verifico che la variabile di sessione sia impostata...
            session_unset(); // Dunque elimino le variabili di sessione
            session_destroy(); // Distruggo la sessione
            echo "Utente disconnesso";
            http_response_code(200); // 200 : OK - Richiesta HTTP andata a buon fine
        }else{
            http_response_code(400);
            echo "Errore: sessione non valida";
        }
    }else{
        header("location: ../../loginPage.php");
    }
?>