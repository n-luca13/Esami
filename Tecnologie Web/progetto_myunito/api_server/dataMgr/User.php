<?php
class User{
    private $conn;
    public $nome;
    public $cognome;
    public $username;
    public $pass;
    public $genere;
    public $data_nascita;
    public $nazione_nascita;
    public $nazione_residenza;
    public $indirizzo_residenza;
    public $cap_residenza;
    public $citta_residenza;
    public $email;
    public $telefono;

    public function __construct($db){
        $this->conn = $db;
    }

    function getAllUsers(){ // funzione mai invocata
        $sql = "SELECT * FROM utenti";
        $stmt = $this->conn->query($sql);
        return $stmt;
    }
    function doesUsernameExist(){ // Verifica che esista un utente con un certo username
        // la funzione restituisce il numero righe del risultato della query
        $query = "SELECT username FROM utenti WHERE utenti.username = ?";
        $stmt = $this->conn->prepare($query);
        $stmt->bindParam(1, $this->username);
        $stmt->execute();
        return $stmt->rowCount(); 
    }
    function doesEmailExist(){
        $query = "SELECT email FROM utenti WHERE utenti.email = ?";
        $stmt = $this->conn->prepare($query);
        $stmt->bindParam(1, $this->email);
        $stmt->execute();
        return $stmt->rowCount();
    }
	function getUser(){ // Modifica le variabili d'istanza con i dati di uno specifico utente e restituisce il recordset
		$query = "SELECT * FROM utenti WHERE utenti.username = ?";
		$stmt = $this->conn->prepare($query);
		$stmt->bindParam(1, $this->username);
		$stmt->execute(); // $stmt = risultato dell'esecuzione della query = recordset con un solo elemento (UTENTE)

		// leggo la prima (e unica) riga del risultato della query (record UTENTE)
		$row = $stmt->fetch(PDO::FETCH_ASSOC); // la funzione fetch restituisce un record ($row), un array le cui chiavi sono i nomi delle colonne della tabella
        if($row){ // se row contiene qualcosa, inserisco i valori nelle variabili d'istanza 
            $this->nome = $row['nome'];
            $this->cognome = $row['cognome'];
            $this->username = $row['username'];
            $this->pass = $row['pass'];
            $this->genere = $row['genere'];
            $this->data_nascita = date("d/m/Y", strtotime($row['data_nascita']));
            $this->nazione_nascita = $row['nazione_nascita'];
            $this->nazione_residenza = $row['nazione_residenza'];
            $this->indirizzo_residenza = $row['indirizzo_residenza'];
            $this->cap_residenza = $row['cap_residenza'];
            $this->citta_residenza = $row['citta_residenza'];
            $this->email = $row['email'];
            $this->telefono = $row['telefono'];
        };
        return $stmt;
    }
    function register(){ // REGISTRAZIONE
		$query = "INSERT INTO utenti SET
            username=:username, pass=:pass, nome=:nome, cognome=:cognome, genere=:genere, data_nascita=:data_nascita, nazione_nascita=:nazione_nascita,
            nazione_residenza=:nazione_residenza, indirizzo_residenza=:indirizzo_residenza, cap_residenza=:cap_residenza, citta_residenza=:citta_residenza,
            email=:email, telefono=:telefono";
		$stmt = $this->conn->prepare($query);
		// sanifico quanto inserito dall'utente
		$this->nome=htmlspecialchars(strip_tags($this->nome));
		$this->cognome=htmlspecialchars(strip_tags($this->cognome));
		$this->username=htmlspecialchars(strip_tags($this->username));
        $this->pass=htmlspecialchars(strip_tags($this->pass));
        $this->genere=htmlspecialchars(strip_tags($this->genere));
        $this->data_nascita=htmlspecialchars(strip_tags($this->data_nascita));
        $this->nazione_nascita=htmlspecialchars(strip_tags($this->nazione_nascita));
        $this->nazione_residenza=htmlspecialchars(strip_tags($this->nazione_residenza));
        $this->indirizzo_residenza=htmlspecialchars(strip_tags($this->indirizzo_residenza));
        $this->cap_residenza=htmlspecialchars(strip_tags($this->cap_residenza));
        $this->citta_residenza=htmlspecialchars(strip_tags($this->citta_residenza));
        $this->email=htmlspecialchars(strip_tags($this->email));
        $this->telefono=htmlspecialchars(strip_tags($this->telefono));

		// invio i valori (di cui alle variabili d'istanza) per i parametri 
		$stmt->bindParam(":nome", $this->nome);
		$stmt->bindParam(":cognome", $this->cognome);
		$stmt->bindParam(":username", $this->username);
        $stmt->bindParam(":pass", $this->pass);
        $stmt->bindParam(":genere", $this->genere);
        $stmt->bindParam(":data_nascita", $this->data_nascita);
        $stmt->bindParam(":nazione_nascita", $this->nazione_nascita);
        $stmt->bindParam(":nazione_residenza", $this->nazione_residenza);
        $stmt->bindParam(":indirizzo_residenza", $this->indirizzo_residenza);
        $stmt->bindParam(":cap_residenza", $this->cap_residenza);
        $stmt->bindParam(":citta_residenza", $this->citta_residenza);
        $stmt->bindParam(":email", $this->email);
        $stmt->bindParam(":telefono", $this->telefono);
 
		// eseguo la query
		$stmt->execute();
		return $stmt; // $stmt = risultato dell'esecuzione della query = recordset con un solo elemento (NUOVO UTENTE)
	}
}
?>