package ba.edu.ibu.stu.classes;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import ba.edu.ibu.stu.R;

public class CategoriesAdapter extends BaseAdapter implements View.OnClickListener {

    private Activity activity;
    private ArrayList kategorije;
    private static LayoutInflater inflater = null;
    public Resources res;
    Category category = null;
    int i = 0;

    @Override
    public void onClick(View v) {
    }

    public static class ViewHolder{
        public TextView naziv;
        public ImageView ikona;
    }

    public CategoriesAdapter(Activity activity, ArrayList data, Resources res) {
        this.activity = activity;
        this.kategorije = data;
        this.res = res;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        if (kategorije.size() <= 0)
            return 1;
        return kategorije.size();
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
        CategoriesAdapter.ViewHolder holder;
        if (convertView == null) {
            v = inflater.inflate(R.layout.element_lists, null);
            holder = new CategoriesAdapter.ViewHolder();
            holder.naziv = (TextView) v.findViewById(R.id.Itemname);
            holder.ikona = (ImageView) v.findViewById(R.id.icon);
            v.setTag(holder);
        }
        else {
            holder = (CategoriesAdapter.ViewHolder) v.getTag();
        }
        if (kategorije.size() <= 0) {
            holder.naziv.setText(R.string.nema_info);
            holder.ikona.setImageResource(0);
        }
        else {
            category = (Category) kategorije.get(position);
            holder.naziv.setText(category.getName());
            //holder.ikona.setImageResource(res.getIdentifier("ba.unsa.etf.rma:drawable/" + kategorija.getId(),null, null));
            //holder.ikona.setImageResource(Integer.parseInt(kategorija.getId()));
            //v.setOnClickListener(new AdapterView.OnItemClickListener(position));
        }
        return v;
    }
}
