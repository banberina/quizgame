package ba.edu.ibu.stu.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import ba.edu.ibu.stu.R;
import ba.edu.ibu.stu.activities.QuizzesActivity;
import ba.edu.ibu.stu.classes.AsyncResponse;

public class RankListFragment extends Fragment {
    private ListView lvRangLista;
    private String nazivKviza;
    private String procenat;
    private String imeIgraca;
    private int pozicija = 0;
    private ArrayAdapter<String> adapter;
    public static ArrayList<String> ukupnaRangLista = new ArrayList<>();
    private ArrayList<Pair<String, Map<Integer, Map<String, String>>>> rangListaIzBaze = new ArrayList<>();
    private ArrayList<Map<Integer, Map<String, String>>> sviPodaciLista = new ArrayList<>();
    private boolean nestaloNeta = false;

    public class KlasaDodajURangListuBaza extends AsyncTask<String, Void, Void>{

        @Override
        protected Void doInBackground(String... strings) {
            InputStream is = getResources().openRawResource(R.raw.secret);
            try {
                GoogleCredential credentials = GoogleCredential.fromStream(is).createScoped(Lists.newArrayList("https://www.googleapis.com/auth/datastore"));
                credentials.refreshToken();
                String TOKEN = credentials.getAccessToken();
                System.out.println("SADA JE TOKEN: " + TOKEN);
                String url = "https://firestore.googleapis.com/v1/projects/quizgame-9bb9e/databases/(default)/documents/RankList?access_token=";
                URL urlObj = new URL (url + URLEncoder.encode(TOKEN, "UTF-8"));
                HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");

                String dokument = "{\"fields\" : { \"name\" : { \"stringValue\" : \"" + nazivKviza + "\" }, \"list\" : { \"mapValue\" : { \"fields\" : { \"position\" : { \"integerValue\" : \"" + pozicija + "\" }, \"map\" : { \"mapValue\" : { \"fields\" : { \"percentage\" : { \"stringValue\" : \"" + procenat + "\" }, \"playerName\" : { \"stringValue\" : \"" + imeIgraca + "\" }}}}}}}}}";
                try (OutputStream os = connection.getOutputStream())
                {
                    byte[] input = dokument.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int code = connection.getResponseCode();
                InputStream odgovor = connection.getInputStream();
                StringBuilder response = null;
                try(BufferedReader br = new BufferedReader (
                        new InputStreamReader(odgovor, "utf-8"))
                ){
                    response = new StringBuilder();
                    String rensponseLine = null;
                    while((rensponseLine = br.readLine()) != null){
                        response.append(rensponseLine.trim());
                    }
                    Log.d("ODGOVOR", response.toString());
                }
            }catch (IOException e) {
                //e.printStackTrace();
            }
            return null;
        }
    }

    public class KlasaUcitajRangListeIzBaze extends AsyncTask<String, Void, Void>{

        AsyncResponse ar = null;

