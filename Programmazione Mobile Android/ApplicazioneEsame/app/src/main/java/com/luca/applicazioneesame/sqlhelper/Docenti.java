package com.luca.applicazioneesame.sqlhelper;

public class Docenti {
    private int id_docente;
    private String nome_doc;
    private int id_materia;

    // COSTRUTTORI
    public Docenti() {
    }

    public Docenti(int id_docente, String nome_doc, int id_materia) {
        this.id_docente = id_docente;
        this.nome_doc = nome_doc;
        this.id_materia = id_materia;
    }

    // GETTER
    public int getId_docente() {
        return id_docente;
    }

    public String getNome_doc() {
        return nome_doc;
    }

    public int getId_materia() {
        return id_materia;
    }

    // SETTER
    public void setId_docente(int id_docente) {
        this.id_docente = id_docente;
    }

    public void setNome_doc(String nome_doc) {
        this.nome_doc = nome_doc;
    }

    public void setId_materia(int id_materia) {
        this.id_materia = id_materia;
    }
}
