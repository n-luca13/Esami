package com.luca.applicazioneesame.BookingFragments;

import static com.luca.applicazioneesame.BookingFragments.CalendarUtils.daysInWeekArray;
import static com.luca.applicazioneesame.BookingFragments.CalendarUtils.monthYearFromDate;
import static com.luca.applicazioneesame.BookingFragments.CalendarUtils.selectedDate;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.luca.applicazioneesame.BookingFragments.adapters.CalendarAdapter;
import com.luca.applicazioneesame.BookingFragments.adapters.MaterieRecyclerAdapter;
import com.luca.applicazioneesame.BookingViewModel;
import com.luca.applicazioneesame.R;
import com.luca.applicazioneesame.sqlhelper.DatabaseHelper;
import com.luca.applicazioneesame.sqlhelper.Materie;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class FirstFragment extends Fragment implements MaterieRecyclerAdapter.ClickInterface, CalendarAdapter.OnItemListener {
    MaterieRecyclerAdapter materieRecyclerAdapter;
    private RecyclerView recyclerViewSearchMaterie, calendarRecyclerView;
    private ArrayList<Materie> materie;
    private DatabaseHelper db;
    private TextInputEditText edtSearchMateria;
    private TextView txtNoLessons, monthYearText;
    private BookingViewModel viewModel;
    private ArrayList<LocalDate> days;

    public FirstFragment(){
        super(R.layout.booking_first_fragment);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initObjects();
        CalendarUtils.selectedDate = LocalDate.now();

    }

    private void initObjects(){
        materie = new ArrayList<>();
        db = new DatabaseHelper(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.booking_first_fragment, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(BookingViewModel.class);

        // MATERIA
        recyclerViewSearchMaterie = view.findViewById(R.id.recyclerViewSelectInsegnamento);
        txtNoLessons = view.findViewById(R.id.txtNoLessons);
        setMateriaView();
        searchMateria(view);

        // CALENDARIO
        calendarRecyclerView = view.findViewById(R.id.calendarRecyclerView);
        monthYearText = view.findViewById(R.id.monthYearTV);
        setWeekView();
        changeWeek(view);
        viewModel.setGiorno(selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        final Observer<Boolean> observeFlag = isDataSended -> {
            if(isDataSended){
                viewModel.getPosMateria().observe(getViewLifecycleOwner(), posPreviousMateria ->
                        materieRecyclerAdapter.setPosMateriaSelezionata(posPreviousMateria));
            }
        };
        viewModel.getdataSended().observe(getViewLifecycleOwner(), observeFlag);
    }

    private void setMateriaView(){
        materieRecyclerAdapter = new MaterieRecyclerAdapter(materie, getContext(), this);
        recyclerViewSearchMaterie.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewSearchMaterie.setHasFixedSize(true);
        recyclerViewSearchMaterie.setAdapter(materieRecyclerAdapter);
    }

    private void searchMateria(View view){
        Executor executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        edtSearchMateria = view.findViewById(R.id.edtInsegnamento);
        edtSearchMateria.addTextChangedListener(new TextWatcher() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(edtSearchMateria.getText().toString().equals("")){
                    int size = materie.size();
                    materie.clear();
                    materieRecyclerAdapter.notifyItemRangeRemoved(0, size);
                    txtNoLessons.setVisibility(View.GONE);
                }else{
                    String searchMateria = Objects.requireNonNull(edtSearchMateria.getText()).toString().toLowerCase(Locale.getDefault());
                    executor.execute(() -> {
                        materie.clear();
                        materie.addAll(db.searchMaterie(searchMateria));

                        handler.postDelayed(() -> {
                            materieRecyclerAdapter.notifyDataSetChanged();
                            if(materie.size() == 0){
                                txtNoLessons.setVisibility(View.VISIBLE);
                            }else{
                                txtNoLessons.setVisibility(View.GONE);
                            }
                        }, 0);
                    });
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

            }
        });
    }

    public void onItemClick(int positionOfMateria){
        int idClicked = materie.get(positionOfMateria).getId_materia();
        viewModel.setPosMateria(positionOfMateria);
        viewModel.setMateria(idClicked);
        //viewModel.setDataSended(false); //che minchia serve?
    }

    private void setWeekView(){
        monthYearText.setText(monthYearFromDate(CalendarUtils.selectedDate));
        days = daysInWeekArray(CalendarUtils.selectedDate);

        CalendarAdapter calendarAdapter = new CalendarAdapter(days, this);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getContext(), 7);
        calendarRecyclerView.setLayoutManager(layoutManager);
        calendarRecyclerView.setAdapter(calendarAdapter);

    }

    private void changeWeek(View view){
        ImageView prevWeek = view.findViewById(R.id.prevWeek);
        prevWeek.setOnClickListener(view1 -> {
            if(!days.contains(LocalDate.now())) {
                CalendarUtils.selectedDate = CalendarUtils.selectedDate.minusWeeks(1);
                if(CalendarUtils.selectedDate.isBefore(LocalDate.now())){
                    CalendarUtils.selectedDate = LocalDate.now();
                }
                setWeekView();
                viewModel.setGiorno(CalendarUtils.selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }
        });
        ImageView nextWeek = view.findViewById(R.id.nextWeek);
        nextWeek.setOnClickListener(view12 -> {
            CalendarUtils.selectedDate = CalendarUtils.selectedDate.plusWeeks(1);
            setWeekView();
            viewModel.setGiorno(CalendarUtils.selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        });
    }

    @Override
    public void onItemClick(int position, LocalDate date)
    {
        if(date.isEqual(LocalDate.now()) || date.isAfter(LocalDate.now())) {
            CalendarUtils.selectedDate = date;
        }else{
            CalendarUtils.selectedDate = LocalDate.now();
        }
        setWeekView();
        viewModel.setGiorno(CalendarUtils.selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

    }
}
