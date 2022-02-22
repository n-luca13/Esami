<?php
    session_start();
    if(!isset($_SESSION["username"])){ // Se l'utente non è loggato, lo ridireziono alla pagina di login
        header("location: loginPage.php");
    }else{
        $_SESSION["http_referer"] = "libretto";
    };
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Appelli</title>
    <link rel="stylesheet" href="css/common.css">
    <link rel="stylesheet" href="css/common-tables.css">
    <link rel="stylesheet" href="css/fontawasome/css/all.css">
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
    <div class="floating-button-container">
        <a href="libretto.php"><i class="fas fa-arrow-left"> Libretto</i></a>
    </div>
    <div id="content-container" class="padd-30">
        <div id="title-container">
            <h1>Prenotazione Appelli</h1>
            <div class="red-line"></div>
            <p>Questa pagina mostra gli appelli prenotabili per le attività didattiche presenti nel proprio libretto</p>
        </div>
        <div id="center-container" class="flex-column">
        </div>
    </div>
    <div id="footer">
        <p>Università degli Studi di Torino - Via Verdi, 8 - 10124 Torino - Progetto realizzato da Luca Nuzzo per l'esame di Tecnologie Web: approcci avanzati</p>
    </div>
    <script type="text/javascript" src="js/modalLogout.js"></script>
    <script type="text/javascript">
        jQuery(document).ready(function(){
            let data = {
                'username' : "<?php echo $_SESSION['username'] ?>",
                'cod_materia' : "<?php if(isset($_GET['cod_materia'])){echo $_GET['cod_materia'];} ?>"
            };
            let variableContainer = $("#center-container");
            $.ajax({ // Chiamata AJAX per ottenere gli APPELLI
                url: 'api_server/api/alterRound.php',
                method: 'POST',
                contentType: 'application/JSON',
                data: JSON.stringify(data)
            }).done(function(response){              
                if(!response.message){
                    // inserisco lo scheletro della tabella per ospitare gli appelli
                    variableContainer.html("<div class=\"flex-column container-table\"><table id=\"table-appelli\" class=\"text-centered\"><thead><tr><th class=\"text-left col-title\">Attivit&agrave; Didattica</th><th>Peso in crediti</th><th>Data</th><th>Prenota/Annulla</th><th>Stato Prenotazione</th></tr></thead><tbody></tbody></table></div>");
                    let tbodyRicerca = $("#table-appelli > tbody");
                    // inizializzo un array per gli appelli
                    let arrayAppelli = response.appelli;
                    $.each(arrayAppelli, function(i){ // per ogni elemento dell'array di appelli
                        let nome_materia = arrayAppelli[i].nome_materia;
                        let id_appello = arrayAppelli[i].id_appello;
                        // inserisco nella tabella una riga contenente i dati dell'appello
                        tbodyRicerca.append("<tr id=\""+id_appello+"\"><td class=\"nome-materia text-left col-title\">"+nome_materia.toUpperCase()+"</td><td class=\"crediti-materia\">"+arrayAppelli[i].crediti_materia+"</td><td class=\"data-appello\">"+arrayAppelli[i].data_appello+"</td><td class=\"action\"></td><td class=\"status\"></td></tr>");
                        if(arrayAppelli[i].prenotazione == true){ // se la matricola risulta prenotata a quell'appello...
                            $("tr[id=\""+id_appello+"\"]").css("background-color", "rgba(255,237,33,0.31)");
                            $("tr[id=\""+id_appello+"\"]").find("td.status").html("<i class=\"fas fa-check green\"></i>");
                            // inserisco un link per cancellare la prenotazione
                            $("tr[id=\""+id_appello+"\"]").find("td.action").html("<a id=\""+id_appello+"\" name=\""+id_appello+"\" class=\"annulla-prenotazione\" href=\"\"><i class=\"fas fa-times\"></i></a>");
                        }else{ // altrimenti inserisco un link per la prenotazione
                            $("tr[id=\""+id_appello+"\"]").find("td.action").html("<a id=\""+id_appello+"\" name=\""+id_appello+"\" class=\"aggiungi-prenotazione\" href=\"\"><i class=\"far fa-calendar-plus\"></i></i></a>");
                        }
                    });
                    // Chiamata AJAX per ANNULLARE PRENOTAZIONE 
                    $(".annulla-prenotazione").on('click', function(e){ // al click su un elemento con classe "annulla-prenotazione"..
                        e.preventDefault();
                        if(confirm("Annullare la prenotazione?")){ // finestra di conferma
                            let datiPrenotazione = {
                                'username' : "<?php echo $_SESSION['username'] ?>",
                                'id_appello' : $(this).attr('id'),
                                'action' : "delete" // tipo di azione richiesta all'api
                            };
                            $.ajax({ // Chiamata AJAX
                            url: 'api_server/api/booking.php',
                            method: 'DELETE',
                            contentType: 'application/JSON',
                            data: JSON.stringify(datiPrenotazione)
                            }).done(function(response){
                                alert(response.message);
                            }).fail(function(response){
                                if(response.error){
                                    alert(response.error);
                                }else{
                                    alert("Qualcosa è andato storto. Riprova")
                                };
                            }).always(function(){
                                location.reload();
                            });
                        };
                    });
                    // Chiamata AJAX per PRENOTARE APPELLO 
                    $(".aggiungi-prenotazione").on('click', function(e){  // al click su un elemento con classe "aggiungi-prenotazione"..
                        e.preventDefault();
                        if(confirm("Confermi la prenotazione?")){ // finestra di conferma
                            let datiPrenotazione = {
                                'username' : "<?php echo $_SESSION['username'] ?>",
                                'id_appello' : $(this).attr('id'),
                                'action' : "insert" // tipo di azione richiesta all'api
                            };
                            $.ajax({ // Chiamata AJAX
                            url: 'api_server/api/booking.php',
                            method: 'POST',
                            contentType: 'application/JSON',
                            data: JSON.stringify(datiPrenotazione)
                            }).done(function(response){
                                alert(response.message);
                            }).fail(function(response,statusText,xhr){
                                if(response.error){
                                    alert(response.error);
                                }else{
                                    alert("Qualcosa è andato storto. Riprova")
                                };
                            }).always(function(){
                                location.reload();
                            });
                        };
                    });              
                }else{
                    variableContainer.html(response.message);
                };
            }).fail(function(response){ // se la chiamata AJAX per popolare gli appelli fallisce
                if(response.message){
                    variableContainer.html(response.message);
                }else{ // messaggio di errore generico
                    variableContainer.html("<p style='font-weight: bold'>Servizio momentaneamente non disponibile</p><p>Riprova più tardi!</p>");
                };
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
                });
            }); 
        });
    </script>
</body>
</html>