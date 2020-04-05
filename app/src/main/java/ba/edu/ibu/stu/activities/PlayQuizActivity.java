package ba.edu.ibu.stu.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.provider.AlarmClock;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import ba.edu.ibu.stu.R;
import ba.edu.ibu.stu.fragments.InfoFragment;
import ba.edu.ibu.stu.fragments.QuestionFragment;
import ba.edu.ibu.stu.fragments.RankListFragment;
import ba.edu.ibu.stu.classes.Quiz;
import ba.edu.ibu.stu.classes.Question;

public class PlayQuizActivity extends AppCompatActivity implements QuestionFragment.PromjenaVrijednosti, InfoFragment.Nazad {
    private int brojTacnihPitanja = 0;
    private int brojOstalihPitanja = 0;
    private double procenat = 0;
    private int brojDoTadaOdgovora = 0;
    private Quiz quiz = new Quiz();
    private ArrayList<Question> listPitanjaKviza = new ArrayList<>();
    private int i = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_quiz);
        quiz.setNaziv(getIntent().getStringExtra("nazivKviza"));
        listPitanjaKviza = (ArrayList<Question>) getIntent().getSerializableExtra("pitanja");
        if (listPitanjaKviza.size() != 0) {
            if (listPitanjaKviza.get(listPitanjaKviza.size() - 1).getNaziv().equals("Dodaj pitanje"))
                listPitanjaKviza.remove(listPitanjaKviza.size() - 1);
        }

        if(listPitanjaKviza.size() != 0) {
            //broj minuta do eventa, ako je manji nemas pravo igrat kviz
            double x = (double) listPitanjaKviza.size() / 2;
            int pom = (int) Math.round(x);
            int sati = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            int minute = Calendar.getInstance().get(Calendar.MINUTE) + pom;
            if (sati > 23) {
                sati = 0;
            }
            while (minute >= 60) {
                minute -= 60;
                sati++;
                if (sati > 23) {
                    sati = 0;
                }
            }
            Intent i = new Intent(AlarmClock.ACTION_SET_ALARM);
            i.putExtra(AlarmClock.EXTRA_SKIP_UI, true);
            i.putExtra(AlarmClock.EXTRA_HOUR, sati);
            i.putExtra(AlarmClock.EXTRA_MINUTES, minute);
            startActivity(i);
        }
        else {
            Toast.makeText(getApplicationContext(), "Broj pitanja je nula, alarm se ne aktivira", Toast.LENGTH_LONG).show();
        }

        brojOstalihPitanja = listPitanjaKviza.size() - 1;
        quiz.setPitanja(listPitanjaKviza);
        Collections.shuffle(quiz.getQuestions());
        Question randomQuestion;
        if (listPitanjaKviza.size() != 0) {
            randomQuestion = quiz.getQuestions().get(0);
        } else {
            brojOstalihPitanja = 0;
            ArrayList<String> odg = new ArrayList<>();
            randomQuestion = new Question("Kviz je završen!", "Kviz je završen!", odg, "");
        }
        if (randomQuestion.getNaziv().equals("Kviz je završen!")) {
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(PlayQuizActivity.this);
            alertDialog.setTitle("UNOS");
            alertDialog.setMessage("Unesite ime: ");
            final EditText input = new EditText(PlayQuizActivity.this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            input.setLayoutParams(lp);
            alertDialog.setView(input);
            alertDialog.setIcon(R.drawable.error);
            alertDialog.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
            //alertDialog.show();
            final AlertDialog ad = alertDialog.create();
            ad.show();
            ad.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //System.out.println("INPUT JEE: " + input.getText().toString());
                    if (input.getText().toString().length() != 0) {
                        Bundle arg = new Bundle();
                        RankListFragment df = new RankListFragment();
                        arg.putString("nazivKviza", quiz.getNaziv());
                        arg.putString("imeIgraca", input.getText().toString());
                        arg.putString("procenat", String.valueOf(procenat));
                        df.setArguments(arg);
                        getSupportFragmentManager().beginTransaction().replace(R.id.pitanjePlace, df).commit();
                        Toast.makeText(getApplicationContext(), "You clicked on OK", Toast.LENGTH_SHORT).show();
                        ad.dismiss();
                    } else {
                        input.setBackgroundColor(Color.RED);
                        Toast.makeText(getApplicationContext(), "Unesite ime", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            FragmentManager fm = getSupportFragmentManager();
            FrameLayout pitanja = (FrameLayout) findViewById(R.id.pitanjePlace);

            if (pitanja != null) {
                QuestionFragment fd = (QuestionFragment) fm.findFragmentById(R.id.pitanjePlace);
                if (fd == null) {
                    fd = new QuestionFragment();
                    Bundle arg = new Bundle();
                    arg.putString("tekstPitanja", randomQuestion.getNaziv());
                    arg.putStringArrayList("odgovoriPitanja", randomQuestion.getOdgovori());
                    arg.putString("jesteTacan", randomQuestion.getTacan());
                    fd.setArguments(arg);
                    fm.beginTransaction().replace(R.id.pitanjePlace, fd).commit();
                } else {
                    fm.popBackStack(null, android.support.v4.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
                }
            }
        }
        FragmentManager fm = getSupportFragmentManager();
        FrameLayout inf = (FrameLayout) findViewById(R.id.informacijePlace);

        if (inf != null) {
            InfoFragment fd = (InfoFragment) fm.findFragmentById(R.id.informacijePlace);
            if (fd == null) {
                fd = new InfoFragment();
                Bundle arg = new Bundle();
                arg.putString("naziv", quiz.getNaziv());
                arg.putString("brojTacnihPitanja", String.valueOf(brojTacnihPitanja));
                arg.putString("brojOstalihPitanja", String.valueOf(brojOstalihPitanja));
                arg.putString("procenat", String.valueOf(procenat));
                fd.setArguments(arg);
                fm.beginTransaction().replace(R.id.informacijePlace, fd).commit();
            } else {
                fm.popBackStack(null, android.support.v4.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        }
    }

    @Override
    public void promijeniVrijednostiTacan() {
        Bundle arg = new Bundle();
        brojTacnihPitanja++;
        if (brojOstalihPitanja == 0) {
            brojOstalihPitanja = 0;
        } else brojOstalihPitanja--;
        brojDoTadaOdgovora++;
        procenat = (double) brojTacnihPitanja / brojDoTadaOdgovora;
        procenat = Math.round(procenat * 100.0) / 100.0;
        arg.putString("naziv", quiz.getNaziv());
        arg.putString("brojTacnihPitanja", String.valueOf(brojTacnihPitanja));
        arg.putString("brojOstalihPitanja", String.valueOf(brojOstalihPitanja));
        arg.putString("procenat", String.valueOf(procenat));
        InfoFragment df = new InfoFragment();
        df.setArguments(arg);
        getSupportFragmentManager().beginTransaction().replace(R.id.informacijePlace, df).commit();
    }

    @Override
    public void promijeniVrijednostiOstali() {
        if (brojOstalihPitanja == 0) {
            brojOstalihPitanja = 0;
        } else brojOstalihPitanja--;
        Bundle arg = new Bundle();
        brojDoTadaOdgovora++;
        procenat = (double) brojTacnihPitanja / brojDoTadaOdgovora;
        procenat = Math.round(procenat * 100.0) / 100.0;
        arg.putString("naziv", quiz.getNaziv());
        arg.putString("brojTacnihPitanja", String.valueOf(brojTacnihPitanja));
        arg.putString("brojOstalihPitanja", String.valueOf(brojOstalihPitanja));
        arg.putString("procenat", String.valueOf(procenat));
        InfoFragment df = new InfoFragment();
        df.setArguments(arg);
        getSupportFragmentManager().beginTransaction().replace(R.id.informacijePlace, df).commit();
    }

    @Override
    public void funkcija() {
        Question randomQuestion;
        i++;
        if (i == listPitanjaKviza.size()) {
            ArrayList<String> odg = new ArrayList<>();
            randomQuestion = new Question("Kviz je završen!", "Kviz je završen!", odg, "");
        } else {
            randomQuestion = listPitanjaKviza.get(i);
        }
        if (randomQuestion.getNaziv().equals("Kviz je završen!")) {
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(PlayQuizActivity.this);
            alertDialog.setTitle("UNOS");
            alertDialog.setMessage("Unesite ime: ");
            final EditText input = new EditText(PlayQuizActivity.this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            input.setLayoutParams(lp);
            alertDialog.setView(input);
            alertDialog.setIcon(R.drawable.error);
            alertDialog.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
            //alertDialog.show();
            final AlertDialog ad = alertDialog.create();
            ad.show();
            ad.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //System.out.println("INPUT JEE: " + input.getText().toString());
                    if (input.getText().toString().length() != 0) {
                        Bundle arg = new Bundle();
                        RankListFragment df = new RankListFragment();
                        arg.putString("nazivKviza", quiz.getNaziv());
                        arg.putString("imeIgraca", input.getText().toString());
                        arg.putString("procenat", String.valueOf(procenat));
                        df.setArguments(arg);
                        getSupportFragmentManager().beginTransaction().replace(R.id.pitanjePlace, df).commit();
                        Toast.makeText(getApplicationContext(), "You clicked on OK", Toast.LENGTH_SHORT).show();
                        ad.dismiss();
                    } else {
                        input.setBackgroundColor(Color.RED);
                        Toast.makeText(getApplicationContext(), "Unesite ime", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Bundle arg = new Bundle();
            QuestionFragment df = new QuestionFragment();
            arg.putString("tekstPitanja", randomQuestion.getNaziv());
            arg.putStringArrayList("odgovoriPitanja", randomQuestion.getOdgovori());
            arg.putString("jesteTacan", randomQuestion.getTacan());
            df.setArguments(arg);
            getSupportFragmentManager().beginTransaction().replace(R.id.pitanjePlace, df).commit();
        }
    }

    @Override
    public void nazad() {
        super.onBackPressed();
    }
}
