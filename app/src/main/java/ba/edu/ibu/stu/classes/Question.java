package ba.edu.ibu.stu.classes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

public class Question implements Serializable {
    private String naziv = "";
    private String tekstPitanja = "";
    private ArrayList<String> odgovori = new ArrayList<>();
    private String tacan = "";

    public Question() {
    }

    public Question(String naziv) {
        this.naziv = naziv;
    }

    public Question(String naziv, String tekstPitanja, ArrayList<String> odgovori, String tacan) {
        this.naziv = naziv;
        this.tekstPitanja = tekstPitanja;
        this.odgovori = odgovori;
        this.tacan = tacan;
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public String getTekstPitanja() {
        return tekstPitanja;
    }

    public void setTekstPitanja(String tekstPitanja) {
        this.tekstPitanja = tekstPitanja;
    }

    public ArrayList<String> getOdgovori() {
        return odgovori;
    }

    public void setOdgovori(ArrayList<String> odgovori) {
        this.odgovori = odgovori;
    }

    public String getTacan() {
        return tacan;
    }

    public void setTacan(String tacan) {
        this.tacan = tacan;
    }

    ArrayList<String> dajRandomOdgovore(){
        Collections.shuffle(odgovori);
        return odgovori;
    }
}
