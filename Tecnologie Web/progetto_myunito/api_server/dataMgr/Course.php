<?php
class Course{
    private $conn;
    public $codice_corso;
    private $nome_corso;
    private $tipologia;

    public function __construct($db){
        $this->conn = $db;
    }

	function getAllCourses(){ // Restituisce un recordset contenente tutti i tutti i corsi di laurea
		$sql = "SELECT * FROM corsi_laurea";
		$result = $this->conn->query($sql);
		return $result;
	}
	
	function getCorso(){
		$query = "SELECT * FROM corsi_laurea WHERE corsi_laurea.codice_corso = ?";
		// preparo la query
		$stmt = $this->conn->prepare($query);
		// invio il valore per il parametro
		$stmt->bindParam(1, $this->codice_corso);
		// eseguo la query
		$stmt->execute(); // $stmt = risultato dell'esecuzione della query = recordset con un solo elemento (CORSO DI LAUREA)

		// leggo la prima (e unica) riga del risultato della query (record CORSO DI LAUREA)
		$row = $stmt->fetch(PDO::FETCH_ASSOC); 
		// la funzione fetch restituisce un record ($row), un array le cui chiavi sono i nomi delle colonne della tabella 
		if($row){ // solo se row contiene qualcosa, inserisco i suoi valori nelle variabili d'istanza
			$this->codice_corso = $row['codice_corso'];
			$this->nome_corso = $row['nome_corso'];
			$this->tipologia = $row['tipologia'];
		};
		// la funzione, oltre a modificare l'istanza dell'oggetto su cui è invocata, restituisce il record del corso di laurea
		return $row; 
    }
}
?>