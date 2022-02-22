package com.luca.applicazioneesame.sqlhelper;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DatabaseHelper extends SQLiteOpenHelper {
    // Logcat tag
    private static final String LOG = "DatabaseHelper";
    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "DBEsame";

    // Table Names
    private static final String TABLE_STUDENTI = "studenti";
    private static final String TABLE_MATERIE = "materie";
    private static final String TABLE_LEZIONI = "lezioni";
    private static final String TABLE_DOCENTI = "docenti";

    // TABELLA STUDENTI - NOMI COLONNE
    private static final String KEY_MATRICOLA = "matricola_ID";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_NOME_ST = "nome_st";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASS = "pass";

    // TABELLA MATERIE - NOMI COLONNE
    private static final String KEY_ID_MATERIA = "materia_ID";
    private static final String KEY_NOME_MATERIA = "nome_materia";

    // TABELLA DOCENTI - NOMI COLONNE
    private static final String KEY_ID_DOCENTE = "docente_ID";
    private static final String KEY_NOME_DOC = "nome_doc";
    private static final String KEY_EXT_ID_MAT = "id_materia";

    // TABELLA LEZIONI - NOMI COLONNE
    private static final String KEY_ID_LEZIONE = "lezione_ID";
    private static final String KEY_EXT_MAT_STUDENTE = "matricola_studente";
    private static final String KEY_EXT_ID_DOCENTE = "id_docente";
    private static final String KEY_STATO = "stato";
    private static final String KEY_DATE = "data";


    // STATEMENT CREAZIONE TABELLE
    // STUDENTI
    private static final String CREATE_TABLE_STUDENTI = "CREATE TABLE " + TABLE_STUDENTI + "("
            + KEY_MATRICOLA + " INTEGER PRIMARY KEY,"
            + KEY_USERNAME + " TEXT,"
            + KEY_NOME_ST + " TEXT,"
            + KEY_EMAIL + " TEXT,"
            + KEY_PASS + " TEXT)";
    // MATERIE
    private static final String CREATE_TABLE_MATERIE = "CREATE TABLE " + TABLE_MATERIE + "("
            + KEY_ID_MATERIA + " INTEGER PRIMARY KEY,"
            + KEY_NOME_MATERIA + " TEXT)";
    // DOCENTI
    private static final String CREATE_TABLE_DOCENTI = "CREATE TABLE " + TABLE_DOCENTI + "("
            + KEY_ID_DOCENTE + " INTEGER PRIMARY KEY,"
            + KEY_NOME_DOC + " TEXT,"
            + KEY_EXT_ID_MAT + " INTEGER,"
            + " FOREIGN KEY (" + KEY_EXT_ID_MAT + ") REFERENCES " + TABLE_MATERIE + "(" + KEY_ID_MATERIA + ")" + ")";

    // LEZIONI
    private static final String CREATE_TABLE_LEZIONI = "CREATE TABLE " + TABLE_LEZIONI + "("
            + KEY_ID_LEZIONE + " INTEGER PRIMARY KEY,"
            + KEY_EXT_MAT_STUDENTE + " INTEGER,"
            + KEY_EXT_ID_DOCENTE + " INTEGER,"
            + KEY_STATO + " TEXT,"
            + KEY_DATE + " TEXT,"
            + " FOREIGN KEY (" + KEY_EXT_MAT_STUDENTE + ") REFERENCES " + TABLE_STUDENTI + "(" + KEY_MATRICOLA + "),"
            + " FOREIGN KEY (" + KEY_EXT_ID_DOCENTE + ") REFERENCES " + TABLE_DOCENTI + "(" + KEY_ID_DOCENTE + ")" + ")";



    // DATI PREFISSATI PER TABELLA STUDENTI
    private static final String INSERT_TABLE_STUDENTI = "INSERT INTO " + TABLE_STUDENTI + "(" + KEY_USERNAME + " ," + KEY_NOME_ST + " ," + KEY_EMAIL + " ," + KEY_PASS + ") VALUES "
            + "(\"Luca13\", \"Luca Nuzzo\", \"luca@email.it\", \"admin\"), (\"Marco13\", \"Marco Simone\", \"marco@email.it\", \"marcosimone\")";

    // DATI PREFISSATI PER TABELLA MATERIE
    private static final String INSERT_TABLE_MATERIE = "INSERT INTO " + TABLE_MATERIE + "(" + KEY_NOME_MATERIA + ") VALUES "
            + "(\"Programmazione Mobile\"), (\"Intelligenza Artificiale\"), (\"Programmazione Avanzata\"), (\"Innovazione Sociale\"), (\"Interazione Uomo-Macchina\")";

    // DATI PREFISSATI PER TABELLA DOCENTI
    private static final String INSERT_TABLE_DOCENTI = "INSERT INTO " + TABLE_DOCENTI + "(" + KEY_NOME_DOC + " ," + KEY_EXT_ID_MAT + ") VALUES "
            + "(\"Marino Segnan\", 1), (\"Amon Rapp\", 2), (\"Luca Console\", 3), (\"Filippo Barbera\", 4), (\"Cristina Gena\", 5), (\"Pippo Balluzzo\", 1), (\"Felice Buonanno\", 1), (\"Gustavo La Pasta\", 2), (\"Rosa Culetto\",5)";

    // DATI PREFISSATI PER TABELLA LEZIONI
    //private static final String INSERT_TABLE_LEZIONI = "INSERT INTO " + TABLE_LEZIONI
    //        + "(" + KEY_EXT_MAT_STUDENTE + " ," + KEY_EXT_ID_DOCENTE + " ," + KEY_STATO + " ," + KEY_DATE + ") VALUES "
    //        + "(1, 1, \"prenotata\", \"10/02/2022 10:00\"), (1, 2, \"prenotata\", \"20/01/2022 10:00\"), (1, 3, \"prenotata\", \"18/01/2022 08:00\"), (1, 4, \"disdetta\", \"10/01/2022 10:00\"), (1, 5, \"frequentata\", \"08/01/2022 14:00\"), (1, 3, \"disdetta\", \"08/01/2022 16:00\")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // CREAZIONE DELLE TABELLE
        db.execSQL(CREATE_TABLE_STUDENTI);
        db.execSQL(CREATE_TABLE_MATERIE);
        db.execSQL(CREATE_TABLE_DOCENTI);
        db.execSQL(CREATE_TABLE_LEZIONI);

        // POPOLAZIONE CON DATI
        db.execSQL(INSERT_TABLE_STUDENTI);
        db.execSQL(INSERT_TABLE_MATERIE);
        db.execSQL(INSERT_TABLE_DOCENTI);
        //db.execSQL(INSERT_TABLE_LEZIONI);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // AGGIORNAMENTO TABELLE
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STUDENTI);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MATERIE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DOCENTI);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LEZIONI);

        onCreate(db);
    }

    // GET STUDENTE FROM MATRICOLA
    @SuppressLint("Range")
    public Studenti getStudenteFromDB(int matricola){
        SQLiteDatabase db = this.getReadableDatabase();
        String sqlQuery = "SELECT * FROM " + TABLE_STUDENTI + " WHERE " + KEY_MATRICOLA + " = " + matricola;
        Log.e(LOG, sqlQuery);
        Cursor c = db.rawQuery(sqlQuery, null);

        if(c != null)
            c.moveToFirst();

        Studenti studente = new Studenti();
        studente.setMatricola(c.getInt(c.getColumnIndex(KEY_MATRICOLA)));
        studente.setUsername(c.getString(c.getColumnIndex(KEY_USERNAME)));
        studente.setNome_st(c.getString(c.getColumnIndex(KEY_NOME_ST)));
        studente.setEmail(c.getString(c.getColumnIndex(KEY_EMAIL)));
        studente.setPass(c.getString(c.getColumnIndex(KEY_PASS)));

        c.close();
        db.close();
        return studente;
    }

    // CONTROLLA SE ESISTE UN INDIRIZZO EMAIL NEL DB STUDENTI
    public boolean checkUser(String email){
        String[] columns = {KEY_MATRICOLA};
        SQLiteDatabase db = this.getReadableDatabase();
        String selectionCriteria = KEY_EMAIL + " = ?";
        String[] selectionArgs = {email};

        Cursor c = db.query(TABLE_STUDENTI, columns, selectionCriteria, selectionArgs, null, null, null);
        int cursorCount = c.getCount();
        c.close();
        db.close();

        return cursorCount > 0;
    }

    // CONTROLLA SE ESISTE UNO STUDENTE CON EMAIL E PASSWORD - RESTITUISCE STUDENTE
    @SuppressLint("Range")
    public Studenti checkUser(String email, String pass){
        SQLiteDatabase db = this.getReadableDatabase();
        String sqlQuery = "SELECT * FROM " + TABLE_STUDENTI + " WHERE " + KEY_EMAIL + " = ? AND " + KEY_PASS + " = ?";
        Log.e(LOG, sqlQuery);
        Cursor c = db.rawQuery(sqlQuery, new String[]{email, pass});

        if(c == null || c.getCount() <= 0){
            c.close();
            db.close();
            return null;
        }

        Studenti studente = new Studenti();
        c.moveToFirst();
        studente.setMatricola(c.getInt(c.getColumnIndex(KEY_MATRICOLA)));
        studente.setUsername(c.getString(c.getColumnIndex(KEY_USERNAME)));
        studente.setNome_st(c.getString(c.getColumnIndex(KEY_NOME_ST)));
        studente.setEmail(c.getString(c.getColumnIndex(KEY_EMAIL)));
        studente.setPass(c.getString(c.getColumnIndex(KEY_PASS)));
        c.close();
        db.close();
        return studente;
    }

    // INSERT NUOVO STUDENTE
    public boolean insertStudente(Studenti studente){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        //values.put(KEY_MATRICOLA, studente.getMatricola());
        values.put(KEY_USERNAME, studente.getUsername());
        values.put(KEY_NOME_ST, studente.getNome_st());
        values.put(KEY_EMAIL, studente.getEmail());
        values.put(KEY_PASS, studente.getPass());

        boolean isSuccessful = db.insert(TABLE_STUDENTI, null, values) > 0;
        db.close();
        return isSuccessful;
    }

    //  GET DOCENTE FROM ID_DOCENTE
    @SuppressLint("Range")
    public Docenti getDocenteFromDB(int id_docente){
        SQLiteDatabase db = this.getReadableDatabase();
        String sqlQuery = "SELECT * FROM " + TABLE_DOCENTI + " WHERE " + KEY_ID_DOCENTE + " = " + id_docente;
        Cursor c = db.rawQuery(sqlQuery, null);

        if(c != null)
            c.moveToFirst();

        Docenti docente = new Docenti();
        docente.setId_docente(c.getInt(c.getColumnIndex(KEY_ID_DOCENTE)));
        docente.setNome_doc(c.getString(c.getColumnIndex(KEY_NOME_DOC)));
        docente.setId_materia(c.getInt(c.getColumnIndex(KEY_EXT_ID_MAT)));

        c.close();
        db.close();
        return docente;
    }

    //  GET DOCENTI FROM MATERIA
    @SuppressLint("Range")
    public ArrayList<Docenti> getDocentiFromMateria(int id_materia){
        ArrayList<Docenti> docenti = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String sqlQuery = "SELECT * FROM " + TABLE_DOCENTI + " WHERE " + KEY_EXT_ID_MAT + " = " + id_materia;
        Cursor c = db.rawQuery(sqlQuery, null);

        if(c.moveToFirst()){
            do{
                Docenti docente = new Docenti();
                docente.setId_docente(c.getInt(c.getColumnIndex(KEY_ID_DOCENTE)));
                docente.setNome_doc(c.getString(c.getColumnIndex(KEY_NOME_DOC)));
                docente.setId_materia(c.getInt(c.getColumnIndex(KEY_EXT_ID_MAT)));

                docenti.add(docente);
            }while (c.moveToNext());
            c.close();
        }
        db.close();
        return docenti;
    }

    // GET MATERIA FROM ID_MATERIA
    @SuppressLint("Range")
    public Materie getMateriaFromDB(int id_materia){
        SQLiteDatabase db = this.getReadableDatabase();
        String sqlQuery = "SELECT * FROM " + TABLE_MATERIE + " WHERE " + KEY_ID_MATERIA+ " = " + id_materia;
        Log.e(LOG, sqlQuery);
        Cursor c = db.rawQuery(sqlQuery, null);

        Materie materia = null;
        if(c != null && c.moveToFirst()){
            materia = new Materie(
                    c.getInt(c.getColumnIndex(KEY_ID_MATERIA)),
                    c.getString(c.getColumnIndex(KEY_NOME_MATERIA))
            );
            c.close();
        }
        db.close();
        return materia;

    }

    //  RICERCA DI MATERIE
    @SuppressLint("Range")
    public ArrayList<Materie> searchMaterie(String user_query){
        ArrayList<Materie> materie = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String simpleQuery = "SELECT * FROM " + TABLE_MATERIE + " WHERE LOWER(" + KEY_NOME_MATERIA + ") = ?";
        Cursor c = db.rawQuery(simpleQuery, new String[]{user_query});

        if (c != null && c.moveToFirst()) {
            Materie materia = new Materie();
            materia.setId_materia(c.getInt(c.getColumnIndex(KEY_ID_MATERIA)));
            materia.setNome_materia((c.getString((c.getColumnIndex(KEY_NOME_MATERIA)))));
            materie.add(materia);
            c.close();
        } else {
            StringBuilder advancedQuery = new StringBuilder("SELECT * FROM " + TABLE_MATERIE + " WHERE " + KEY_NOME_MATERIA + " LIKE '%" + user_query + "%'");
            Pattern pattern = Pattern.compile("\\w+");
            Matcher matcher = pattern.matcher(user_query);
            while (matcher.find()) {
                if(matcher.group().length() > 2){
                    advancedQuery.append(" OR ").append(KEY_NOME_MATERIA).append(" LIKE '%").append(matcher.group()).append("%'");
                }
            }
            advancedQuery.append(" ORDER BY ").append(KEY_NOME_MATERIA).append(" LIKE '%").append(user_query).append("%' DESC");

            c = db.rawQuery(advancedQuery.toString(), null);
            if (c != null && c.moveToFirst()) {
                do {
                    Materie materia = new Materie();
                    materia.setId_materia(c.getInt(c.getColumnIndex(KEY_ID_MATERIA)));
                    materia.setNome_materia((c.getString((c.getColumnIndex(KEY_NOME_MATERIA)))));

                    materie.add(materia);
                } while (c.moveToNext());
                c.close();
            }
        }
        db.close();
        return materie;
    }

    // GET LEZIONE FROM ID_LEZIONE
    @SuppressLint("Range")
    public Lezioni getLezioneFromDB(int id_lezione){
        String sqlQuery = "SELECT * FROM " + TABLE_LEZIONI + " WHERE " + KEY_ID_LEZIONE + " = " + id_lezione;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(sqlQuery, null);

        Lezioni lezione = null;
        if(c != null && c.moveToFirst()){
            lezione = new Lezioni(
                    c.getInt(c.getColumnIndex(KEY_ID_LEZIONE)),
                    c.getInt(c.getColumnIndex(KEY_EXT_MAT_STUDENTE)),
                    c.getInt((c.getColumnIndex(KEY_EXT_ID_DOCENTE))),
                    c.getString(c.getColumnIndex(KEY_STATO)),
                    c.getString(c.getColumnIndex(KEY_DATE))
            );
            c.close();
        }
        db.close();
        return lezione;
    }

    //  GET ALL LEZIONI FROM MATRICOLA
    @SuppressLint("Range")
    public ArrayList<Lezioni> readLezioni(int matricola_studente){
        ArrayList<Lezioni> lezioni = new ArrayList<>();
        String sqlQuery = "SELECT * FROM " + TABLE_LEZIONI + " WHERE " + KEY_EXT_MAT_STUDENTE + " = " + matricola_studente + " ORDER BY " + KEY_ID_LEZIONE + " DESC";
        Log.e(LOG, sqlQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(sqlQuery, null);

        if(c != null && c.moveToFirst()){
            do {
                Lezioni lezione = new Lezioni(
                        c.getInt(c.getColumnIndex(KEY_ID_LEZIONE)),
                        c.getInt(c.getColumnIndex(KEY_EXT_MAT_STUDENTE)),
                        c.getInt((c.getColumnIndex(KEY_EXT_ID_DOCENTE))),
                        c.getString(c.getColumnIndex(KEY_STATO)),
                        c.getString(c.getColumnIndex(KEY_DATE))
                );
                lezioni.add(lezione);
            } while (c.moveToNext());
            c.close();
        }

        db.close();
        return lezioni;
    }

    // GET ORARI NON DISPONIBILI PER DOCENTE E GIORNO
    @SuppressLint("Range")
    public ArrayList<String> getNotAvailableTimes(int id_docente, String giorno){
        ArrayList<String> notAvailableTimes = new ArrayList<>();
        String sqlQuery = "SELECT * FROM " + TABLE_LEZIONI + " WHERE " + KEY_EXT_ID_DOCENTE + " = " + id_docente + " AND " + KEY_DATE + " LIKE '" + giorno + "%' AND " + KEY_STATO + " IN ('prenotata','frequentata')";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(sqlQuery, null);

        if(c != null && c.moveToFirst()){
            do {
                notAvailableTimes.add(c.getString(c.getColumnIndex(KEY_DATE)));
            } while (c.moveToNext());
            c.close();
        }
        db.close();
        return notAvailableTimes;
    }

    //  VERIFICA DISPONIBILITA' STUDENTE PER GIORNO E ORA
    public boolean checkUserAvailability(int matricola, String fullDate){
        String sqlQuery = "SELECT * FROM " + TABLE_LEZIONI + " WHERE " + KEY_EXT_MAT_STUDENTE + " = " + matricola + " AND " + KEY_DATE + " = ? AND " + KEY_STATO + " IN ('prenotata','frequentata')";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(sqlQuery, new String[]{fullDate});
        boolean isNotAvailable = false;
        if(c != null && c.moveToFirst()){
            isNotAvailable = true;
        }
        c.close();
        db.close();
        return isNotAvailable;
    }

    // INSERT LEZIONE
    public boolean insertLezione(Lezioni lezione){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_EXT_MAT_STUDENTE, lezione.getMatricola_studente());
        values.put(KEY_EXT_ID_DOCENTE, lezione.getId_docente());
        values.put(KEY_STATO, lezione.getStato_lezione());
        values.put(KEY_DATE, lezione.getData());

        boolean isSuccessful = db.insert(TABLE_LEZIONI, null, values) > 0;
        db.close();
        return isSuccessful;
    }

    // UPDATE STATO LEZIONE -- modifica in "effettuata" o "disdetta"
    public boolean updateStatoLezione(Lezioni lezione){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_STATO, lezione.getStato_lezione());
        boolean isSuccessfull = db.update(TABLE_LEZIONI, values, KEY_ID_LEZIONE + "='"+lezione.getId_lezione()+"'", null) > 0;
        db.close();
        return isSuccessfull;
    }

    public void deleteLezione(int id_lezione){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_LEZIONI, KEY_ID_LEZIONE + "=" + id_lezione, null);
    }


    // UTILITIES
