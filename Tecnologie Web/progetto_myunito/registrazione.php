<?php
    session_start();
    if(isset($_SESSION["username"])){
        header("location: homepageStudente.html");
    };
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Registrazione</title>
    <link rel="stylesheet" href="css/common.css">
    <link rel="stylesheet" href="css/common-form.css">
    <link rel="stylesheet" href="css/register.css">
    <script src="jquery-3.5.1.js" type="text/javascript"></script>
</head>
<body>
    <div id="header">
        <div id="left-header" class="header-img-container">
            <a style="display: flex" href="homepageStudente.php" title="homepage"><img style="width: 170px" src="imgs/logo_uni.png"></a>
        </div>
        <div id="right-header">
            <div class="header-img-container">
                <img src="imgs/logo_uni1.png" id="uni-image">
            </div>
        </div>
    </div>
    <div id="content-container" class="flex-centered">
        <div id="center-container">
            <div id="form-title-container" class="text-centered">
                <p>Registrazione al Portale di Ateneo</p>
            </div>
            <div id="form-container">
                <form id="form" name="form" action="" method="post">
                    <label for="nome" style="display: inline-block;">Nome:</label><input type="text" name="nome" id="nome" class="form-input" maxlength="30" spellcheck="false"  required/>
                    <label for="cognome">Cognome:</label><input type="text" name="cognome" id="cognome" maxlength="20" class="form-input" spellcheck="false" required/>
                    <label for="username">Username:</label><input type="text" name="username" id="username" maxlength="20" class="form-input" spellcheck="false" required/>
                    <label for="pass">Password:</label><input type="password" name="pass" id="pass" minlength="5" maxlength="20" class="form-input"  required/>
                    <div id="form-gender-radio" >
                        <div class="label-gender"><label>Genere:</label></div>
                        <input type="radio" name="genere" value="m" required/><label for="m" style="font-weight: normal"> M</label>
                        <input type="radio" name="genere" value="f" required/><label for="f"style="font-weight: normal"> F</label>
                    </div>
                    <label for="data_nascita">Data di nascita:</label><input type="date" name="data_nascita" class="form-input" id="data_nascita" required>
                    <label for="nazione_nascita">Nazione di nascita:</label><input type="text" name="nazione_nascita" class="form-input" id="nazione_nascita" maxlength="20" required/>
                    <div id="form-residenza">
                        <table id="table-residenza">
                            <caption class="label-residenza">Residenza:</caption>
                            <tr>
                                <td>
                                    <label for="nazione_residenza" class="normal-weight">Nazione:</label>
                                </td>
                                <td>
                                    <div class="box-alert nazione_residenza"></div>
                                    <input type="text" name="nazione_residenza" id="nazione_residenza" maxlength="20" class="form-input" required/>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <label for="indirizzo_residenza" class="normal-weight">Indirizzo:</label>
                                </td>
                                <td>
                                    <div class="box-alert indirizzo_residenza"></div>
                                    <input type="text" name="indirizzo_residenza" id="indirizzo_residenza" maxlength="50" class="form-input" required/>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <label for="cap_residenza" class="normal-weight">CAP:</label>
                                </td>
                                <td>
                                    <div class="box-alert cap_residenza"></div>
                                    <input type="number" name="cap_residenza" id="cap_residenza" maxlength="5" class="form-input" required/>
                                </td>
                            </tr>
                            <tr>
                                <td>
                                    <label for="citta_residenza" class="normal-weight">Città:</label>
                                </td>
                                <td>
                                    <div class="box-alert citta_residenza"></div>
                                    <input type="text" name="citta_residenza"  id="citta_residenza" maxlength="30" class="form-input" required/>
                                </td>
                            </tr>
                        </table>
                    </div>
                    <label for="email">Email:</label><input type="email" name="email" id="email" pattern="^\S+$" maxlength="40" class="form-input" required/>
                    <label for="telefono">Numero di telefono cellulare:</label><input type="number" name="telefono" id="telefono" maxlength="10" class="form-input"/>
                    <div id="form-buttons">
                        <button type="button" class="form-button" onClick="clearForm()">Cancella Dati</button>
                        <input type="submit" id="submit" name="submit" class="form-button" value="Registrazione"/>
                    </div>
                </form>
            </div>
        </div>
    </div>
    <div id="footer">
        <p>Università degli Studi di Torino - Via Verdi, 8 - 10124 Torino - Progetto realizzato da Luca Nuzzo per l'esame di Tecnologie Web: approcci avanzati</p>
    </div>
    <script type="text/javascript">
        // ALERT DATA NASCITA
        // creo un oggetto Data -> lo converto in una stringa (standard ISO) -> seleziono solo ciò che precede la "T" | split(separator)[limit];
        data_nascita.max = new Date().toISOString().split("T")[0]; 
        // creo una funzione per RESETTARE il valore e AVVISARE l'utente (tale funzione sarà invocata in jQuery)
        function alertDataNascita(domInputData){
            if(domInputData.value > data_nascita.max){
                let p = "<p class=\"input-alert\" style=\"color: red\">Inserire una data valida</p>";
                domInputData.value = "";
                document.querySelector("label[for=\"data_nascita\"]").innerHTML += p;
            }else{
                document.querySelector("label[for=\"data_nascita\"]").innerHTML = "Data di nascita:";
            }
        }
        // PULIZIA DEL FORM (funzione invocata nel button onClick)
        function clearForm(){
            document.getElementById("form").reset();
        }
    </script>
    <script type="text/javascript">
        jQuery(document).ready(function(){
            // BLOCCO CARATTERI NON OPPORTUNI
            $("input").on('keypress', function(e){ // Quando l'utente preme un tasto in qualsiasi campo input
                var keyCode = e.which; // e.which restituisce il codice ascii del carattere premuto
                /*
                    48-57 - (0-9)Numbers
                    65-90 - (A-Z)
                    97-122 - (a-z)
                    8 - (backspace)
                    32 - (space)s
                */
                // IMPEDISCO L'UTILIZZO DI SPAZI nei campi password e email
                if( ($(this).attr('id') == "pass" || $(this).attr('id') == "email") && (keyCode == 8 || keyCode == 32) ) {
                    e.preventDefault();
                }
                // IMPEDISCO L'UTILIZZO DI NUMERI nei campi nome, cognome, nazione e città
                if( ($(this).attr('id') == "nome" || $(this).attr('id') == "cognome" || $(this).attr('id') == "nazione_nascita" || $(this).attr('id') == "nazione_residenza" || $(this).attr('id') == "citta_residenza") && (keyCode >= 48 && keyCode <= 57) ){
                    e.preventDefault();
                }
            })
            // ALERT LUNGHEZZA MASSIMA DEI CAMPI
            $("input").on('input', function(){ // Intercetto l'evento di input
                let maxLength = $(this).attr("maxlength"); // lunghezza massima del campo
                let myLabel = $("label[for='"+$(this).attr('id')+"']"); // label
                let nameLabel = myLabel.text().replace(':','').toLowerCase(); // testo della label
                let myDiv = $("div[class='box-alert "+$(this).attr('id')+"']"); // div
                if($(this).attr('type') == "date"){ // SOLO SE si tratta di un TYPE DATE
                    // invoco la funzione dedicata alla data
                    // ... passando come parametro un array che rappresenta l'oggetto INPUT DATE del DOM
                    alertDataNascita($(this)[0]); 
                }else{
                    // Solo per i campi di tipo NUMBER è necessario apportare MODIFICHE al VALORE!
                    if($(this).attr('type') == "number"){
                        $(this).val($(this).val().slice(0,maxLength));
                    }
                    // MESSAGGI DI AVVISO per l'utente
                    // Solo se l'utente ha già digitato il massimo possibile per il campo...
                    if($(this).val().length == maxLength){ 
                        //.. intercetto l'evento di PRESSIONE di un tasto per mostrare un avviso (per 4 secondi)
                        $(this).on('keypress', function(){ 
                            // il trattamento è diverso per gli elementi dedicati alla residenza
                            if($(this).attr('id').indexOf("residenza") > 0){ 
                                if(myDiv.children('p').length <= 0){
                                    myDiv.append("<p class=\"input-alert\">Il campo "+nameLabel+" non può superare "+maxLength+" caratteri</p>");
                                    setTimeout(function(){ 
                                        myDiv.children('p').remove();
                                    }, 3000);
                                };
                            }else{
                                if(myLabel.children('p').length <= 0){
                                    myLabel.append("<p class=\"input-alert\">Il campo "+nameLabel+" non può superare "+maxLength+" caratteri</p>");
                                    setTimeout(function(){ 
                                        myLabel.children('p').remove();
                                    }, 3000);
                                };
                            };   
                        })            
                    }else{
                        myDiv.children('p').remove();
                        myLabel.children('p').remove();
                    };
                };
            });

            $("#form").on('submit', function(e){
                e.preventDefault(); //necessario perché l'input type è SUBMIT, non BUTTON (ho scelto questa combinazione per mantenere i suggerimenti nel form)
                var formData = {
                    'nome' : $('input[name=nome]').val(),
	                'cognome': $('input[name=cognome]').val(),
	                'username' : $('input[name=username]').val(),
                    'pass' : $('input[name=pass]').val(),
                    'genere' : $('input[name=genere]:checked').val(),
                    'data_nascita' : $('input[name=data_nascita]').val(),
                    'nazione_nascita' : $('input[name=nazione_nascita]').val(),
                    'nazione_residenza' : $('input[name=nazione_residenza]').val(),
                    'indirizzo_residenza' : $('input[name=indirizzo_residenza]').val(),
                    'cap_residenza' : $('input[name=cap_residenza]').val(),
                    'citta_residenza' : $('input[name=citta_residenza]').val(),
                    'email' : $('input[name=email]').val(),
                    'telefono' : $('input[name=telefono]').val(),
                };
                $.ajax({
                    url: 'api_server/api/register.php',
                    type: 'POST',
                    contentType: 'application/json',
                    data: JSON.stringify(formData) 
                }).done(function(){
                    $('#center-container').html("<div id=\"form-title-container\" class=\"text-centered\"><p>Registrazione completata!</p><p style=\"font-size: 1.1rem; color: black; font-weight: bold;\">Effettua il <a href=\"loginPage.php\">Login</a></p></div>");
                    $("#footer").css("position", "fixed");
                }).fail(function(response, xhr, err, ecc){
                    $(".input-alert").each(function(){ // mi assicuro di rimuovere eventuali alert precedenti
                        $(this).remove();
                    }); 
                    if(response.responseJSON.errorsArray){ // Se ricevo errorsArray (valori già presenti nel DB)
                        let respString = JSON.stringify(response.responseJSON.errorsArray);
                        if(respString.indexOf("username") >= 0){ // se il campo contiene la stringa username..
                            $(window).scrollTop(0);
                            $("label[for='username']").append("<p class=\"input-alert\" style=\"color: red; font-size: 1rem\">Username non valido</p>");
                        }
                        if(respString.indexOf("email") >= 0){ // se il campo contiene la stringa email..
                            $("label[for='email']").append("<p class=\"input-alert\" style=\"color: red; font-size: 1rem\">Email non valida</p>");
                        }
                    }else if(response.responseJSON.emptyFields){ // Se ricevo emptyFields (campi vuoti - remota ipotesi)...
                        let emptyFields = response.responseJSON.emptyFields;
                        emptyFields.forEach(function(field){ // per ogni campo...
                            $(window).scrollTop(0);
                            if(field.indexOf("residenza") > 0){ // se il campo contiene la stringa residenza...
                                $("div[class='box-alert "+field+"']").append("<p class=\"input-alert\" style=\"color: red; font-size: 1rem\">Campo vuoto!</p>");
                            }else{
                                $("label[for='"+field+"']").append("<p class=\"input-alert\" style=\"color: red; font-size: 1rem\">Campo vuoto!</p>");
                            }
                        })
                    }else{ // Ipotesi cui corrisponde - per esclusione - un response code 503 = service unavailable
                        $('#center-container').html("<div id=\"form-title-container\" class=\"text-centered\"><p>Ops! Servizio momentaneamente non disponibile</p><p style=\"font-size: 1.1rem; color: black; font-weight: bold;\"><br/><a href=\"registrazione.php\">Riprova</a> più tardi</p></div>");
                        $("#footer").css("position", "fixed");
                    }
                });
            });
        });
    </script>
</body>
</html>