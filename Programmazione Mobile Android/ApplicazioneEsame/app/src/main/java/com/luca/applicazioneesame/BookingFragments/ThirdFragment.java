package com.luca.applicazioneesame.BookingFragments;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.luca.applicazioneesame.BookingViewModel;
import com.luca.applicazioneesame.R;
import com.luca.applicazioneesame.sqlhelper.DatabaseHelper;

public class ThirdFragment extends Fragment {
    private DatabaseHelper db;
    private BookingViewModel viewModel;
    private TextView txtGiorno, txtOra, txtMateria, txtDocente;
    private CardView cardViewWarning;


    public ThirdFragment() {
        super(R.layout.booking_third_fragment);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new DatabaseHelper(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireActivity()).get(BookingViewModel.class);
        View view = inflater.inflate(R.layout.booking_third_fragment, container, false);

        initViews(view);
        getResourcesFromDB();

        txtMateria.setText(viewModel.getMateria().getValue().getNome_materia());
        txtDocente.setText(viewModel.getNomeDocente().getValue());
        txtGiorno.setText(viewModel.getGiorno().getValue());
        txtOra.setText(String.format(getString(R.string.from_to_time),
                viewModel.getPrenotazione().getValue().getFromTime(),
                viewModel.getPrenotazione().getValue().getToTime())
        );

        return view;
    }

    private void initViews(View view) {
        ConstraintLayout lessonContainer = view.findViewById(R.id.lessonContainer);
        txtGiorno = view.findViewById(R.id.txtGiorno);
        txtOra = view.findViewById(R.id.txtOra);
        txtMateria = view.findViewById(R.id.txtMateria);
        txtDocente = view.findViewById(R.id.txtDocente);
        cardViewWarning = view.findViewById(R.id.cardViewWarning);

        GradientDrawable drawable = (GradientDrawable) lessonContainer.getBackground();
        drawable.setStroke(4, ContextCompat.getColor(getContext(),R.color.danger));
    }

    private void getResourcesFromDB(){
        // Imposta nome materia
        viewModel.getMateria().getValue().setNome_materia(
                db.getMateriaFromDB(
                        viewModel.getMateria().getValue().getId_materia()
                ).getNome_materia());
        // Imposta nome docente
        viewModel.setNomeDocente(
                db.getDocenteFromDB(
                        viewModel.getPrenotazione().getValue().getId_docente()
                ).getNome_doc()
        );
        // Verifica disponibilità studente
        if(db.checkUserAvailability(
                viewModel.getPrenotazione().getValue().getMatricola_studente(),
                viewModel.getPrenotazione().getValue().getData())){
            cardViewWarning.setVisibility(View.VISIBLE);
        }

    }
}
