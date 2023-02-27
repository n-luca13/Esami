package com.example.applicazionetesi;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class VoiceInterfaceFragment2 extends Fragment {
    AppCompatImageButton btnPrevious, btnNext;
    TextView txtViewOccurrences, txtStringHint;
    int i;
    List<Pair<Integer, Integer>> finalStartParams, finalEndParams;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.voice_interface_fragment2, container, false);
        TextView utteranceTextView = rootView.findViewById(R.id.utterance_textView);

        RelativeLayout disambiguationLayout = rootView.findViewById(R.id.vi_center_container);
        btnPrevious = rootView.findViewById(R.id.prev_strg);
        btnNext = rootView.findViewById(R.id.next_strg);
        txtViewOccurrences = rootView.findViewById(R.id.number_strg);
        txtStringHint = rootView.findViewById(R.id.string_hint);

        RelativeLayout checkboxLayout = rootView.findViewById(R.id.vi_checkbox_container);
        CheckBox checkBox = rootView.findViewById(R.id.checkbox);
        Button confirmBtn = rootView.findViewById(R.id.confirm_button);
        Button cancelBtn = rootView.findViewById(R.id.cancel_button);

        cancelBtn.setOnClickListener(v -> {
            listener.onNotepad(null, null, null, null);
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.fragment_container, new VoiceInterfaceFragment1(), "FIRST_FRAGMENT")
                    .addToBackStack(null)
                    .commit();
        });

        if (getActivity() instanceof NotepadCallbackInterface) {
            setOnEditTextListener((NotepadCallbackInterface) getActivity());
        }
        if (getActivity() instanceof executeActionInterface) {
            setActionListener((executeActionInterface) getActivity());
        }

        List<Pair<Integer, Integer>> startParams = null, endParams = null;
        String utterance = null, startString = null, endString = null;
        String intent = null;

        Bundle bundle = getArguments();
        if (bundle != null){
            SerializablePair parameters = (SerializablePair) bundle.getSerializable("parameters");
            startParams = parameters.getFirst();
            endParams = parameters.getSecond();
            utterance = bundle.getString("utterance");
            startString = bundle.getString("start_string");
            endString = bundle.getString("end_string");
            intent = bundle.getString("intent");
        }

        List<Pair<Integer, Integer>> startParamsCopy = startParams;
        List<Pair<Integer, Integer>> endParamsCopy = endParams;

        if (utterance != null) utteranceTextView.setText(utterance);

        SharedPreferences sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE);
        boolean isChecked = sharedPreferences.getBoolean("dontShowAgain", false);
        if (!isChecked && (intent.equals("#cut") || intent.equals("#delete"))){
            // Prompt di conferma per operazioni di taglia e rimuovi
            checkboxLayout.setVisibility(View.VISIBLE);
            finalStartParams = startParamsCopy;
            finalEndParams = endParamsCopy;
            listener.onNotepad(null, null, finalStartParams, finalEndParams);
            checkBox.setOnCheckedChangeListener((buttonView, isChecked1) -> {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if (isChecked1){
                    AlertDialog.Builder dialogue = new AlertDialog.Builder(requireContext(), R.style.CheckboxAlertDialogStyle);
                    dialogue.setTitle("Sei sicuro?")
                            .setMessage("Se confermi non vedrai più questa schermata e non potrai più modificare la selezione corrente del testo")
                            .setPositiveButton("Conferma", (dialog, which) -> {
                                editor.putBoolean("dontShowAgain", true);
                                editor.apply();
                                checkBox.setChecked(true);
                            })
                            .setNegativeButton("Annulla", (dialog, which) -> {
                                editor.putBoolean("dontShowAgain", false);
                                editor.apply();
                                checkBox.setChecked(false);
                            }).
                            setOnCancelListener(dialog -> {
                                editor.putBoolean("dontShowAgain", false);
                                editor.apply();
                                checkBox.setChecked(false);
                            }).show();
                } else {
                    editor.putBoolean("dontShowAgain", false);
                    editor.apply();
                    checkBox.setChecked(false);
                }
            });
            confirmBtn.setOnClickListener(v1 -> execute());
        } else {
            // Percorso di disambiguazione delle occorrenze
            disambiguationLayout.setVisibility(View.VISIBLE);
            if (startParams != null && endParams != null){
                if (startParams.size() > 1){
                    if (endParams.size() > 1){
                        updateFragmentTwo(startParams, startString);
                        confirmBtn.setText(R.string.avanti);
                        String finalEndString = endString;
                        confirmBtn.setOnClickListener(v -> {
                            List<Pair<Integer, Integer>> temporaryStartPar = new ArrayList<>();
                            temporaryStartPar.add(startParamsCopy.get(i));
                            finalStartParams = temporaryStartPar;
                            final List<Pair<Integer, Integer>> filteredEndParams = filterEndParams(finalStartParams, endParamsCopy);
                            if (filteredEndParams.size() > 1){
                                updateFragmentTwo(filteredEndParams, finalEndString);

                                confirmBtn.setText(R.string.ok);
                                confirmBtn.setOnClickListener(v1 -> {
                                    List<Pair<Integer, Integer>> temporaryEndPar = new ArrayList<>();
                                    temporaryEndPar.add(filteredEndParams.get(i));
                                    finalEndParams = temporaryEndPar;
                                    execute();
                                });
                            } else {
                                finalEndParams = filteredEndParams;
                                execute();
                            }
                        });
                    } else {
                        finalEndParams = endParams;
                        updateFragmentTwo(startParams, startString);
                        confirmBtn.setText(R.string.ok);
                        confirmBtn.setOnClickListener(v -> {
                            List<Pair<Integer, Integer>> temporaryStartPar = new ArrayList<>();
                            temporaryStartPar.add(startParamsCopy.get(i));
                            finalStartParams = temporaryStartPar;
                            execute();
                        });
                    }
                } else if (endParams.size() > 1){
                    finalStartParams = startParams;
                    updateFragmentTwo(endParams, endString);
                    confirmBtn.setText(R.string.ok);
                    confirmBtn.setOnClickListener(v -> {
                        List<Pair<Integer, Integer>> temporaryEndPar = new ArrayList<>();
                        temporaryEndPar.add(endParamsCopy.get(i));
                        finalEndParams = temporaryEndPar;
                        execute();
                    });
                }
            } else if (startParams != null){
                updateFragmentTwo(startParams, startString);
                switch (intent) {
                    case "#search":
                        confirmBtn.setText(R.string.ok);
                        break;
                    case "#paste":
                        confirmBtn.setText(R.string.incolla);
                        break;
                    default:
                        listener.onNotepad(null, null, null, null);
                        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                        fragmentManager.popBackStackImmediate();
                }
                confirmBtn.setOnClickListener(v -> {
                    List<Pair<Integer, Integer>> temporaryStartPar = new ArrayList<>();
                    temporaryStartPar.add(startParamsCopy.get(i));
                    finalStartParams = temporaryStartPar;
                    execute();
                });
            }
        }

        return rootView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(((MainActivity) requireActivity()).getSupportActionBar()).hide();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Objects.requireNonNull(((MainActivity) requireActivity()).getSupportActionBar()).show();
    }

    private void updateFragmentTwo(List<Pair<Integer, Integer>> currentParams, String currentString){
        i = 0;
        int paramSize = currentParams.size();
        // Per mostrare la selezione corrente
        listener.onNotepad(currentParams.get(0).first, currentParams.get(0).second, finalStartParams, finalEndParams);

        btnPrevious.setEnabled(false);
        btnPrevious.setBackgroundTintList(ContextCompat.getColorStateList(requireActivity(), R.color.bckgrd_gray_3));
        btnPrevious.setImageTintList(ContextCompat.getColorStateList(requireActivity(), R.color.gray_disabled));
        btnNext.setEnabled(true);
        btnNext.setBackgroundTintList(ContextCompat.getColorStateList(requireActivity(), R.color.gray_active));
        btnNext.setImageTintList(ContextCompat.getColorStateList(requireActivity(), R.color.white));

        txtStringHint.setText(currentString);
        txtViewOccurrences.setText(getString(R.string.occurrence_text,1, paramSize));

        btnNext.setOnClickListener(v -> {
            if (i+1 < paramSize){
                i++;
                listener.onNotepad(currentParams.get(i).first, currentParams.get(i).second, finalStartParams, finalEndParams);
                txtViewOccurrences.setText(getString(R.string.occurrence_text, (i+1), paramSize));

                if(i+1 == paramSize){
                    btnNext.setEnabled(false);
                    btnNext.setBackgroundTintList(ContextCompat.getColorStateList(requireActivity(), R.color.bckgrd_gray_3));
                    btnNext.setImageTintList(ContextCompat.getColorStateList(requireActivity(), R.color.gray_disabled));
                }
                if (!btnPrevious.isEnabled()){
                    btnPrevious.setEnabled(true);
                    btnPrevious.setBackgroundTintList(ContextCompat.getColorStateList(requireActivity(), R.color.gray_active));
                    btnPrevious.setImageTintList(ContextCompat.getColorStateList(requireActivity(), R.color.white));
                }
            }
        });

        btnPrevious.setOnClickListener(v -> {
            if (i > 0){
                i--;
                listener.onNotepad(currentParams.get(i).first, currentParams.get(i).second, finalStartParams, finalEndParams);
                txtViewOccurrences.setText(getString(R.string.occurrence_text, (i+1), paramSize));
                if (i == 0){
                    btnPrevious.setEnabled(false);
                    btnPrevious.setBackgroundTintList(ContextCompat.getColorStateList(requireActivity(), R.color.bckgrd_gray_3));
                    btnPrevious.setImageTintList(ContextCompat.getColorStateList(requireActivity(), R.color.gray_disabled));
                }
                if (!btnNext.isEnabled()){
                    btnNext.setEnabled(true);
                    btnNext.setBackgroundTintList(ContextCompat.getColorStateList(requireActivity(), R.color.gray_active));
                    btnNext.setImageTintList(ContextCompat.getColorStateList(requireActivity(), R.color.white));
                }
            }
        });
    }


    private static List<Pair<Integer, Integer>> filterEndParams(List<Pair<Integer, Integer>> startParams, List<Pair<Integer, Integer>> endParams){
        List<Pair<Integer, Integer>> filteredEndList = new ArrayList<>();
        boolean x;
        int i;

        for (Pair<Integer, Integer> endOccurrence: endParams) {
            x = false;
            i = 0;
            while (!x && i < startParams.size()){
                if (endOccurrence.first > startParams.get(i).second){
                    filteredEndList.add(endOccurrence);
                    x = true;
                }
                i++;
            }
        }

        return filteredEndList;
    }

    private void execute(){
        Pair<List<Pair<Integer, Integer>>, List<Pair<Integer, Integer>>> parameters = new Pair<>(finalStartParams, finalEndParams);
        listener.onNotepad(null, null, null, null);
        executeActionListener.executeAction(parameters);
    }

    public interface NotepadCallbackInterface{
        void onNotepad(Integer startSelection, Integer endSelection, List<Pair<Integer, Integer>> startAbsolute, List<Pair<Integer, Integer>> endAbsolute);
    }
    private NotepadCallbackInterface listener;

    public void setOnEditTextListener(NotepadCallbackInterface listener){
        this.listener = listener;
    }

    public interface executeActionInterface{
        void executeAction(Pair<List<Pair<Integer, Integer>>, List<Pair<Integer, Integer>>> parameters);
    }
    private executeActionInterface executeActionListener;

    public void setActionListener(executeActionInterface listener){
        this.executeActionListener = listener;
    }

}
