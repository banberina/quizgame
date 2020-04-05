package ba.edu.ibu.stu.classes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SQLHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "banberina.db";
    public static final int DATABASE_VERSION = 1;

    public static final String DATABASE_TABLE_KATEGORIJE = "Kategorije";
    private static final String KATEGORIJA_ID = "_id";
    private static final String KATEGORIJA_NAZIV = "nazivKategorije";
    private static final String KATEGORIJA_IKONICA = "idIkonice";

    private static final String DATABASE_TABLE_PITANJA = "Pitanja";
    private static final String PITANJE_ID = "_id";
    private static final String PITANJE_NAZIV = "nazivPitanja";
    private static final String PITANJE_ODGOVORI = "odgovori";
    private static final String PITANJE_INDEX_TACNOG = "indexTacnog";

    private static final String DATABASE_TABLE_RANGLISTE = "Rangliste";
    private static final String RANGLISTA_ID = "_id";
    private static final String RANGLISTA_NAZIV_KVIZA = "nazivKviza";
    private static final String RANGLISTA_NAZIV_IGRACA = "imeIgraca";
    private static final String RANGLISTA_PROCENAT_TACNIH = "procenat";
    private static final String RANGLISTA_POZICIJA = "pozicija";

    private static final String DATABASE_TABLE_KVIZOVI = "Kvizovi";
    private static final String KVIZ_ID = "_id";
    private static final String KVIZ_NAZIV = "nazivKviza";
    private static final String KVIZ_ID_KATEGORIJE = "idKategorije";
    private static final String KVIZ_PITANJA = "pitanja";

    private static final String DATABASE_CREATE_KATEGORIJE = "CREATE TABLE " +
            DATABASE_TABLE_KATEGORIJE + " ("
            + KATEGORIJA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + KATEGORIJA_NAZIV + " TEXT NOT NULL, "
            + KATEGORIJA_IKONICA + " INTEGER NOT NULL);";

    private static final String DATABASE_CREATE_PITANJA = "CREATE TABLE " +
            DATABASE_TABLE_PITANJA + " ("
            + PITANJE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + PITANJE_NAZIV + " TEXT NOT NULL, "
            + PITANJE_INDEX_TACNOG + " INTEGER NOT NULL, "
            + PITANJE_ODGOVORI + " TEXT NOT NULL);";

    private static final String DATABASE_CREATE_RANGLISTE = "CREATE TABLE " +
            DATABASE_TABLE_RANGLISTE + " ("
            + RANGLISTA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + RANGLISTA_NAZIV_KVIZA + " TEXT NOT NULL, "
            + RANGLISTA_POZICIJA + " TEXT NOT NULL, "
            + RANGLISTA_NAZIV_IGRACA + " TEXT NOT NULL, "
            + RANGLISTA_PROCENAT_TACNIH + " TEXT NOT NULL);";

    private static final String DATABASE_CREATE_KVIZOVI = "CREATE TABLE " +
            DATABASE_TABLE_KVIZOVI + " ("
            + KVIZ_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + KVIZ_NAZIV + " TEXT NOT NULL, "
            + KVIZ_ID_KATEGORIJE + " TEXT NOT NULL, "
            + KVIZ_PITANJA + " TEXT NOT NULL);";


    public SQLHelper(Context c) {
        super(c, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE_KATEGORIJE);
        db.execSQL(DATABASE_CREATE_PITANJA);
        db.execSQL(DATABASE_CREATE_RANGLISTE);
        db.execSQL(DATABASE_CREATE_KVIZOVI);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_KATEGORIJE);
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_PITANJA);
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_RANGLISTE);
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_KVIZOVI);
        onCreate(db);
    }

    public void obrisiSve() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + DATABASE_TABLE_KATEGORIJE);
        db.execSQL("DELETE FROM " + DATABASE_TABLE_PITANJA);
        //db.execSQL("DELETE FROM "+ DATABASE_TABLE_RANGLISTE);
        db.execSQL("DELETE FROM " + DATABASE_TABLE_KVIZOVI);
    }

    public ArrayList<Category> pokupiSveKategorije() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c;
        ArrayList<Category> listCategory = new ArrayList<>();
        Category newCategory = new Category();
        c = db.rawQuery("SELECT * FROM " + DATABASE_TABLE_KATEGORIJE, null);
        int ID_KATEGORIJE = c.getColumnIndexOrThrow(KATEGORIJA_ID);
        int NAZIV_KATEGORIJE = c.getColumnIndexOrThrow(KATEGORIJA_NAZIV);
        int ID_IKONICE_KATEGORIJE = c.getColumnIndexOrThrow(KATEGORIJA_IKONICA);
        while (c.moveToNext()) {
            newCategory = new Category();
            newCategory.setNaziv(c.getString(NAZIV_KATEGORIJE));
            newCategory.setId(Integer.toString(c.getInt(ID_IKONICE_KATEGORIJE)));
            listCategory.add(newCategory);
        }
        c.close();
        return listCategory;
    }

    public ArrayList<Question> pokupiSvaPitanja() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c;
        ArrayList<Question> listaPitanja = new ArrayList<>();
        Question pit = new Question();
        c = db.rawQuery("SELECT * FROM " + DATABASE_TABLE_PITANJA, null);
        int ID_PITANJA = c.getColumnIndexOrThrow(PITANJE_ID);
        int NAZIV_PITANJA = c.getColumnIndexOrThrow(PITANJE_NAZIV);
        int INDEX_TACNOG_PITANJA = c.getColumnIndexOrThrow(PITANJE_INDEX_TACNOG);
        int ODGOVORI_PITANJA = c.getColumnIndexOrThrow(PITANJE_ODGOVORI);
        while (c.moveToNext()) {
            pit = new Question();
            pit.setNaziv(c.getString(NAZIV_PITANJA));
            pit.setTekstPitanja(c.getString(NAZIV_PITANJA));
            pit.getOdgovori().addAll(Arrays.asList(c.getString(ODGOVORI_PITANJA).split(",")));
            pit.setTacan(pit.getOdgovori().get(c.getInt(INDEX_TACNOG_PITANJA)));
            listaPitanja.add(pit);
        }
        c.close();
        return listaPitanja;
    }

    public ArrayList<Quiz> pokupiSveKvizove() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c;
        ArrayList<Quiz> listaKvizova = new ArrayList<>();
        ArrayList<Question> svaPitanja = pokupiSvaPitanja();
        ArrayList<Category> sveKategorije = pokupiSveKategorije();
        Quiz k = new Quiz();
        c = db.rawQuery("SELECT * FROM " + DATABASE_TABLE_KVIZOVI, null);
        int ID_KVIZA = c.getColumnIndexOrThrow(KVIZ_ID);
        int NAZIV_KVIZA = c.getColumnIndexOrThrow(KVIZ_NAZIV);
        int ID_KATEGORIJE_KVIZA = c.getColumnIndexOrThrow(KVIZ_ID_KATEGORIJE);
        int PITANJA_KVIZA = c.getColumnIndexOrThrow(KVIZ_PITANJA);
        while (c.moveToNext()) {
            k = new Quiz();
            k.setNaziv(c.getString(NAZIV_KVIZA));
            for(int i = 0; i < sveKategorije.size(); i++){
                if(sveKategorije.get(i).getName().equals(c.getString(ID_KATEGORIJE_KVIZA))){
                    k.setCategory(sveKategorije.get(i));
                    break;
                }
            }
            String[] pitanjaString = c.getString(PITANJA_KVIZA).split(",");
            ArrayList<String> listaPitanjaStringova = new ArrayList<>();
            for(int i = 0; i < pitanjaString.length; i++){
                listaPitanjaStringova.add(pitanjaString[i]);
            }
            if(listaPitanjaStringova.size() == 1 && listaPitanjaStringova.get(0).equals("")){
                listaPitanjaStringova.clear();
            }

            for(int i = 0; i < svaPitanja.size(); i++){
                for(int j = 0; j < listaPitanjaStringova.size(); j++){
                    if(listaPitanjaStringova.get(j).equals(svaPitanja.get(i).getNaziv())){
                        k.getQuestions().add(svaPitanja.get(i));
                    }
                }
            }

            listaKvizova.add(k);
        }
        c.close();
        return listaKvizova;
    }

    public long dodajKategorijuUSQL(Category category) {
        ContentValues noveVrijednosti = new ContentValues();
        noveVrijednosti.put(KATEGORIJA_NAZIV, category.getName());
        noveVrijednosti.put(KATEGORIJA_IKONICA, category.getId());

        SQLiteDatabase db = this.getWritableDatabase();
        long noviRedID = db.insert(DATABASE_TABLE_KATEGORIJE, null, noveVrijednosti);
        return noviRedID;
    }

    public long dodajPitanjeUSQL(Question question) {
        ArrayList<String> odgovori = question.getOdgovori();

        String stringSvihOdgovora = "";

        for (int i = 0; i < odgovori.size(); i++) {
            stringSvihOdgovora += odgovori.get(i);
            if (i == odgovori.size() - 1) {

            } else {
                stringSvihOdgovora += ",";
            }
        }

        int index = -1;
        for (int i = 0; i < odgovori.size(); i++) {
            if (odgovori.get(i).equals(question.getTacan())) {
                index = i;
            }
        }

        ContentValues noveVrijednosti = new ContentValues();
        noveVrijednosti.put(PITANJE_NAZIV, question.getNaziv());
        noveVrijednosti.put(PITANJE_INDEX_TACNOG, index);
        noveVrijednosti.put(PITANJE_ODGOVORI, stringSvihOdgovora);

        SQLiteDatabase db = this.getWritableDatabase();
        long noviRedID = db.insert(DATABASE_TABLE_PITANJA, null, noveVrijednosti);
        return noviRedID;
    }

    public long dodajKvizUSQL(Quiz quiz) {

        ArrayList<Category> sveKategorije = pokupiSveKategorije();
        String kategorijaId = "Svi";
        for (int i = 0; i < sveKategorije.size(); i++) {
            if (sveKategorije.get(i).getName().equals(quiz.getCategory().getName())) {
                kategorijaId = sveKategorije.get(i).getName();
            }
        }

        ArrayList<Question> svaPitanja = pokupiSvaPitanja();
        String sviIdPitanjaPotrebni = "";
        for (int i = 0; i < quiz.getQuestions().size(); i++) {
            for (int j = 0; j < svaPitanja.size(); j++) {
                if (svaPitanja.get(j).getNaziv().equals(quiz.getQuestions().get(i).getNaziv())) {
                    sviIdPitanjaPotrebni += svaPitanja.get(j).getNaziv();
                    if (i != quiz.getQuestions().size() - 1) {
                        sviIdPitanjaPotrebni += ",";
                    }
                }
            }
        }

        ContentValues noveVrijednosti = new ContentValues();
        noveVrijednosti.put(KVIZ_NAZIV, quiz.getNaziv());
        noveVrijednosti.put(KVIZ_ID_KATEGORIJE, kategorijaId);
        noveVrijednosti.put(KVIZ_PITANJA, sviIdPitanjaPotrebni);

        SQLiteDatabase db = this.getWritableDatabase();
        long noviRedID = db.insert(DATABASE_TABLE_KVIZOVI, null, noveVrijednosti);
        return noviRedID;
    }

    public long dodajRangListuUSQL(String nazivKviza, String pozicija, String ime, String procenat) {
        ContentValues noveVrijednosti = new ContentValues();
        noveVrijednosti.put(RANGLISTA_NAZIV_KVIZA, nazivKviza);
        noveVrijednosti.put(RANGLISTA_NAZIV_IGRACA, ime);
        noveVrijednosti.put(RANGLISTA_PROCENAT_TACNIH, procenat);
        noveVrijednosti.put(RANGLISTA_POZICIJA, pozicija);

        SQLiteDatabase db = this.getWritableDatabase();
        long noviRedID = db.insert(DATABASE_TABLE_RANGLISTE, null, noveVrijednosti);
        return noviRedID;
    }

    public ArrayList<Pair<String, Map<Integer, Map<String, String>>>> pokupiSveRangListe() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c;
        ArrayList<Pair<String, Map<Integer, Map<String, String>>>>  listaRangListi = new ArrayList<>();
        c = db.rawQuery("SELECT * FROM " + DATABASE_TABLE_RANGLISTE, null);
        int ID_RANGLISTE = c.getColumnIndexOrThrow(RANGLISTA_ID);
        int NAZIV_KVIZA_RANGLISTE = c.getColumnIndexOrThrow(RANGLISTA_NAZIV_KVIZA);
        int NAZIV_IGRACA = c.getColumnIndexOrThrow(RANGLISTA_NAZIV_IGRACA);
        int PROCENAT_RANGLISTA = c.getColumnIndexOrThrow(RANGLISTA_PROCENAT_TACNIH);
        int POZICIJA_RANGLISTA = c.getColumnIndexOrThrow(RANGLISTA_POZICIJA);
        while (c.moveToNext()) {
            Map<String, String> trenutnaDrugaMapa = new HashMap<>();
            trenutnaDrugaMapa.put(c.getString(NAZIV_IGRACA), c.getString(PROCENAT_RANGLISTA));
            Map<Integer, Map<String, String>> trenutnaMapa = new HashMap<>();
            trenutnaMapa.put(Integer.parseInt(c.getString(POZICIJA_RANGLISTA)), trenutnaDrugaMapa);
            Pair par = new Pair(c.getString(NAZIV_KVIZA_RANGLISTE), trenutnaMapa);
            listaRangListi.add(par);
        }
        c.close();
        return listaRangListi;
    }

}
