package com.luca.applicazioneesame.sqlhelper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Lezioni {
    private int id_lezione;
    private int matricola_studente;
    private int id_docente;
    private String stato_lezione;
    private String data;
    private int fromTime;
    private int toTime;

    // COSTRUTTORI
    public Lezioni() {
    }

    public Lezioni(int id_lezione, int matricola_studente, int id_docente, String stato_lezione, String data) {
        this.id_lezione = id_lezione;
        this.matricola_studente = matricola_studente;
        this.id_docente = id_docente;
        this.stato_lezione = stato_lezione;
        this.data = data;
        this.fromTime = getParsedData().getHour();
        this.toTime = getParsedData().plusHours(2).getHour();
    }

    // GETTER
    public int getId_lezione() {
        return id_lezione;
    }

    public int getMatricola_studente() {
        return matricola_studente;
    }

    public int getId_docente() {
        return id_docente;
    }

    public String getStato_lezione() {
        return stato_lezione;
    }

    public String getData() {
        return data;
    }

    public int getToTime() {
        return toTime;
    }

    public int getFromTime() {
        return fromTime;
    }

    // SETTER
    public void setId_lezione(int id_lezione) {
        this.id_lezione = id_lezione;
    }

    public void setMatricola_studente(int matricola_studente) {
        this.matricola_studente = matricola_studente;
    }

    public void setId_docente(int id_docente) {
        this.id_docente = id_docente;
    }

    public void setStato_lezione(String stato_lezione) {
        this.stato_lezione = stato_lezione;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setToTime(int toTime) {
        this.toTime = toTime;
    }

    public void setFromTime(int fromTime) {
        this.fromTime = fromTime;
    }



    public LocalDateTime getParsedData(){
        return LocalDateTime.parse(this.data, formatter);
    }

    private static final DateTimeFormatter formatter
            = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
}
