<?php
    class Round{
        private $conn;
        public $id_appello;
        public $cod_materia;
        private $data_appello;
        private $prenotazione;

        public function __construct($db){
            $this->conn = $db;
        }

        function getRound(){ // funzione mai invocata
            $query = "SELECT * FROM appelli WHERE id_appello = ?";
            $stmt = $this->conn->prepare($query);
            $stmt->bindParam(1, $this->id_appello);
            $stmt->execute();
            return $stmt;
        }

        function getAllRounds(){ // funzione mai invocata
            $sql = "SELECT * FROM appelli";
            $result = $this->conn->query($sql);
            return $result;
        }

        function getRoundsByTeaching(){ 
            // Restituisce un recordset contenente gli appelli (id_appello, data_appello, cod_materia) corrispondenti a una materia
            $query = "SELECT * FROM appelli WHERE cod_materia = ?";
            $stmt = $this->conn->prepare($query);
            $stmt->bindParam(1, $this->cod_materia);
            $stmt->execute();
            return $stmt;
        }

        function getPrenotazione($matricola){ 
            // Verifica l'esistenza di una prenotazione da parte di una matricola per uno specifico appello
            // Restituisce TRUE/FALSE e modifica il valore della variabile d'istanza 'prenotazione'
            $query = "SELECT COUNT(*) FROM prenotazioni WHERE matricola_studente = ? AND appello_id = ?";
            $stmt = $this->conn->prepare($query);
            $matricola = htmlspecialchars(strip_tags($matricola));
            $stmt->bindParam(1, $matricola);
            $stmt->bindParam(2, $this->id_appello);
            $stmt->execute();
            if($stmt->fetchColumn()>0){ // Se la query restituisce almeno un record...
                $this->prenotazione = true;
                return true;
            }else{
                $this->prenotazione = false;
                return false;
            };
        }

        function createPrenotazione($matricola){
            // Inserisce la prenotazione ad un appello da parte di una matricola
            // Restituisce il risultato della query, interpretato come TRUE/FALSE
            $query = "INSERT INTO prenotazioni VALUES (?, ?)";
            $stmt = $this->conn->prepare($query);
            $matricola = htmlspecialchars(strip_tags($matricola));
            $stmt->bindParam(1, $matricola); // parametro 'matricola'
            $stmt->bindParam(2, $this->id_appello); // variabile d'istanza 'id_appello'
            $stmt->execute();
            return $stmt;
        }

        function deletePrenotazione($matricola){
            // Cancella la prenotazione ad un appello da parte di una matricola
            // Restituisce il risultato della query, interpretato come TRUE/FALSE
            $query = "DELETE FROM prenotazioni WHERE matricola_studente = ? AND appello_id = ?";
            $stmt = $this->conn->prepare($query);
            $matricola = htmlspecialchars(strip_tags($matricola));
            $stmt->bindParam(1, $matricola);
            $stmt->bindParam(2, $this->id_appello);
            $stmt->execute();
            return $stmt;
        }

    }
?>