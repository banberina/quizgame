package ba.edu.ibu.stu.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import ba.edu.ibu.stu.R;

public class InfoFragment extends Fragment {
    private Button btnKraj;
    private TextView infNazivKviza;
    private String nazivKviza;
    private TextView infBrojTacnihPitanja;
    private TextView infBrojPreostalihPitanja;
    private TextView infProcenatTacni;
    private String tacni;
    private String ostali;
    private String procenat;
    private Nazad back;

    public interface Nazad{
        public void nazad();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info,container,false);
        if(getArguments().containsKey("naziv")){
            nazivKviza = getArguments().getString("naziv");
            infNazivKviza = (TextView) view.findViewById(R.id.infNazivKviza);
            infBrojTacnihPitanja = (TextView) view.findViewById(R.id.infBrojTacnihPitanja);
            infBrojPreostalihPitanja = (TextView) view.findViewById(R.id.infBrojPreostalihPitanja);
            btnKraj = (Button) view.findViewById(R.id.btnKraj);
            infProcenatTacni = (TextView) view.findViewById(R.id.infProcenatTacni);
            infNazivKviza.setText(nazivKviza);
            tacni = getArguments().getString("brojTacnihPitanja");
            ostali = getArguments().getString("brojOstalihPitanja");
            procenat = getArguments().getString("procenat");
            procenat += "%";
            infBrojTacnihPitanja.setText(tacni);
            infBrojPreostalihPitanja.setText(ostali);
            infProcenatTacni.setText(procenat);
        }
        try {
            back = (Nazad) getActivity();
        }catch(ClassCastException e) {
            throw new ClassCastException(getActivity().toString());
        }
        btnKraj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back.nazad();
            }
        });
        return view;
    }
}
