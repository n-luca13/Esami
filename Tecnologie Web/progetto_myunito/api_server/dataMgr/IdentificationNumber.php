<?php
class IdentificationNumber{
    private $conn;
    public $matricola;
    public $username_studente;
    public $cod_corso;

    public function __construct($db){
        $this->conn = $db;
    }

	function getAllMatricole(){ // funzione mai invocata
		$sql = "SELECT * FROM matricola";
		$result = $this->conn->query($sql);
		return $sql;
	}
	
	function getMatricola(){ // funzione mai invocata
		$query = "SELECT * FROM matricole WHERE matricole.matricola = ?";
		$stmt = $this->conn->prepare($query);
		$stmt->bindParam(1, $this->matricola);
		$stmt->execute(); 
		$row = $stmt->fetch(PDO::FETCH_ASSOC); 
		$this->matricola = $row['matricola'];
		$this->username_studente = $row['username_studente'];
		$this->cod_corso = $row['cod_corso'];
	}

	function getMatricolaFromUsername(){ // Restituisce la matricola con username X
		$query = "SELECT * FROM matricole WHERE matricole.username_studente = ?";
		// preparo la query
		$stmt = $this->conn->prepare($query);
		// invio il valore per il parametro
		$stmt->bindParam(1, $this->username_studente);
		// eseguo la query
		$stmt->execute(); // $stmt = risultato dell'esecuzione della query = recordset con un solo elemento (MATRICOLA)
		// leggo la prima (e unica) riga del risultato della query (record matricola)
		// la funzione fetch restituisce un record ($row), un array le cui chiavi sono i nomi delle colonne della tabella 
		$row = $stmt->fetch(PDO::FETCH_ASSOC);
		if($row){ // solo se row contiene qualcosa, inserisco i suoi valori nelle variabili d'istanza
			$this->matricola = $row['matricola'];
			$this->username_studente = $row['username_studente'];
			$this->cod_corso = $row['cod_corso'];
		};
		return $stmt->rowCount(); // La funzione, in ogni caso, restituisce il numero di record risultanti dalla query
	}

	function createMatricola(){ // IMMATRICOLAZIONE
		// La funzione restituisce il risultato della query (interpretato come TRUE/FALSE)
		$query = "INSERT INTO matricole (username_studente, cod_corso) VALUES (?, ?)";
		$stmt = $this->conn->prepare($query);
		$this->username_studente=htmlspecialchars(strip_tags($this->username_studente));
		$this->cod_corso=htmlspecialchars(strip_tags($this->cod_corso));
		$stmt->bindParam(1, $this->username_studente);
		$stmt->bindParam(2, $this->cod_corso);
		$stmt->execute();
		return $stmt;	
	}
}
?>