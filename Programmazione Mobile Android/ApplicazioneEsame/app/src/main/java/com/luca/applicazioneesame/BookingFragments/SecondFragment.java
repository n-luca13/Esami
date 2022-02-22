package com.luca.applicazioneesame.BookingFragments;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.luca.applicazioneesame.BookingFragments.adapters.DocentiRecyclerAdapter;
import com.luca.applicazioneesame.BookingViewModel;
import com.luca.applicazioneesame.R;
import com.luca.applicazioneesame.sqlhelper.DatabaseHelper;
import com.luca.applicazioneesame.sqlhelper.Docenti;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SecondFragment extends Fragment implements DocentiRecyclerAdapter.ClickInterface, View.OnClickListener {
    private DocentiRecyclerAdapter docentiRecyclerAdapter;
    private ArrayList<Docenti> docenti;
    private DatabaseHelper db;
    private BookingViewModel viewModel;
    private ArrayList<String> notAvailableTimes;
    TextView btn8_10, btn10_12, btn12_14, btn14_16, btn16_18, btn18_20;

    public SecondFragment(){
        super(R.layout.booking_second_fragment);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initObjects();
    }

    private void initObjects(){
        docenti = new ArrayList<>();
        db = new DatabaseHelper(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireActivity()).get(BookingViewModel.class);
        View view = inflater.inflate(R.layout.booking_second_fragment, container, false);
        docentiRecyclerAdapter = new DocentiRecyclerAdapter(docenti, getContext(), this);

        RecyclerView recyclerViewSelectDocente = view.findViewById(R.id.recyclerViewSelectDocente);
        recyclerViewSelectDocente.setLayoutManager(new LinearLayoutManager(view.getContext()));
        recyclerViewSelectDocente.setHasFixedSize(true);
        recyclerViewSelectDocente.setAdapter(docentiRecyclerAdapter);

        initOrarioViews(view);

        if(viewModel.getdataSended().getValue()){
            docentiRecyclerAdapter.setPosDocenteSelezionato(viewModel.getPosDocente().getValue());
            onItemClick(viewModel.getPosDocente().getValue());
            viewModel.getPrenotazione().getValue().setData(null);
        }else{
            getDocentiFromSQLite();
        }
        return view;
    }

    private void initOrarioViews(View view){
        btn8_10 = view.findViewById(R.id.btn8_10);
        btn8_10.setOnClickListener(null);
        btn10_12 = view.findViewById(R.id.btn10_12);
        btn10_12.setOnClickListener(null);
        btn12_14 = view.findViewById(R.id.btn12_14);
        btn12_14.setOnClickListener(null);
        btn14_16 = view.findViewById(R.id.btn14_16);
        btn14_16.setOnClickListener(null);
        btn16_18 = view.findViewById(R.id.btn16_18);
        btn16_18.setOnClickListener(null);
        btn18_20 = view.findViewById(R.id.btn18_20);
        btn18_20.setOnClickListener(null);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getDocentiFromSQLite(){
        Executor executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(()->{
            docenti.clear();
            docenti.addAll(db.getDocentiFromMateria(viewModel.getMateria().getValue().getId_materia()));
            handler.post(() -> docentiRecyclerAdapter.notifyDataSetChanged());
        });
    }

    public void onItemClick(int positionOfDocente) {
        int idClicked;
        if(viewModel.getdataSended().getValue()){
            idClicked = viewModel.getPrenotazione().getValue().getId_docente();
            viewModel.setDataSended(false);
        }else{
            idClicked = docenti.get(positionOfDocente).getId_docente();
            viewModel.setPosDocente(positionOfDocente);
        }
        notAvailableTimes = db.getNotAvailableTimes(idClicked, viewModel.getGiorno().getValue());
        viewModel.setDocente(idClicked);
        restoreTextViews(notAvailableTimes);
    }

    @SuppressLint("NonConstantResourceId")
    public void onClick(View view){
        restoreTextViews(notAvailableTimes);
        switch (view.getId()){
            case R.id.btn8_10:
                viewModel.setOrario(8);
                btn8_10.setTextColor(Color.BLACK);
                btn8_10.setBackgroundResource(R.color.danger);
                btn8_10.setElevation(5);
                break;
            case R.id.btn10_12:
                viewModel.setOrario(10);
                btn10_12.setTextColor(Color.BLACK);
                btn10_12.setBackgroundResource(R.color.danger);
                btn10_12.setElevation(5);
                break;
            case R.id.btn12_14:
                viewModel.setOrario(12);
                btn12_14.setTextColor(Color.BLACK);
                btn12_14.setBackgroundResource(R.color.danger);
                btn12_14.setElevation(5);
                break;
            case R.id.btn14_16:
                viewModel.setOrario(14);
                btn14_16.setTextColor(Color.BLACK);
                btn14_16.setBackgroundResource(R.color.danger);
                btn14_16.setElevation(5);
                break;
            case R.id.btn16_18:
                viewModel.setOrario(16);
                btn16_18.setTextColor(Color.BLACK);
                btn16_18.setBackgroundResource(R.color.danger);
                btn16_18.setElevation(5);
                break;
            case R.id.btn18_20:
                viewModel.setOrario(18);
                btn18_20.setTextColor(Color.BLACK);
                btn18_20.setBackgroundResource(R.color.danger);
                btn18_20.setElevation(5);
                break;
        }
    }

    public boolean checkAvailableTimes(ArrayList<String> notAvailableTimes, String btnTag){
        return notAvailableTimes.stream().noneMatch(s -> s.contains(btnTag));
    }

    private void restoreTextViews(ArrayList<String> notAvailableTimes){
        if(checkAvailableTimes(notAvailableTimes, String.valueOf(btn8_10.getTag()))){
            btn8_10.setOnClickListener(this);
            btn8_10.setTextColor(ContextCompat.getColor(getContext(), R.color.color_text));
            btn8_10.setBackgroundResource(R.color.color_background);
            btn8_10.setElevation(5);
        }else{
            btn8_10.setOnClickListener(null);
            btn8_10.setTextColor(ContextCompat.getColor(getContext(), R.color.gray_dark));
            btn8_10.setBackgroundResource(R.color.gray_dark);
            btn8_10.setElevation(0);
        }
        if(checkAvailableTimes(notAvailableTimes, String.valueOf(btn10_12.getTag()))){
            btn10_12.setOnClickListener(this);
            btn10_12.setTextColor(ContextCompat.getColor(getContext(), R.color.color_text));
            btn10_12.setBackgroundResource(R.color.color_background);
            btn10_12.setElevation(5);
        }else{
            btn10_12.setOnClickListener(null);
            btn10_12.setTextColor(ContextCompat.getColor(getContext(), R.color.gray_dark));
            btn10_12.setBackgroundResource(R.color.gray_dark);
            btn10_12.setElevation(0);
        }
        if(checkAvailableTimes(notAvailableTimes, String.valueOf(btn12_14.getTag()))){
            btn12_14.setOnClickListener(this);
            btn12_14.setTextColor(ContextCompat.getColor(getContext(), R.color.color_text));
            btn12_14.setBackgroundResource(R.color.color_background);
            btn12_14.setElevation(5);
        }else{
            btn12_14.setOnClickListener(null);
            btn12_14.setTextColor(ContextCompat.getColor(getContext(), R.color.gray_dark));
            btn12_14.setBackgroundResource(R.color.gray_dark);
            btn12_14.setElevation(0);
        }
        if(checkAvailableTimes(notAvailableTimes, String.valueOf(btn14_16.getTag()))){
            btn14_16.setOnClickListener(this);
            btn14_16.setTextColor(ContextCompat.getColor(getContext(), R.color.color_text));
            btn14_16.setBackgroundResource(R.color.color_background);
            btn14_16.setElevation(5);
        }else{
            btn14_16.setOnClickListener(null);
            btn14_16.setTextColor(ContextCompat.getColor(getContext(), R.color.gray_dark));
            btn14_16.setBackgroundResource(R.color.gray_dark);
            btn14_16.setElevation(0);
        }
        if(checkAvailableTimes(notAvailableTimes, String.valueOf(btn16_18.getTag()))){
            btn16_18.setOnClickListener(this);
            btn16_18.setTextColor(ContextCompat.getColor(getContext(), R.color.color_text));
            btn16_18.setBackgroundResource(R.color.color_background);
            btn16_18.setElevation(5);
        }else{
            btn16_18.setOnClickListener(null);
            btn16_18.setTextColor(ContextCompat.getColor(getContext(), R.color.gray_dark));
            btn16_18.setBackgroundResource(R.color.gray_dark);
            btn16_18.setElevation(0);
        }
        if(checkAvailableTimes(notAvailableTimes, String.valueOf(btn18_20.getTag()))){
            btn18_20.setOnClickListener(this);
            btn18_20.setTextColor(ContextCompat.getColor(getContext(), R.color.color_text));
            btn18_20.setBackgroundResource(R.color.color_background);
            btn18_20.setElevation(5);
        }else{
            btn18_20.setOnClickListener(null);
            btn18_20.setTextColor(ContextCompat.getColor(getContext(), R.color.gray_dark));
            btn18_20.setBackgroundResource(R.color.gray_dark);
            btn18_20.setElevation(0);
        }
    }
}
