<?php
    session_start();
    if(!isset($_SESSION["username"])){ // Se l'utente non è loggato, lo ridireziono alla pagina di login
        header("location: loginPage.php");
    }else{
        $_SESSION["http_referer"] = "homepage";
    };
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Home Studente</title>
    <link rel="stylesheet" type="text/css" href="css/common.css">
    <link rel="stylesheet" type="text/css" href="css/homepage.css">
    <link rel="stylesheet" type="text/css" href="css/fontawasome/css/all.css">
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
    <div id="content-container" class="padd-30">
        <div id="title-container">
            <h1>
                <?php
                    echo $_SESSION['nome']." ".$_SESSION['cognome'];
                ?> 
            </h1>
            <div class="red-line"></div>
            <p>Benvenut<?php
                            if($_SESSION['genere']=="m"){
                                echo "o";
                            }else{
                                echo "a";
                            }
                        ?> nella tua area riservata
            </p>
        </div>
        <div id="center-container">
            <div id="container-dati-anagrafici" class="sub-container">
                <table id="table-dati-anagrafici">
                    <caption class="caption-left">Dati Personali</caption>
                    <thead>
                        <th>
                            Nome e Cognome
                        </th>
                        <td>
                            <?php
                                echo $_SESSION['nome']." ".$_SESSION['cognome'];
                            ?>
                        </td>
                    </thead>
                    <tr>
                        <th>
                            Sesso
                        </th>
                        <td>
                            <?php
                                if($_SESSION['genere']=="m"){
                                    echo "Uomo";
                                }else{
                                    echo "Donna";
                                }
                            ?>
                        </td>
                    </tr>
                    <tr>
                        <th>
                            Data di nascita
                        </th>
                        <td>
                            <?php
                                echo $_SESSION['data_nascita'];
                            ?>
                        </td>
                    </tr>
                    <tr>
                        <th>
                            Residenza
                        </th>
                        <td>
                            <?php
                                echo $_SESSION['indirizzo_residenza']."<br/>".$_SESSION['cap_residenza'].", ".$_SESSION['citta_residenza']."<br/>".$_SESSION['nazione_residenza'];
                            ?>
                        </td>
                    </tr>
                    <tr>
                        <th>
                            Email
                        </th>
                        <td>
                            <?php
                                echo $_SESSION['email'];
                            ?>
                        </td>
                    </tr>
                    <tr>
                        <th>
                            Numero di telefono
                        </th>
                        <td>
                            <?php
                                if(!empty($_SESSION['telefono'])){
                                    echo $_SESSION['telefono'];
                                }else{
                                    echo "Numero di telefono non indicato";
                                };
                            ?>
                        </td>
                    </tr>
                </table>
            </div>
            <div id="container-dati-carriera" class="sub-container">
                <table id="table-dati-carriera">
                    <caption class="caption-left">Riepilogo Carriera</caption>
                    <thead>
                        <td id="tipologia-corso">Percorso di laurea di tipo <b></b></td>
                    </thead>
                    <tr>
                        <td id="nome-corso">Corso di Studi in <b></b></td>
                    </tr>
                    <tr>
                        <td id="crediti">Crediti: <b></b></td>
                    </tr>
                    <tr>
                        <td><a href="libretto.php">Libretto<i class="fas fa-book-open"></i></a></td>
                    </tr>
                    <tfoot>
                        <td>Vai alla <a href="appelli.php">Lista Appelli<i class="fas fa-list-alt"></i></a></td>
                    </tfoot>
                </table>
            </div>
        </div>
    </div>
    <div id="footer">
        <p>Università degli Studi di Torino - Via Verdi, 8 - 10124 Torino - Progetto realizzato da Luca Nuzzo per l'esame di Tecnologie Web: approcci avanzati</p>
    </div>
    <script type="text/javascript" src="js/modalLogout.js"></script>
    <script type="text/javascript">
        jQuery(document).ready(function(){ // Quando il DOM è stato caricato -> chiamata ajax per ottenere dati sulla MATRICOLA
            $.ajax({
                url: 'api_server/api/home.php',
                method: 'GET',
                data: {
                    'username_studente' : "<?php echo $_SESSION['username'] ?>"
                },
                contentType: 'application/JSON',
            }).done(function(response,textStatus,xhr){
                if(response.message){
                    $('#title-container > h1').append(" ("+response.message+")");
                    if(response.message == "Utente non immatricolato"){
                        $("#table-dati-carriera").html("<caption class=\"caption-left\">Riepilogo Carriera</caption><thead><td>"+response.message+"</td><td><a href=\"immatricolazione.php\">Scegli il tuo percorso di studio</a></td></thead>");
                    };
                }else{
                    $('#title-container > h1').append(" (Matricola N. "+response.matricola+")");
                    if(response.error){ // Se però c'è un campo 'error', ne mostro il valore all'utente (INCONSISTENZA DB: impossibile trovare corso di laurea o libretto)
                        $("#table-dati-carriera").html("<caption class=\"caption-left\">Riepilogo Carriera</caption><thead><td>"+response.error+"</td></thead>");
                    }else{
                        $('#tipologia-corso > b').append(response.tipologia_corso);
                        $('#nome-corso > b').append(response.nome_corso);
                        $('#crediti > b').append(response.crediti_conseguiti+"/"+response.crediti_totali);
                    };
                }   
            }).fail(function(response,textStatus,xhr){
                if(xhr.status == 403){ // L'utente risulta NON LOGGATO
                    window.location = "loginPage.php";
                }else{ // Ipotesi cui non corrispondono specifiche gestioni degli errori server-side; resta per esclusione la mancata connessione al db
                    $('#content-container').html("<div id=\"title-container\"><h1>Server in manutenzione. Riprovare più tardi</h1><div class=\"red-line\"></div><p>Ci scusiamo per il disagio</p></div>");
                }
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