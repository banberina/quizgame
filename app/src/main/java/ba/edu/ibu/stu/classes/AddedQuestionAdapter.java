package ba.edu.ibu.stu.classes;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.maltaisn.icondialog.IconView;

import java.util.ArrayList;

import ba.edu.ibu.stu.R;

public class AddedQuestionAdapter extends BaseAdapter implements View.OnClickListener {

    private Activity activity;
    private ArrayList pitanja;
    private static LayoutInflater inflater = null;
    public Resources res;
    Question question = null;
    int i = 0;

    @Override
    public void onClick(View v) {
    }

    public static class ViewHolder{
        public TextView naziv;
        public IconView ikona;
    }

    public AddedQuestionAdapter(Activity activity, ArrayList data, Resources res) {
        this.activity = activity;
        this.pitanja = data;
        this.res = res;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        if (pitanja.size() <= 0)
            return 1;
        return pitanja.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        AddedQuestionAdapter.ViewHolder holder;
        if (convertView == null) {
            v = inflater.inflate(R.layout.element_lists, null);
            holder = new AddedQuestionAdapter.ViewHolder();
            holder.naziv = (TextView) v.findViewById(R.id.Itemname);
            holder.ikona = (IconView) v.findViewById(R.id.icon);
            v.setTag(holder);
        }
        else {
            holder = (AddedQuestionAdapter.ViewHolder) v.getTag();
        }
        if (pitanja.size() <= 0) {
            holder.naziv.setText(R.string.nema_info);
            holder.ikona.setImageResource(0);
        }
        if(pitanja.size() > 0) {
            question = (Question) pitanja.get(position);
            holder.naziv.setText(question.getNaziv());
            if(position == pitanja.size() - 1){
                holder.ikona.setImageResource(R.drawable.plus);
            }
            else {
                holder.ikona.setImageResource(0);
                holder.ikona.setImageResource(R.drawable.slika);
            }
            //v.setOnClickListener(new AdapterView.OnItemClickListener(position));
        }
        return v;
    }
}
