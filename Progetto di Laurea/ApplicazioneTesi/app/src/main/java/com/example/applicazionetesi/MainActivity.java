package com.example.applicazionetesi;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.Editable;
import android.text.InputType;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.util.Consumer;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements VoiceInterfaceFragment2.NotepadCallbackInterface, VoiceInterfaceFragment2.executeActionInterface, VoiceInterfaceFragment1.MicCallbackInterface{
    private AppCompatImageButton keyboardBtn, smallMicBtn, clearBtn, tutorialBtn, boldBtn, highlightBtn;
    private Button cutBtn, copyBtn, deleteBtn;
    private EditText notepad, title_edit_text;
    private ViewTreeObserver.OnGlobalLayoutListener layoutListener;
    private InputMethodManager imm;
    public static final Integer RecordAudioRequestCode = 1;
    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private boolean pasteAfter, isHideChecked, undoing, isListening = false;
    private Integer keyboardHeight;
    private String intent, treatedUtterance, clipboardText;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Map<String, List<String>> intents = defineIntentTriggers();
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);

        // FRAGMENT CONTAINER & FIRST FRAGMENT
        FrameLayout fragmentContainer = findViewById(R.id.fragment_container);
        if (savedInstanceState == null){
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.fragment_container, new VoiceInterfaceFragment1(), "FIRST_FRAGMENT")
                    .addToBackStack(null)
                    .commit();
        }
        fragmentContainer.setVisibility(View.GONE);

        // ACTIVITY ROOT VIEW GROUP
        ViewGroup activityRootViewGroup = findViewById(R.id.activity_root);
        // NOTEPAD
        notepad = findViewById(R.id.notepad_edit_text);
        notepad.setText(R.string.placeholder_text);
        Stack<String> notepadStack = new Stack<>();

        // CUSTOM ACTION BAR (down)
        keyboardBtn = findViewById(R.id.keyboard_btn);
        smallMicBtn = findViewById(R.id.mic_btn);
        tutorialBtn = findViewById(R.id.tutorial_btn);
        clearBtn = findViewById(R.id.clear_btn);
        AppCompatImageButton undoBtn = findViewById(R.id.undo_btn);


        // INPUT METHOD MANAGER & NOTEPAD SETTINGS
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        notepad.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED);
        notepad.requestFocus();

        // GENERAL TOOLS BAR
        LinearLayoutCompat generalToolsLayout = findViewById(R.id.generalToolsLayout);
        cutBtn = findViewById(R.id.cutBtn);
        copyBtn = findViewById(R.id.copyBtn);
        deleteBtn = findViewById(R.id.deleteBtn);
        boldBtn = findViewById(R.id.boldBtn);
        highlightBtn = findViewById(R.id.highlighterBtn);
        generalToolsLayout.setVisibility(View.GONE);

        // ACTION BAR (up)
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(false);
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            @SuppressLint("InflateParams") View customView = inflater.inflate(R.layout.custom_action_bar, null);
            actionBar.setCustomView(customView);

            ImageView arrow_left_btn= findViewById(R.id.arrow_left_btn);
            ImageView settings_btn = findViewById(R.id.settings_btn);
            title_edit_text = findViewById(R.id.title_edit_text);

            arrow_left_btn.setOnClickListener(v -> Toast.makeText(this, "Pagina principale", Toast.LENGTH_SHORT).show());
            settings_btn.setOnClickListener(v -> Toast.makeText(this, "Impostazioni", Toast.LENGTH_SHORT).show());
            title_edit_text.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus && fragmentContainer.getVisibility() == View.VISIBLE){
                    if (isListening) {
                        speechRecognizer.stopListening();
                        isListening = false;
                    }
                    notepad.setShowSoftInputOnFocus(true);
                    smallMicBtn.setVisibility(View.VISIBLE);
                    fragmentContainer.setVisibility(View.GONE);
                }
            });
        }


        // SPEECH RECOGNIZER
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            checkPermission();
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                isListening = true;
                intent = null;

                notepad.setShowSoftInputOnFocus(false);
                if (title_edit_text.hasFocus()) title_edit_text.clearFocus();
                tutorialBtn.setEnabled(false);
                tutorialBtn.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.gray_7));

                ((VoiceInterfaceFragment1) Objects.requireNonNull(getSupportFragmentManager().
                        findFragmentById(R.id.fragment_container))).
                        updateFragmentOne(isListening);

                // MICROPHONE BUTTON ON CLICK LISTENER: START/END RECOGNITION & UPDATE UI
                AppCompatImageButton microphone_button = findViewById(R.id.microphone_button);
                microphone_button.setOnClickListener(v -> {
                    if (isListening){
                        speechRecognizer.stopListening();
                        speechRecognizer.cancel();
                        isListening = false;
                        notepad.setShowSoftInputOnFocus(true);
                    } else {
                        speechRecognizer.startListening(speechRecognizerIntent);
                        isListening = true;
                    }
                    ((VoiceInterfaceFragment1) Objects.requireNonNull(getSupportFragmentManager().
                            findFragmentById(R.id.fragment_container))).
                            updateFragmentOne(isListening);
                });

            }
            @Override
            public void onBeginningOfSpeech() {
                isListening = true;
            }

            @Override
            public void onRmsChanged(float rmsdB) {

            }

            @Override
            public void onBufferReceived(byte[] buffer) {

            }

            @Override
            public void onEndOfSpeech() {
                speechRecognizer.stopListening();
                isListening = false;
                notepad.setShowSoftInputOnFocus(true);
                ((VoiceInterfaceFragment1) Objects.requireNonNull(getSupportFragmentManager().
                        findFragmentById(R.id.fragment_container))).
                        updateFragmentOne(isListening);
            }

            @Override
            public void onError(int error) {
                if (error == 7){
                    isListening = false;
                    ((VoiceInterfaceFragment1) Objects.requireNonNull(getSupportFragmentManager().
                            findFragmentById(R.id.fragment_container))).
                            updateFragmentOne(isListening);
                }
                tutorialBtn.setEnabled(true);
                tutorialBtn.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.blue_1));
            }

            @Override
            public void onResults(Bundle results) {
                tutorialBtn.setEnabled(true);
                tutorialBtn.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.blue_1));

                ArrayList<String> data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                String utterance = data.get(0);

                // INTENT NON RICONOSCIUTO
                if (intent == null || intent.isEmpty()) {
                    intent = getIntent(intents, utterance.toLowerCase());
                    if (intent == null || intent.isEmpty()) {
                        Toast.makeText(MainActivity.this, "Comando incompleto o non riconosciuto", Toast.LENGTH_SHORT).show();
                        speechRecognizer.cancel();
                        isListening = false;
                        ((VoiceInterfaceFragment1) Objects.requireNonNull(getSupportFragmentManager().
                                findFragmentById(R.id.fragment_container))).
                                updateFragmentOne(isListening);
                        return;
                    }
                }

                String notepadText = notepad.getText().toString();
                Editable editable = notepad.getText();

                // Rimozione della stringa corrispondente all'intent
                treatedUtterance = "";
                if (utterance.contains(" ")) {
                    treatedUtterance = utterance.substring(utterance.indexOf(" ")+1);
                }

                Integer startSelection, endSelection, cursorPosition;
                startSelection = endSelection = cursorPosition = null;
                String slctdText = "";
                String cursorWord = "";
                List<Pair<Integer, Integer>> occorrenze = null;

                if (notepad.hasSelection()){
                    // ELABORAZIONE DI UN EVENTUALE TESTO SELEZIONATO
                    startSelection = notepad.getSelectionStart();
                    endSelection = notepad.getSelectionEnd();
                    slctdText = notepadText.substring(startSelection, endSelection);
                }
                if (notepad.hasFocus() && slctdText.isEmpty()){
                    // ELABORAZIONE DELLA POSIZIONE DEL CURSORE
                    cursorPosition = notepad.getSelectionStart();
                    cursorWord = getWord(cursorPosition);
                }



                switch (intent)  {
                    case "#write":
                        // Rimozione di stopwords specifiche dell'intent ("qui")
                        if (treatedUtterance.startsWith("qui")){
                            String[] substrings = treatedUtterance.split("^\\s*qui\\s*",2);
                            treatedUtterance = substrings.length > 1 ? substrings[1] : "";
                        }
                        if (treatedUtterance.isEmpty()){
                            Toast.makeText(MainActivity.this, "Comando incompleto", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (!slctdText.isEmpty()){ // C'è del testo selezionato --> Scrivo al posto del testo selezionato
                            editable.delete(startSelection, endSelection);
                            editable.insert(startSelection, treatedUtterance);
                            notepad.setSelection(startSelection+treatedUtterance.length());
                            visualFeedback(startSelection, treatedUtterance);
                        } else if (cursorPosition != null){ // La posizione del cursore è nota
                            if (!cursorWord.isEmpty()) { // Il cursore è posizionato su una parola --> Scrivo dopo la parola
                                int end = getWordLastPositionFromCursor(cursorPosition, cursorWord);
                                treatedUtterance = addSpace(treatedUtterance, end);
                                editable.insert(end, treatedUtterance);
                                notepad.setSelection(end + treatedUtterance.length());
                                visualFeedback(end, treatedUtterance);
                            } else { // Il cursore NON è posizionato su una parola --> Scrivo in corrispondenza del cursore
                                treatedUtterance = addSpace(treatedUtterance, cursorPosition);
                                editable.insert(cursorPosition, treatedUtterance);
                                notepad.setSelection(cursorPosition + treatedUtterance.length());
                                visualFeedback(cursorPosition, treatedUtterance);
                            }
                        } else {
                            treatedUtterance = addSpace(treatedUtterance, notepad.length());
                            notepad.append(treatedUtterance);
                            notepad.setSelection(notepad.length());
                            visualFeedback(notepad.length() - (treatedUtterance.length()), treatedUtterance);
                        }
                        showKeyboard(fragmentContainer);

                        break;
                    case "#search":
                        // Rimozione di stopwords specifiche dell'intent
                        String[] stopwords = new String[]{
                                "occorrenze di",
                                "le occorrenze di",
                                "il testo selezionato",
                                "occorrenze della parola",
                                "le occorrenze della parola",
                                "altre occorrenze",
                                "le altre occorrenze",
                                "altre occorrenze di",
                                "altre occorrenze di questa parola",
                                "parola",
                                "vocabolo",
                                "il vocabolo",
                                "termine",
                                "il termine",
                                "questa parola",
                                "parola selezionata",
                                "la parola selezionata",
                                "parole",
                                "frase",
                                "la parola",
                                "la frase",
                                "queste parole",
                        };
                        for (String stopword : stopwords){
                            if (treatedUtterance.startsWith(stopword)){
                                treatedUtterance = treatedUtterance.substring(stopword.length());
                                break;
                            }
                        }

                        if (!treatedUtterance.isEmpty()) { // Cerco la stringa menzionata dall'utente
                            occorrenze = findOccurrences(notepadText, treatedUtterance);
                        } else if (!slctdText.isEmpty()) { // Cerco il testo selezionato
                            occorrenze = findOccurrences(notepadText, slctdText);
                        } else if (!cursorWord.isEmpty()) { // Cerco la parola su cui è posizionato il cursore
                            occorrenze = findOccurrences(notepadText, cursorWord);
                        }

                        if (occorrenze != null && occorrenze.size() == 1){
                            Toast.makeText(MainActivity.this, "È stata trovata una sola occorrenza", Toast.LENGTH_SHORT).show();
                            notepad.setSelection(occorrenze.get(0).second);
                            visualFeedback(occorrenze.get(0).first, treatedUtterance);
                            showKeyboard(fragmentContainer);
                        } else if (occorrenze != null && occorrenze.size() > 1){
                            // NUOVO FRAGMENT PER VISUALIZZAZIONE DELLE OCCORRENZE
                            Fragment fragment2 = new VoiceInterfaceFragment2();
                            Bundle bundle = new Bundle();
                            bundle.putString("intent", intent);
                            bundle.putString("utterance", utterance);
                            bundle.putString("start_string", treatedUtterance);
                            bundle.putSerializable("parameters", new SerializablePair(occorrenze, null));
                            fragment2.setArguments(bundle);
                            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment2, "SECOND_FRAGMENT").commit();
                            disableButtons();
                        } else {
                            Toast.makeText(MainActivity.this, "Nessuna corrispondenza", Toast.LENGTH_LONG).show();
                            return;
                        }

                        break;
                    case "#paste":
                        ClipData clipData;
                        if (clipboardManager.hasPrimaryClip()){
                            clipData = clipboardManager.getPrimaryClip();
                        } else {
                            clipData = null;
                        }
                        if (clipData == null || clipData.getItemCount() < 1){
                            Toast.makeText(MainActivity.this, "Nessun contenuto da copiare", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        clipboardText = clipData.getItemAt(0).getText().toString();

                        treatedUtterance = treatedUtterance.toLowerCase();

                        pasteAfter = false;
                        if (treatedUtterance.contains("dopo")){ // ad es. "Incolla dopo Pluto"
                            pasteAfter = true;
                            // Estraggo la substring che segue "dopo" --> "Pluto"
                            int startIndex = treatedUtterance.indexOf("dopo") + "dopo".length();
                            treatedUtterance = treatedUtterance.substring(startIndex).trim();

                            // Se non ha detto altro, controllo se ha selezionato una parola o posizionato il cursore
                            if (treatedUtterance.isEmpty() || treatedUtterance.matches("^\\s+$")) {
                                if (!slctdText.isEmpty()) {
                                    // L'utente ha selezionato del testo
                                    // Incollo dopo il testo selezionato
                                    if (String.valueOf(notepadText.charAt(endSelection)).matches("^[?!.]$")) endSelection++;
                                    String strClipboard = addSpace(clipboardText, endSelection);
                                    editable.insert(endSelection, strClipboard);
                                    visualFeedback(endSelection, strClipboard);
                                    initializeFragmentOne();
                                } else if (cursorPosition != null && !cursorWord.isEmpty()) {
                                    // Se il cursore è posizionato su una parola, incollo il testo dopo la parola
                                    int end = getWordLastPositionFromCursor(cursorPosition, cursorWord);
                                    editable.insert(end, " " + clipboardText);
                                    visualFeedback(end, clipboardText);
                                    initializeFragmentOne();
                                } else {
                                    Toast.makeText(MainActivity.this, "Comando incompleto o non riconosciuto", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            } else {
                                // Ricerca occorrenze della substring
                                occorrenze = findOccurrences(notepadText, treatedUtterance);
                                if (occorrenze.size() < 1){ // Nessuna occorrenza
                                    Toast.makeText(MainActivity.this, "Nessuna occorrenza di " + treatedUtterance, Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                // Se sono state trovate più occorrenze della substring, cerco di filtrarle in base al cursore e/o alla selezione corrente
                                // è selezionata un'eventuale occorrenza i cui estremi includano il cursore o il testo selezionato
                                if (occorrenze.size() > 1) {
                                    List<Pair<Integer, Integer>> occorrenzeFiltered = innerFilterOccurrences(occorrenze, cursorPosition, startSelection, endSelection);
                                    if (!occorrenzeFiltered.isEmpty()) occorrenze = occorrenzeFiltered;
                                }

                                if (occorrenze.size() == 1){
                                    // Se risulta una sola occorrenza della substring, incollo il testo dopo la substring
                                    int pastePos = occorrenze.get(0).second;
                                    if (String.valueOf(notepadText.charAt(pastePos)).matches("^[?!.]$")) pastePos++;
                                    String strClipboard = addSpace(clipboardText, pastePos);
                                    editable.insert(pastePos, strClipboard);
                                    visualFeedback(pastePos, strClipboard);
                                    initializeFragmentOne();
                                } else {
                                    // Se ci sono più occorrenze della substring chiedo all'utente di selezionarne una
                                    // NUOVO FRAGMENT PER VISUALIZZAZIONE DELLE OCCORRENZE
                                    Fragment fragment2 = new VoiceInterfaceFragment2();
                                    Bundle bundle = new Bundle();
                                    bundle.putString("intent", intent);
                                    bundle.putString("utterance", utterance);
                                    bundle.putString("start_string", treatedUtterance);
                                    bundle.putSerializable("parameters", new SerializablePair(occorrenze, null));
                                    fragment2.setArguments(bundle);
                                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment2, "SECOND_FRAGMENT").commit();
                                    disableButtons();
                                }
                            }
                        } else if (treatedUtterance.contains("prima")){ // ad es. "incolla prima di pluto"
                            // Estraggo la substring che segue "prima"
                            int startIndex = treatedUtterance.indexOf("prima") + "prima".length();
                            treatedUtterance = treatedUtterance.substring(startIndex).trim();

                            if (treatedUtterance.isEmpty() || treatedUtterance.matches("^(\\s+)?$")){
                                // CONTROLLO TOCCO
                                if (!slctdText.isEmpty()) {
                                    // L'utente ha selezionato del testo
                                    // Incollo prima del testo selezionato
                                    editable.insert(startSelection, clipboardText);
                                    visualFeedback(startSelection, clipboardText);
                                    initializeFragmentOne();
                                } else if (cursorPosition != null && !cursorWord.isEmpty()) {
                                    // Se il cursore è posizionato su una parola, incollo il testo prima della parola
                                    int start = getWordStartPositionFromCursor(cursorPosition, cursorWord);
                                    editable.insert(start, clipboardText);
                                    visualFeedback(start, clipboardText);
                                    initializeFragmentOne();
                                } else {
                                    Toast.makeText(MainActivity.this, "Comando incompleto o non riconosciuto", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            } else {
                                Matcher matcher = Pattern.compile("^(?:\\s+)?di(?:$|\\W)(.+)").matcher(treatedUtterance);
                                if (matcher.find()) {
                                    treatedUtterance = matcher.group(1);
                                    // Ricerca occorrenze della substring
                                    occorrenze = findOccurrences(notepadText, treatedUtterance);
                                    if (occorrenze.size() < 1){
                                        Toast.makeText(MainActivity.this, "Comando incompleto o non riconosciuto", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    // Se sono state trovate più occorrenze della substring, cerco di filtrarle in base al cursore e/o alla selezione corrente
                                    // è selezionata un'eventuale occorrenza i cui estremi includano il cursore o il testo selezionato
                                    if (occorrenze.size() > 1) {
                                        List<Pair<Integer, Integer>> occorrenzeFiltered = innerFilterOccurrences(occorrenze, cursorPosition, startSelection, endSelection);
                                        if (!occorrenzeFiltered.isEmpty()) occorrenze = occorrenzeFiltered;
                                    }

                                    if(occorrenze.size() == 1){
                                        // Se risulta una sola occorrenza della substring, incollo il testo prima della substring
                                        int pastePos = occorrenze.get(0).first;
                                        editable.insert(pastePos, clipboardText);
                                        visualFeedback(pastePos, clipboardText);
                                        initializeFragmentOne();
                                    } else {
                                        // Se ci sono più occorrenze della substring chiedo all'utente di selezionarne una
                                        Toast.makeText(MainActivity.this, "TROPPE OCCORRENZE!", Toast.LENGTH_SHORT).show();
                                        // NUOVO FRAGMENT PER VISUALIZZAZIONE DELLE OCCORRENZE
                                        pasteAfter = false;
                                        Fragment fragment2 = new VoiceInterfaceFragment2();
                                        Bundle bundle = new Bundle();
                                        bundle.putString("intent", intent);
                                        bundle.putString("utterance", utterance);
                                        bundle.putString("start_string", treatedUtterance);
                                        bundle.putSerializable("parameters", new SerializablePair(occorrenze, null));
                                        fragment2.setArguments(bundle);
                                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment2, "SECOND_FRAGMENT").commit();
                                        disableButtons();
                                    }
                                }
                            }

                        } else if (!slctdText.isEmpty()) {
                            // L'utente ha selezionato del testo; non ha menzionato stringhe di riferimento
                            // Incollo il testo al posto del testo selezionato
                            editable.delete(startSelection,endSelection);
                            editable.replace(startSelection, startSelection, clipboardText);
                            visualFeedback(startSelection, clipboardText);
                            initializeFragmentOne();

                        } else if (cursorPosition != null) {
                            // è nota la posizione del cursore. L'utente non ha menzionato stringhe di riferimento e non ha selezionato del testo
                            if (!cursorWord.isEmpty()){
                                // Se il cursore è posizionato su una parola, incollo il testo dopo la parola
                                int end = getWordLastPositionFromCursor(cursorPosition, cursorWord);
                                editable.insert(end, " " + clipboardText);
                                visualFeedback(end, clipboardText);
                            } else {
                                // Altrimenti incollo il testo in corrispondenza del cursore
                                String strClipboard = addSpace(clipboardText, cursorPosition);
                                editable.insert(cursorPosition, strClipboard);
                                visualFeedback(cursorPosition, strClipboard);
                            }
                            initializeFragmentOne();
                        } else {
                            // L'utente non ha menzionato stringhe di riferimento e non ha selezionato del testo; parimenti, non è nota la posizione del cursore
                            Toast.makeText(MainActivity.this, "Comando incompleto o non riconosciuto", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        break;
                    default:
                        Pair<List<Pair<Integer, Integer>>, List<Pair<Integer, Integer>>> parameters = getVoiceParams(
                                utterance.toLowerCase(),
                                startSelection,
                                endSelection,
                                slctdText.toLowerCase(),
                                cursorPosition,
                                cursorWord.toLowerCase()
                        );
                        if (parameters == null || Objects.equals(parameters.first.get(0).first, parameters.second.get(0).second)){
                            Toast.makeText(MainActivity.this, "Comando incompleto o non riconosciuto", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (parameters.first.size() > 1 || parameters.second.size() > 1){
                            if (parameters.first.get(0).second != null && parameters.second.get(0).first != null){
                                parameters = filterParams(parameters);
                            }
                        }

                        if (parameters.first.size() > 1 || parameters.second.size() > 1){
                            String startString = null, endString = null;
                            if (parameters.first.size() > 1) {
                                startString = notepadText.substring(parameters.first.get(0).first, parameters.first.get(0).second).toLowerCase();
                            }
                            if (parameters.second.size() > 1){
                                endString = notepadText.substring(parameters.second.get(0).first, parameters.second.get(0).second).toLowerCase();
                            }
                            Bundle bundle = new Bundle();
                            bundle.putString("intent", intent);
                            bundle.putString("utterance", data.get(0));
                            bundle.putString("start_string", startString);
                            bundle.putString("end_string", endString);
                            bundle.putSerializable("parameters", new SerializablePair(parameters.first, parameters.second));
                            Fragment fragment2 = new VoiceInterfaceFragment2();
                            fragment2.setArguments(bundle);
                            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment2, "SECOND_FRAGMENT").addToBackStack(null).commit();
                        } else {
                            isHideChecked = sharedPreferences.getBoolean("dontShowAgain", false);
                            if ((intent.equals("#cut") || intent.equals("#delete")) && !isHideChecked){
                                Bundle bundle = new Bundle();
                                bundle.putString("intent", intent);
                                bundle.putString("utterance", data.get(0));
                                bundle.putSerializable("parameters", new SerializablePair(parameters.first, parameters.second));
                                Fragment fragment2 = new VoiceInterfaceFragment2();
                                fragment2.setArguments(bundle);
                                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment2, "SECOND_FRAGMENT").addToBackStack(null).commit();
                            } else {
                                executeAction(parameters);
                            }
                        }
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                // Mostrare risultati parziali all'utente
                TextView utterance_textView = findViewById(R.id.utterance_textView);
                ArrayList<String> p_data = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                String utterance = p_data.get(0);
                if (utterance_textView != null) {
                    utterance_textView.setText(utterance);
                }

                if (utterance.matches("\\b\\w+\\b") && intent == null) {
                    intent = getIntent(intents, utterance.toLowerCase());
                    if (intent == null) {
                        Toast.makeText(MainActivity.this, "Comando incompleto o non riconosciuto", Toast.LENGTH_SHORT).show();
                        speechRecognizer.cancel();
                        isListening = false;
                        ((VoiceInterfaceFragment1) Objects.requireNonNull(getSupportFragmentManager().
                                findFragmentById(R.id.fragment_container))).
                                updateFragmentOne(isListening);
                    }
                }
            }

            @Override
            public void onEvent(int eventType, Bundle params) {

            }
        });

        // KEYBOARD BUTTON ON CLICK LISTENER
        keyboardBtn.setOnClickListener(v -> {
            // STOP SPEECH RECOGNIZER
            if (isListening) {
                speechRecognizer.stopListening();
                speechRecognizer.cancel();
                isListening = false;
            }
            VoiceInterfaceFragment2 fragment2 = (VoiceInterfaceFragment2) getSupportFragmentManager().findFragmentByTag("SECOND_FRAGMENT");
            if (fragment2 != null && fragment2.isVisible()){
                notepad.setSelection(notepad.getSelectionEnd());
                getSupportFragmentManager().popBackStack();
            }
            showKeyboard(fragmentContainer);
        });

        // SMALL MICROPHONE BUTTON ON CLICK LISTENER
        smallMicBtn.setOnClickListener(v -> {
            // HIDE KEYBOARD
            if (imm != null) {
                imm.hideSoftInputFromWindow(notepad.getWindowToken(), 0);
            }

            // DELAY TO AVOID VISUAL ARTIFACTS
            new CountDownTimer(7, 7) {
                public void onTick(long millisUntilFinished) {
                }
                public void onFinish() {
                    smallMicBtn.setVisibility(View.GONE);

                    if (fragmentContainer.getVisibility() == View.GONE){
                        fragmentContainer.setVisibility(View.VISIBLE);

                        ViewGroup.LayoutParams params = fragmentContainer.getLayoutParams();
                        params.height = keyboardHeight;
                        fragmentContainer.setLayoutParams(params);

                        // START SPEECH RECOGNIZER
                        speechRecognizer.startListening(speechRecognizerIntent);
                    }
                }
            }.start();
        });

        // CHECK KEYBOARD VISIBILITY
        addKeyboardListener((visible) -> {
            if(visible) {
                // (AT LAUNCH) GET KEYBOARD HEIGHT
                if (keyboardHeight == null){
                    WindowInsets windowInsets = activityRootViewGroup.getRootWindowInsets();
                    int bottomKeyboard = windowInsets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
                    int bottomBar = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
                    keyboardHeight = bottomKeyboard - bottomBar;
                }
                // HIDE KEYBOARD BUTTON
                keyboardBtn.setVisibility(View.GONE);
            }else{
                // SHOW KEYBOARD BUTTON
                keyboardBtn.setVisibility(View.VISIBLE);
            }
        });

        notepad.setOnLongClickListener(v -> {
            VoiceInterfaceFragment2 fragment2 = (VoiceInterfaceFragment2) getSupportFragmentManager().findFragmentByTag("SECOND_FRAGMENT");
            if (fragment2 != null && fragment2.isVisible()){
                initializeFragmentOne();
                updateUIToFragment();
                removeHighlightGraySpan();
            }
            return false;
        });


        notepad.setOnTouchListener((v, event) -> {
            VoiceInterfaceFragment2 fragment2 = (VoiceInterfaceFragment2) getSupportFragmentManager().findFragmentByTag("SECOND_FRAGMENT");
            if (!isListening && fragmentContainer.getVisibility() == View.VISIBLE && (fragment2 == null || !fragment2.isVisible())){
                smallMicBtn.setVisibility(View.VISIBLE);
                fragmentContainer.setVisibility(View.GONE);
            }
            return false;
        });

        notepad.setOnClickListener(v -> {
            if (isListening) {
                selectWord();
            }else{
                VoiceInterfaceFragment2 fragment2 = (VoiceInterfaceFragment2) getSupportFragmentManager().findFragmentByTag("SECOND_FRAGMENT");
                if (fragment2 != null && fragment2.isVisible()){
                    initializeFragmentOne();
                    updateUIToFragment();
                    removeHighlightGraySpan();
                }
            }
            if (generalToolsLayout.getVisibility() == View.VISIBLE){
                generalToolsLayout.setVisibility(View.GONE);
            }
        });

        notepad.setAccessibilityDelegate(new View.AccessibilityDelegate(){
            @Override
            public void sendAccessibilityEvent(@NonNull View host, int eventType) {
                if (eventType == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED){
                    if (isListening){
                        selectWord();
                    }
                }
                super.sendAccessibilityEvent(host, eventType);
            }
        });

        notepad.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                clearBtn.setEnabled(true);
                clearBtn.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.blue_1));
            } else {
                clearBtn.setEnabled(false);
                clearBtn.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.gray_7));
                if (generalToolsLayout.getVisibility() == View.VISIBLE){
                    generalToolsLayout.setVisibility(View.GONE);
                }
            }
        });

        notepad.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (!undoing) {
                    notepadStack.push(s.toString());
                    if (!notepadStack.empty()){
                        undoBtn.setEnabled(true);
                        undoBtn.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.blue_1));
                    }
                }
                if (generalToolsLayout.getVisibility() == View.VISIBLE){
                    generalToolsLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // TUTORIAL BUTTON ON CLICK LISTENER
        tutorialBtn.setOnClickListener(v -> Toast.makeText(MainActivity.this, "TUTORIAL", Toast.LENGTH_SHORT).show());

        // CLEAR BUTTON ON CLICK LISTENER
        clearBtn.setOnClickListener(v -> {
            FragmentManager fragmentManager = getSupportFragmentManager();
            VoiceInterfaceFragment2 fragment2 = (VoiceInterfaceFragment2) fragmentManager.findFragmentByTag("SECOND_FRAGMENT");
            if (fragment2 == null || !fragment2.isVisible()){
                notepad.clearFocus();
                Selection.removeSelection(notepad.getText());
            }
        });

        // UNDO BUTTON ON CLICK LISTENER
        undoBtn.setOnClickListener(v -> {
            if (!notepadStack.empty()){
                undoing = true;
                notepad.setText(notepadStack.pop());
                notepad.clearFocus();
                undoing = false;
                if (notepadStack.empty()){
                    undoBtn.setEnabled(false);
                    undoBtn.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.gray_7));
                }
            }
        });
        undoBtn.setEnabled(false);

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                FragmentManager fragmentManager = getSupportFragmentManager();
                VoiceInterfaceFragment1 fragment1 = (VoiceInterfaceFragment1) fragmentManager.findFragmentByTag("FIRST_FRAGMENT");
                VoiceInterfaceFragment2 fragment2 = (VoiceInterfaceFragment2) fragmentManager.findFragmentByTag("SECOND_FRAGMENT");
                FrameLayout fragmentContainer = findViewById(R.id.fragment_container);
                if (fragment2 != null && fragment2.isVisible()){
                    int slctEnd = notepad.getSelectionEnd();
                    Selection.removeSelection(notepad.getText());
                    notepad.setSelection(slctEnd);
                    fragmentManager.beginTransaction()
                            .setReorderingAllowed(true)
                            .replace(R.id.fragment_container, new VoiceInterfaceFragment1(), "FIRST_FRAGMENT")
                            .addToBackStack(null)
                            .commit();
                } else if (fragment1 != null && fragment1.isVisible() && fragmentContainer.getVisibility() == View.VISIBLE){
                    if (isListening){
                        speechRecognizer.stopListening();
                        speechRecognizer.cancel();
                        isListening = false;
                    }
                    fragmentContainer.setVisibility(View.GONE);
                    showKeyboard(fragmentContainer);
                }
            }
        };
        this.getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // STOP LISTENING AND CANCEL THE SPEECH RECOGNITION
        if (isListening) {
            speechRecognizer.stopListening();
            isListening = false;

            ((VoiceInterfaceFragment1) Objects.requireNonNull(getSupportFragmentManager().
                    findFragmentById(R.id.fragment_container))).
                    updateFragmentOne(isListening);
        }
        speechRecognizer.cancel();

        FragmentManager fragmentManager = getSupportFragmentManager();
        VoiceInterfaceFragment1 fragment1 = (VoiceInterfaceFragment1) fragmentManager.findFragmentByTag("FIRST_FRAGMENT");
        if (fragment1 != null && fragment1.isVisible()){
            notepad.clearFocus();
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        // DESTROY KEYBOARD LISTENER & SPEECH RECOGNIZER
        @SuppressLint("CutPasteId") View rootView = findViewById(R.id.activity_root);
        rootView.getViewTreeObserver().removeOnGlobalLayoutListener(layoutListener);
        speechRecognizer.destroy();
    }

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        VoiceInterfaceFragment1 fragment1 = (VoiceInterfaceFragment1) fragmentManager.findFragmentByTag("FIRST_FRAGMENT");
        VoiceInterfaceFragment2 fragment2 = (VoiceInterfaceFragment2) fragmentManager.findFragmentByTag("SECOND_FRAGMENT");
        if (fragment2 != null && fragment2.isVisible()){
            int slctEnd = notepad.getSelectionEnd();
            Selection.removeSelection(notepad.getText());
            notepad.setSelection(slctEnd);
            fragmentManager.beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.fragment_container, new VoiceInterfaceFragment1(), "FIRST_FRAGMENT")
                    .addToBackStack(null)
                    .commit();
        } else if (fragment1 != null && fragment1.isVisible()){
            FrameLayout fragmentContainer = findViewById(R.id.fragment_container);
            fragmentContainer.setVisibility(View.GONE);
            showKeyboard(fragmentContainer);
        } else {
            super.onBackPressed();
        }
    }

    // SPEECH RECOGNIZER PERMISSION
    private void checkPermission() {
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECORD_AUDIO},RecordAudioRequestCode);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RecordAudioRequestCode && grantResults.length > 0 ){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this,"Permission Granted",Toast.LENGTH_SHORT).show();
        }
    }

    // KEYBOARD VISIBILITY LISTENER ON GLOBAL LAYOUT
    private void addKeyboardListener(Consumer<Boolean> listener) {
        View rootView = findViewById(R.id.activity_root);
        layoutListener = () -> {
            WindowInsets insets = rootView.getRootWindowInsets();
            boolean isVisible = insets.isVisible(WindowInsetsCompat.Type.ime());
            listener.accept(isVisible);
        };
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);
    }
    private void showKeyboard(FrameLayout fragmentContainer){
        if (fragmentContainer.getVisibility() == View.VISIBLE) fragmentContainer.setVisibility(View.GONE);
        notepad.setShowSoftInputOnFocus(true);
        smallMicBtn.setVisibility(View.VISIBLE);
        if (!notepad.hasFocus()) notepad.requestFocus();
        if (imm != null) imm.showSoftInput(notepad, InputMethodManager.SHOW_IMPLICIT);
    }


    private Map<String, List<String>> defineIntentTriggers(){
        Map<String, List<String>> intents = new HashMap<>();

        List<String> intentSynonyms = Arrays.asList("incolla", "incollare", "inserisci", "inserire");
        intents.put("#paste",intentSynonyms);
        intentSynonyms = Arrays.asList("scrivi", "scrivere", "inserisci", "inserire");
        intents.put("#write",intentSynonyms);
        intentSynonyms = Arrays.asList("trova", "trovare", "cerca", "cercare", "mostra", "mostrare");
        intents.put("#search",intentSynonyms);
        intentSynonyms = Arrays.asList("copia", "copiare");
        intents.put("#copy",intentSynonyms);
        intentSynonyms = Arrays.asList("taglia", "tagliare");
        intents.put("#cut",intentSynonyms);
        intentSynonyms = Arrays.asList("seleziona", "selezionare");
        intents.put("#select",intentSynonyms);
        intentSynonyms = Arrays.asList("cancella", "cancellare", "elimina", "eliminare", "rimuovi", "rimuovere");
        intents.put("#delete",intentSynonyms);
        intentSynonyms = Arrays.asList("evidenzia", "evidenziare");
        intents.put("#highlight",intentSynonyms);

        return intents;
    }
    public static String getIntent(Map<String, List<String>> map, String value) {
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            if (entry.getValue().contains(value)){
                return entry.getKey();
            }
        }
        return null;
    }


    private VoiceInputResult analyzeVoiceInput(String utterance) {
        List<Pair<String, String>> coupleStrings = new ArrayList<>();
        Integer scope = null;
        String startString;
        Matcher matcher;

        if (utterance.matches(".*(^|\\W)(tutto|tutto il testo)(\\s+)?$")){
            scope = 1;
        } else if (utterance.matches(".*(?:^|\\W)(?:da|dalla parola)(?:$|\\W).+")){
            matcher = Pattern.compile(".*(?:^|\\W)(?:da|dalla parola)(?:$|\\W)(.+)").matcher(utterance);
            if (matcher.find()){
                startString = matcher.group(1);
                coupleStrings.add(new Pair<>(startString, null));

                matcher = Pattern.compile("(.+)(?:^|\\W)(?:fino|sino) alla fine(?:$|\\W).*").matcher(Objects.requireNonNull(startString));
                if (matcher.find()){
                    startString = matcher.group(1);
                    coupleStrings.add(new Pair<>(startString, "@FLAG_FINO_ALLA_FINE"));
                } else {
                    String refererString = startString;
                    int countPos = 0;
                    matcher = Pattern.compile("(?:^|\\W)(?:fino\\W|sino\\W)?(?:a|ad|alla parola)(?:$|\\W)(.+)").matcher(Objects.requireNonNull(startString));
                    while (matcher.find()){
                        startString = refererString.substring(0,countPos+matcher.start());
                        String endString = matcher.group(1);
                        countPos = countPos+matcher.start()+3;
                        coupleStrings.add(new Pair<>(startString, endString));
                        matcher = Pattern.compile("(?:^|\\W)(?:fino\\W|sino\\W)?(?:a|ad|alla parola)(?:$|\\W)(.+)").matcher(Objects.requireNonNull(endString));
                    }
                }
            }
        } else if (utterance.matches(".*(?:^|\\W)(?:fino|sino) a(?:d|lla parola)?(?:$|\\W).+")){
            matcher = Pattern.compile(".*?(?:^|\\W)(?:fino|sino) a(?:d|lla parola)?(?:$|\\W)(.+)").matcher(utterance);
            if (matcher.find()){
                String endString = matcher.group(1);
                coupleStrings.add(new Pair<>(null, endString));

                matcher = Pattern.compile(".*?(?:^|\\W)(?:tutto|dall'inizio)(?:$|\\W).+").matcher(utterance);
                if (matcher.find()){
                    coupleStrings.add(new Pair<>("@FLAG_DALL'INIZIO",endString));
                }
            }
        } else if (utterance.matches(".*\\b(parola|vocabolo|termine)\\b.*")){
            scope = 2;
        } else if (utterance.matches(".*\\b(frase|enunciato|proposizione)\\b.*")){
            scope = 3;
        } else if (utterance.matches(".*\\b(paragrafo|capoverso)\\b.*")){
            scope = 4;
        }

        return new VoiceInputResult(coupleStrings, scope);
    }

    private static class VoiceInputResult {
        private final List<Pair<String, String>> coupleStrings;
        private final Integer scope;

        public VoiceInputResult(List<Pair<String, String>> coupleStrings, Integer scope) {
            this.coupleStrings = coupleStrings;
            this.scope = scope;
        }

        public List<Pair<String, String>> getCoupleStrings() {
            return coupleStrings;
        }

        public Integer getScope() {
            return scope;
        }
    }

    private Pair<List<Pair<Integer, Integer>>, List<Pair<Integer, Integer>>> getVoiceParams(String utterance, Integer startSlctd, Integer endSlctd, String slctdText, Integer cursPos, String cursWord){
        VoiceInputResult voiceAnalysysResults = analyzeVoiceInput(utterance);
        List<Pair<String, String>> coupleStrings = voiceAnalysysResults.getCoupleStrings();
        Integer scope = voiceAnalysysResults.getScope();

        List<Pair<Integer, Integer>> startParams = new ArrayList<>();
        List<Pair<Integer, Integer>> endParams = new ArrayList<>();

        if (coupleStrings == null || coupleStrings.isEmpty()){
            // L'utente non ha menzionato stringhe rappresentative
            if (scope == null){
                // L'utente non ha menzionato entità lessicali
                if (!slctdText.isEmpty()){
                    // Testo preselezionato
                    startParams.add(new Pair<>(startSlctd, null));
                    endParams.add(new Pair<>(null, endSlctd));
                } else if (cursPos != null && cursWord != null) {
                    // Cursore
                    startParams.add(new Pair<>(getWordStartPositionFromCursor(cursPos, cursWord), null));
                    endParams.add(new Pair<>(null, getWordLastPositionFromCursor(cursPos, cursWord)));
                }
            } else {
                // L'utente ha menzionato entità lessicali
                if (scope == 1){ // Operazione globale su tutto il testo
                    startParams.add(new Pair<>(0, null));
                    endParams.add(new Pair<>(null, notepad.length()));
                } else { // Operazione su un'entità lessicale (parola, frase o paragrafo)
                    if (slctdText != null || (cursPos != null && cursWord != null)){
                        List<Pair<Integer, Integer>> lexicalParams = getParamsOfLexicalEntity(startSlctd, endSlctd, cursPos, cursWord, scope);
                        if (lexicalParams.size() > 0){
                            startParams.add(new Pair<>(lexicalParams.get(0).first, null));
                            endParams.add(new Pair<>(null, lexicalParams.get(0).second));
                        }
                    }
                }
            }

        } else {
            // L'utente ha menzionato stringhe rappresentative
            String notepadText = notepad.getText().toString().toLowerCase();
            for (Pair<String, String> pair : coupleStrings) {
                if (pair.first != null && pair.second != null){
                    // Comando vocale completo più eventuale input tattile. Ad es. "Copia da startString a endString"
                    // Cerco le due stringhe per ottenere i rispettivi parametri
                    List<Pair<Integer, Integer>> listStartParams = new ArrayList<>();
                    List<Pair<Integer, Integer>> listEndParams = new ArrayList<>();

                    if (pair.first.equals("@FLAG_DALL'INIZIO")){
                        listStartParams.add(new Pair<>(0 , null));
                    } else {
                        listStartParams = findOccurrences(notepadText, pair.first);
                    }
                    if (pair.second.equals("@FLAG_FINO_ALLA_FINE")){
                        listEndParams.add(new Pair<>(null, notepadText.length()));
                    } else {
                        listEndParams = findOccurrences(notepadText, pair.second);
                    }

                    if (listStartParams.size() > 0 && listEndParams.size() > 0){
                        if (listStartParams.size() > 1) {
                            List<Pair<Integer, Integer>> filtered = innerFilterOccurrences(listStartParams, cursPos, startSlctd, endSlctd);
                            if (filtered != null && filtered.size() == 1){
                                listStartParams = filtered;
                            }
                        }
                        if (listEndParams.size() > 1){
                            List<Pair<Integer, Integer>> filtered = innerFilterOccurrences(listEndParams, cursPos, startSlctd, endSlctd);
                            if (filtered != null && filtered.size() == 1){
                                listEndParams = filtered;
                            }
                        }
                        startParams = listStartParams;
                        endParams = listEndParams;
                    }
                } else if (pair.first != null){
                    // Comando vocale incompleto più input tattile. Ad es. "Copia da startString"
                    // Cerco la stringa iniziale per ottenerne i parametri
                    List<Pair<Integer, Integer>> listStartParams = findOccurrences(notepadText, pair.first);
                    if (listStartParams.size() > 0){
                        listStartParams = outerFilterOccurrences(listStartParams, cursPos, startSlctd, endSlctd, true);

                        if (listStartParams != null && listStartParams.size() > 0){
                            startParams = listStartParams;
                            if (startSlctd != null && endSlctd != null){
                                endParams.add(new Pair<>(startSlctd, endSlctd));
                            } else {
                                if (cursWord != null){
                                    int cursStart = getWordStartPositionFromCursor(cursPos, cursWord);
                                    int cursEnd = getWordLastPositionFromCursor(cursPos, cursWord);
                                    if (cursStart != startParams.get(0).first && cursEnd != startParams.get(0).second){
                                        endParams.add(new Pair<>(cursStart, cursEnd));
                                    }
                                } else {
                                    if(cursPos > startParams.get(0).second+1){
                                        endParams.add(new Pair<>(0, cursPos));
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Comando vocale incompleto più input tattile. Ad es. "Copia fino a endString"
                    // Cerco la stringa finale per ottenerne i parametri
                    List<Pair<Integer, Integer>> listEndParams = findOccurrences(notepadText, pair.second);
                    if (listEndParams.size() > 0){
                        listEndParams = outerFilterOccurrences(listEndParams, cursPos, startSlctd, endSlctd, false);

                        if (listEndParams != null && listEndParams.size() > 0){
                            endParams = listEndParams;
                            if (startSlctd != null && endSlctd != null){
                                startParams.add(new Pair<>(startSlctd, endSlctd));
                            } else {
                                if (cursWord != null){
                                    int cursStart = getWordStartPositionFromCursor(cursPos, cursWord);
                                    int cursEnd = getWordLastPositionFromCursor(cursPos, cursWord);
                                    if (cursStart != endParams.get(0).first && cursEnd != endParams.get(0).second){
                                        startParams.add(new Pair<>(cursStart, cursEnd));
                                    }
                                } else {
                                    if (cursPos < endParams.get(0).first-1){
                                        startParams.add(new Pair<>(0, cursPos));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (startParams.isEmpty() || endParams.isEmpty()) return null;
        return new Pair<>(startParams,endParams);
    }

    private static Pair<List<Pair<Integer, Integer>>, List<Pair<Integer, Integer>>> filterParams(Pair<List<Pair<Integer, Integer>>, List<Pair<Integer, Integer>>> par){
        List<Pair<Integer, Integer>> filteredStartList = new ArrayList<>();
        List<Pair<Integer, Integer>> filteredEndList = new ArrayList<>();
        boolean x ;
        int i;

        for (Pair<Integer, Integer> startOccurrence: par.first ) {
            x = false;
            i = 0;
            while (!x && i < par.second.size()){
                if (startOccurrence.second < par.second.get(i).first){
                    filteredStartList.add(startOccurrence);
                    x = true;
                }
                i++;
            }
        }
        for (Pair<Integer, Integer> endOccurrence: par.second ) {
            x = false;
            i = 0;
            while (!x && i < filteredStartList.size()){
                if (endOccurrence.first > filteredStartList.get(i).second){
                    filteredEndList.add(endOccurrence);
                    x = true;
                }
                i++;
            }
        }

        return new Pair<>(filteredStartList, filteredEndList);
    }

    private static List<Pair<Integer, Integer>> findOccurrences(String text, String searchString) {
        List<Pair<Integer, Integer>> occurrences = new ArrayList<>();
        int currentIndex = 0;

        text = text.toLowerCase();
        searchString = searchString.toLowerCase();

        while (currentIndex != -1) {
            currentIndex = text.indexOf(searchString, currentIndex);
            if (currentIndex != -1) {
                int startIndex = currentIndex;
                int endIndex = currentIndex + searchString.length();
                occurrences.add(new Pair<>(startIndex, endIndex));
                currentIndex += searchString.length();
            }
        }
        return occurrences;
    }

    private static List<Pair<Integer, Integer>> innerFilterOccurrences(List<Pair<Integer, Integer>> occurrences, Integer cursorPos, Integer startSelection, Integer endSelection){
        if (cursorPos == null && startSelection == null && endSelection == null){
            return occurrences;
        }

        List<Pair<Integer, Integer>> filtered = new ArrayList<>();
        for (Pair<Integer, Integer> occurrence : occurrences) {
            if ((cursorPos != null) && (cursorPos >= occurrence.first && cursorPos <= occurrence.second)) {
                if (filtered.isEmpty()) {
                    filtered.add(occurrence);
                } else {
                    return occurrences;
                }
            } else if ((startSelection != null && endSelection != null) && (startSelection >= occurrence.first && endSelection <= occurrence.second)) {
                if (filtered.isEmpty()) {
                    filtered.add(occurrence);
                } else {
                    return occurrences;
                }
            }
        }
        return filtered;
    }

    private static List<Pair<Integer, Integer>> outerFilterOccurrences(List<Pair<Integer, Integer>> occurrences, Integer cursorPos, Integer startSelection, Integer endSelection, Boolean flagBeforeAfter) {
        if (cursorPos == null && startSelection == null && endSelection == null){
            return null;
        }

        List<Pair<Integer, Integer>> filtered = new ArrayList<>();
        if (flagBeforeAfter) {
            for (Pair<Integer, Integer> occurrence : occurrences) {
                if ((startSelection != null && endSelection != null && occurrence.second <= startSelection) || (cursorPos != null && occurrence.second <= cursorPos)){
                    filtered.add(occurrence);
                }
            }
        } else {
            for (Pair<Integer, Integer> occurrence : occurrences) {
                if ((startSelection != null && endSelection != null && occurrence.first >= endSelection) || (cursorPos != null && occurrence.first >= cursorPos)){
                    filtered.add(occurrence);
                }
            }
        }

        return filtered;
    }

    private String getWord(int cursorPos){
        Spannable txtSpan = notepad.getText();
        final Pattern pattern = Pattern.compile("\\w+(-\\w+)*");
        final Matcher matcher = pattern.matcher(txtSpan);
        int start;
        int end;
        String word = "";

        while (matcher.find()){
            start = matcher.start();
            end = matcher.end();
            if (start < cursorPos && cursorPos < end){
                word = txtSpan.subSequence(start, end).toString();
                break;
            }
        }

        return word;
    }
    private void selectWord(){
        Layout layout = notepad.getLayout();
        int cursorPos = notepad.getSelectionStart();
        int line = layout.getLineForOffset(cursorPos);
        int lineStart = layout.getLineStart(line);
        int lineEnd = layout.getLineEnd(line);

        String lineText = notepad.getText().toString().substring(lineStart, lineEnd);
        String[] words = lineText.split("\\s*[?!.,:]?\\s+");

        for (int i = words.length - 1; i >= 0; i--) {
            int wordStart = lineStart + lineText.indexOf(words[i]);
            int wordEnd = wordStart + words[i].length();
            if (cursorPos >= wordStart && cursorPos <= wordEnd) {
                notepad.setSelection(wordStart, wordEnd);
                break;
            }
        }
    }
    private int getWordLastPositionFromCursor(int pos, String word){
        String notepadText = notepad.getText().toString();
        String punctuationMarks = ".?!,;:";

        int end = pos + word.length();
        for (int i = pos; i < end; i++){
            if (Character.isWhitespace(notepadText.charAt(i)) || Character.isSpaceChar(notepadText.charAt(i)) || punctuationMarks.contains(String.valueOf(notepadText.charAt(i)))){
                end = i;
                break;
            }
        }
        return end;
    }
    private int getWordStartPositionFromCursor(int pos, String word) {
        String notepadText = notepad.getText().toString();
        String punctuationMarks = ".?!,;:";

        int start = pos - word.length();
        for (int i = pos - 1; i >= start; i--) {
            if (i == -1 || Character.isWhitespace(notepadText.charAt(i)) || Character.isSpaceChar(notepadText.charAt(i)) || punctuationMarks.contains(String.valueOf(notepadText.charAt(i)))) {
                start = i + 1;
                break;
            }
        }
        return start;
    }

    private Pair<Integer, Integer> getSentenceParams(Integer startSlctd, Integer endSlctd){
        String notepadText = notepad.getText().toString();
        int startSentence = startSlctd;
        int endSentence = endSlctd;

        for (int i = startSlctd; i >= 0; i--) {
            char c = notepadText.charAt(i);
            if (c == '.' || c == ';' || c == ':' || c == '!' || c == '?' || c == '\n') {
                startSentence = i + 1;
                break;
            }
        }
        for (int i = endSlctd; i < notepadText.length(); i++) {
            char c = notepadText.charAt(i);
            if (c == '.' || c == ';' || c == ':' || c == '!' || c == '?' || c == '\n') {
                endSentence = i + 1;
                break;
            }
        }

        return new Pair<>(startSentence, endSentence);
    }

    private Pair<Integer, Integer> getParagraphParams(Integer startSlctd, Integer endSlctd){
        String notepadText = notepad.getText().toString();
        String lineSeparator = System.getProperty("line.separator");
        assert lineSeparator != null;
        int startParagraph = notepadText.lastIndexOf(lineSeparator, startSlctd - 1);
        int endParagraph = notepadText.indexOf(lineSeparator, endSlctd);

        if (startParagraph == -1){
            startParagraph = 0;
        } else {
            startParagraph += lineSeparator.length();
        }

        if (endParagraph == -1){
            endParagraph = notepadText.length();
        }
        return new Pair<>(startParagraph, endParagraph);
    }

    private List<Pair<Integer, Integer>> getParamsOfLexicalEntity(Integer startSlctd, Integer endSlctd,
                                                                  Integer cursorPos, String cursorWord,
                                                                  Integer scope){
        List<Pair<Integer, Integer>> params = new ArrayList<>();
        if (startSlctd != null && endSlctd != null){
            if (scope == 2){ // Parola
                params.add(new Pair<>(
                        notepad.getSelectionStart(),
                        notepad.getSelectionEnd())
                );
            } else if (scope == 3) { // Frase
                // Cercare all'indietro finché non c'è un punto, oppure finisce il testo, oppure c'è una riga vuota
                // Cercare in avanti finché non c'è un punto, oppure finisce il testo, oppure c'è una riga vuota
                Pair<Integer, Integer> sentenceParams = getSentenceParams(startSlctd, endSlctd);
                params.add(sentenceParams);
            } else if (scope == 4){ // Paragrafo
                // Cercare all'indietro finché non c'è una riga vuota, oppure finisce il testo
                // Cercare in avanti finché non c'è una riga vuota, oppure finisce il testo
                Pair<Integer, Integer> sentenceParams = getParagraphParams(startSlctd, endSlctd);
                params.add(sentenceParams);
            }
        } else if (cursorPos != null) {
            if (scope == 2){ // Parola
                params.add(new Pair<>(
                        getWordStartPositionFromCursor(cursorPos, cursorWord),
                        getWordLastPositionFromCursor(cursorPos, cursorWord))
                );
            } else if (scope == 3) { // Frase
                Pair<Integer, Integer> sentenceParams = getSentenceParams(cursorPos, cursorPos);
                params.add(sentenceParams);
            } else if (scope == 4){ // Paragrafo
                Pair<Integer, Integer> sentenceParams = getParagraphParams(cursorPos, cursorPos);
                params.add(sentenceParams);
            }
        }
        return params;
    }

    @SuppressWarnings("DuplicateExpressions")
    private String addSpace(String utterance, int pos){
        String notepadText = notepad.getText().toString();
        String punctuationMarks = ".?!,;:";

        // Se la destinazione è preceduta da ".", "?" o "!" --> Imposto la prima lettera maiuscola
        if (pos < notepadText.length() && notepadText.substring(0, pos+1).matches(".*[?!.](\\s+)?$")){
            utterance = utterance.substring(0, 1).toUpperCase() + utterance.substring(1);
            if (pos > 0 && !Character.isWhitespace(notepadText.charAt(pos-1)) && !Character.isSpaceChar(notepadText.charAt(pos-1))){
                // Se il carattere che precede il cursore non è uno spazio --> Aggiungo uno spazio prima dell'utterance
                utterance = " " + utterance;
            }
        } else {
            if (pos > 0 && !Character.isWhitespace(notepadText.charAt(pos-1)) && !Character.isSpaceChar(notepadText.charAt(pos-1))){
                // Se il carattere che precede il cursore non è uno spazio --> Aggiungo uno spazio prima dell'utterance
                utterance = " " + utterance;
            }
            if ((pos == notepadText.length()) || (!Character.isWhitespace(notepadText.charAt(pos)) && !Character.isSpaceChar(notepadText.charAt(pos)) && !punctuationMarks.contains(String.valueOf(notepadText.charAt(pos))))){
                // Se il carattere che segue il cursore non è uno spazio o un segno di punteggiatura --> Aggiungo uno spazio dopo l'utterance
                utterance = utterance + " ";
            }
        }

        return utterance;
    }


    @Override
    public void onNotepad(Integer startSelection, Integer endSelection, List<Pair<Integer, Integer>> startAbsolute, List<Pair<Integer, Integer>> endAbsolute){
        notepad.requestFocus();
        undoing = true;

        Spannable notepadSpan = notepad.getText();

        BackgroundColorSpan bckgrndSpan = new BackgroundColorSpan(ContextCompat.getColor(MainActivity.this, R.color.gray_transparent));
        BackgroundColorSpan[] existingSpans = notepadSpan.getSpans(0, notepadSpan.length(), BackgroundColorSpan.class);
        for (BackgroundColorSpan span : existingSpans) {
            if (span.getBackgroundColor() == ContextCompat.getColor(MainActivity.this, R.color.gray_transparent)) {
                notepadSpan.removeSpan(span);
            }
        }

        if (startAbsolute == null && endAbsolute != null && startSelection != null){
            notepadSpan.setSpan(bckgrndSpan, startSelection, endAbsolute.get(0).second, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            notepad.setText(notepadSpan);
        } else if (startAbsolute != null && endAbsolute == null && endSelection != null){
            notepadSpan.setSpan(bckgrndSpan, startAbsolute.get(0).first, endSelection, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            notepad.setText(notepadSpan);
        } else if (startAbsolute != null && endAbsolute != null && startSelection == null && endSelection == null){
            notepadSpan.setSpan(bckgrndSpan, startAbsolute.get(0).first, endAbsolute.get(0).second, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            notepad.setText(notepadSpan);
        }

        Selection.removeSelection(notepad.getText());
        if (startSelection != null && endSelection != null){
            notepad.setSelection(startSelection, endSelection);
        }
        undoing = false;
    }

    @Override
    public void onMicCallback() {
        AppCompatImageButton microphone_button = findViewById(R.id.microphone_button);
        microphone_button.setOnClickListener(v -> {
            if (isListening){
                speechRecognizer.stopListening();
                speechRecognizer.cancel();
                isListening = false;
                notepad.setShowSoftInputOnFocus(true);
            } else {
                speechRecognizer.startListening(speechRecognizerIntent);
                isListening = true;
            }
            ((VoiceInterfaceFragment1) Objects.requireNonNull(getSupportFragmentManager().
                    findFragmentById(R.id.fragment_container))).
                    updateFragmentOne(isListening);
        });
    }

    @Override
    public void executeAction(Pair<List<Pair<Integer, Integer>>, List<Pair<Integer, Integer>>> parameters){
        if (parameters == null || parameters.first.size() == 0 && parameters.second.size() == 0) {
            Toast.makeText(this, "Comando incompleto o non riconosciuto", Toast.LENGTH_SHORT).show();
            return;
        }

        int startAction = parameters.first.get(0).first;
        int endAction;
        if (parameters.second != null && parameters.second.size() > 0) {
            endAction = parameters.second.get(0).second;
        } else {
            endAction = parameters.first.get(0).second;
        }

        String notepadText = notepad.getText().toString();
        Editable editable = notepad.getText();

        if (endAction < notepadText.length()-1){
            char firstChar = notepadText.charAt(startAction);
            String nextChar = notepadText.substring(endAction, endAction+1);
            if (nextChar.matches("[.!?]") && Character.isUpperCase(firstChar)){
                endAction++;
            }
        }

        switch (intent){
            case "#search":
                notepad.setSelection(endAction);
                visualFeedback(startAction, treatedUtterance);
                break;
            case "#paste":
                if (pasteAfter){
                    if (String.valueOf(notepadText.charAt(endAction)).matches("^[?!.]$")) endAction = endAction+1;
                    String strClipboard = addSpace(clipboardText, endAction);
                    editable.insert(endAction, strClipboard);
                    visualFeedback(endAction, strClipboard);
                } else {
                    String strClipboard = addSpace(clipboardText, startAction-1);
                    editable.insert(startAction, strClipboard);
                    visualFeedback(startAction, strClipboard);
                }
                break;
            case "#select":
                notepad.setSelection(startAction, endAction);
                LinearLayoutCompat generalToolsLayout = findViewById(R.id.generalToolsLayout);
                generalToolsLayout.setVisibility(View.VISIBLE);
                final int finalEndAction = notepad.getSelectionEnd();

                cutBtn.setOnClickListener(v -> {
                    String actionText = notepadText.substring(startAction, finalEndAction);
                    ClipData clipData = ClipData.newPlainText("actionText", actionText);
                    ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboardManager.setPrimaryClip(clipData);

                    SpannableStringBuilder spannableNotepadString = new SpannableStringBuilder(notepad.getText());
                    spannableNotepadString.delete(startAction, finalEndAction);
                    notepad.setText(spannableNotepadString);
                    notepad.setSelection(startAction);

                    generalToolsLayout.setVisibility(View.GONE);
                });

                copyBtn.setOnClickListener(v -> {
                    String actionText = notepadText.substring(startAction, finalEndAction);
                    ClipData clipData = ClipData.newPlainText("actionText", actionText);
                    ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboardManager.setPrimaryClip(clipData);
                    notepad.setSelection(finalEndAction);
                    visualFeedback(startAction, actionText);

                    generalToolsLayout.setVisibility(View.GONE);
                });

                deleteBtn.setOnClickListener(v -> {
                    SpannableStringBuilder spannableNotepadString = new SpannableStringBuilder(notepad.getText());
                    spannableNotepadString.delete(startAction, finalEndAction);
                    notepad.setText(spannableNotepadString);
                    notepad.setSelection(startAction);
                    Toast.makeText(MainActivity.this, "Testo rimosso", Toast.LENGTH_SHORT).show();

                    generalToolsLayout.setVisibility(View.GONE);
                });

                boldBtn.setOnClickListener(v -> {
                    Spannable spannableString = notepad.getText();
                    spannableString.setSpan(new StyleSpan(Typeface.BOLD), startAction, finalEndAction,0);
                    notepad.setText(spannableString);

                    generalToolsLayout.setVisibility(View.GONE);
                });

                highlightBtn.setOnClickListener(v -> {
                    Spannable spannableNotepad = notepad.getText();
                    spannableNotepad.setSpan(new BackgroundColorSpan(Color.YELLOW), startAction, finalEndAction, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    notepad.setText(spannableNotepad);
                    notepad.setSelection(finalEndAction);

                    generalToolsLayout.setVisibility(View.GONE);
                });

                break;
            case "#highlight":
                Spannable spannableNotepad = notepad.getText();
                spannableNotepad.setSpan(new BackgroundColorSpan(Color.YELLOW), startAction, endAction, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                notepad.setText(spannableNotepad);
                notepad.setSelection(endAction);
                break;
            default:
                String actionText = notepadText.substring(startAction, endAction);
                if (intent.equals("#copy") || intent.equals("#cut")){
                    ClipData clipData = ClipData.newPlainText("actionText", actionText);
                    ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboardManager.setPrimaryClip(clipData);

                    if (intent.equals("#copy")){
                        notepad.setSelection(endAction);
                        visualFeedback(startAction, actionText);
                    }
                }
                if (intent.equals("#delete") || intent.equals("#cut")){
                    SpannableStringBuilder spannableNotepadString = new SpannableStringBuilder(notepad.getText());
                    spannableNotepadString.delete(startAction, endAction);
                    notepad.setText(spannableNotepadString);

                    notepad.setSelection(startAction);
                    if (intent.equals("#delete")){
                        Toast.makeText(MainActivity.this, "Testo rimosso", Toast.LENGTH_SHORT).show();
                    }
                }
        }

        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragment_container, new VoiceInterfaceFragment1(), "FIRST_FRAGMENT")
                .addToBackStack(null)
                .commit();

        FrameLayout fragmentContainer = findViewById(R.id.fragment_container);
        showKeyboard(fragmentContainer);

        clearBtn.setEnabled(true);
        tutorialBtn.setEnabled(true);
        clearBtn.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.blue_1));
        tutorialBtn.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.blue_1));
    }


    private void visualFeedback(int start, String text){
        notepad.setSelection(start, start + text.length());
        notepad.postDelayed(() -> notepad.setSelection(start + text.length()), 1000);
    }

    private void removeHighlightGraySpan(){
        Spannable notepadSpan = notepad.getText();
        BackgroundColorSpan[] existingSpans = notepadSpan.getSpans(0, notepadSpan.length(), BackgroundColorSpan.class);
        for (BackgroundColorSpan span : existingSpans) {
            if (span.getBackgroundColor() == ContextCompat.getColor(MainActivity.this, R.color.gray_transparent)) {
                notepadSpan.removeSpan(span);
            }
        }
    }

    private void updateUIToFragment(){
        smallMicBtn.setVisibility(View.VISIBLE);
        clearBtn.setEnabled(true);
        tutorialBtn.setEnabled(true);
        clearBtn.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.blue_1));
        tutorialBtn.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.blue_1));
    }

    private void disableButtons(){
        tutorialBtn.setEnabled(false);
        clearBtn.setEnabled(false);
        tutorialBtn.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.gray_7));
        clearBtn.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.gray_7));
    }

    private void initializeFragmentOne(){
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragment_container, new VoiceInterfaceFragment1(), "FIRST_FRAGMENT")
                .addToBackStack(null)
                .commit();

        FrameLayout fragmentContainer = findViewById(R.id.fragment_container);
        fragmentContainer.setVisibility(View.GONE);
        showKeyboard(fragmentContainer);
    }
}