package ba.edu.ibu.stu.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.common.collect.Lists;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import ba.edu.ibu.stu.R;
import ba.edu.ibu.stu.classes.Category;
import ba.edu.ibu.stu.fragments.DetailsFragment;
import ba.edu.ibu.stu.fragments.ListFragment;
import ba.edu.ibu.stu.classes.AsyncResponse;
import ba.edu.ibu.stu.classes.CategoriesAdapter;
import ba.edu.ibu.stu.classes.Quiz;
import ba.edu.ibu.stu.classes.QuizAdapter;
import ba.edu.ibu.stu.classes.NetworkUtil;
import ba.edu.ibu.stu.classes.Question;
import ba.edu.ibu.stu.classes.SQLHelper;

public class QuizzesActivity extends AppCompatActivity implements ListFragment.PressListView {

    private static final int MY_CAL_REQ = 0;

    private ListView lvKvizovi; //listview za kviyove
    private Spinner spPostojeceKategorije; //kategorije
    public static ArrayList<Quiz> kvizovi = new ArrayList<>(); //lista svih kviyova
    public static ArrayList<Quiz> filterKvizovi = new ArrayList<>(); //filtrirani kviyovi, kada biras kateg
    public static ArrayList<Quiz> filterKvizovi2 = new ArrayList<>();
    private QuizAdapter adapter;
    private CategoriesAdapter adapterKategorija;
    public static ArrayList<Category> kategorije = new ArrayList<>(); //sve kateg
    public static ArrayList<Quiz> validacijaKvizova = new ArrayList<>();
    public static int pozicija = 0;
    public static boolean filtrirano = false;
    public static boolean filtrirano2 = false;
    public static ArrayList<Pair<String, Category>> initialDatabaseCategories = new ArrayList<>();
    public static ArrayList<Pair<String, Question>> initialDatabaseQuestions = new ArrayList<>();
    public static ArrayList<Pair<String, Quiz>> initialDatabaseQuizzes = new ArrayList<>();
    public static boolean hasAddQuestion = false;
    private Quiz quizToBeReturned = new Quiz();
    public static boolean nemaInterneta = false;
    public static SQLiteDatabase db = null;
    public static SQLHelper sqlHelper;
    public ArrayList<Pair<String, Map<Integer, Map<String, String>>>> sveRangListeIzSQL = new ArrayList<>();
    public static ArrayList<Question> pitanjaIzSQL = new ArrayList<>();
    public static int trenutnaRangListaUFB = 0;

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public class KlasaUcitajRangListeIzFB extends AsyncTask<String, Void, Void> {

        AsyncResponse ar = null;

        public KlasaUcitajRangListeIzFB(AsyncResponse asyncResponse) {
            this.ar = asyncResponse;
        }

        @Override
        protected Void doInBackground(String... strings) {
            InputStream is = getResources().openRawResource(R.raw.secret);
            try {
                GoogleCredential credentials = GoogleCredential.fromStream(is).createScoped(Lists.newArrayList("https://www.googleapis.com/auth/datastore"));
                credentials.refreshToken();
                String TOKEN = credentials.getAccessToken();
                System.out.println("SADA JE TOKEN: " + TOKEN);

                String urlPitanja = "https://firestore.googleapis.com/v1/projects/quizgame-9bb9e/databases/(default)/documents/RankList?access_token=";
                URL urlObjPitanja = new URL(urlPitanja + URLEncoder.encode(TOKEN, "UTF-8"));
                HttpURLConnection connectionPitanja = (HttpURLConnection) urlObjPitanja.openConnection();
                connectionPitanja.connect();
                InputStream odgovorPitanja = connectionPitanja.getInputStream();
                StringBuilder responsePitanja = null;
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(odgovorPitanja, "utf-8"))
                ) {
                    responsePitanja = new StringBuilder();
                    String rensponseLine = null;
                    while ((rensponseLine = br.readLine()) != null) {
                        responsePitanja.append(rensponseLine.trim());
                    }
                    Log.d("ODGOVOR RANG LISTE: ", responsePitanja.toString());
                }
                String rezultat = responsePitanja.toString();
                trenutnaRangListaUFB = 0;
                if (!rezultat.equals("{}")) {
                    JSONObject jo = new JSONObject(rezultat);
                    JSONArray documents = jo.getJSONArray("documents");
                    for (int i = 0; i < documents.length(); i++) {
                        trenutnaRangListaUFB++;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            ar.processFinish();
        }
    }

    public class KlasaDodajURangListuIzSQLuFB extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            InputStream is = getResources().openRawResource(R.raw.secret);
            try {
                GoogleCredential credentials = GoogleCredential.fromStream(is).createScoped(Lists.newArrayList("https://www.googleapis.com/auth/datastore"));
                credentials.refreshToken();
                String TOKEN = credentials.getAccessToken();
                System.out.println("SADA JE TOKEN: " + TOKEN);
                for (int i = trenutnaRangListaUFB; i < sveRangListeIzSQL.size(); i++) {
                    String url = "https://firestore.googleapis.com/v1/projects/quizgame-9bb9e/databases/(default)/documents/RankList?access_token=";
                    URL urlObj = new URL(url + URLEncoder.encode(TOKEN, "UTF-8"));
                    HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
                    connection.setDoOutput(true);
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setRequestProperty("Accept", "application/json");

                    String dokument = "{\"fields\" : { \"name\" : { \"stringValue\" : \"" + sveRangListeIzSQL.get(i).first + "\" }, \"list\" : { \"mapValue\" : { \"fields\" : { \"position\" : { \"integerValue\" : \"";
                    Iterator it = sveRangListeIzSQL.get(i).second.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry m = (Map.Entry) it.next();
                        int pozicija = (int) m.getKey();
                        dokument += String.valueOf(pozicija);
                        dokument += "\" }, \"map\" : { \"mapValue\" : { \"fields\" : { \"percentage\" : { \"stringValue\" : \"";
                        Map<String, String> druga = (Map<String, String>) m.getValue();
                        Iterator it2 = druga.entrySet().iterator();
                        while (it2.hasNext()) {
                            Map.Entry m2 = (Map.Entry) it2.next();
                            String imeIgraca = (String) m2.getKey();
                            String procenat = (String) m2.getValue();
                            dokument += procenat + "\" }, \"playerName\" : { \"stringValue\" : \"" + imeIgraca + "\" }}}}}}}}}";
                        }
                    }
                    try (OutputStream os = connection.getOutputStream()) {
                        byte[] input = dokument.getBytes("utf-8");
                        os.write(input, 0, input.length);
                    }

                    int code = connection.getResponseCode();
                    InputStream odgovor = connection.getInputStream();
                    StringBuilder response = null;
                    try (BufferedReader br = new BufferedReader(
                            new InputStreamReader(odgovor, "utf-8"))
                    ) {
                        response = new StringBuilder();
                        String rensponseLine = null;
                        while ((rensponseLine = br.readLine()) != null) {
                            response.append(rensponseLine.trim());
                        }
                        Log.d("ODGOVOR", response.toString());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public class FiltrirajKvizoveKlasa extends AsyncTask<String, Void, Void> {

        AsyncResponse ar = null;

        public FiltrirajKvizoveKlasa(AsyncResponse dodaj_kviz) {
            this.ar = dodaj_kviz;
        }

        @Override
        protected Void doInBackground(String... strings) {
            InputStream is = getResources().openRawResource(R.raw.secret);
            try {
                GoogleCredential credentials = GoogleCredential.fromStream(is).createScoped(Lists.newArrayList("https://www.googleapis.com/auth/datastore"));
                credentials.refreshToken();
                String TOKEN = credentials.getAccessToken();
                System.out.println("SADA JE TOKEN: " + TOKEN);
                String query = "{\n" +
                        "\"structuredQuery\": {\n" +
                        "\"where\": {\n" +
                        "\"fieldFilter\": {\n" +
                        "\"field\": {\"fieldPath\": \"categoryID\"}, \n" +
                        "\"op\": \"EQUAL\",\n" +
                        "\"value\": {\"stringValue\": \"" + strings[0] + "\"}\n" +
                        "}\n" +
                        "},\n" +
                        "\"select\": {\"fields\": [ {\"fieldPath\": \"categoryID\"}, {\"fieldPath\": \"name\"}, {\"fieldPath\": \"questions\"} ] }, \n" +
                        "\"from\": [{\"collectionId\" : \"Quizzes\"}], \n" +
                        "\"limit\" : 1000\n" +
                        "}\n" +
                        "}";
                String url = "https://firestore.googleapis.com/v1/projects/quizgame-9bb9e/databases/(default)/documents:runQuery?access_token=";
                URL urlObj = new URL(url + URLEncoder.encode(TOKEN, "UTF-8"));
                HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = query.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int code = connection.getResponseCode();
                StringBuilder response = null;
                InputStream odgovor = connection.getInputStream();
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(odgovor, "utf-8"))
                ) {
                    response = new StringBuilder();
                    String rensponseLine = null;
                    while ((rensponseLine = br.readLine()) != null) {
                        response.append(rensponseLine.trim());
                    }
                    Log.d("ODGOVOR QUERIJAA:  ", response.toString());
                }

                String rezultatKvizovi = response.toString();
                rezultatKvizovi = "{ \"documents\": " + rezultatKvizovi + "}";
                if (!rezultatKvizovi.equals("{}")) {
                    JSONObject joKvizovi = new JSONObject(rezultatKvizovi);
                    JSONArray documentsKvizovi = joKvizovi.getJSONArray("documents");
                    System.out.println("DOKUMENT: " + documentsKvizovi.toString());
                    System.out.println("parsDokument" + documentsKvizovi.toString().substring(4, 11));
                    for (int i = 0; i < documentsKvizovi.length(); i++) {
                        JSONObject doc = documentsKvizovi.getJSONObject(i);
                        if (doc.has("document")) {
                            JSONObject docc = doc.getJSONObject("document");
                            String nameKviza = docc.getString("name");
                            int brojac3 = 0;
                            for (int kv = 0; kv < nameKviza.length(); kv++) {
                                if (nameKviza.charAt(kv) == '/') {
                                    brojac3++;
                                }
                                if (brojac3 == 6) {
                                    nameKviza = nameKviza.substring(++kv, nameKviza.length());
                                    break;
                                }
                            }
                            JSONObject fields = docc.getJSONObject("fields");
                            JSONObject stringValue = fields.getJSONObject("name");
                            String nazivKviza = stringValue.getString("stringValue");
                            if (nazivKviza.equals("Dodaj kviz")) continue;
                            JSONObject referenceValue = fields.getJSONObject("categoryID");
                            String idKategorijeKviza = referenceValue.getString("stringValue");
                            Category kat = new Category();
                            for (int k = 0; k < initialDatabaseCategories.size(); k++) {
                                if (idKategorijeKviza.equals(initialDatabaseCategories.get(k).first)) {
                                    kat = initialDatabaseCategories.get(k).second;
                                    break;
                                }
                            }

                            JSONObject pitt = fields.getJSONObject("questions");
                            JSONObject arrayValue = pitt.getJSONObject("arrayValue");
                            if (!arrayValue.toString().equals("{}")) {
                                JSONArray values = arrayValue.getJSONArray("values");
                                ArrayList<String> trenutniIdeviPitanja = new ArrayList<>();
                                for (int j = 0; j < values.length(); j++) {
                                    JSONObject item = values.getJSONObject(j);
                                    String pit = item.getString("stringValue");
                                    trenutniIdeviPitanja.add(pit);
                                }
                                ArrayList<Question> pitanjaZaKviz = new ArrayList<>();
                                for (int s = 0; s < initialDatabaseQuestions.size(); s++) {
                                    for (int h = 0; h < trenutniIdeviPitanja.size(); h++) {
                                        if (trenutniIdeviPitanja.get(h).equals(initialDatabaseQuestions.get(s).first)) {
                                            pitanjaZaKviz.add(initialDatabaseQuestions.get(s).second);
                                        }
                                    }
                                }
                                trenutniIdeviPitanja.clear();
                                Quiz quizAtTheMoment = new Quiz(nazivKviza, pitanjaZaKviz, kat);
                                filterKvizovi.add(quizAtTheMoment);
                            } else {
                                ArrayList<Question> pitanjaZaKviz = new ArrayList<>();
                                Quiz quizAtTheMoment = new Quiz(nazivKviza, pitanjaZaKviz, kat);
                                filterKvizovi.add(quizAtTheMoment);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                //e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            ar.processFinish();
        }
    }

    public class KlasaUcitajBazu extends AsyncTask<String, Void, Void> {

        AsyncResponse ar = null;

        public KlasaUcitajBazu(AsyncResponse asyncResponse) {
            this.ar = asyncResponse;
        }

        @Override
        protected Void doInBackground(String... strings) {
            InputStream is = getResources().openRawResource(R.raw.secret);
            try {
                GoogleCredential credentials = GoogleCredential.fromStream(is).createScoped(Lists.newArrayList("https://www.googleapis.com/auth/datastore"));
                credentials.refreshToken();
                String TOKEN = credentials.getAccessToken();
                System.out.println("SADA JE TOKEN: " + TOKEN);
                String urlKategorije = "https://firestore.googleapis.com/v1/projects/quizgame-9bb9e/databases/(default)/documents/Categories?access_token=";
                URL urlObjKategorija = new URL(urlKategorije + URLEncoder.encode(TOKEN, "UTF-8"));
                HttpURLConnection connectionKategorija = (HttpURLConnection) urlObjKategorija.openConnection();
                connectionKategorija.connect();
                InputStream odgovorKategorija = connectionKategorija.getInputStream();
                StringBuilder responseKategorija = null;
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(odgovorKategorija, "utf-8"))
                ) {
                    responseKategorija = new StringBuilder();
                    String rensponseLine = null;
                    while ((rensponseLine = br.readLine()) != null) {
                        responseKategorija.append(rensponseLine.trim());
                    }
                    Log.d("ODGOVOR KATEGORIJA: ", responseKategorija.toString());
                }
                kvizovi.clear();
                kategorije.clear();
                kvizovi.add(new Quiz("Dodaj kviz"));
                kategorije.add(new Category("Svi"));
                initialDatabaseQuizzes.clear();
                initialDatabaseCategories.clear();
                initialDatabaseQuestions.clear();
                Category categoryAtTheMoment = new Category();
                String rezultatKategorija = responseKategorija.toString();
                if (!rezultatKategorija.equals("{}")) {
                    JSONObject jo = new JSONObject(rezultatKategorija);
                    JSONArray documents = jo.getJSONArray("documents");
                    for (int i = 0; i < documents.length(); i++) {
                        JSONObject doc = documents.getJSONObject(i);
                        String nameKategorije = doc.getString("name");
                        int brojac1 = 0;
                        for (int kat = 0; kat < nameKategorije.length(); kat++) {
                            if (nameKategorije.charAt(kat) == '/') {
                                brojac1++;
                            }
                            if (brojac1 == 6) {
                                nameKategorije = nameKategorije.substring(++kat, nameKategorije.length());
                                break;
                            }
                        }
                        JSONObject fields = doc.getJSONObject("fields");
                        JSONObject stringValue = fields.getJSONObject("name");
                        String nazivKategorije = stringValue.getString("stringValue");
                        if (nazivKategorije.equals("Svi")) continue;
                        if (nazivKategorije.equals("Dodaj kategoriju")) continue;
                        JSONObject integerValue = fields.getJSONObject("iconID");
                        int idIkoniceKategorije = integerValue.getInt("integerValue");
                        categoryAtTheMoment = new Category(nazivKategorije, String.valueOf(idIkoniceKategorije));
                        initialDatabaseCategories.add(new Pair<String, Category>(nameKategorije, categoryAtTheMoment));
                        kategorije.add(categoryAtTheMoment);
                    }
                }

                String urlPitanja = "https://firestore.googleapis.com/v1/projects/quizgame-9bb9e/databases/(default)/documents/Questions?access_token=";
                URL urlObjPitanja = new URL(urlPitanja + URLEncoder.encode(TOKEN, "UTF-8"));
                HttpURLConnection connectionPitanja = (HttpURLConnection) urlObjPitanja.openConnection();
                connectionPitanja.connect();
                InputStream odgovorPitanja = connectionPitanja.getInputStream();
                StringBuilder responsePitanja = null;
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(odgovorPitanja, "utf-8"))
                ) {
                    responsePitanja = new StringBuilder();
                    String rensponseLine = null;
                    while ((rensponseLine = br.readLine()) != null) {
                        responsePitanja.append(rensponseLine.trim());
                    }
                    Log.d("ODGOVOR PITANJA: ", responsePitanja.toString());
                }
                String rezultatPitanja = responsePitanja.toString();
                if (!rezultatPitanja.equals("{}")) {
                    JSONObject joPitanja = new JSONObject(rezultatPitanja);
                    JSONArray documentsPitanja = joPitanja.getJSONArray("documents");
                    for (int i = 0; i < documentsPitanja.length(); i++) {
                        JSONObject doc = documentsPitanja.getJSONObject(i);
                        String namePitanja = doc.getString("name");
                        int brojac2 = 0;
                        for (int pit = 0; pit < namePitanja.length(); pit++) {
                            if (namePitanja.charAt(pit) == '/') {
                                brojac2++;
                            }
                            if (brojac2 == 6) {
                                namePitanja = namePitanja.substring(++pit, namePitanja.length());
                                break;
                            }
                        }
                        JSONObject fields = doc.getJSONObject("fields");
                        JSONObject stringValue = fields.getJSONObject("name");
                        String nazivPitanja = stringValue.getString("stringValue");
                        if (nazivPitanja.equals("Dodaj pitanje")) continue;
                        JSONObject integerValue = fields.getJSONObject("correctIndex");
                        int indexTacnog = integerValue.getInt("integerValue");
                        JSONObject odgg = fields.getJSONObject("answers");
                        JSONObject arrayValue = odgg.getJSONObject("arrayValue");
                        JSONArray values = arrayValue.getJSONArray("values");
                        ArrayList<String> trenutnabazaOdgovori = new ArrayList<>();
                        for (int j = 0; j < values.length(); j++) {
                            JSONObject item = values.getJSONObject(j);
                            String odg = item.getString("stringValue");
                            trenutnabazaOdgovori.add(odg);
                        }
                        String trenutniTacanOdgovorBaza = "";
                        for (int j = 0; j < trenutnabazaOdgovori.size(); j++) {
                            if (j == indexTacnog) {
                                trenutniTacanOdgovorBaza = trenutnabazaOdgovori.get(j);
                                break;
                            }
                        }
                        Question questionAtTheMoment = new Question(nazivPitanja, nazivPitanja, trenutnabazaOdgovori, trenutniTacanOdgovorBaza);
                        initialDatabaseQuestions.add(new Pair<String, Question>(namePitanja, questionAtTheMoment));
                    }
                }

                String urlKvizovi = "https://firestore.googleapis.com/v1/projects/quizgame-9bb9e/databases/(default)/documents/Quizzes?access_token=";
                URL urlObjKvizovi = new URL(urlKvizovi + URLEncoder.encode(TOKEN, "UTF-8"));
                HttpURLConnection connectionKvizovi = (HttpURLConnection) urlObjKvizovi.openConnection();
                connectionKvizovi.connect();
                InputStream odgovorKvizovi = connectionKvizovi.getInputStream();
                StringBuilder responseKvizovi = null;
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(odgovorKvizovi, "utf-8"))
                ) {
                    responseKvizovi = new StringBuilder();
                    String rensponseLine = null;
                    while ((rensponseLine = br.readLine()) != null) {
                        responseKvizovi.append(rensponseLine.trim());
                    }
                    Log.d("ODGOVOR KVIZOVI: ", responseKvizovi.toString());
                }
                String rezultatKvizovi = responseKvizovi.toString();
                if (!rezultatKvizovi.equals("{}")) {
                    JSONObject joKvizovi = new JSONObject(rezultatKvizovi);
                    JSONArray documentsKvizovi = joKvizovi.getJSONArray("documents");
                    for (int i = 0; i < documentsKvizovi.length(); i++) {
                        JSONObject doc = documentsKvizovi.getJSONObject(i);
                        String nameKviza = doc.getString("name");
                        int brojac3 = 0;
                        for (int kv = 0; kv < nameKviza.length(); kv++) {
                            if (nameKviza.charAt(kv) == '/') {
                                brojac3++;
                            }
                            if (brojac3 == 6) {
                                nameKviza = nameKviza.substring(++kv, nameKviza.length());
                                break;
                            }
                        }
                        JSONObject fields = doc.getJSONObject("fields");
                        JSONObject stringValue = fields.getJSONObject("name");
                        String nazivKviza = stringValue.getString("stringValue");
                        if (nazivKviza.equals("Dodaj kviz")) continue;
                        JSONObject referenceValue = fields.getJSONObject("categoryID");
                        String idKategorijeKviza = referenceValue.getString("stringValue");
                        Category kat = new Category();
                        for (int k = 0; k < initialDatabaseCategories.size(); k++) {
                            if (idKategorijeKviza.equals(initialDatabaseCategories.get(k).first)) {
                                kat = initialDatabaseCategories.get(k).second;
                                break;
                            }
                        }
                        if (kat.getName().length() == 0) kat.setNaziv("Svi");
                        JSONObject pitt = fields.getJSONObject("questions");
                        JSONObject arrayValue = pitt.getJSONObject("arrayValue");
                        if (!arrayValue.toString().equals("{}")) {
                            JSONArray values = arrayValue.getJSONArray("values");
                            ArrayList<String> trenutniIdeviPitanja = new ArrayList<>();
                            for (int j = 0; j < values.length(); j++) {
                                JSONObject item = values.getJSONObject(j);
                                String pit = item.getString("stringValue");
                                trenutniIdeviPitanja.add(pit);
                            }
                            ArrayList<Question> pitanjaZaKviz = new ArrayList<>();
                            for (int s = 0; s < initialDatabaseQuestions.size(); s++) {
                                for (int h = 0; h < trenutniIdeviPitanja.size(); h++) {
                                    if (trenutniIdeviPitanja.get(h).equals(initialDatabaseQuestions.get(s).first)) {
                                        pitanjaZaKviz.add(initialDatabaseQuestions.get(s).second);
                                    }
                                }
                            }
                            trenutniIdeviPitanja.clear();
                            Quiz quizAtTheMoment = new Quiz(nazivKviza, pitanjaZaKviz, kat);
                            initialDatabaseQuizzes.add(new Pair<String, Quiz>(nameKviza, quizAtTheMoment));
                            kvizovi.add(kvizovi.size() - 1, quizAtTheMoment);
                            validacijaKvizova.add(quizAtTheMoment);
                            hasAddQuestion = true;
                        } else {
                            ArrayList<Question> pitanjaZaKviz = new ArrayList<>();
                            Quiz quizAtTheMoment = new Quiz(nazivKviza, pitanjaZaKviz, kat);
                            initialDatabaseQuizzes.add(new Pair<String, Quiz>(nameKviza, quizAtTheMoment));
                            kvizovi.add(kvizovi.size() - 1, quizAtTheMoment);
                            validacijaKvizova.add(quizAtTheMoment);
                            hasAddQuestion = true;
                        }
                    }
                }
            } catch (IOException e) {
                //e.printStackTrace();
                //nemaInterneta = true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void string) {
            ar.processFinish();
        }
    }

    public ArrayList<Pair<String, Pair<String, String>>> procitajPodatke(View v) {
        Cursor cur = null;
        ContentResolver cr = getContentResolver();

        ArrayList<Pair<String, Pair<String, String>>> lista = new ArrayList<>();

        String[] projection =
                {
                        CalendarContract.Events.TITLE,
                        CalendarContract.Events.DTSTART,
                        CalendarContract.Events.DTEND
                };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CALENDAR}, MY_CAL_REQ);
        }
        cur = cr.query(CalendarContract.Events.CONTENT_URI, projection, null, null, null);

        while (cur.moveToNext()) {
            String naziv = cur.getString(0);
            String pocetak = cur.getString(1);
            String kraj = cur.getString(2);
            Pair<String, String> vrijeme = new Pair<>(pocetak, kraj);
            lista.add(new Pair<String, Pair<String, String>>(naziv, vrijeme));
        }
        return lista;
    }

    BroadcastReceiver Brisiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int status = NetworkUtil.getConnectivityStatusString(context);
            if ("android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction())) {
                if (status == NetworkUtil.NETWORK_STATUS_NOT_CONNECTED) {
                    Toast.makeText(context, "Niste konektovani na internet", Toast.LENGTH_LONG).show();
                    if (kategorije.size() <= 1) kategorije.addAll(sqlHelper.pokupiSveKategorije());
                    if (kvizovi.size() <= 1)
                        kvizovi.addAll(kvizovi.size() - 1, sqlHelper.pokupiSveKvizove());
                    pitanjaIzSQL.addAll(sqlHelper.pokupiSvaPitanja());
                } else {
                    Toast.makeText(context, "Konektovani ste na internet", Toast.LENGTH_LONG).show();
                    new KlasaUcitajBazu(new AsyncResponse() {
                        @Override
                        public void processFinish() {
                            //kvizovi.addAll(kvizovi.size() - 1, kvizovi);
                            if (lvKvizovi != null) {
                                Resources res = getResources();
                                adapter.notifyDataSetChanged();
                                adapterKategorija.notifyDataSetChanged();

                                sqlHelper.obrisiSve();
                                for (Pair<String, Category> k : initialDatabaseCategories) {
                                    sqlHelper.dodajKategorijuUSQL(k.second);
                                }
                                for (Pair<String, Question> p : initialDatabaseQuestions) {
                                    sqlHelper.dodajPitanjeUSQL(p.second);
                                }
                                for (Pair<String, Quiz> k : initialDatabaseQuizzes) {
                                    sqlHelper.dodajKvizUSQL(k.second);
                                }

                            } else {
                                Bundle arg = new Bundle();
                                DetailsFragment df = new DetailsFragment();
                                arg.putParcelableArrayList("kvizovi", kvizovi);
                                df.setArguments(arg);
                                getSupportFragmentManager().beginTransaction().replace(R.id.detailPlace, df).commitAllowingStateLoss();
                                ListFragment df2 = new ListFragment();
                                arg.putSerializable("sveKategorije", kategorije);
                                df2.setArguments(arg);
                                getSupportFragmentManager().beginTransaction().replace(R.id.listPlace, df2).commitAllowingStateLoss();
                            }
                        }
                    }).execute();

                    new KlasaUcitajRangListeIzFB(new AsyncResponse() {
                        @Override
                        public void processFinish() {
                            sveRangListeIzSQL.clear();
                            sveRangListeIzSQL.addAll(sqlHelper.pokupiSveRangListe());
                            new KlasaDodajURangListuIzSQLuFB().execute();
                        }
                    }).execute();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quizzes);
        lvKvizovi = (ListView) findViewById(R.id.lvKvizovi);

        sqlHelper = new SQLHelper(this);
        try {
            db = sqlHelper.getWritableDatabase();
        } catch (SQLException e) {
            db = sqlHelper.getReadableDatabase();
        }

        try {
            IntentFilter internetIntent = new IntentFilter();
            internetIntent.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            registerReceiver(Brisiver, internetIntent);
        } catch (Exception e) {

        }

        if (lvKvizovi != null) {
            spPostojeceKategorije = (Spinner) findViewById(R.id.spPostojeceKategorije);
            Resources res = getResources();
            boolean imaSvi = false;
            for (int i = 0; i < kategorije.size(); i++) {
                if (kategorije.get(i).getName().equals("Svi")) {
                    imaSvi = true;
                }
            }
            if (!imaSvi) {
                kategorije.add(new Category("Svi"));
            }
            final Category svi = kategorije.get(0);
            validacijaKvizova.addAll(kvizovi);
            boolean imaDodaj = false;
            for (int i = 0; i < kvizovi.size(); i++) {
                if (kvizovi.get(i).getNaziv().equals("Dodaj kviz")) imaDodaj = true;
            }
            if (!imaDodaj) kvizovi.add(new Quiz("Dodaj kviz"));
            adapter = new QuizAdapter(this, kvizovi, res);
            lvKvizovi.setAdapter(adapter);
            adapterKategorija = new CategoriesAdapter(this, kategorije, res);
            spPostojeceKategorije.setAdapter(adapterKategorija);

            spPostojeceKategorije.setSelected(false);
            spPostojeceKategorije.setSelection(0, true);
            spPostojeceKategorije.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    if (kategorije.get(spPostojeceKategorije.getSelectedItemPosition()).getName().equals(svi.getName())) {
                        filtrirano = false;
                        Resources res = getResources();
                        adapter = new QuizAdapter(QuizzesActivity.this, kvizovi, res);
                        lvKvizovi.setAdapter(adapter);
                    } else {
                        if(isNetworkAvailable()) {
                            String idKategorijeZaFiltriranje = "";
                            String kategorijaIzSpinneraNaziv = kategorije.get(spPostojeceKategorije.getSelectedItemPosition()).getName();
                            for (int i = 0; i < initialDatabaseCategories.size(); i++) {
                                if (kategorijaIzSpinneraNaziv.equals(initialDatabaseCategories.get(i).second.getName())) {
                                    idKategorijeZaFiltriranje = initialDatabaseCategories.get(i).first;
                                    break;
                                }
                            }

                            new FiltrirajKvizoveKlasa(new AsyncResponse() {
                                @Override
                                public void processFinish() {
                                    filtrirano = true;
                                    filterKvizovi.add(new Quiz("Dodaj kviz"));
                                    Resources res = getResources();
                                    adapter = new QuizAdapter(QuizzesActivity.this, filterKvizovi, res);
                                    lvKvizovi.setAdapter(adapter);
                                }
                            }).execute(idKategorijeZaFiltriranje);
                            filterKvizovi = new ArrayList<Quiz>();
                        }
                        else {
                            filterKvizovi = new ArrayList<Quiz>();
                            for (int i = 0; i < kvizovi.size() - 1; i++) {
                                if (kvizovi.get(i).getCategory().getName().equals(kategorije.get(spPostojeceKategorije.getSelectedItemPosition()).getName())) {
                                    filterKvizovi.add(kvizovi.get(i));
                                }
                            }
                            filterKvizovi.add(new Quiz("Dodaj kviz"));
                            Resources res = getResources();
                            adapter = new QuizAdapter(QuizzesActivity.this, filterKvizovi, res);
                            lvKvizovi.setAdapter(adapter);
                        }
                    }
                }

                public void onNothingSelected(AdapterView<?> arg0) {
                    filtrirano = false;
                    Resources res = getResources();
                    adapter = new QuizAdapter(QuizzesActivity.this, kvizovi, res);
                    lvKvizovi.setAdapter(adapter);
                }
            });

            lvKvizovi.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (!filtrirano) {
                        if (position != kvizovi.size() - 1) {
                            boolean alertt = false;
                            ArrayList<Pair<String, Pair<String, String>>> calendarEventi = procitajPodatke(view);
                            Date sadasnjiDatum = new Date();
                            long sadasnjiDatumMili = sadasnjiDatum.getTime();

                            ArrayList<Pair<String, Pair<String, String>>> eventiNakonDanas = new ArrayList<>();

                            for (Pair<String, Pair<String, String>> par : calendarEventi) {
                                long pocetnoVrijemeMili = Long.parseLong(par.second.first);
                                long krajnjeVrijemeMili = Long.parseLong(par.second.second);
                                if (krajnjeVrijemeMili > sadasnjiDatumMili && pocetnoVrijemeMili > sadasnjiDatumMili) {
                                    eventiNakonDanas.add(par);
                                }
                                if (krajnjeVrijemeMili > sadasnjiDatumMili && pocetnoVrijemeMili <= sadasnjiDatumMili) {
                                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(QuizzesActivity.this);
                                    alertDialog.setTitle("Imate trenutno aktivan događaj u kalendaru!");
                                    alertDialog.setIcon(R.drawable.error);
                                    alertDialog.setPositiveButton("OK",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    Toast.makeText(getApplicationContext(), "You clicked on OK", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                    alertDialog.show();
                                    alertt = true;
                                    break;
                                }
                            }

                            if (!alertt) {
                                int x = (int) Math.round(kvizovi.get(position).getQuestions().size() / 2.);
                                long xMili = TimeUnit.MINUTES.toMillis(x);

                                if (eventiNakonDanas.size() != 0) {
                                    Pair<String, Pair<String, String>> prviEvent = eventiNakonDanas.get(0);
                                    long min = Long.parseLong(eventiNakonDanas.get(0).second.first) - sadasnjiDatumMili;

                                    for (Pair<String, Pair<String, String>> par : eventiNakonDanas) {
                                        long pocetnoVrijemeMili = Long.parseLong(par.second.first);
                                        if ((pocetnoVrijemeMili - sadasnjiDatumMili) < min) {
                                            prviEvent = par;
                                            min = pocetnoVrijemeMili - sadasnjiDatumMili;
                                        }
                                    }

                                    long yMili = Long.parseLong(prviEvent.second.first) - sadasnjiDatumMili;

                                    if (yMili < xMili) {
                                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(QuizzesActivity.this);
                                        long ukupnoY = TimeUnit.MILLISECONDS.toMinutes(yMili) + 1;
                                        alertDialog.setTitle("Imate događaj u kalendaru za " + ukupnoY + " minuta!");
                                        alertDialog.setIcon(R.drawable.error);
                                        alertDialog.setPositiveButton("OK",
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        Toast.makeText(getApplicationContext(), "You clicked on OK", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                        alertDialog.show();
                                        alertt = true;
                                    }
                                } else {
                                    Toast.makeText(getApplicationContext(), "Nemate evenata u kalendaru (nakon danas)", Toast.LENGTH_LONG).show();
                                }
                            }

                            if (!alertt) {
                                Intent myIntent = new Intent(QuizzesActivity.this, PlayQuizActivity.class);
                                myIntent.putExtra("nazivKviza", kvizovi.get(position).getNaziv());
                                myIntent.putExtra("pitanja", kvizovi.get(position).getQuestions());
                                QuizzesActivity.this.startActivityForResult(myIntent, 2);
                            }
                        }
                    } else {
                        if (position != filterKvizovi.size() - 1) {
                            boolean alerttt = false;
                            ArrayList<Pair<String, Pair<String, String>>> calendarEventi = procitajPodatke(view);
                            Date sadasnjiDatum = new Date();
                            long sadasnjiDatumMili = sadasnjiDatum.getTime();

                            ArrayList<Pair<String, Pair<String, String>>> eventiNakonDanas = new ArrayList<>();

                            for (Pair<String, Pair<String, String>> par : calendarEventi) {
                                long pocetnoVrijemeMili = Long.parseLong(par.second.first);
                                long krajnjeVrijemeMili = Long.parseLong(par.second.second);
                                if (krajnjeVrijemeMili > sadasnjiDatumMili && pocetnoVrijemeMili > sadasnjiDatumMili) {
                                    eventiNakonDanas.add(par);
                                }
                                if (krajnjeVrijemeMili > sadasnjiDatumMili && pocetnoVrijemeMili <= sadasnjiDatumMili) {
                                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(QuizzesActivity.this);
                                    alertDialog.setTitle("Imate trenutno aktivan događaj u kalendaru!");
                                    alertDialog.setIcon(R.drawable.error);
                                    alertDialog.setPositiveButton("OK",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    Toast.makeText(getApplicationContext(), "You clicked on OK", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                    alertDialog.show();
                                    alerttt = true;
                                    break;
                                }
                            }

                            if (!alerttt) {
                                int x = (int) Math.round(filterKvizovi.get(position).getQuestions().size() / 2.);
                                long xMili = TimeUnit.MINUTES.toMillis(x);

                                Pair<String, Pair<String, String>> prviEvent = eventiNakonDanas.get(0);
                                long min = Long.parseLong(eventiNakonDanas.get(0).second.first) - sadasnjiDatumMili;

                                for (Pair<String, Pair<String, String>> par : eventiNakonDanas) {
                                    long pocetnoVrijemeMili = Long.parseLong(par.second.first);
                                    if ((pocetnoVrijemeMili - sadasnjiDatumMili) < min) {
                                        prviEvent = par;
                                        min = pocetnoVrijemeMili - sadasnjiDatumMili;
                                    }
                                }

                                long yMili = Long.parseLong(prviEvent.second.first) - sadasnjiDatumMili;

                                if (yMili < xMili) {
                                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(QuizzesActivity.this);
                                    long ukupnoY = TimeUnit.MILLISECONDS.toMinutes(yMili) + 1;
                                    alertDialog.setTitle("Imate događaj u kalendaru za " + ukupnoY + " minuta!");
                                    alertDialog.setIcon(R.drawable.error);
                                    alertDialog.setPositiveButton("OK",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    Toast.makeText(getApplicationContext(), "You clicked on OK", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                    alertDialog.show();
                                    alerttt = true;
                                }
                            }

                            if (!alerttt) {
                                Intent myIntent = new Intent(QuizzesActivity.this, PlayQuizActivity.class);
                                myIntent.putExtra("nazivKviza", filterKvizovi.get(position).getNaziv());
                                myIntent.putExtra("pitanja", filterKvizovi.get(position).getQuestions());
                                QuizzesActivity.this.startActivityForResult(myIntent, 2);
                            }
                        }
                    }
                }
            });

            lvKvizovi.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    if (!filtrirano) {
                        if (position == kvizovi.size() - 1) {
                            Intent myIntent = new Intent(QuizzesActivity.this, AddQuizActivity.class);
                            QuizzesActivity.this.startActivityForResult(myIntent, 0);
                        } else {
                            Intent myIntent = new Intent(QuizzesActivity.this, AddQuizActivity.class);
                            myIntent.putExtra("nazivKviza", kvizovi.get(position).getNaziv());
                            myIntent.putExtra("kategorija", kvizovi.get(position).getCategory());
                            myIntent.putExtra("pitanja", kvizovi.get(position).getQuestions());
                            pozicija = position;
                            QuizzesActivity.this.startActivityForResult(myIntent, 1);
                        }
                    } else {
                        if (position == filterKvizovi.size() - 1) {
                            Intent myIntent = new Intent(QuizzesActivity.this, AddQuizActivity.class);
                            QuizzesActivity.this.startActivityForResult(myIntent, 0);
                        } else {
                            Intent myIntent = new Intent(QuizzesActivity.this, AddQuizActivity.class);
                            myIntent.putExtra("nazivKviza", filterKvizovi.get(position).getNaziv());
                            myIntent.putExtra("kategorija", filterKvizovi.get(position).getCategory());
                            myIntent.putExtra("pitanja", filterKvizovi.get(position).getQuestions());
                            pozicija = position;
                            QuizzesActivity.this.startActivityForResult(myIntent, 1);
                        }
                    }
                    //new Klasa().execute();
                    return true;
                }
            });
        } else {
            //validacijaKvizova = new ArrayList<>();
            if (kvizovi.size() == 0) {
                kvizovi.add(new Quiz("Dodaj kviz"));
            }
            FragmentManager fm = getSupportFragmentManager();
            FrameLayout lista = (FrameLayout) findViewById(R.id.listPlace);

            if (lista != null) {
                ListFragment fd = (ListFragment) fm.findFragmentById(R.id.listPlace);
                if (fd == null) {
                    fd = new ListFragment();
                    Bundle arg = new Bundle();
                    ArrayList<Category> sveKategorije = kategorije;
                    boolean imaSvi = false;
                    for (int i = 0; i < sveKategorije.size(); i++) {
                        if (sveKategorije.get(i).getName().equals("Svi")) {
                            imaSvi = true;
                        }
                    }
                    if (!imaSvi) sveKategorije.add(new Category("Svi"));
                    arg.putSerializable("sveKategorije", sveKategorije);
                    fd.setArguments(arg);
                    fm.beginTransaction().replace(R.id.listPlace, fd).commit();
                } else {
                    fm.popBackStack(null, android.support.v4.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
                }
            }

            FrameLayout detalji = (FrameLayout) findViewById(R.id.detailPlace);

            if (detalji != null) {
                DetailsFragment fd = (DetailsFragment) fm.findFragmentById(R.id.detailPlace);
                if (fd == null) {
                    fd = new DetailsFragment();
                    Bundle arg = new Bundle();
                    arg.putParcelableArrayList("kvizovi", kvizovi);
                    fd.setArguments(arg);
                    fm.beginTransaction().replace(R.id.detailPlace, fd).commit();
                } else {
                    fm.popBackStack(null, android.support.v4.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                if (lvKvizovi != null) {
                    if (!filtrirano) {
                        Category kat = ((Category) data.getSerializableExtra("Kategorija"));
                        kvizovi.add(kvizovi.size() - 1, new Quiz(data.getStringExtra("nazivKviza"), (ArrayList<Question>) data.getSerializableExtra("pitanja"), kat));
                        validacijaKvizova.add(new Quiz(data.getStringExtra("nazivKviza"), (ArrayList<Question>) data.getSerializableExtra("pitanja"), kat));
                        sqlHelper.obrisiSve();
                        for (Pair<String, Category> k : initialDatabaseCategories) {
                            sqlHelper.dodajKategorijuUSQL(k.second);
                        }
                        for (Pair<String, Question> p : initialDatabaseQuestions) {
                            sqlHelper.dodajPitanjeUSQL(p.second);
                        }
                        for (int i = 0; i < kvizovi.size() - 1; i++) {
                            sqlHelper.dodajKvizUSQL(kvizovi.get(i));
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Category kat = ((Category) data.getSerializableExtra("Kategorija"));
                        filterKvizovi.add(filterKvizovi.size() - 1, new Quiz(data.getStringExtra("nazivKviza"), (ArrayList<Question>) data.getSerializableExtra("pitanja"), kat));
                        kvizovi.add(kvizovi.size() - 1, new Quiz(data.getStringExtra("nazivKviza"), (ArrayList<Question>) data.getSerializableExtra("pitanja"), kat));
                        validacijaKvizova.clear();
                        validacijaKvizova.addAll(kvizovi);
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    if (!filtrirano2) {
                        Category kat = ((Category) data.getSerializableExtra("Kategorija"));
                        kvizovi.add(kvizovi.size() - 1, new Quiz(data.getStringExtra("nazivKviza"), (ArrayList<Question>) data.getSerializableExtra("pitanja"), kat));
                        validacijaKvizova.add(new Quiz(data.getStringExtra("nazivKviza"), (ArrayList<Question>) data.getSerializableExtra("pitanja"), kat));
                        Bundle arg = new Bundle();
                        DetailsFragment df = new DetailsFragment();
                        arg.putParcelableArrayList("kvizovi", kvizovi);
                        df.setArguments(arg);
                        getSupportFragmentManager().beginTransaction().replace(R.id.detailPlace, df).commitAllowingStateLoss();
                        ListFragment df2 = new ListFragment();
                        arg.putSerializable("sveKategorije", kategorije);
                        df2.setArguments(arg);
                        getSupportFragmentManager().beginTransaction().replace(R.id.listPlace, df2).commitAllowingStateLoss();
                    } else {
                        Category kat = ((Category) data.getSerializableExtra("Kategorija"));
                        filterKvizovi2.add(filterKvizovi2.size() - 1, new Quiz(data.getStringExtra("nazivKviza"), (ArrayList<Question>) data.getSerializableExtra("pitanja"), kat));
                        kvizovi.add(kvizovi.size() - 1, new Quiz(data.getStringExtra("nazivKviza"), (ArrayList<Question>) data.getSerializableExtra("pitanja"), kat));
                        validacijaKvizova.clear();
                        validacijaKvizova.addAll(kvizovi);
                        Bundle arg = new Bundle();
                        DetailsFragment df = new DetailsFragment();
                        arg.putParcelableArrayList("kvizovi", filterKvizovi2);
                        df.setArguments(arg);
                        getSupportFragmentManager().beginTransaction().replace(R.id.detailPlace, df).commitAllowingStateLoss();
                        ListFragment df2 = new ListFragment();
                        arg.putSerializable("sveKategorije", kategorije);
                        df2.setArguments(arg);
                        getSupportFragmentManager().beginTransaction().replace(R.id.listPlace, df2).commitAllowingStateLoss();
                    }
                }
            } else if (resultCode == RESULT_CANCELED) {
                if (lvKvizovi == null) {
                    Bundle arg = new Bundle();
                    ListFragment df2 = new ListFragment();
                    arg.putSerializable("sveKategorije", kategorije);
                    df2.setArguments(arg);
                    getSupportFragmentManager().beginTransaction().replace(R.id.listPlace, df2).commitAllowingStateLoss();
                }
            }

        } else if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                if (lvKvizovi != null) {
                    if (!filtrirano) {
                        Category kat = ((Category) data.getSerializableExtra("kategorija"));
                        kvizovi.set(pozicija, new Quiz(data.getStringExtra("nazivKviza"), (ArrayList<Question>) data.getSerializableExtra("pitanja"), kat));
                        quizToBeReturned = new Quiz(data.getStringExtra("nazivKviza"), (ArrayList<Question>) data.getSerializableExtra("pitanja"), kat);
                        validacijaKvizova.set(pozicija, new Quiz(data.getStringExtra("nazivKviza"), (ArrayList<Question>) data.getSerializableExtra("pitanja"), kat));
                        sqlHelper.obrisiSve();
                        for (Pair<String, Category> k : initialDatabaseCategories) {
                            sqlHelper.dodajKategorijuUSQL(k.second);
                        }
                        for (Pair<String, Question> p : initialDatabaseQuestions) {
                            sqlHelper.dodajPitanjeUSQL(p.second);
                        }
                        for (int i = 0; i < kvizovi.size() - 1; i++) {
                            sqlHelper.dodajKvizUSQL(kvizovi.get(i));
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Category kat = ((Category) data.getSerializableExtra("kategorija"));
                        String naziv = filterKvizovi.get(pozicija).getNaziv();
                        filterKvizovi.set(pozicija, new Quiz(data.getStringExtra("nazivKviza"), (ArrayList<Question>) data.getSerializableExtra("pitanja"), kat));
                        quizToBeReturned = new Quiz(data.getStringExtra("nazivKviza"), (ArrayList<Question>) data.getSerializableExtra("pitanja"), kat);
                        int novaPozicija = 0;
                        for (int i = 0; i < kvizovi.size() - 1; i++) {
                            if (kvizovi.get(i).getNaziv().equals(naziv)) {
                                novaPozicija = i;
                            }
                        }
                        kvizovi.set(novaPozicija, new Quiz(data.getStringExtra("nazivKviza"), (ArrayList<Question>) data.getSerializableExtra("pitanja"), kat));
                        validacijaKvizova.clear();
                        validacijaKvizova.addAll(kvizovi);
                        //validacijaKvizova.set(pozicija, new Kviz(data.getStringExtra("nazivKviza"), (ArrayList<Pitanje>) data.getSerializableExtra("pitanja"), kat));
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    if (!filtrirano2) {
                        Category kat = ((Category) data.getSerializableExtra("kategorija"));
                        kvizovi.set(pozicija, new Quiz(data.getStringExtra("nazivKviza"), (ArrayList<Question>) data.getSerializableExtra("pitanja"), kat));
                        quizToBeReturned = new Quiz(data.getStringExtra("nazivKviza"), (ArrayList<Question>) data.getSerializableExtra("pitanja"), kat);
                        validacijaKvizova.set(pozicija, new Quiz(data.getStringExtra("nazivKviza"), (ArrayList<Question>) data.getSerializableExtra("pitanja"), kat));
                        Bundle arg = new Bundle();
                        DetailsFragment df = new DetailsFragment();
                        arg.putParcelableArrayList("kvizovi", kvizovi);
                        df.setArguments(arg);
                        getSupportFragmentManager().beginTransaction().replace(R.id.detailPlace, df).commitAllowingStateLoss();
                        ListFragment df2 = new ListFragment();
                        arg.putSerializable("sveKategorije", kategorije);
                        df2.setArguments(arg);
                        getSupportFragmentManager().beginTransaction().replace(R.id.listPlace, df2).commitAllowingStateLoss();
                    } else {
                        Category kat = ((Category) data.getSerializableExtra("kategorija"));
                        String naziv = filterKvizovi2.get(pozicija).getNaziv();
                        filterKvizovi2.set(pozicija, new Quiz(data.getStringExtra("nazivKviza"), (ArrayList<Question>) data.getSerializableExtra("pitanja"), kat));
                        quizToBeReturned = new Quiz(data.getStringExtra("nazivKviza"), (ArrayList<Question>) data.getSerializableExtra("pitanja"), kat);
                        int novaPozicija = 0;
                        for (int i = 0; i < kvizovi.size() - 1; i++) {
                            if (kvizovi.get(i).getNaziv().equals(naziv)) {
                                novaPozicija = i;
                            }
                        }
                        kvizovi.set(novaPozicija, new Quiz(data.getStringExtra("nazivKviza"), (ArrayList<Question>) data.getSerializableExtra("pitanja"), kat));
                        validacijaKvizova.clear();
                        validacijaKvizova.addAll(kvizovi);
                        Bundle arg = new Bundle();
                        DetailsFragment df = new DetailsFragment();
                        arg.putParcelableArrayList("kvizovi", filterKvizovi2);
                        df.setArguments(arg);
                        getSupportFragmentManager().beginTransaction().replace(R.id.detailPlace, df).commitAllowingStateLoss();
                        ListFragment df2 = new ListFragment();
                        arg.putSerializable("sveKategorije", kategorije);
                        df2.setArguments(arg);
                        getSupportFragmentManager().beginTransaction().replace(R.id.listPlace, df2).commitAllowingStateLoss();
                    }
                }
            } else if (resultCode == RESULT_CANCELED) {
                if (lvKvizovi == null) {
                    Bundle arg = new Bundle();
                    ListFragment df2 = new ListFragment();
                    arg.putSerializable("sveKategorije", kategorije);
                    df2.setArguments(arg);
                    getSupportFragmentManager().beginTransaction().replace(R.id.listPlace, df2).commitAllowingStateLoss();
                } else {
                    initialDatabaseQuizzes.add(new Pair<String, Quiz>(AddQuizActivity.quizForEditID, quizToBeReturned));
                }
            }
        } else if (requestCode == 2) {

        }
    }

    @Override
    public void pressLV(ArrayList<Quiz> k) {
        Bundle arg = new Bundle();
        DetailsFragment df = new DetailsFragment();
        arg.putParcelableArrayList("kvizovi", k);
        df.setArguments(arg);
        getSupportFragmentManager().beginTransaction().replace(R.id.detailPlace, df).commit();
    }
}
