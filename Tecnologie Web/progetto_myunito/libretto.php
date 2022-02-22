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
    <title>Libretto</title>
    <link rel="stylesheet" href="css/common.css">
    <link rel="stylesheet" href="css/common-tables.css">
    <link rel="stylesheet" href="css/common-form.css">
    <link rel="stylesheet" href="css/libretto.css">
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
        <a href="homepageStudente.php"><i class="fas fa-arrow-left"> Home</i></a>
    </div>
    <div id="content-container" class="padd-30">
        <div id="title-container">
            <h1>Libretto di <?php
                    echo ($_SESSION['nome']." ".$_SESSION['cognome']);
                ?>
            </h1>
            <div class="red-line"></div>
            <p>Questa pagina visualizza le informazioni relative alle attività didattiche del libretto dello studente</p>
        </div>
        <div id="center-container" class="flex-column">
            <div class="container-table flex-column">
                <table id="table-libretto" class="text-centered">
                    <thead>
                        <tr>
                            <th class="text-left col-title">Attivit&agrave; Didattica</th>
                            <th>Peso in crediti</th>
                            <th>Stato</th>
                            <th>Voto</th>
                            <th>Appelli</th>
                        </tr>
                    </thead>
                    <tbody id="table-body">
                    </tbody>
                </table>
            </div>
        </div>
        <hr class="hr-line">
        <div id="title-container">
            <h1>Attività didattiche a scelta</h1>
            <div class="red-line"></div>
            <p>Questa sezione cosente di visualizzare e modificare le attività didattiche a scelta del piano carriera</p>

        </div>
        <div class="flex-column">
            <div id="container-materie-scelte" class="container-table flex-column">
                
            </div>
            
            <div id="variable-container">
                
            </div>
        </div>
    </div>
    <div id="footer">
        <p>Università degli Studi di Torino - Via Verdi, 8 - 10124 Torino - Progetto realizzato da Luca Nuzzo per l'esame di Tecnologie Web: approcci avanzati</p>
    </div>
    <script type="text/javascript" src="js/modalLogout.js"></script>
    <script type="text/javascript">
        // PULIZIA DEL FORM (funzione invocata nel button onClick)
        function clearForm(){
            document.getElementById("form-insegnamenti").reset();
            document.getElementById("container-table-results").innerHTML = "";
        };
    </script>
    <script type="text/javascript">
        jQuery(document).ready(function(){ // Al caricamento del DOM
            $.ajax({ // Chiamata AJAX per popolare il LIBRETTO
                url: 'api_server/api/home.php',
                method: 'GET',
                data: {
                    'username_studente' : "<?php echo $_SESSION['username'] ?>"
                },
                contentType: 'application/JSON',
            }).done(function(response,textStatus,xhr){
                if(xhr.status == 200){ // se l'utente è IMMATRICOLATO
                    $('#title-container > h1').append(" (Matricola N. "+response.matricola+")");
                    if(response.error){ // Se però c'è un campo 'ERROR', ne mostro il valore all'utente (INCONSISTENZA DB: impossibile trovare corso di laurea o libretto)
                        $("#table-dati-carriera").html("<caption class=\"caption-left\">Riepilogo Carriera</caption><thead><td>"+response.error+"</td></thead>");
                    }else{ // se va tutto bene...
                        // PREPARO LA SEZIONE PER LE MATERIE A SCELTA
                        if(response.num_materie_scelte > 0){ // Solo se l'utente ha già scelto almeno una materia, mostro la tabella per le materie scelte
                            $("#container-materie-scelte").html("<table id=\"table-scelte\" class=\"text-centered margin-bottom\"><thead><tr><th>Rimuovi</th><th class=\"text-left col-title\">Attivit&agrave; Didattica</th><th>Peso in crediti</th><th>Stato</th><th>Voto</th><th>Appelli</th></tr></thead><tbody></tbody></table>");  
                        };
                        if(response.num_materie_scelte < 2){ // Se l'utente ha scelto meno di due materie (o non ne ha scelta nessuna)...
                            var container_variabile = $("#variable-container");
                            let num_residuo = 2 - response.num_materie_scelte;
                            // paragrafo di notifica
                            container_variabile.html("<div class=\"title-container\"><p>&Egrave; possibile aggiungere "+num_residuo+" attività a scelta</p></div>");
                            // FORM per INSERIMENTO materie
                            container_variabile.append("<div class=\"flex-column\"><form id=\"form-insegnamenti\" action=\"\" method=\"post\" class=\"flex-column\"><label for=\"slct-corso\">Seleziona il corso di laurea</label><select name=\"slct-corso\" id=\"slct-corso\" class=\"form-select\"><option disabled selected value=\"\"> -- Seleziona un corso -- </option></select><label for=\"insegnamento\">Ricerca il nome o il codice d'insegnamento</label><input type=\"text\" id=\"insegnamento\" name=\"insegnamento\" class=\"form-input\"/><div id=\"form-buttons\"><button type=\"button\" class=\"form-button\" onClick=\"clearForm()\">Ripristina</button></div></form></div><div id=\"view-content\" class=\"flex-column\"><div id=\"container-table-results\" class=\"container-table flex-column\"></div></div>");                            
                            // POPOLO LA SELECT del corso di laurea
                            var selectCorso = $("#slct-corso");
                            $.ajax({
                                url: 'api_server/api/alterbooklet.php',
                                method: 'GET',
                                data: {
                                    'tipologia_corso' : response.tipologia_corso 
                                },
                                contentType: 'application/JSON',
                            }).done(function(res){
                                selectCorso.html("<option disabled selected value=\"\"> -- Seleziona un corso -- </option>");
                                let arrayCorsi = res.corsi_laurea; // creo e popolo un array con i corsi di laurea corrispondenti alla tipologia
                                $.each(arrayCorsi, function(i){ // per ogni elemento i dell'array...
                                    let cod = arrayCorsi[i].codice_corso; // codice del corso di laurea
                                    let nome = arrayCorsi[i].nome_corso; // nome del corso 
                                    selectCorso.append('<option value="'+cod+'">'+nome+'</option>'); // inserisco una option
                                });
                            }).fail(function(){
                                container_variabile.html("<p style='font-weight: bold'>Servizio momentaneamente non disponibile</p><p>Riprova più tardi!</p>");
                            });

                            $($('input[name=insegnamento]')).on('keypress', function(e){ // Quando l'utente preme un tasto nel campo di input dedicato alla ricerca di nome o codice insegnamento
                                var keyCode = e.which; // e.which restituisce il codice ascii del carattere premuto
                                /*
                                    8 - (backspace)
                                    32 - (space)s
                                */
                                // IMPEDISCO l'utilizzo di uno SPAZIO quale PRIMO CARATTERE
                                if( ($(this).val() == "" ) && (keyCode == 8 || keyCode == 32) ) {
                                    e.preventDefault();
                                }
                            });
                            // chiamata AJAX per popolare tabella materie a scelta <-- FORM
                            $("#form-insegnamenti").on('change keyup submit', function(e){ // intercetto gli eventi di rilascio di un tasto, cambiamento del form e submit
                                e.preventDefault();
                                let formData = {
                                    'corso_selezionato' : selectCorso.val(),
                                    'ricerca_materia' : $('input[name=insegnamento]').val().replace(/ +(?= )/g,''), // rimuovo doppi spazi e sostituisco le maiuscole
                                    'matricola_studente' : response.matricola
                                };
                                if($('input[name=insegnamento]').val() != "" || selectCorso.val() != null){ // <-- Per prevenire una chiamata allo svuotamento dei campi
                                    $.ajax({
                                        url: 'api_server/api/alterbooklet.php',
                                        method: 'POST',
                                        contentType: 'application/JSON',
                                        data: JSON.stringify(formData),
                                    }).done(function(resForm){
                                        if(resForm.materie.length != 0){ // Se l'array non è vuoto..
                                            // Inserisco una tabella (vuota) per ospitare i risultati
                                            $("#container-table-results").html("<table id=\"table-insegnamenti\" class=\"text-centered\"><thead><tr><th class=\"text-left col-title\">Attivit&agrave; Didattica</th><th>Peso in crediti</th><th>Corso di laurea</th><th>Aggiungi</th></tr></thead><tbody></tbody></table>");
                                            let tbodyRicerca = $("#table-insegnamenti > tbody");
                                            let arrayMaterie = resForm.materie;
                                            $.each(arrayMaterie, function(i){ // per ogni elemento dell'array di materie
                                                let nome_materia = arrayMaterie[i].nome_materia;
                                                let cod_materia = arrayMaterie[i].codice_materia;
                                                // inserisco nella tabella una riga contenente i dati della materia
                                                tbodyRicerca.append("<tr id=\""+cod_materia+"\"><td class=\"nome-materia text-left col-title\">"+nome_materia.toUpperCase()+"</td><td class=\"crediti-materia\">"+arrayMaterie[i].crediti_materia+"</td><td class=\"corso-laurea\">"+arrayMaterie[i].nome_corso+"</td><td class=\"aggiungi-materia\"><a id=\""+cod_materia+"\" name=\""+cod_materia+"\" class=\"add-link\" href=\"\"><i class=\"fas fa-plus\"></i></a></td></tr>");
                                            });
                                            // Chiamata AJAX al click su un link per AGGIUNGERE UNA MATERIA A SCELTA AL LIBRETTO
                                            $('.add-link').click(function(e){
                                                e.preventDefault();
                                                let linkData = {
                                                    'cod_materia' : $(this).attr("id"),
                                                    'action' : $(this).attr("class"),
                                                    'username' : "<?php echo $_SESSION['username'] ?>"
                                                };
                                                $.ajax({
                                                    url: 'api_server/api/alterbooklet.php',
                                                    method: 'POST',
                                                    contentType: 'application/JSON',
                                                    data: JSON.stringify(linkData)
                                                }).done(function(resAdding){
                                                    if(resAdding.error){
                                                        alert(resAdding.error);
                                                    }else{
                                                        if(resAdding.message){
                                                            alert(resAdding.message);
                                                        };
                                                        location.reload();
                                                    };
                                                }).fail(function(resAdding){
                                                    if(resAdding.error){ // se ricevo una risposta JSON
                                                        alert(resAdding.error); // mostro il messaggio di errore preconfezionato
                                                    }else{ // messaggio di errore generico
                                                        alert("Servizio momentaneamente non disponibile");
                                                    };
                                                });
                                            });
                                        }else{
                                            $("#container-table-results").html("<p>Nessun risultato</p>");
                                        };
                                    }).fail(function(res){
                                        if(res.responseJSON){ // se ricevo una risposta JSON
                                            $("#container-table-results").html("<p>"+res.responseJSON.message+"</p>"); // mostro il messaggio di errore preconfezionato
                                        }else{ // altrimenti, messaggio di errore generico
                                            $("#container-table-results").html("<p>Servizio momentaneamente non disponibile</p>"); 
                                        };
                                    });
                                }else{
                                    $("#container-table-results").empty();
                                }
                            });

                        }else{ // Se l'utente ha già scelto 2 materie, mostro solamente un avviso
                            $("#variable-container").html("<div class=\"title-container\"><p>Numero massimo di attività a scelta raggiunto. Rimuovere una materia per eventuali sostituzioni.</p></div>");
                        };
                        // RIEMPIO LE TABELLE DI LIBRETTO E MATERIE SCELTE
                        $.each(response.materie, function(key, val){ // per ogni array materia contenuto nell'array multidimensionale della matricola
                            // creo delle variabili per nome e codice materia
                            let nome_materia = val['materia']['nome_materia'];
                            let cod_materia = val['materia']['cod_materia'];
                            let scelta = val['materia']['scelta_studente'];
                            // inserisco una riga nella tabella
                            if(scelta == 1){ // se si tratta di una MATERIA A SCELTA
                                $("#table-scelte > tbody").append("<tr id=\""+cod_materia+"\"><td class=\"rimuovi-materia\"></td><td class=\"nome-materia text-left col-title\">"+nome_materia.toUpperCase()+"</td><td class=\"crediti-materia\">"+val['materia']['crediti_materia']+"</td><td class=\"stato-materia\"></td><td class=\"voto-materia\"></td><td class=\"appelli-materia\"></td></tr>");
                            }else{ // MATERIE PREDEFINITE DEL LIBRETTO
                                $('#table-body').append("<tr id=\""+cod_materia+"\"><td class=\"nome-materia text-left col-title\">"+nome_materia.toUpperCase()+"</td><td class=\"crediti-materia\">"+val['materia']['crediti_materia']+"</td><td class=\"stato-materia\"></td><td class=\"voto-materia\"></td><td class=\"appelli-materia\"></td></tr>");
                            };  
                            // Se l'esame è stato superato, inserisco stato e voto
                            if(val['materia']['voto_materia'] != null){
                                $("tr[id=\""+cod_materia+"\"]").find("td.stato-materia").html("<i class=\"fas fa-check green\"></i>");
                                $("tr[id=\""+cod_materia+"\"]").find("td.voto-materia").html(val['materia']['voto_materia']);
                            }else{ // altrimenti..  
                                // inserisco un link agli appelli passando come parametro il codice della materia
                                $("tr[id=\""+cod_materia+"\"]").find("td.appelli-materia").html("<a href=\"appelli.php?cod_materia="+cod_materia+"\"><i class=\"fas fa-eye\"></i></a>");
                                if(scelta == 1){ // inserisco un link per rimuovere la materia a scelta
                                    $("tr[id=\""+cod_materia+"\"]").find("td.rimuovi-materia").html("<a id=\""+cod_materia+"\" name=\""+cod_materia+"\" class=\"remove-link\" href=\"\"><i class=\"fas fa-minus\"></i></a>")                                      
                                };
                                if(val['materia']['prenotazione']){
                                    $("tr[id=\""+cod_materia+"\"]").find("td.stato-materia").html("<a href=\"appelli.php?cod_materia="+cod_materia+"\"><i class=\"far fa-calendar-check\"></i></a>");
                                }
                            };
                        });
                        // Chiamata AJAX per RIMOZIONE MATERIA A SCELTA
                        $('.remove-link').click(function(e){ // intercetto l'evento di click su un elemento con classe "remove-link"
                            e.preventDefault();
                            let linkData = {
                                'cod_materia' : $(this).attr("id"),
                                'action' : $(this).attr("class"), // tipo di azione richiesta all'api
                                'username' : "<?php echo $_SESSION['username'] ?>"
                            };
                            $.ajax({
                                url: 'api_server/api/alterbooklet.php',
                                method: 'DELETE',
                                contentType: 'application/JSON',
                                data: JSON.stringify(linkData)
                            }).done(function(resAdding){
                                if(resAdding.error){
                                    alert(resAdding.error);
                                }else{
                                    if(resAdding.message){
                                        alert(resAdding.message);
                                    };
                                    location.reload();
                                };
                            }).fail(function(resAdding){
                                if(resAdding.error){ // se ricevo una risposta JSON
                                    alert(resAdding.error); // mostro il messaggio di errore preconfezionato
                                }else{ // messaggio di errore generico
                                    alert("Servizio momentaneamente non disponibile");
                                };
                            });
                        });
                    };
                };
                if(xhr.status == 204){ // Utente NON IMMATRICOLATO
                    window.location = "homepageStudente.php";
                };    
            }).fail(function(response,textStatus,xhr){
                if(xhr.status == 403){ // utente NON LOGGATO
                    window.location = "loginPage.php";
                }else{ // Ipotesi cui non corrispondono specifiche gestioni degli errori server-side; resta per esclusione la mancata connessione al db
                    $('#content-container').html("<div id=\"title-container\"><h1>Server in manutenzione. Riprovare più tardi</h1><div class=\"red-line\"></div><p>Ci scusiamo per il disagio</p></div>");
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