package ba.edu.ibu.stu.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
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

import ba.edu.ibu.stu.R;
import ba.edu.ibu.stu.classes.AsyncResponse;
import ba.edu.ibu.stu.classes.Category;
import ba.edu.ibu.stu.classes.CategoriesAdapter;
import ba.edu.ibu.stu.classes.Quiz;
import ba.edu.ibu.stu.classes.PossibleQuestionsAdapter;
import ba.edu.ibu.stu.classes.Question;
import ba.edu.ibu.stu.classes.AddedQuestionAdapter;

public class AddQuizActivity extends AppCompatActivity {
    private Spinner spCategories;
    private EditText etName;
    private ListView lvAddedQuestions;
    private ListView lvPossibleQuestions;
    private Button btnAddQuiz;
    private ArrayList<Question> questions = new ArrayList<Question>();
    private ArrayList<Question> possibleQuestions = new ArrayList<Question>();
    private ArrayList<Category> categories = new ArrayList<Category>();
    private AddedQuestionAdapter adapter;
    private PossibleQuestionsAdapter possibleAdapter;
    private CategoriesAdapter categoriesAdapter;
    private boolean has = false;
    public static ArrayList<Question> validateQuestions = new ArrayList<>();
    public static ArrayList<Category> validateCategory = new ArrayList<>();
    private Quiz newQuiz = new Quiz();
    private boolean validFile = true;
    ArrayList<Question> novaPitanja = new ArrayList<>();
    public Quiz quizForDatabase = new Quiz();
    public Quiz quizForDatabaseEdit = new Quiz();
    public static String quizForEditID = "";

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public class EditQuizClass extends AsyncTask<String, Void, Void> {

        AsyncResponse ar = null;

        public EditQuizClass(AsyncResponse asyncResponse) {
            this.ar = asyncResponse;
        }

        @Override
        protected Void doInBackground(String... strings) {
            InputStream is = getResources().openRawResource(R.raw.secret);
            try {
                GoogleCredential credentials = GoogleCredential.fromStream(is).createScoped(Lists.newArrayList("https://www.googleapis.com/auth/datastore"));
                credentials.refreshToken();
                String TOKEN = credentials.getAccessToken();
                System.out.println("Current token: " + TOKEN);

                System.out.println("String:  " + strings[1]);

                String url = "https://firestore.googleapis.com/v1/projects/quizgame-9bb9e/databases/(default)/documents/Quizzes/" + strings[0] + "?access_token=";
                URL urlObj = new URL(url + URLEncoder.encode(TOKEN, "UTF-8"));
                HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("PATCH");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");

                String dokument = "{ \"fields\": { \"name\": {\"stringValue\":\"" + etName.getText().toString() + "\"}";
                if (spCategories.getSelectedItemPosition() == 1) {
                    dokument += ", \"categoryID\": { \"stringValue\" : \"Svi\"}";
                } else if (spCategories.getSelectedItemPosition() == 0) {
                    if(strings[1].length() == 0){
                        dokument += ", \"categoryID\": { \"stringValue\" : \"Svi\"}";
                    }
                    else dokument += ", \"categoryID\": { \"stringValue\" : \"" + strings[1] + "\"}";
                } else {
                    dokument += ", \"categoryID\": ";
                    for (int i = 0; i < QuizzesActivity.initialDatabaseCategories.size(); i++) {
                        if (QuizzesActivity.initialDatabaseCategories.get(i).second.getName().equals(quizForDatabaseEdit.getCategory().getName())) {
                            dokument += "{\"stringValue\" : \"" + QuizzesActivity.initialDatabaseCategories.get(i).first + "\"}";
                            break;
                        }
                    }
                }
                AddQuestionActivity.questionID.clear();
                    for (int i = 0; i < QuizzesActivity.initialDatabaseQuestions.size(); i++) {
                        for (int j = 0; j < quizForDatabaseEdit.getQuestions().size(); j++) {
                            if (quizForDatabaseEdit.getQuestions().get(j).getNaziv().equals(QuizzesActivity.initialDatabaseQuestions.get(i).second.getNaziv())) {
                                AddQuestionActivity.questionID.add(QuizzesActivity.initialDatabaseQuestions.get(i).first);
                            }
                        }
                    }
                dokument += " , \"questions\" : { \"arrayValue\" : { \"values\" : [";
                for (int i = 0; i < AddQuestionActivity.questionID.size(); i++) {
                    if (i != AddQuestionActivity.questionID.size() - 1) {
                        dokument += "{\"stringValue\" : \"" + AddQuestionActivity.questionID.get(i) + "\"},";
                    } else {
                        dokument += "{\"stringValue\" : \"" + AddQuestionActivity.questionID.get(i) + "\"}";
                    }
                }
                dokument += " ] } } } }";

                AddQuestionActivity.questionID.clear();

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
                String rezultatKvizovi = response.toString();
                if(!rezultatKvizovi.equals("{}")) {
                    JSONObject joKvizovi = new JSONObject(rezultatKvizovi);
                    JSONObject fields = joKvizovi.getJSONObject("fields");
                    JSONObject stringValue = fields.getJSONObject("name");
                    String nazivKviza = stringValue.getString("stringValue");
                    JSONObject referenceValue = fields.getJSONObject("categoryID");
                    String idKategorijeKviza = referenceValue.getString("stringValue");
                    Category kat = new Category();
                    for (int k = 0; k < QuizzesActivity.initialDatabaseCategories.size(); k++) {
                        if (idKategorijeKviza.equals(QuizzesActivity.initialDatabaseCategories.get(k).first)) {
                            kat = QuizzesActivity.initialDatabaseCategories.get(k).second;
                            break;
                        }
                    }

                    JSONObject pitt = fields.getJSONObject("questions");
                    JSONObject arrayValue = pitt.getJSONObject("arrayValue");
                    if(!arrayValue.toString().equals("{}")) {
                        JSONArray values = arrayValue.getJSONArray("values");
                        ArrayList<String> trenutniIdeviPitanja = new ArrayList<>();
                        for (int j = 0; j < values.length(); j++) {
                            JSONObject item = values.getJSONObject(j);
                            String pit = item.getString("stringValue");
                            trenutniIdeviPitanja.add(pit);
                        }
                        ArrayList<Question> pitanjaZaKviz = new ArrayList<>();
                        for (int s = 0; s < QuizzesActivity.initialDatabaseQuestions.size(); s++) {
                            for (int h = 0; h < trenutniIdeviPitanja.size(); h++) {
                                if (trenutniIdeviPitanja.get(h).equals(QuizzesActivity.initialDatabaseQuestions.get(s).first)) {
                                    pitanjaZaKviz.add(QuizzesActivity.initialDatabaseQuestions.get(s).second);
                                }
                            }
                        }
                        trenutniIdeviPitanja.clear();
                        Quiz quizAtTheMoment = new Quiz(nazivKviza, pitanjaZaKviz, kat);
                        QuizzesActivity.initialDatabaseQuizzes.add(new Pair<String, Quiz>(strings[0], quizAtTheMoment));
                    }
                    else{
                        ArrayList<Question> pitanjaZaKviz = new ArrayList<>();
                        Quiz quizAtTheMoment = new Quiz(nazivKviza, pitanjaZaKviz, kat);
                        QuizzesActivity.initialDatabaseQuizzes.add(new Pair<String, Quiz>(strings[0], quizAtTheMoment));
                    }
                }

            } catch (IOException e) {
                //e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public class AddQuizClass extends AsyncTask<String, Void, Void> {

        @SuppressLint("WrongThread")
        @Override
        protected Void doInBackground(String... strings) {
            InputStream is = getResources().openRawResource(R.raw.secret);
            try {
                GoogleCredential credentials = GoogleCredential.fromStream(is).createScoped(Lists.newArrayList("https://www.googleapis.com/auth/datastore"));
                credentials.refreshToken();
                String TOKEN = credentials.getAccessToken();
                System.out.println("Current token: " + TOKEN);
                String url = "https://firestore.googleapis.com/v1/projects/quizgame-9bb9e/databases/(default)/documents/Quizzes?access_token=";
                URL urlObj = new URL(url + URLEncoder.encode(TOKEN, "UTF-8"));
                HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");

                String dokument = "{ \"fields\": { \"name\": {\"stringValue\":\"" + etName.getText().toString() + "\"}";
                if (spCategories.getSelectedItemPosition() != 0) {
                    dokument += ", \"categoryID\": ";
                    for (int i = 0; i < QuizzesActivity.initialDatabaseCategories.size(); i++) {
                        if (QuizzesActivity.initialDatabaseCategories.get(i).second.getName().equals(quizForDatabase.getCategory().getName())) {
                            dokument += "{\"stringValue\" : \"" + QuizzesActivity.initialDatabaseCategories.get(i).first + "\"}";
                            break;
                        }
                    }
                } else {
                    dokument += ", \"categoryID\": { \"stringValue\" : \"Svi\"}";
                }
                AddQuestionActivity.questionID.clear();
                for (int i = 0; i < QuizzesActivity.initialDatabaseQuestions.size(); i++) {
                    for (int j = 0; j < quizForDatabase.getQuestions().size(); j++) {
                        if (quizForDatabase.getQuestions().get(j).getNaziv().equals(QuizzesActivity.initialDatabaseQuestions.get(i).second.getNaziv())) {
                            AddQuestionActivity.questionID.add(QuizzesActivity.initialDatabaseQuestions.get(i).first);
                        }
                    }
                }
                dokument += " , \"questions\" : { \"arrayValue\" : { \"values\" : [";
                for (int i = 0; i < AddQuestionActivity.questionID.size(); i++) {
                    if (i != AddQuestionActivity.questionID.size() - 1) {
                        dokument += "{\"stringValue\" : \"" + AddQuestionActivity.questionID.get(i) + "\"},";
                    } else {
                        dokument += "{\"stringValue\" : \"" + AddQuestionActivity.questionID.get(i) + "\"}";
                    }
                }
                dokument += " ] } } } }";

                AddQuestionActivity.questionID.clear();

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = dokument.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int code = connection.getResponseCode();
                InputStream answer = connection.getInputStream();
                StringBuilder response = null;
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(answer, "utf-8"))
                ) {
                    response = new StringBuilder();
                    String rensponseLine = null;
                    while ((rensponseLine = br.readLine()) != null) {
                        response.append(rensponseLine.trim());
                    }
                    Log.d("Answer", response.toString());
                }
                String quizzesResult = response.toString();
                if(!quizzesResult.equals("{}")) {
                    JSONObject jsonQuizzes = new JSONObject(quizzesResult);
                    String quizName = jsonQuizzes.getString("name");
                    int brojac3 = 0;
                    for(int kv = 0; kv < quizName.length(); kv++){
                        if(quizName.charAt(kv) == '/' ){
                            brojac3++;
                        }
                        if(brojac3 == 6){
                            quizName = quizName.substring(++kv, quizName.length());
                            break;
                        }
                    }
                    JSONObject fields = jsonQuizzes.getJSONObject("fields");
                    JSONObject stringValue = fields.getJSONObject("name");
                    String nazivKviza = stringValue.getString("stringValue");
                    JSONObject referenceValue = fields.getJSONObject("categoryID");
                    String idKategorijeKviza = referenceValue.getString("stringValue");
                    Category kat = new Category();
                    for (int k = 0; k < QuizzesActivity.initialDatabaseCategories.size(); k++) {
                        if (idKategorijeKviza.equals(QuizzesActivity.initialDatabaseCategories.get(k).first)) {
                            kat = QuizzesActivity.initialDatabaseCategories.get(k).second;
                            break;
                        }
                    }

                    JSONObject pitt = fields.getJSONObject("questions");
                    JSONObject arrayValue = pitt.getJSONObject("arrayValue");
                    if(!arrayValue.toString().equals("{}")) {
                        JSONArray values = arrayValue.getJSONArray("values");
                        ArrayList<String> trenutniIdeviPitanja = new ArrayList<>();
                        for (int j = 0; j < values.length(); j++) {
                            JSONObject item = values.getJSONObject(j);
                            String pit = item.getString("stringValue");
                            trenutniIdeviPitanja.add(pit);
                        }
                        ArrayList<Question> pitanjaZaKviz = new ArrayList<>();
                        for (int s = 0; s < QuizzesActivity.initialDatabaseQuestions.size(); s++) {
                            for (int h = 0; h < trenutniIdeviPitanja.size(); h++) {
                                if (trenutniIdeviPitanja.get(h).equals(QuizzesActivity.initialDatabaseQuestions.get(s).first)) {
                                    pitanjaZaKviz.add(QuizzesActivity.initialDatabaseQuestions.get(s).second);
                                }
                            }
                        }
                        trenutniIdeviPitanja.clear();
                        Quiz quizAtTheMoment = new Quiz(nazivKviza, pitanjaZaKviz, kat);
                        QuizzesActivity.initialDatabaseQuizzes.add(new Pair<String, Quiz>(quizName, quizAtTheMoment));
                    }
                    else{
                        ArrayList<Question> pitanjaZaKviz = new ArrayList<>();
                        Quiz quizAtTheMoment = new Quiz(nazivKviza, pitanjaZaKviz, kat);
                        QuizzesActivity.initialDatabaseQuizzes.add(new Pair<String, Quiz>(quizName, quizAtTheMoment));
                    }
                }
            } catch (IOException e) {
                //e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_quiz);
        lvAddedQuestions = (ListView) findViewById(R.id.lvDodanaPitanja);
        lvPossibleQuestions = (ListView) findViewById(R.id.lvMogucaPitanja);
        spCategories = (Spinner) findViewById(R.id.spKategorije);
        etName = (EditText) findViewById(R.id.etNaziv);
        btnAddQuiz = (Button) findViewById(R.id.btnDodajKviz);
        etName.setHint("Input quiz name");
        categories.add(new Category("Svi"));
        categories.add(new Category("Dodaj kategoriju"));
        Resources res = getResources();
        possibleAdapter = new PossibleQuestionsAdapter(this, possibleQuestions, res);
        if (getIntent().getExtras() != null) {
            etName.setText(getIntent().getStringExtra("nazivKviza"));
            Category newCategory = (Category) getIntent().getSerializableExtra("kategorija");
            categories.add(0, newCategory);
            for (int i = 0; i < QuizzesActivity.initialDatabaseQuizzes.size(); i++) {
                if (QuizzesActivity.initialDatabaseQuizzes.get(i).second.getNaziv().equals(etName.getText().toString())) {
                    quizForEditID = QuizzesActivity.initialDatabaseQuizzes.get(i).first;
                    break;
                }
            }

            for(int i = 0; i < QuizzesActivity.initialDatabaseQuizzes.size(); i++){
                if(quizForEditID.equals(QuizzesActivity.initialDatabaseQuizzes.get(i).first)){
                    QuizzesActivity.initialDatabaseQuizzes.remove(i);
                }
            }

            int brojac = 0;
            for (int pit = 0; pit < quizForEditID.length(); pit++) {
                if (quizForEditID.charAt(pit) == '/') {
                    brojac++;
                }
                if (brojac == 6) {
                    quizForEditID = quizForEditID.substring(++pit, quizForEditID.length());
                    break;
                }
            }

            if(QuizzesActivity.initialDatabaseQuestions.size() == 0) possibleQuestions.addAll(QuizzesActivity.pitanjaIzSQL);
            else {
                for (int i = 0; i < QuizzesActivity.initialDatabaseQuestions.size(); i++) {
                    possibleQuestions.add(QuizzesActivity.initialDatabaseQuestions.get(i).second);
                }
            }

            ArrayList<Question> pomocnaMogucaPitanja = new ArrayList<>();
            pomocnaMogucaPitanja.addAll(possibleQuestions);
            possibleQuestions.removeAll(possibleQuestions);
            for (int i = 0; i < QuizzesActivity.kvizovi.size(); i++) {
                if (QuizzesActivity.kvizovi.get(i).getNaziv().equals(etName.getText().toString())) {
                    if (QuizzesActivity.kvizovi.get(i).getQuestions().size() == 0) {

                    } else {
                        for (int j = 0; j < QuizzesActivity.kvizovi.get(i).getQuestions().size(); j++) {
                            for (int k = 0; k < pomocnaMogucaPitanja.size(); k++) {
                                if (pomocnaMogucaPitanja.get(k).getNaziv().equals(QuizzesActivity.kvizovi.get(i).getQuestions().get(j).getNaziv())) {
                                    pomocnaMogucaPitanja.remove(k);
                                }
                            }
                        }

                    }
                }
            }
            possibleQuestions.addAll(pomocnaMogucaPitanja);
            possibleAdapter.notifyDataSetChanged();
            pomocnaMogucaPitanja.clear();

            for (int i = 2; i < categories.size() - 1; i++) {
                if (!categories.get(i).getName().equals("Svi"))
                    QuizzesActivity.kategorije.add(new Category(categories.get(i).getName(), categories.get(i).getId()));
                for (int j = 0; j < QuizzesActivity.kategorije.size(); j++) {
                    if (!QuizzesActivity.kategorije.get(j).getName().equals("Svi")) {
                        QuizzesActivity.kategorije.add(QuizzesActivity.kategorije.size() - 1, new Category(categories.get(i).getName(), categories.get(i).getId()));
                    } else if (!categories.get(i).getName().equals("Svi")) {
                        QuizzesActivity.kategorije.add(QuizzesActivity.kategorije.size() - 1, new Category(categories.get(i).getName(), categories.get(i).getId()));
                    } else if (!categories.get(i).getName().equals(QuizzesActivity.kategorije.get(j).getName())) {
                        QuizzesActivity.kategorije.add(QuizzesActivity.kategorije.size() - 1, new Category(categories.get(i).getName(), categories.get(i).getId()));
                    }
                }
            }
            for (int i = 1; i < QuizzesActivity.kategorije.size(); i++) {
                categories.add(categories.size() - 1, QuizzesActivity.kategorije.get(i));
            }
            ArrayList<Question> listPitanja = (ArrayList<Question>) getIntent().getSerializableExtra("pitanja");
            for (Question question : listPitanja) {
                questions.add(new Question(question.getNaziv(), question.getTekstPitanja(), question.getOdgovori(), question.getTacan()));
            }
            boolean neDodaji = false;
            for (int i = 0; i < questions.size(); i++) {
                if (questions.get(i).getNaziv().equals("Dodaj pitanje")) {
                    neDodaji = true;
                }
            }
            if (!neDodaji) questions.add(new Question("Dodaj pitanje", null, null, null));
            QuizzesActivity.hasAddQuestion = false;

        } else {
            for (int i = 1; i < categories.size() - 1; i++) {
                if (!categories.get(i).getName().equals("Svi"))
                    QuizzesActivity.kategorije.add(QuizzesActivity.kategorije.size() - 1, new Category(categories.get(i).getName(), categories.get(i).getId()));
                for (int j = 0; j < QuizzesActivity.kategorije.size(); j++) {
                    if (!QuizzesActivity.kategorije.get(j).getName().equals("Svi")) {
                        QuizzesActivity.kategorije.add(QuizzesActivity.kategorije.size() - 1, new Category(categories.get(i).getName(), categories.get(i).getId()));
                    } else if (!categories.get(i).getName().equals("Svi")) {
                        QuizzesActivity.kategorije.add(QuizzesActivity.kategorije.size() - 1, new Category(categories.get(i).getName(), categories.get(i).getId()));
                    } else if (!categories.get(i).getName().equals(QuizzesActivity.kategorije.get(j).getName())) {
                        QuizzesActivity.kategorije.add(QuizzesActivity.kategorije.size() - 1, new Category(categories.get(i).getName(), categories.get(i).getId()));
                    }
                }
            }
            for (int i = 1; i < QuizzesActivity.kategorije.size(); i++) {
                categories.add(categories.size() - 1, QuizzesActivity.kategorije.get(i));
            }

        categoriesAdapter = new CategoriesAdapter(this, categories, res); }
        if (getIntent().getExtras() == null)
            questions.add(new Question("Add question", null, null, null));
        validateQuestions.addAll(questions);
        validateCategory.addAll(categories);
    adapter = new AddedQuestionAdapter(this, questions, res);
        if (getIntent().getExtras() == null) {
        if(QuizzesActivity.initialDatabaseQuestions.size() == 0) possibleQuestions.addAll(QuizzesActivity.pitanjaIzSQL);
        else {
            for (int i = 0; i < QuizzesActivity.initialDatabaseQuestions.size(); i++) {
                possibleQuestions.add(QuizzesActivity.initialDatabaseQuestions.get(i).second);
            }
        }
    }
        lvAddedQuestions.setAdapter(adapter);
        lvPossibleQuestions.setAdapter(possibleAdapter);
        spCategories.setAdapter(categoriesAdapter);
        //if(mogucaPitanja.size() == 0) lvMogucaPitanja.setAdapter(adapterMoguce);

        lvAddedQuestions.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == questions.size() - 1) {
                    Intent myIntent = new Intent(AddQuizActivity.this, AddQuestionActivity.class);
                    AddQuizActivity.this.startActivityForResult(myIntent, 1);
                } else {
                    possibleQuestions.add(questions.get(position));
                    questions.remove(position);
                    adapter.notifyDataSetChanged();
                    possibleAdapter.notifyDataSetChanged();
                }
            }
        });

        lvPossibleQuestions.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (possibleQuestions.size() != 0) {
                    Question novo = possibleQuestions.get(position);
                    questions.add(questions.size() - 1, novo);
                    possibleQuestions.remove(position);
                    adapter.notifyDataSetChanged();
                    possibleAdapter.notifyDataSetChanged();
                }
            }
        });

        btnAddQuiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean alertttt = false;
                if(!isNetworkAvailable()){
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(AddQuizActivity.this);
                    alertDialog.setTitle("There's no internet connection at the moment, try again in a few moments!");
                    alertDialog.setIcon(R.drawable.error);
                    alertDialog.setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(getApplicationContext(), "You clicked on OK", Toast.LENGTH_SHORT).show();
                                }
                            });
                    alertDialog.show();
                    alertttt = true;
                }
                if(!alertttt) {
                    for (int i = 0; i < QuizzesActivity.validacijaKvizova.size(); i++) {
                        if (QuizzesActivity.validacijaKvizova.get(i).getNaziv().equals(etName.getText().toString())) {
                            has = true;
                        }
                    }
                    if (getIntent().getExtras() == null) {
                        if (etName.getText().toString().length() == 0 || has) {
                            etName.setBackgroundColor(Color.RED);
                            has = false;
                        } else {
                            //validacijaPitanja.clear();
                            etName.setBackgroundColor(Color.WHITE);
                            Category kat = new Category(categories.get(spCategories.getSelectedItemPosition()).getName(), categories.get(spCategories.getSelectedItemPosition()).getId());
                            Quiz quiz = new Quiz(etName.getText().toString(), questions, kat);
                            quizForDatabase = quiz;
                            new AddQuizClass().execute();
                            Intent returnIntent = getIntent();
                            returnIntent.putExtra("nazivKviza", quiz.getNaziv());
                            returnIntent.putExtra("pitanja", quiz.getQuestions());
                            returnIntent.putExtra("Kategorija", quiz.getCategory());
                            setResult(RESULT_OK, returnIntent);
                            finish();
                        }
                    } else {
                        if (!QuizzesActivity.filtrirano) {
                            for (int i = 0; i < QuizzesActivity.kvizovi.size(); i++) {
                                if (QuizzesActivity.kvizovi.get(i).getNaziv().equals(etName.getText().toString()) && QuizzesActivity.kvizovi.get(QuizzesActivity.pozicija).getNaziv().equals(etName.getText().toString())) {
                                    has = false;
                                }
                            }
                        } else if (QuizzesActivity.filtrirano) {
                            for (int i = 0; i < QuizzesActivity.filterKvizovi.size(); i++) {
                                if (QuizzesActivity.filterKvizovi.get(i).getNaziv().equals(etName.getText().toString()) && QuizzesActivity.filterKvizovi.get(QuizzesActivity.pozicija).getNaziv().equals(etName.getText().toString())) {
                                    has = false;
                                }
                            }
                        }
                        if (!QuizzesActivity.filtrirano2) {
                            for (int i = 0; i < QuizzesActivity.kvizovi.size(); i++) {
                                if (QuizzesActivity.kvizovi.get(i).getNaziv().equals(etName.getText().toString()) && QuizzesActivity.kvizovi.get(QuizzesActivity.pozicija).getNaziv().equals(etName.getText().toString())) {
                                    has = false;
                                }
                            }
                        } else if (QuizzesActivity.filtrirano2) {
                            for (int i = 0; i < QuizzesActivity.filterKvizovi2.size(); i++) {
                                if (QuizzesActivity.filterKvizovi2.get(i).getNaziv().equals(etName.getText().toString()) && QuizzesActivity.filterKvizovi2.get(QuizzesActivity.pozicija).getNaziv().equals(etName.getText().toString())) {
                                    has = false;
                                }
                            }
                        }
                        if (etName.getText().toString().length() == 0 || has) {
                            etName.setBackgroundColor(Color.RED);
                            has = false;
                        } else {
                            //validacijaPitanja.clear();
                            etName.setBackgroundColor(Color.WHITE);
                            Category kat = new Category(categories.get(spCategories.getSelectedItemPosition()).getName(), categories.get(spCategories.getSelectedItemPosition()).getId());
                            Quiz quiz = new Quiz(etName.getText().toString(), questions, kat);
                            quizForDatabaseEdit = quiz;
                            String categoriesForEditID = "";
                            for (int i = 0; i < QuizzesActivity.initialDatabaseCategories.size(); i++) {
                                if (QuizzesActivity.initialDatabaseCategories.get(i).second.getName().equals(quizForDatabaseEdit.getCategory().getName())) {
                                    categoriesForEditID = QuizzesActivity.initialDatabaseCategories.get(i).first;
                                    break;
                                }
                            }
                            new EditQuizClass(new AsyncResponse() {
                                @Override
                                public void processFinish() {
                                    adapter.notifyDataSetChanged();
                                }
                            }).execute(quizForEditID, categoriesForEditID);
                            Intent returnIntent = getIntent();
                            returnIntent.putExtra("nazivKviza", quiz.getNaziv());
                            returnIntent.putExtra("pitanja", quiz.getQuestions());
                            returnIntent.putExtra("kategorija", quiz.getCategory());
                            setResult(RESULT_OK, returnIntent);
                            finish();
                        }
                    }
                }
            }
        });

        spCategories.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onClick(View v) {
                // Auto-generated method stub
            }

            @Override
            public void onItemSelected(AdapterView<?> arg0, View view, int position, long arg3) {
                if (position == 0) {
                    // do nothing, ispisuje "Svi"
                } else if (position == categories.size() - 1) {
                    Intent myIntent = new Intent(AddQuizActivity.this, AddCategoryActivity.class);
                    AddQuizActivity.this.startActivityForResult(myIntent, 0);
                } else {
                    //ostale kategorije
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // Auto-generated method stub
            }

        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                categories.add(categories.size() - 1, new Category(data.getStringExtra("nazivKat"), data.getStringExtra("id")));
                QuizzesActivity.kategorije.add(new Category(data.getStringExtra("nazivKat"), data.getStringExtra("id")));
                validateCategory.add(new Category(data.getStringExtra("nazivKat"), data.getStringExtra("id")));
                categoriesAdapter.notifyDataSetChanged();
            }
            if (resultCode == RESULT_CANCELED) {
                //Write your code if there's no result
            }

        } else if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                questions.add(questions.size() - 1, new Question(data.getStringExtra("nazivPitanja"), data.getStringExtra("tekstPitanja"), (ArrayList<String>) data.getSerializableExtra("odgovori"), data.getStringExtra("isTacan")));
                validateQuestions.add(new Question(data.getStringExtra("nazivPitanja"), data.getStringExtra("tekstPitanja"), (ArrayList<String>) data.getSerializableExtra("odgovori"), data.getStringExtra("isTacan")));
                adapter.notifyDataSetChanged();
                possibleAdapter.notifyDataSetChanged();
            }
            if (resultCode == RESULT_CANCELED) {
                //Write your code if there's no result
            }
        } /*else if (requestCode == 2) {
            if (resultCode == RESULT_OK) {
                Uri uri = null;
                if (data != null) {
                    uri = data.getData();
                    ArrayList<String> dat = null;
                    try {
                        dat = readTextFromUri(uri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    assert dat != null;
                    boolean postoji = false;
                    boolean alert = false;
                    boolean sadrzi = false;
                    boolean sadrzi2 = false;
                    if (ispravnaDatoteka) {
                        for (int i = 0; i < dat.size(); i++) {
                            if (i == 0) {
                                int brojac = 0;
                                int pokazivac = 0;
                                for (int j = 0; j < dat.get(i).length(); j++) {
                                    if (dat.get(i).charAt(j) == ',' && brojac == 0) {
                                        String naziv = dat.get(i).substring(pokazivac, j);
                                        for (int k = 0; k < KvizoviAkt.kvizovi.size(); k++) {
                                            if (KvizoviAkt.kvizovi.get(k).getNaziv().equals(naziv)) {
                                                postoji = true;
                                            }
                                        }
                                        if (postoji) {
                                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(DodajKvizAkt.this);
                                            alertDialog.setTitle("Kviz kojeg importujete već postoji!");
                                            alertDialog.setIcon(R.drawable.error);
                                            alertDialog.setPositiveButton("OK",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            Toast.makeText(getApplicationContext(), "You clicked on OK", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                            alertDialog.show();
                                            alert = true;
                                            break;
                                        } else {
                                            novi.setNaziv(naziv);
                                            postoji = false;
                                        }
                                        pokazivac = j + 1;
                                        brojac++;
                                    } else if (dat.get(i).charAt(j) == ',' && brojac == 1) {
                                        if (!postoji) {
                                            String kategorija = dat.get(i).substring(pokazivac, j);
                                            novi.setKategorija(new Kategorija(kategorija));
                                            for (int k = 0; k < kategorije.size(); k++) {
                                                if (kategorije.get(k).getNaziv().equals(kategorija)) {
                                                    sadrzi = true;
                                                }
                                            }
                                            for (int k = 0; k < KvizoviAkt.kategorije.size(); k++) {
                                                if (KvizoviAkt.kategorije.get(k).getNaziv().equals(kategorija)) {
                                                    sadrzi2 = true;
                                                }
                                            }
                                            pokazivac = j + 1;
                                            brojac++;
                                        }
                                    } else if (brojac == 2) {
                                        if (!postoji) {
                                            try {
                                                int brojPitanja = Integer.parseInt(dat.get(i).substring(pokazivac, ++j));
                                                if (brojPitanja != dat.size() - 1) {
                                                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(DodajKvizAkt.this);
                                                    alertDialog.setTitle("Kviz kojeg imporujete ima neispravan broj pitanja!");
                                                    alertDialog.setIcon(R.drawable.error);
                                                    alertDialog.setPositiveButton("OK",
                                                            new DialogInterface.OnClickListener() {
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    Toast.makeText(getApplicationContext(), "You clicked on OK", Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                    alertDialog.show();
                                                    alert = true;
                                                    break;
                                                }
                                                brojac++;
                                            } catch (Exception e) {
                                                AlertDialog.Builder alertDialog = new AlertDialog.Builder(DodajKvizAkt.this);
                                                alertDialog.setTitle("Kviz kojeg imporujete ima neispravan broj pitanja!");
                                                alertDialog.setIcon(R.drawable.error);
                                                alertDialog.setPositiveButton("OK",
                                                        new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                Toast.makeText(getApplicationContext(), "You clicked on OK", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                alertDialog.show();
                                                alert = true;
                                                break;
                                            }
                                        }
                                    }
                                }
                            } else {
                                if (!postoji && !alert) {
                                    int brojac = 0;
                                    int pokazivac = 0;
                                    int brojOdgovora = 0;
                                    int tacanOdg = 0;
                                    Pitanje novoPitanje = new Pitanje();
                                    //novaPitanja = new ArrayList<>();
                                    for (int j = 0; j < dat.get(i).length(); j++) {
                                        if (dat.get(i).charAt(j) == ',' && brojac == 0) {
                                            String pitanje = dat.get(i).substring(pokazivac, j);
                                            novoPitanje.setNaziv(pitanje);
                                            boolean postojiPitanjeUBazi = false;
                                            for(int ima = 0; ima < mogucaPitanja.size(); ima++){
                                                if(mogucaPitanja.get(ima).getNaziv().equals(novoPitanje.getNaziv())){
                                                    postojiPitanjeUBazi = true;
                                                }
                                            }
                                            if(postojiPitanjeUBazi && !alert){
                                                AlertDialog.Builder alertDialog = new AlertDialog.Builder(DodajKvizAkt.this);
                                                alertDialog.setTitle("Pitanje se vec nalazi u bazi!");
                                                alertDialog.setIcon(R.drawable.error);
                                                alertDialog.setPositiveButton("OK",
                                                        new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                Toast.makeText(getApplicationContext(), "You clicked on OK", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                alertDialog.show();
                                                alert = true;
                                            } else {
                                                novoPitanje.setTekstPitanja(pitanje);
                                                boolean duploPitanje = false;
                                                for (int s = 0; s < novaPitanja.size(); s++) {
                                                    if (novoPitanje.getNaziv().equals(novaPitanja.get(s).getNaziv())) {
                                                        duploPitanje = true;
                                                    }
                                                }
                                                if (duploPitanje && !alert) {
                                                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(DodajKvizAkt.this);
                                                    alertDialog.setTitle("Kviz nije ispravan postoje dva pitanja sa istim nazivom!");
                                                    alertDialog.setIcon(R.drawable.error);
                                                    alertDialog.setPositiveButton("OK",
                                                            new DialogInterface.OnClickListener() {
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    Toast.makeText(getApplicationContext(), "You clicked on OK", Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                    alertDialog.show();
                                                    alert = true;
                                                } else {
                                                    pokazivac = j + 1;
                                                    brojac++;
                                                }
                                            }
                                        } else if (dat.get(i).charAt(j) == ',' && brojac == 1 && !alert) {
                                            try {
                                                brojOdgovora = Integer.parseInt(dat.get(i).substring(pokazivac, j));
                                                pokazivac = j + 1;
                                                brojac++;
                                            } catch (Exception e) {

                                            }
                                        } else if (dat.get(i).charAt(j) == ',' && brojac == 2 && !alert) {
                                            try {
                                                tacanOdg = Integer.parseInt(dat.get(i).substring(pokazivac, j));
                                                if (tacanOdg < 0 || tacanOdg > brojOdgovora) {
                                                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(DodajKvizAkt.this);
                                                    alertDialog.setTitle("Kviz kojeg importujete ima neispravan index tačnog odgovora!");
                                                    alertDialog.setIcon(R.drawable.error);
                                                    alertDialog.setPositiveButton("OK",
                                                            new DialogInterface.OnClickListener() {
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    Toast.makeText(getApplicationContext(), "You clicked on OK", Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                    alertDialog.show();
                                                    alert = true;
                                                    break;
                                                }
                                                pokazivac = j + 1;
                                                brojac++;
                                            } catch (Exception e) {
                                                AlertDialog.Builder alertDialog = new AlertDialog.Builder(DodajKvizAkt.this);
                                                alertDialog.setTitle("Kviz kojeg importujete ima neispravan index tačnog odgovora!");
                                                alertDialog.setIcon(R.drawable.error);
                                                alertDialog.setPositiveButton("OK",
                                                        new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                Toast.makeText(getApplicationContext(), "You clicked on OK", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                alertDialog.show();
                                                alert = true;
                                                break;
                                            }
                                        } else if (brojac == 3 && !alert) {
                                            boolean dupliOdgovor = false;
                                            if (!alert) {
                                                ArrayList<String> listaOdgovora = new ArrayList<>();
                                                String odg = dat.get(i).substring(pokazivac, dat.get(i).length());
                                                int pok2 = 0;
                                                for (int k = 0; k < odg.length(); k++) {
                                                    if (k == odg.length() - 1) {
                                                        if (listaOdgovora.contains(odg.substring(pok2, odg.length())))
                                                            dupliOdgovor = true;
                                                        listaOdgovora.add(odg.substring(pok2, odg.length()));
                                                    } else {
                                                        if (odg.charAt(k) == ',') {
                                                            if (listaOdgovora.contains(odg.substring(pok2, k)))
                                                                dupliOdgovor = true;
                                                            listaOdgovora.add(odg.substring(pok2, k));
                                                            pok2 = k + 1;
                                                        }
                                                    }
                                                }
                                                if (dupliOdgovor) {
                                                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(DodajKvizAkt.this);
                                                    alertDialog.setTitle("Kviz kojeg importujete nije ispravan postoji ponavljanje odgovora!");
                                                    alertDialog.setIcon(R.drawable.error);
                                                    alertDialog.setPositiveButton("OK",
                                                            new DialogInterface.OnClickListener() {
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    Toast.makeText(getApplicationContext(), "You clicked on OK", Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                    alertDialog.show();
                                                    alert = true;
                                                    break;
                                                } else {
                                                    novoPitanje.setTacan(listaOdgovora.get(tacanOdg));
                                                    novoPitanje.setOdgovori(listaOdgovora);
                                                    novaPitanja.add(novoPitanje);
                                                    novi.setPitanja(novaPitanja);
                                                    if (brojOdgovora != listaOdgovora.size()) {
                                                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(DodajKvizAkt.this);
                                                        alertDialog.setTitle("Kviz kojeg importujete ima neispravan broj odgovora!");
                                                        alertDialog.setIcon(R.drawable.error);
                                                        alertDialog.setPositiveButton("OK",
                                                                new DialogInterface.OnClickListener() {
                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                        Toast.makeText(getApplicationContext(), "You clicked on OK", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });
                                                        alertDialog.show();
                                                        alert = true;
                                                        break;
                                                    }
                                                }
                                                brojac++;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (!alert) {
                            etNaziv.setText(novi.getNaziv());
                            if (!sadrzi) {
                                kategorije.add(kategorije.size() - 1, novi.getKategorija());
                                new DodajKategorijuIzImporta().execute();
                                adapterKategorije.notifyDataSetChanged();
                                spKategorije.setSelection(kategorije.size() - 2);
                            } else {
                                int poz = 0;
                                for (int i = 0; i < kategorije.size(); i++) {
                                    if (kategorije.get(i).getNaziv().equals(novi.getKategorija().getNaziv())) {
                                        poz = i;
                                    }
                                }
                                if (poz == 0) {
                                    spKategorije.setSelection(kategorije.size() - 2);
                                } else spKategorije.setSelection(poz);
                                sadrzi = false;
                            }
                            if (!sadrzi2) {
                                KvizoviAkt.kategorije.add(novi.getKategorija());
                            } else sadrzi2 = false;
                            //spKategorije.setSelection(kategorije.size() - 2);
                            pitanja.removeAll(pitanja);
                            pitanja.add(new Pitanje("Dodaj pitanje", null, null, null));
                            pitanja.addAll(pitanja.size() - 1, novi.getPitanja());
                            adapter.notifyDataSetChanged();
                            validacijaPitanja.addAll(novi.getPitanja());
                            new DodajPitanjeIzImporta().execute();
                        }
                    }
                }
            }
        }
    }

    private ArrayList<String> readTextFromUri(Uri uri) throws IOException {
        ArrayList<String> redovi = new ArrayList<>();
        InputStream inputStream = getContentResolver().openInputStream(uri);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            redovi.add(line);
        }
        redovi.remove(redovi.size() - 1);
        return redovi;
    }*/
    }
}
