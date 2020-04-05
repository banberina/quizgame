package ba.edu.ibu.stu.classes;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class Quiz implements Parcelable {
    private String naziv = "";
    private ArrayList<Question> pitanja = new ArrayList<>();
    private Category category;

    public Quiz() {
        this.naziv = "";
        this.pitanja = new ArrayList<>();
        this.category = new Category();
    }

    public Quiz(String naziv) {
        this.naziv = naziv;
        this.category = new Category();
    }

    public Quiz(String naziv, ArrayList<Question> pitanja, Category category) {
        this.naziv = naziv;
        this.pitanja = pitanja;
        this.category = category;
    }

    protected Quiz(Parcel in) {
        naziv = in.readString();
        for(int i = 0; i < pitanja.size(); i++) pitanja.set(i, (Question) in.readSerializable());
        category = (Category) in.readSerializable();
    }

    public static final Creator<Quiz> CREATOR = new Creator<Quiz>() {
        @Override
        public Quiz createFromParcel(Parcel in) {
            return new Quiz(in);
        }

        @Override
        public Quiz[] newArray(int size) {
            return new Quiz[size];
        }
    };

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public ArrayList<Question> getQuestions() {
        return pitanja;
    }

    public void setPitanja(ArrayList<Question> pitanja) {
        this.pitanja = pitanja;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    void dodajPitanje(Question question){
        pitanja.add(question);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(naziv);
        dest.writeSerializable(category);
        dest.writeArray(new ArrayList[]{pitanja});
    }
}
