package com.luca.applicazioneesame.sqlhelper;

public class Studenti {
    private int matricola;
    private String username;
    private String nome_st;
    private String email;
    private String pass;

    // COSTRUTTORI
    public Studenti() {
    }

    public Studenti(int matricola, String username, String nome_st, String email, String pass) {
        this.matricola = matricola;
        this.username = username;
        this.nome_st = nome_st;
        this.email = email;
        this.pass = pass;
    }

    // GETTER
    public String getUsername() {
        return username;
    }

    public int getMatricola() {
        return matricola;
    }

    public String getNome_st() {
        return nome_st;
    }

    public String getPass() {
        return pass;
    }

    public String getEmail() {
        return email;
    }


    // SETTER
    public void setUsername(String username) {
        this.username = username;
    }

    public void setMatricola(int matricola) {
        this.matricola = matricola;
    }

    public void setNome_st(String nome_st) {
        this.nome_st = nome_st;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
