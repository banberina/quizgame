package ba.edu.ibu.stu.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
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
import ba.edu.ibu.stu.classes.Question;

public class AddQuestionActivity extends AppCompatActivity {
    private EditText etName;
    private EditText etAnswer;
    private ListView lvAnswers;
    private Button btnAddCorrect;
    private Button btnAddAnswer;
    private Button btnAddQuestion;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> answers = new ArrayList<>();
    private Question questionAtTheMoment = new Question();
    private boolean has = false;
    private String correctAnswer = "";
    private boolean hasCorrect = false;
    private boolean hasAnswer = false;
    private boolean hasCorrectAnswer = false;
    public static int correctIndex = 0;
    public static ArrayList<String> questionID = new ArrayList<String>();

    //DODAVANJE PITANJA U BAZU
    public class QuestionClass extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            InputStream is = getResources().openRawResource(R.raw.secret);
            try {
                GoogleCredential credentials = GoogleCredential.fromStream(is).createScoped(Lists.newArrayList("https://www.googleapis.com/auth/datastore"));
                credentials.refreshToken();
                String TOKEN = credentials.getAccessToken();
                System.out.println("SADA JE TOKEN: " + TOKEN);
                String url = "https://firestore.googleapis.com/v1/projects/quizgame-9bb9e/databases/(default)/documents/Questions?access_token=";
                URL urlObj = new URL(url + URLEncoder.encode(TOKEN, "UTF-8"));
                HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");

                String dokument = "{ \"fields\": { \"name\": {\"stringValue\":\"" + etName.getText().toString() + "\"} , \"answers\" : { \"arrayValue\" : { \"values\" : [";
                for (int i = 0; i < answers.size(); i++) {
                    if (i != answers.size() - 1) {
                        dokument += "{\"stringValue\" : \"" + answers.get(i) + "\"},";
                    } else {
                        dokument += "{\"stringValue\" : \"" + answers.get(i) + "\"}";
                    }
                }
                dokument += "]}} , \"correctIndex\": {\"integerValue\":\"" + correctIndex + "\"}}}";
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
                String rezultat = response.toString();
                JSONObject jo = new JSONObject(rezultat);
                String name = jo.getString("name");
                JSONObject fields = jo.getJSONObject("fields");
                JSONObject stringValue = fields.getJSONObject("name");
                String naziv = stringValue.getString("stringValue");
                JSONObject integerValue = fields.getJSONObject("correctIndex");
                int indexTacnog = integerValue.getInt("integerValue");
                JSONObject odgg = fields.getJSONObject("answers");
                JSONObject arrayValue = odgg.getJSONObject("arrayValue");
                JSONArray values = arrayValue.getJSONArray("values");
                ArrayList<String> bazaOdgovori = new ArrayList<>();
                for (int i = 0; i < values.length(); i++) {
                    JSONObject item = values.getJSONObject(i);
                    String odg = item.getString("stringValue");
                    bazaOdgovori.add(odg);
                }
                String tacanOdgovorBaza = "";
                for (int i = 0; i < bazaOdgovori.size(); i++) {
                    if (i == indexTacnog) {
                        tacanOdgovorBaza = bazaOdgovori.get(i);
                        break;
                    }
                }
                int brojac = 0;
                for (int i = 0; i < name.length(); i++) {
                    if (name.charAt(i) == '/') {
                        brojac++;
                    }
                    if (brojac == 6) {
                        name = name.substring(++i, name.length());
                        break;
                    }
                }
                questionID.add(name);
                Question newDatabaseQuestion = new Question(naziv, naziv, bazaOdgovori, tacanOdgovorBaza);
                QuizzesActivity.initialDatabaseQuestions.add(new Pair<String, Question>(name, newDatabaseQuestion));
            } catch (IOException e) {
                //e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_question);
        etName = (EditText) findViewById(R.id.etNaziv);
        etAnswer = (EditText) findViewById(R.id.etOdgovor);
        lvAnswers = (ListView) findViewById(R.id.lvOdgovori);
        btnAddAnswer = (Button) findViewById(R.id.btnDodajOdgovor);
        btnAddCorrect = (Button) findViewById(R.id.btnDodajTacan);
        btnAddQuestion = (Button) findViewById(R.id.btnDodajPitanje);
        etName.setHint("Input question name");
        etAnswer.setHint("Answer");
        Resources res = getResources();
        adapter = new ArrayAdapter<String>(this, R.layout.element_lists, R.id.Itemname, answers) {
            @Override
            public View getView(int position, @Nullable View convertView, @Nullable ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                String s = answers.get(position);
                TextView naziv = (TextView) view.findViewById(R.id.Itemname);
                ImageView ikona = (ImageView) view.findViewById(R.id.icon);
                ikona.setImageResource(R.drawable.slika);
                naziv.setText(s);
                if (s.equals(correctAnswer)) {
                    naziv.setBackgroundColor(Color.GREEN);
                    correctIndex = position;
                } else {
                    naziv.setBackgroundColor(0);
                }
                return view;
            }
        };

        lvAnswers.setAdapter(adapter);

        lvAnswers.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (answers.get(position).equals(correctAnswer)) btnAddCorrect.setEnabled(true);
                answers.remove(position);
                adapter.notifyDataSetChanged();
            }
        });

        btnAddAnswer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (String odg : answers) {
                    if (odg.equals(etAnswer.getText().toString())) {
                        hasAnswer = true;
                    }
                }
                if (hasAnswer) {
                    etAnswer.setBackgroundColor(Color.RED);
                    hasAnswer = false;
                } else if (etAnswer.getText().toString().equals("")) {
                    etAnswer.setBackgroundColor(Color.RED);
                } else {
                    etAnswer.setBackgroundColor(Color.WHITE);
                    answers.add(etAnswer.getText().toString());
                    adapter.notifyDataSetChanged();
                    etAnswer.setText("");
                }
            }
        });

        btnAddCorrect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (String odg : answers) {
                    if (odg.equals(etAnswer.getText().toString())) {
                        hasCorrectAnswer = true;
                    }
                }
                if (hasCorrectAnswer) {
                    etAnswer.setBackgroundColor(Color.RED);
                    hasCorrectAnswer = false;
                } else if (etAnswer.getText().toString().equals("")) {
                    etAnswer.setBackgroundColor(Color.RED);
                } else {
                    etAnswer.setBackgroundColor(Color.WHITE);
                    answers.add(etAnswer.getText().toString());
                    correctAnswer = etAnswer.getText().toString();
                    questionAtTheMoment.setTacan(etAnswer.getText().toString());
                    adapter.notifyDataSetChanged();
                    etAnswer.setText("");
                    btnAddCorrect.setEnabled(false);
                }
            }
        });

        btnAddQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean alert = false;
                if(!isNetworkAvailable()){
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(AddQuestionActivity.this);
                    alertDialog.setTitle("No internet connection at the moment, try later!");
                    alertDialog.setIcon(R.drawable.error);
                    alertDialog.setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(getApplicationContext(), "You clicked on OK", Toast.LENGTH_SHORT).show();
                                }
                            });
                    alertDialog.show();
                    alert = true;
                }
                if(!alert) {
                    for (int i = 0; i < AddQuizActivity.validateQuestions.size(); i++) {
                        if (AddQuizActivity.validateQuestions.get(i).getNaziv().equals(etName.getText().toString())) {
                            has = true;
                        }
                    }
                    for (int i = 0; i < QuizzesActivity.initialDatabaseQuestions.size(); i++) {
                        if (QuizzesActivity.initialDatabaseQuestions.get(i).second.getNaziv().equals(etName.getText().toString())) {
                            has = true;
                        }
                    }
                    for (String odgovor : answers) {
                        if (odgovor.equals(correctAnswer)) hasCorrect = true;
                    }
                    if (etName.getText().toString().length() == 0 || has) {
                        etName.setBackgroundColor(Color.RED);
                        has = false;
                    } else if (!hasCorrect) {
                        //ne radi nista, nema tacnog odgovora
                    } else {
                        new QuestionClass().execute();
                        etName.setBackgroundColor(Color.WHITE);
                        questionAtTheMoment.setNaziv(etName.getText().toString());
                        questionAtTheMoment.setTekstPitanja(etName.getText().toString());
                        questionAtTheMoment.setOdgovori(answers);
                        QuizzesActivity.sqlHelper.dodajPitanjeUSQL(questionAtTheMoment);
                        Intent returnIntent = getIntent();
                        returnIntent.putExtra("nazivPitanja", questionAtTheMoment.getNaziv());
                        returnIntent.putExtra("tekstPitanja", questionAtTheMoment.getTekstPitanja());
                        returnIntent.putExtra("odgovori", questionAtTheMoment.getOdgovori());
                        returnIntent.putExtra("isTacan", questionAtTheMoment.getTacan());
                        setResult(RESULT_OK, returnIntent);
                        finish();
                    }
                }
            }
        });
    }
}
