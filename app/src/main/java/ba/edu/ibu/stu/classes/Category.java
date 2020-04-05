package ba.edu.ibu.stu.classes;

import java.io.Serializable;

public class Category implements Serializable {
    private String naziv = "";
    private String id = "";

    public Category() {
        this.naziv = "";
        this.id = "";
    }

    public Category(String naziv) {
        this.naziv = naziv;
    }

    public Category(String naziv, String id) {
        this.naziv = naziv;
        this.id = id;
    }

    public String getName() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
