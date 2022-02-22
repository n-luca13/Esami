package com.luca.applicazioneesame.sqlhelper;

public class Materie {
    private int id_materia;
    private String nome_materia;

    // COSTRUTTORI
    public Materie() {
    }

    public Materie(int id_materia, String nome_materia) {
        this.id_materia = id_materia;
        this.nome_materia = nome_materia;
    }

    // GETTER
    public int getId_materia() {
        return id_materia;
    }

    public String getNome_materia() {
        return nome_materia;
    }

    // SETTER
    public void setId_materia(int id_materia) {
        this.id_materia = id_materia;
    }

    public void setNome_materia(String nome_materia) {
        this.nome_materia = nome_materia;
    }

}
