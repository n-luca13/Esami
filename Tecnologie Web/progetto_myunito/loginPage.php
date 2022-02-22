<?php
    session_start();
    if(isset($_SESSION["username"])){
        header("location: homepageStudente.php");
    };
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login</title>
    <link rel="stylesheet" href="css/common.css">
    <link rel="stylesheet" href="css/common-form.css">
    <link rel="stylesheet" href="css/login.css">
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
                <p>Accedi ai servizi di unito.it</p>
            </div>
            <div id="form-container">
                <form id="form" action="post" method="">
                    <input type="text" id="username" name="username" class="form-input" placeholder="username">
                    <input type="password" id="pass" name="pass" class="form-input" placeholder="password">
                    <input type="submit" value="Login" class="form-button">
                </form>
            </div>
            <div id="reg-container" class="text-centered">
                <p>Sei un nuovo utente?</p>
                <a href="registrazione.php">Registrati</a>
            </div>
        </div>
    </div>
    <div id="footer">
        <p>Università degli Studi di Torino - Via Verdi, 8 - 10124 Torino - Progetto realizzato da Luca Nuzzo per l'esame di Tecnologie Web: approcci avanzati</p>
    </div>
    <script type="text/javascript">
        jQuery(document).ready(function(){
            $("#form").on('submit', function(e){
                e.preventDefault();
                var loginData = {
                    'username' : $('input[name=username]').val(),
	                'pass': $('input[name=pass]').val(),
                };
                $.ajax({
                    url: 'api_server/api/login.php',
                    type: 'POST',
                    contentType: 'application/JSON',
                    data: JSON.stringify(loginData)
                }).done(function(){
                    window.location = "homepageStudente.php";
                }).fail(function(response){
                    // se la responseText contiene una substring "Connection error" --> servizio non disponibile
                    if(response.responseText.indexOf("Connection error") != -1){
                        $('#center-container').html("<div id=\"form-title-container\" class=\"text-centered\"><p>Ops! Servizio momentaneamente non disponibile</p><p style=\"font-size: 1.1rem; color: black; font-weight: bold;\"><br/><a href=\"loginPage.php\">Riprova</a> più tardi</p></div>");
                    }else{
                        alert(response.responseJSON.error);
                    };
                    
                });
            });
        });
    </script>
</body>
</html>