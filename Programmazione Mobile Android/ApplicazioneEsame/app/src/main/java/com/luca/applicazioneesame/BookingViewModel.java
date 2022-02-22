package com.luca.applicazioneesame;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.luca.applicazioneesame.sqlhelper.Lezioni;
import com.luca.applicazioneesame.sqlhelper.Materie;

public class BookingViewModel extends ViewModel {
    MutableLiveData<Lezioni> prenotazione = new MutableLiveData<>();
    MutableLiveData<Materie> materia = new MutableLiveData<>();
    MutableLiveData<Boolean> dataSended = new MutableLiveData<>(false);
    MutableLiveData<Integer> posMateria =  new MutableLiveData<>(-1);
    MutableLiveData<Integer> posDocente =  new MutableLiveData<>(-1);
    MutableLiveData<String> giorno = new MutableLiveData<>("");
    MutableLiveData<String> nomeDocente = new MutableLiveData<>();

    public BookingViewModel() {
        Lezioni prenotazione = new Lezioni();
        Materie materia = new Materie();
        this.prenotazione.setValue(prenotazione);
        this.materia.setValue(materia);
    }


    public LiveData<Lezioni> getPrenotazione(){
        return prenotazione;
    }

    public LiveData<Materie> getMateria(){
        return materia;
    }

    public LiveData<Integer> getPosMateria(){
        return posMateria;
    }

    public LiveData<Integer> getPosDocente(){
        return posDocente;
    }

    public LiveData<Boolean> getdataSended(){
        return dataSended;
    }

    public LiveData<String> getNomeDocente(){
        return nomeDocente;
    }

    public LiveData<String> getGiorno(){
        return giorno;
    }

    public void setMateria(int idMateria){
        materia.getValue().setId_materia(idMateria);
    }

    public void setDocente(int idDocente){
        prenotazione.getValue().setId_docente(idDocente);
    }

    public void setNomeDocente(String nome){
        nomeDocente.setValue(nome);
    }

    public void setGiorno(String dayString){
        giorno.setValue(dayString);
    }

    public void setOrario(int ora){
        prenotazione.getValue().setFromTime(ora);
        prenotazione.getValue().setToTime(ora+2);
        setFullDate();
    }

    public void setFullDate(){
        String stringTime = String.valueOf(this.prenotazione.getValue().getFromTime());
        if(stringTime.equals("8")){
            stringTime = "0"+stringTime;
        }
        prenotazione.getValue().setData(giorno.getValue()+" "+stringTime+":00");
    }

    public void setPosMateria(int pos){
        posMateria.setValue(pos);
    }

    public void setPosDocente(int pos){
        posDocente.setValue(pos);
    }

    public void setDataSended(boolean isDataSended){
        dataSended.setValue(isDataSended);
    }



}

