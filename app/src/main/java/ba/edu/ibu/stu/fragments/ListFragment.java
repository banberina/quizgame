package ba.edu.ibu.stu.fragments;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import ba.edu.ibu.stu.R;
import ba.edu.ibu.stu.activities.QuizzesActivity;
import ba.edu.ibu.stu.classes.Category;
import ba.edu.ibu.stu.classes.CategoriesAdapter;
import ba.edu.ibu.stu.classes.Quiz;

public class ListFragment extends Fragment {
    private ListView listCategory;
    private ArrayList<Category> sveKategorije = new ArrayList<>();
    private CategoriesAdapter adapterKategorije;
    private PressListView press;

    public interface PressListView {
        public void pressLV(ArrayList<Quiz> k);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);
        listCategory = (ListView) view.findViewById(R.id.listCategory);
        sveKategorije = (ArrayList<Category>) getArguments().getSerializable("sveKategorije");
        Resources res = getResources();
        adapterKategorije = new CategoriesAdapter(getActivity(), sveKategorije, res);
        listCategory.setAdapter(adapterKategorije);
        try {
            press = (PressListView) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString());
        }
        listCategory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (QuizzesActivity.kategorije.get(position).getName().equals("Svi")) {
                    QuizzesActivity.filtrirano2 = false;
                    press.pressLV(QuizzesActivity.kvizovi);
                } else {
                    QuizzesActivity.filtrirano2 = true;
                    QuizzesActivity.filterKvizovi2 = new ArrayList<Quiz>();
                    for (int i = 0; i < QuizzesActivity.kvizovi.size() - 1; i++) {
                        if (QuizzesActivity.kvizovi.get(i).getCategory().getName().equals(QuizzesActivity.kategorije.get(position).getName())) {
                            QuizzesActivity.filterKvizovi2.add(QuizzesActivity.kvizovi.get(i));
                        }
                    }
                    QuizzesActivity.filterKvizovi2.add(new Quiz("Dodaj kviz"));
                    press.pressLV(QuizzesActivity.filterKvizovi2);
                }
            }
        });
        return view;
    }
}
