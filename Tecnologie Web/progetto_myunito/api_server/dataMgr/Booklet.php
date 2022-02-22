<?php
    class Booklet{
        private $conn;
        public $matricola_studente;
        public $materie_studente;

        public function __construct($db, $matricola){ 
            // il costruttore dell'oggetto Booklet (LIBRETTO) riceve come parametro (oltre all'istanza di DB) la matricola studente
            $this->conn = $db;
            $this->matricola_studente = $matricola;
            $this->materie_studente = array();
        }

        function getBooklet(){ 
            // Restituisce SOLO i campi della tabella libretti (matricola, codice materia, voto materia, scelta studente) corrispondenti alla matricola X
            $query = "SELECT * FROM libretti WHERE matricola_studente = ?";
            $stmt = $this->conn->prepare($query);
            $this->matricola_studente=htmlspecialchars(strip_tags($this->matricola_studente));
            $stmt->bindParam(1, $this->matricola_studente);
            $stmt->execute();
            return $stmt;
        }
        function getBookletExt(){ // Restituisce TUTTE LE INFO sulle MATERIE facenti parte del LIBRETTO. In particolare:
            // 1) MODIFICA L'ISTANZA, popolando la variabile d'istanza 'materie_studente' con le materie di cui al libretto corrispondente alla matricola
            // 2) RESTITUISCE il risultato dell'esecuzione della prima query --> sarà interpretato come TRUE o FALSE | libretto TROVATO o NON trovato
            $query = "SELECT * FROM libretti WHERE matricola_studente = ?";
            $stmt = $this->conn->prepare($query);
            $this->matricola_studente=htmlspecialchars(strip_tags($this->matricola_studente));
            $stmt->bindParam(1, $this->matricola_studente);
            $stmt->execute();
            // per ogni record del recordset (cioè, per ogni materia del libretto)
            while($row = $stmt->fetch(PDO::FETCH_ASSOC)){
                $voto = $row['voto_materia'];
                $scelta_studente = $row['scelta_studente'];
                $cod_materia = $row['cod_materia'];
                // Seconda query per ottenere nome e crediti della materia
                $query_materie = "SELECT nome_materia, crediti FROM materie WHERE codice_materia = ?";
                $stmt_materie = $this->conn->prepare($query_materie);
                $stmt_materie->bindParam(1, $cod_materia);
                $stmt_materie->execute();
                // per ogni riga (sebbene solo una) del recordset 
                while($row_materia = $stmt_materie->fetch(PDO::FETCH_ASSOC)){
                    $nome_materia = $row_materia['nome_materia'];
                    $crediti_materia = $row_materia['crediti'];
                };
                // MODIFICO L'ISTANZA materie_studente: array multidimensionale
                $this->materie_studente[] = array("materia" => array("cod_materia" => $cod_materia, "nome_materia" => $nome_materia, "voto_materia" => $voto, "crediti_materia" => $crediti_materia, "scelta_studente" => $scelta_studente));
            };
            return $stmt; // risultato prima query
        }

        function createBooklet($cod_corso){
            // Crea un nuovo libretto per un utente appena immatricolato
            // Seleziona e inserisce nel libretto tutte le materie appartenenti al corso di laurea, eccetto quelle a scelta
            // Non modifica l'istanza, bensì restituisce TRUE o FALSE
            $sqlMaterie = "SELECT codice_materia FROM materie WHERE cod_corso = ? AND opzionale = FALSE";
            $stmtMaterie = $this->conn->prepare($sqlMaterie);
            $cod_corso = htmlspecialchars(strip_tags($cod_corso));
            $stmtMaterie->bindParam(1, $cod_corso);
            $stmtMaterie->execute();
            if($stmtMaterie){
                while($row = $stmtMaterie->fetch(PDO::FETCH_ASSOC)){
                    $sqlLibretto = "INSERT INTO libretti (matricola_studente, cod_materia) VALUES (?, ?)";
                    $stmtLibretto = $this->conn->prepare($sqlLibretto);
                    $stmtLibretto->bindParam(1, $this->matricola_studente);
                    $stmtLibretto->bindParam(2, $row['codice_materia']);
                    $stmtLibretto->execute();
                };
                return true;
            }else{
                return false;
            };
        }

        function addToBooklet($cod_materia){ // AGGIUNGERE MATERIA (a scelta) al libretto
            // La funzione riceve come parametro il codice della materia
            // Conto le materie a scelta già presenti nel libretto
            $sql = "SELECT COUNT(scelta_studente) FROM libretti WHERE matricola_studente = ? AND scelta_studente = TRUE";
            $stmtCount = $this->conn->prepare($sql);
            $this->matricola_studente=htmlspecialchars(strip_tags($this->matricola_studente));
            $stmtCount->bindParam(1, $this->matricola_studente);
            $stmtCount->execute();
            // Se le materie a scelta già presenti sono minori di 2...
            if($stmtCount->fetchColumn()<2){
                // inserisco la nuova materia a scelta nel libretto
                $query = "INSERT INTO libretti (matricola_studente, cod_materia, scelta_studente) VALUES (?, ?, TRUE)";
                $stmt = $this->conn->prepare($query);
                $cod_materia = htmlspecialchars(strip_tags($cod_materia));
                $stmt->bindParam(1, $this->matricola_studente);
                $stmt->bindParam(2, $cod_materia);
                $stmt->execute();
                return $stmt; // restituisco il risultato della query
            }else{ // Se ci sono già 2 materie restituisco FALSE
                return false;
            };
        }
        function removeFromBooklet($cod_materia){ // RIMUOVERE MATERIA (a scelta) dal libretto
            $query = "DELETE FROM libretti WHERE matricola_studente = ? AND cod_materia = ? AND scelta_studente = TRUE";
            $stmt = $this->conn->prepare($query);
            $cod_materia = htmlspecialchars(strip_tags($cod_materia));
            $stmt->bindParam(1, $this->matricola_studente);
            $stmt->bindParam(2, $cod_materia);
            $stmt->execute();
            return $stmt;
        }
    }
?>