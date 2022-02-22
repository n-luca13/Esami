package com.luca.applicazioneesame;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.luca.applicazioneesame.BookingFragments.FirstFragment;
import com.luca.applicazioneesame.BookingFragments.SecondFragment;
import com.luca.applicazioneesame.BookingFragments.ThirdFragment;
import com.luca.applicazioneesame.sqlhelper.DatabaseHelper;

public class BookingActivity extends AppCompatActivity {
    private DatabaseHelper db;
    private BookingViewModel viewModel;
    private Button btnNext;
    public BookingActivity(){
        super(R.layout.activity_booking);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new DatabaseHelper(getApplicationContext());
        viewModel = new ViewModelProvider(this).get(BookingViewModel.class);
        viewModel.getPrenotazione().getValue().setMatricola_studente(getIntent().getIntExtra("matricola",0));

        if(savedInstanceState == null){
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.fragmentContainerView, FirstFragment.class, null)
                    .commit();
        }

        btnNext = findViewById(R.id.btnNext);
        btnNext.setOnClickListener(view -> {
            if(getSupportFragmentManager().getBackStackEntryCount() == 0) {
                viewModel.getMateria().observe(this, item -> {
                    if (item.getId_materia() != 0 && !viewModel.getGiorno().getValue().equals("")) {
                        viewModel.setDataSended(false);
                        getSupportFragmentManager().beginTransaction()
                                .setReorderingAllowed(true)
                                .addToBackStack(null)
                                .replace(R.id.fragmentContainerView, SecondFragment.class, null)
                                .commit();
                    }
                });
            }
            if(getSupportFragmentManager().getBackStackEntryCount() == 1){
                viewModel.getPrenotazione().observe(this, finalItem -> {
                    if(finalItem.getData() != null){
                        viewModel.setDataSended(false);
                        getSupportFragmentManager().beginTransaction()
                                .setReorderingAllowed(true)
                                .addToBackStack(null)
                                .replace(R.id.fragmentContainerView, ThirdFragment.class, null)
                                .commit();
                        btnNext.setText(R.string.conferma);
                    }
                });
            }
            if(getSupportFragmentManager().getBackStackEntryCount() > 1){
                viewModel.prenotazione.getValue().setStato_lezione("prenotata");
                try {
                    if(db.insertLezione(viewModel.prenotazione.getValue())) {
                        Toast.makeText(BookingActivity.this, "Prenotazione effettuata", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(BookingActivity.this, "SERVIZIO MOMENTANEAMENTE NON DISPONIBILE", Toast.LENGTH_LONG).show();
                }
                Intent toMainActivity = new Intent(BookingActivity.this, MainActivity.class);
                toMainActivity.putExtra("matricola", viewModel.getPrenotazione().getValue().getMatricola_studente());
                startActivity(toMainActivity);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().getBackStackEntryCount() > 0){
            viewModel.setDataSended(true);
            btnNext.setText(getString(R.string.avanti));
            getSupportFragmentManager().popBackStack();
        }else{
            super.onBackPressed();
        }
    }

}