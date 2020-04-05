package ba.edu.ibu.stu.fragments;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import java.util.ArrayList;

import ba.edu.ibu.stu.R;
import ba.edu.ibu.stu.activities.AddQuizActivity;
import ba.edu.ibu.stu.activities.PlayQuizActivity;
import ba.edu.ibu.stu.activities.QuizzesActivity;
import ba.edu.ibu.stu.classes.Quiz;
import ba.edu.ibu.stu.classes.QuizAdapter2;

public class DetailsFragment extends Fragment {
    private GridView gridKvizovi;
    private QuizAdapter2 adapter;
    private ArrayList<Quiz> kvizovi = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail, container, false);
        gridKvizovi = (GridView) view.findViewById(R.id.gridKvizovi);
        ArrayList<Quiz> noviKvizovi = getArguments().getParcelableArrayList("kvizovi");
        kvizovi.addAll(noviKvizovi);
        Resources res = getResources();
        adapter = new QuizAdapter2(getActivity(), kvizovi, res);
        gridKvizovi.setAdapter(adapter);

        gridKvizovi.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!QuizzesActivity.filtrirano2) {
                    if (position != kvizovi.size() - 1) {
                        Intent myIntent = new Intent(getActivity(), PlayQuizActivity.class);
                        myIntent.putExtra("nazivKviza", kvizovi.get(position).getNaziv());
                        myIntent.putExtra("pitanja", kvizovi.get(position).getQuestions());
                        getActivity().startActivityForResult(myIntent, 2);
                    }
                } else {
                    if (position != QuizzesActivity.filterKvizovi2.size() - 1) {
                        Intent myIntent = new Intent(getActivity(), PlayQuizActivity.class);
                        myIntent.putExtra("nazivKviza", QuizzesActivity.filterKvizovi2.get(position).getNaziv());
                        myIntent.putExtra("pitanja", QuizzesActivity.filterKvizovi2.get(position).getQuestions());
                        getActivity().startActivityForResult(myIntent, 2);
                    }
                }
            }
        });

        gridKvizovi.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (!QuizzesActivity.filtrirano2) {
                    if (position == kvizovi.size() - 1) {
                        Intent myIntent = new Intent(getActivity(), AddQuizActivity.class);
                        getActivity().startActivityForResult(myIntent, 0);
                    } else {
                        Intent myIntent = new Intent(getActivity(), AddQuizActivity.class);
                        myIntent.putExtra("nazivKviza", kvizovi.get(position).getNaziv());
                        myIntent.putExtra("kategorija", kvizovi.get(position).getCategory());
                        myIntent.putExtra("pitanja", kvizovi.get(position).getQuestions());
                        QuizzesActivity.pozicija = position;
                        getActivity().startActivityForResult(myIntent, 1);
                    }
                } else {
                    if (position == QuizzesActivity.filterKvizovi2.size() - 1) {
                        Intent myIntent = new Intent(getActivity(), AddQuizActivity.class);
                        getActivity().startActivityForResult(myIntent, 0);
                    } else {
                        Intent myIntent = new Intent(getActivity(), AddQuizActivity.class);
                        myIntent.putExtra("nazivKviza", QuizzesActivity.filterKvizovi2.get(position).getNaziv());
                        myIntent.putExtra("kategorija", QuizzesActivity.filterKvizovi2.get(position).getCategory());
                        myIntent.putExtra("pitanja", QuizzesActivity.filterKvizovi2.get(position).getQuestions());
                        QuizzesActivity.pozicija = position;
                        getActivity().startActivityForResult(myIntent, 1);
                    }
                }
                return true;
            }
        });

        return view;
    }
}