        public KlasaUcitajRangListeIzBaze(AsyncResponse asyncResponse) {
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
                URL urlObjPitanja = new URL (urlPitanja + URLEncoder.encode(TOKEN, "UTF-8"));
                HttpURLConnection connectionPitanja = (HttpURLConnection) urlObjPitanja.openConnection();
                connectionPitanja.connect();
                InputStream odgovorPitanja = connectionPitanja.getInputStream();
                StringBuilder responsePitanja = null;
                try(BufferedReader br = new BufferedReader (
                        new InputStreamReader(odgovorPitanja, "utf-8"))
                ){
                    responsePitanja = new StringBuilder();
                    String rensponseLine = null;
                    while((rensponseLine = br.readLine()) != null){
                        responsePitanja.append(rensponseLine.trim());
                    }
                    Log.d("ODGOVOR RANG LISTE: ", responsePitanja.toString());
                }
                String rezultat = responsePitanja.toString();
                if(!rezultat.equals("{}")) {
                    JSONObject jo = new JSONObject(rezultat);
                    JSONArray documents = jo.getJSONArray("documents");
                    for (int i = 0; i < documents.length(); i++) {
                        JSONObject doc = documents.getJSONObject(i);
                        String name = doc.getString("name");
                        JSONObject fields = doc.getJSONObject("fields");
                        JSONObject stringValue = fields.getJSONObject("name");
                        String nazivKvizaIzRanga = stringValue.getString("stringValue");
                        JSONObject mapValue = fields.getJSONObject("list");
                        JSONObject mapValue2 = mapValue.getJSONObject("mapValue");
                        JSONObject fieldsMape = mapValue2.getJSONObject("fields");
                        JSONObject integerValue = fieldsMape.getJSONObject("position");
                        int trenutnaPozicijaIgraca = integerValue.getInt("integerValue");
                        JSONObject mapaRang = fieldsMape.getJSONObject("map");
                        JSONObject mapaRang2 = mapaRang.getJSONObject("mapValue");
                        JSONObject fieldsDrugeMape = mapaRang2.getJSONObject("fields");
                        JSONObject integerValueProcenat = fieldsDrugeMape.getJSONObject("percentage");
                        String procenatIzBaze = integerValueProcenat.getString("stringValue");
                        JSONObject stringValueImeIgraca = fieldsDrugeMape.getJSONObject("playerName");
                        String imeIgracaIzBaze = stringValueImeIgraca.getString("stringValue");
                        Map<String, String> trenutnaDrugaMapa = new HashMap<>();
                        trenutnaDrugaMapa.put(imeIgracaIzBaze, procenatIzBaze);
                        Map<Integer, Map<String, String>> trenutnaMapa = new HashMap<>();
                        trenutnaMapa.put(trenutnaPozicijaIgraca, trenutnaDrugaMapa);
                        Pair par = new Pair(nazivKvizaIzRanga, trenutnaMapa);
                        rangListaIzBaze.add(par);
                    }
                }
            } catch (IOException e) {
                rangListaIzBaze.addAll(QuizzesActivity.sqlHelper.pokupiSveRangListe());
                nestaloNeta = true;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.rank_list_fragment,container,false);
        new KlasaUcitajRangListeIzBaze(new AsyncResponse() {
            @Override
            public void processFinish() {
                //adapter.notifyDataSetChanged();
                sviPodaciLista.clear();
                ukupnaRangLista.clear();
                for(int i = 0; i < rangListaIzBaze.size(); i++){
                    if(rangListaIzBaze.get(i).first.equals(nazivKviza)) sviPodaciLista.add(rangListaIzBaze.get(i).second);
                }
                if(!nestaloNeta) {
                    Map<String, String> novo = new HashMap<>();
                    novo.put(imeIgraca, procenat);
                    Map<Integer, Map<String, String>> novo2 = new HashMap<>();
                    novo2.put(pozicija, novo);
                    sviPodaciLista.add(novo2);
                    nestaloNeta = false;
                }
                else {
                    //Toast.makeText(getContext(), "Hvala na cekanju, internet ne radi", Toast.LENGTH_LONG).show();
                }
                new KlasaDodajURangListuBaza().execute();
                Collections.sort(sviPodaciLista, new Comparator<Map<Integer, Map<String, String>>>() {
                    @Override
                    public int compare(Map<Integer, Map<String, String>> o1, Map<Integer, Map<String, String>> o2) {
                        double procenat1 = 0;
                        double procenat2 = 0;
                        Iterator it1 = o1.entrySet().iterator();
                        while (it1.hasNext()) {
                            Map.Entry m1 = (Map.Entry) it1.next();
                            Map<String, String> map1 = (Map<String, String>) m1.getValue();
                            Iterator it2 = map1.entrySet().iterator();
                            while (it2.hasNext()) {
                                Map.Entry m2 = (Map.Entry) it2.next();
                                String procenatString = (String) m2.getValue();
                                procenat1 = Double.parseDouble(procenatString);
                            }
                        }
                        Iterator it3 = o2.entrySet().iterator();
                        while (it3.hasNext()) {
                            Map.Entry m3 = (Map.Entry) it3.next();
                            Map<String, String> map2 = (Map<String, String>) m3.getValue();
                            Iterator it4 = map2.entrySet().iterator();
                            while (it4.hasNext()) {
                                Map.Entry m4 = (Map.Entry) it4.next();
                                String procenatString = (String) m4.getValue();
                                procenat2 = Double.parseDouble(procenatString);
                            }
                        }
                        if (procenat1 < procenat2) return 1;
                        if (procenat1 > procenat2) return -1;
                        return 0;
                    }
                });
                for(int i = 0 ; i < sviPodaciLista.size(); i ++){
                    Iterator it = sviPodaciLista.get(i).entrySet().iterator();
                    while(it.hasNext()){
                        Map.Entry m = (Map.Entry) it.next();
                        int poz = i + 1;
                        Map<String, String> druga = (Map<String, String>) m.getValue();
                        Iterator it2 = druga.entrySet().iterator();
                        while(it2.hasNext()){
                            Map.Entry m2 = (Map.Entry) it2.next();
                            String ime = (String) m2.getKey();
                            String procenatt = (String) m2.getValue();
                            if(ime.equals(imeIgraca)){
                                pozicija = poz;
                            }
                            //KvizoviAkt.sqlHelper.dodajRangListuUSQL(nazivKviza, String.valueOf(poz), ime, procenatt);
                            String ispis = String.valueOf(poz) + ". " + ime + " " + procenatt + "%";
                            ukupnaRangLista.add(ispis);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }
        }).execute();
        lvRangLista = (ListView) view.findViewById(R.id.rangLista);
        nazivKviza = getArguments().getString("nazivKviza");
        imeIgraca = getArguments().getString("imeIgraca");
        procenat = getArguments().getString("procenat");
        QuizzesActivity.sqlHelper.dodajRangListuUSQL(nazivKviza, "0", imeIgraca, procenat);
        adapter = new ArrayAdapter<String>(getActivity(), R.layout.element_ranklists, R.id.ispis, ukupnaRangLista) {
            @Override
            public View getView(int position, @Nullable View convertView, @Nullable ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView ispis = (TextView) view.findViewById(R.id.ispis);
                ispis.setText(ukupnaRangLista.get(position));
                return view;
            }
        };
        lvRangLista.setAdapter(adapter);
        return view;
    }
}
