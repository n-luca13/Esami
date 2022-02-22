<?php
    class Teaching{
        private $conn;

        public $codice;
        public $cod_corso;
        private $nome;
        private $crediti;
        
        public function __construct($db){
            $this->conn = $db;
        }

        function getAllTeachings(){ // funzione mai invocata
            $sql = "SELECT * FROM materie";
            $result = $this->conn->query($sql);
            return $result;
        }
        function getTeachingsByCorso(){ // funzione mai invocata
            $query = "SELECT * FROM materie WHERE cod_corso = ?";
            $stmt = $this->conn->prepare($query); 
            $stmt->bindParam(1, $this->cod_corso);
            $stmt->execute();
            return $stmt;
        }
        function getTeaching(){ // funzione mai invocata
            $query = "SELECT * FROM materie WHERE codice_materia = ?";
            $stmt = $this->conn->prepare($query);
            $stmt->bindParam(1, $this->codice);
            $stmt->execute(); // $stmt = risultato dell'esecuzione della query = recordset con un solo elemento (MATERIA)
            // leggo la prima (e unica) riga del risultato della query (record CORSO DI LAUREA)
            $row = $stmt->fetch(PDO::FETCH_ASSOC); 
            // la funzione fetch restituisce un record ($row), un array le cui chiavi sono i nomi delle colonne della tabella 
            if($row){ // solo se row contiene qualcosa, inserisco i suoi valori nelle variabili d'istanza
                $this->codice = $row['codice_materia'];
                $this->nomea = $row['nome_materia'];
                $this->crediti = $row['crediti'];
                $this->cod_corso = $row['cod_corso'];
            };
            // la funzione, oltre a modificare l'istanza dell'oggetto su cui è invocata, restituisce il numero di righe del recordset
            return $stmt->rowCount();
        }
        function searchTeachings($keywords, $matricola_studente){
            // La funzione restituisce un recordset contenente tutte le materie che corrispondono alla ricerca dell'utente 
            // (SELECT:corso di laurea | INPUT:keywords insegnamento)

            // Se l'utente ha sia selezionato un corso di laura che digitato delle parole chiave (codice materia o nome materia)
            if(($this->cod_corso != null) && ($keywords != "")){
                $query = "SELECT * FROM materie WHERE (cod_corso = ?) AND (materie.codice_materia = ? OR materie.nome_materia LIKE ?) ORDER BY materie.nome_materia ASC";
                $stmt = $this->conn->prepare($query);
                $keywords = htmlspecialchars(strip_tags($keywords));
                $keywords = "%{$keywords}%"; // % --> ingnorare ciò che precede o segue la stringa ricercata
                $stmt->bindParam(1, $this->cod_corso);
                $stmt->bindParam(2, $keywords);
                $stmt->bindParam(3, $keywords);
            }elseif($this->cod_corso != null){ // Se l'utente ha solo selezionato un corso di laurea...
                $queryCorso = "SELECT * FROM materie WHERE cod_corso = ? ORDER BY materie.nome_materia ASC";
                $stmt = $this->conn->prepare($queryCorso);
                $stmt->bindParam(1, $this->cod_corso);
            }elseif($keywords != ""){ // Se lutente ha solo digitato parole chiave (nome materia o codice materia)
                // Volendo ottenere tutti i record contenenti anche solo una parola fra quelle cercate...
                $keywords = htmlspecialchars(strip_tags($keywords));
                $stringa = "%{$keywords}%";
                $queryKeywords = "SELECT * FROM materie WHERE (materie.nome_materia LIKE '".$stringa."' OR materie.codice_materia LIKE '".$stringa."') OR (";
                // .. creo un array contenente tutte le parole digitate dall'utente 
                $arrayWords = array_filter(explode(" ", $keywords));
                foreach($arrayWords as $key => $word){ // per ogni parola cercata...
                    $key++;
                    if($key>1){
                        $queryKeywords .= " OR"; // dopo la prima parola cercata, devo sempre aggiungere 'OR' alla query
                    };
                    $word = "%{$word}%"; // ricerco la singola parola, ignorando gli estremi della stringa
                    $queryKeywords .= " materie.nome_materia LIKE '".$word."'";
                };
                $queryKeywords .= ") ORDER BY nome_materia LIKE '".$stringa."' DESC";
                $stmt = $this->conn->prepare($queryKeywords);
            }else{
                return false;
            }
            $stmt->execute();
            return $stmt;
        }
    };
?>