//    public boolean deleteLezioni(){
//        SQLiteDatabase db = this.getWritableDatabase();
//        return db.delete(TABLE_LEZIONI, null, null) > 0;
//    }
//
//    @SuppressLint("Range")
//    public ArrayList<Studenti> readStudenti(){
//        ArrayList<Studenti> studenti = new ArrayList<>();
//        String sqlQuery = "SELECT * FROM " + TABLE_STUDENTI;
//        Log.e(LOG, sqlQuery);
//        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor c = db.rawQuery(sqlQuery, null);
//        if(c.moveToFirst()){
//            do{
//                Studenti studente = new Studenti();
//                studente.setMatricola(c.getInt(c.getColumnIndex(KEY_MATRICOLA)));
//                studente.setUsername(c.getString(c.getColumnIndex(KEY_USERNAME)));
//                studente.setPass(c.getString(c.getColumnIndex(KEY_PASS)));
//                studente.setEmail(c.getString(c.getColumnIndex(KEY_EMAIL)));
//                studente.setNome_st(c.getString(c.getColumnIndex(KEY_NOME_ST)));
//                studenti.add(studente);
//            }while (c.moveToNext());
//            c.close();
//        }
//        db.close();
//        return studenti;
//    }
//
//    @SuppressLint("Range")
//    public ArrayList<Materie> readMaterie(){
//        ArrayList<Materie> materie = new ArrayList<>();
//        String sqlQuery = "SELECT * FROM " + TABLE_MATERIE;
//        Log.e(LOG, sqlQuery);
//        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor c = db.rawQuery(sqlQuery, null);
//        if(c.moveToFirst()){
//            do{
//                Materie materia = new Materie();
//                materia.setId_materia(c.getInt(c.getColumnIndex(KEY_ID_MATERIA)));
//                materia.setNome_materia(c.getString(c.getColumnIndex(KEY_NOME_MATERIA)));
//                materie.add(materia);
//            }while (c.moveToNext());
//            c.close();
//        }
//        db.close();
//        return materie;
//    }
//
//    @SuppressLint("Range")
//    public ArrayList<Docenti> readDocenti(){
//        ArrayList<Docenti> docenti = new ArrayList<>();
//        String sqlQuery = "SELECT * FROM " + TABLE_DOCENTI;
//        Log.e(LOG, sqlQuery);
//
//        SQLiteDatabase db = this.getReadableDatabase();
//        Cursor c = db.rawQuery(sqlQuery, null);
//
//        if(c.moveToFirst()){
//            do{
//                Docenti docente = new Docenti();
//                docente.setId_docente(c.getInt(c.getColumnIndex(KEY_ID_DOCENTE)));
//                docente.setNome_doc(c.getString(c.getColumnIndex(KEY_NOME_DOC)));
//                docente.setId_materia(c.getInt(c.getColumnIndex(KEY_EXT_ID_MAT)));
//
//                docenti.add(docente);
//            }while (c.moveToNext());
//            c.close();
//        }
//        db.close();
//        return docenti;
//    }


}


