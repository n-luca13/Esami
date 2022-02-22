<?php
    session_start();
    if(!isset($_SESSION["username"])){ // Se l'utente non è loggato, lo ridireziono alla pagina di login
        header("location: loginPage.php");
    };
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Immatricolazione</title>
    <link rel="stylesheet" href="css/common.css">
    <link rel="stylesheet" href="css/common-form.css">
    <link rel="stylesheet" href="css/immatricolazione.css">
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
            <div class="header-img-container">
                <img src="imgs/user.jpg" id="user-image">
            </div>
        </div>
        <div id="logout-modal">
            <p>
                <?php
                    echo $_SESSION['username'];
                ?>
            </p>
            <a id="logout-link" href="">Logout</a>
        </div>
    </div>
    <div id="content-container" class="flex-centered">
        <div id="center-container">
            <div id="form-title-container" class="text-centered">
                <p>Immatricolazione</p>
            </div>
            <div id="form-container">
                <form id="form" action="" method="post">
                    <label for="tipologia">Tipologia del percorso</label>
                    <select name="tipologia" id="sel-tipologia" class="form-select" required>
                        <option disabled selected value=""> -- Seleziona un percorso -- </option>
                        <option value="triennale">Laurea Triennale</option>
                        <option value="magistrale">Laurea Magistrale</option>
                    </select>
                    <label for="codice_corso">Corso di laurea</label>
                    <select name="codice_corso" id="sel-codice_corso" class="form-select" required>
                        <option disabled selected value=""> -- Seleziona il corso -- </option>
                    </select>
                    <div id="alert-container"></div>
                    <div id="form-buttons">
                        <button type="button" class="form-button" onClick="clearForm()">Ripristina</button>
                        <input type="submit" class="form-button" value="Immatricolazione">
                    </div>
                </form>
            </div>
            
        </div>
    </div>
    <div id="footer">
        <p>Università degli Studi di Torino - Via Verdi, 8 - 10124 Torino - Progetto realizzato da Luca Nuzzo per l'esame di Tecnologie Web: approcci avanzati</p>
    </div>
    <script type="text/javascript" src="js/modalLogout.js"></script>
    <script type="text/javascript">
        function clearForm(){ // funzione per pulsante di ripristino del form
            document.getElementById("form").reset();
            jQuery(document).ready(function(){ // rimuovo anche le option eventualmente riempite dalla chiamata ajax
                $("#sel-codice_corso").empty().append("<option disabled selected value=\"\"> -- Seleziona il corso -- </option>");
            })
        }
    </script>
    <script type="text/javascript">
        jQuery(document).ready(function(){
            // Chiamata ajax per POPOLARE LA SELECT corrispondente ai corsi di laurea
            $("#sel-tipologia").change(function(){ // Quando cambia il valore della select corrisondente alla tipologia del percorso...
                let selectCorsi = $("#sel-codice_corso");
                $.ajax({
                    url: 'api_server/api/matriculation.php',
                    method: "GET",
                    data: {
                        'tipologia' : $(this).val() 
                    },
                    contentType: 'application/json',
                }).done(function(response){
                    selectCorsi.empty().append("<option disabled selected value=\"\"> -- Seleziona il corso -- </option>");
                    let arrayCorsi = response.corsi_laurea; // creo e popolo un array con i corsi di laurea corrispondenti alla tipologia
                    $.each(arrayCorsi, function(i){ // per ogni elemento i dell'array...
                        let cod = arrayCorsi[i].codice_corso; // codice del corso di laurea
                        let nome = arrayCorsi[i].nome_corso; // nome del corso 
                        selectCorsi.append('<option value="'+cod+'">'+nome+'</option>'); // inserisco una option
                    })
                }).fail(function(){
                    $("#alert-container").html("<p style='font-weight: bold'>Servizio momentaneamente non disponibile</p><p>Riprova più tardi!</p>")
                });
            });

            // Chiamata ajax per l'IMMATRICOLAZIONE
            $("#form").on('submit', function(e){ // Intercetto l'evento di submit del form
                e.preventDefault();
                let formData = {
                    'username' : "<?php echo $_SESSION["username"] ?>",
                    'codice_corso' : $('#sel-codice_corso').val()
                };
                $.ajax({
                    url: 'api_server/api/matriculation.php',
                    type: 'POST',
                    contentType: 'application/json',
                    data: JSON.stringify(formData)
                }).done(function(response,textStatus,xhr){
                    if(xhr.status == 201){ // 201 CREATED
                        $('#center-container').html("<div id=\"form-title-container\" class=\"text-centered\"><p>Immatricolazione completata!</p><p style=\"font-size: 1.1rem; color: black; font-weight: bold;\">Vai alla <a href=\"homepageStudente.php\">Homepage</a></p></div>");
                    }else{
                        alert(response.message); // ipotesi 200 OK - Utente già immatriclato
                    };
                }).fail(function(response,textStatus,xhr){
                    // se ricevo 503 Service Unavailable, oppure se la responseText contiene una substring "Connection error" --> servizio non disponibile
                    if(xhr.status == 503 || response.responseText.indexOf("Connection error") != -1){
                        $('#center-container').html("<div id=\"form-title-container\" class=\"text-centered\"><p>Ops! Servizio momentaneamente non disponibile</p><p style=\"font-size: 1.1rem; color: black; font-weight: bold;\"><br/><a href=\"immatricolazione.php\">Riprova</a> più tardi</p></div>");
                    }else{
                        alert(response.message); // ipotesi 400 Bad Request : Dati non pervenuti
                    }
                });
            });

            $("#logout-link").on('click', function(e){ // evento di click sul link di LOGOUT
                e.preventDefault();
                $.ajax({
                    url: 'api_server/api/logout.php',
                    type: 'GET',
                    contentType: 'application/JSON',
                    data: {
                        'username' : "<?php echo $_SESSION['username'] ?>"
                    },
                    dataType: 'text', // mi aspetto di ricevere una stringa di testo
                    // Dato che il comportamento sarebbe lo stesso sia nella .done che nella .fail, ho deciso di incorporarle
                }).always(function(response){
                    $('#content-container').html("<div id=\"title-container\"><h1>"+response+"</h1><div class=\"red-line\"></div><p>Vai alla pagina di <a href=\"loginPage.php\">Login</a></p></div>");
                    $('#content-container').removeClass("flex-centered");
                    $('#title-container').css("margin", "25px");
                });
            }); 
        });
    </script>
</body>
</html>