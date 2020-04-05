package ba.edu.ibu.stu.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.common.collect.Lists;
import com.maltaisn.icondialog.Icon;
import com.maltaisn.icondialog.IconDialog;

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

import ba.edu.ibu.stu.R;
import ba.edu.ibu.stu.classes.Category;

public class AddCategoryActivity extends AppCompatActivity implements IconDialog.Callback {
    private EditText etName;
    private EditText etIcon;
    private Button btnAddIcon;
    private Button btnDodajKategoriju;
    private Icon[] icons;
    private IconDialog iconDialog = new IconDialog();
    private boolean has = false;

    //KLASA YA DODAVANJE U FIREBASE
    public class KlasaKategorija extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            InputStream is = getResources().openRawResource(R.raw.secret);
            try {
                GoogleCredential credentials = GoogleCredential.fromStream(is).createScoped(Lists.newArrayList("https://www.googleapis.com/auth/datastore"));
                credentials.refreshToken();
                String TOKEN = credentials.getAccessToken();
                System.out.println("Current token: " + TOKEN);
                String url = "https://firestore.googleapis.com/v1/projects/quizgame-9bb9e/databases/(default)/documents/Categories?access_token=";
                URL urlObj = new URL(url + URLEncoder.encode(TOKEN, "UTF-8"));
                HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");

                String dokument = "{ \"fields\": { \"name\": {\"stringValue\":\"" + etName.getText().toString() + "\"},  \"iconID\": {\"integerValue\":\"" + Integer.parseInt(etIcon.getText().toString()) + "\"}}}";
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
                JSONObject integerValue = fields.getJSONObject("iconID");
                int idIkoniceKategorije = integerValue.getInt("integerValue");
                Category categoryAtTheMoment = new Category(naziv, String.valueOf(idIkoniceKategorije));
                //KvizoviAkt.kategorije.add(trenutnaKategorija);
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
                Pair<String, Category> par = new Pair<String, Category>(name, categoryAtTheMoment);
                QuizzesActivity.initialDatabaseCategories.add(par);
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
        setContentView(R.layout.activity_add_category);
        etName = (EditText) findViewById(R.id.etNaziv);
        etIcon = (EditText) findViewById(R.id.etIkona);
        etName.setHint("Category name");
        etIcon.setHint("Icon");
        etIcon.setEnabled(false);
        btnAddIcon = (Button) findViewById(R.id.btnDodajIkonu);
        btnDodajKategoriju = (Button) findViewById(R.id.btnDodajKategoriju);

        btnAddIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iconDialog.setSelectedIcons(icons);
                iconDialog.show(getSupportFragmentManager(), "icon_dialog");
                etIcon.setEnabled(true);
            }
        });

        btnDodajKategoriju.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean alert = false;
                if(!isNetworkAvailable()){
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(AddCategoryActivity.this);
                    alertDialog.setTitle("No internet connection at the moment, try again later");
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
                    for (int i = 0; i < AddQuizActivity.validateCategory.size(); i++) {
                        if (AddQuizActivity.validateCategory.get(i).getName().equals(etName.getText().toString())) {
                            has = true;
                        }
                    }
                /*for (int i = 0; i < KvizoviAkt.kategorije.size(); i++) {
                    if (kategorijeParovi.get(i).second.getNaziv().equals(etNaziv.getText().toString())) {
                        ima = true;
                    }
                }*/
                    if (has) {
                        etName.setBackgroundColor(Color.RED);
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(AddCategoryActivity.this);
                        alertDialog.setTitle("The category with this name already exists!");
                        alertDialog.setIcon(R.drawable.error);
                        alertDialog.setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        Toast.makeText(getApplicationContext(), "You clicked on OK", Toast.LENGTH_SHORT).show();
                                    }
                                });
                        alertDialog.show();
                        if (etIcon.getText().toString().length() == 0) {
                            etIcon.setBackgroundColor(Color.RED);
                        } else etIcon.setBackgroundColor(Color.WHITE);
                        has = false;
                    } else if (etName.getText().toString().length() == 0 || etIcon.getText().toString().length() == 0) {
                        if (etName.getText().toString().length() == 0) {
                            etName.setBackgroundColor(Color.RED);
                        } else etName.setBackgroundColor(Color.WHITE);
                        if (etIcon.getText().toString().length() == 0) {
                            etIcon.setBackgroundColor(Color.RED);
                        } else etIcon.setBackgroundColor(Color.WHITE);
                    } else {
                        new KlasaKategorija().execute();
                        etName.setBackgroundColor(Color.WHITE);
                        etIcon.setBackgroundColor(Color.WHITE);
                        Category category = new Category(etName.getText().toString(), etIcon.getText().toString());
                        QuizzesActivity.sqlHelper.dodajKategorijuUSQL(category);
                        Intent returnIntent = getIntent();
                        returnIntent.putExtra("nazivKat", category.getName());
                        returnIntent.putExtra("id", category.getId());
                        setResult(RESULT_OK, returnIntent);
                        finish();
                    }
                }
            }
        });
    }

    @Override
    public void onIconDialogIconsSelected(Icon[] icons) {
        this.icons = icons;
        etIcon.setText(String.valueOf(this.icons[0].getId()));
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
