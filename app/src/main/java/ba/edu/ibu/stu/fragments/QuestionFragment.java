package ba.edu.ibu.stu.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import ba.edu.ibu.stu.R;

public class QuestionFragment extends Fragment {
    private TextView tekstPitanja;
    private String pitanje;
    private ListView odgovoriPitanja;
    private ArrayList<String> odgovori = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private String tacan;
    private int pozicija = -2;
    private int pozicijaNetacnog = -1;
    private PromjenaVrijednosti promjena;
    private boolean jesteTacan = false;

    public interface PromjenaVrijednosti{
        void promijeniVrijednostiTacan();
        void promijeniVrijednostiOstali();
        void funkcija();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_question,container,false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(getArguments().containsKey("tekstPitanja")){
            pitanje = getArguments().getString("tekstPitanja");
            tacan = getArguments().getString("jesteTacan");
            odgovori = getArguments().getStringArrayList("odgovoriPitanja");
            tekstPitanja = (TextView) getView().findViewById(R.id.tekstPitanja);
            odgovoriPitanja = (ListView) getView().findViewById(R.id.odgovoriPitanja);
            tekstPitanja.setText(pitanje);
            adapter = new ArrayAdapter<String>(getActivity(), R.layout.element_lists, R.id.Itemname, odgovori) {
                @Override
                public View getView(int position, @Nullable View convertView, @Nullable ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    String s = odgovori.get(position);
                    TextView naziv = (TextView) view.findViewById(R.id.Itemname);
                    naziv.setText(s);
                    naziv.setBackgroundColor(Color.WHITE);
                    if(pozicija >= 0) {
                        if (position == pozicija){
                            naziv.setBackgroundColor(getResources().getColor(R.color.green));
                        }
                    }
                    else if(pozicija == -1){
                        if(pozicijaNetacnog == position) {
                            naziv.setBackgroundColor(getResources().getColor(R.color.red));
                        }
                        else if(odgovori.get(position).equals(tacan)){
                            naziv.setBackgroundColor(getResources().getColor(R.color.green));
                        }
                    }
                    return view;
                }
            };
            odgovoriPitanja.setAdapter(adapter);
        }
        try {
            promjena = (PromjenaVrijednosti) getActivity();
        }catch(ClassCastException e) {
            throw new ClassCastException(getActivity().toString());
        }
        odgovoriPitanja.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(tacan.equals(odgovori.get(position))){
                    pozicija = position;
                    promjena.promijeniVrijednostiTacan();
                    adapter.notifyDataSetChanged();
                }
                else {
                    pozicija = -1;
                    pozicijaNetacnog = position;
                    promjena.promijeniVrijednostiOstali();
                    adapter.notifyDataSetChanged();
                }
                odgovoriPitanja.setEnabled(false);
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        promjena.funkcija();
                    }
                }, 2000);
            }
        });
    }

}
