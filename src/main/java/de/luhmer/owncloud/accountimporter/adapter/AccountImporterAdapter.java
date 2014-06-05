package de.luhmer.owncloud.accountimporter.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import de.luhmer.owncloud.accountimporter.R;
import de.luhmer.owncloud.accountimporter.helper.CheckableLinearLayout;

/**
 * Created by David on 29.05.2014.
 */
public class AccountImporterAdapter extends ArrayAdapter<AccountImporterAdapter.SingleAccount> implements AdapterView.OnItemClickListener {

    Context context;
    LayoutInflater inflater;

    public AccountImporterAdapter(Activity context, SingleAccount[] accounts, ListView listView) {
        super(context, R.layout.simple_list_item_single_choice, accounts);
        this.context = context;

        listView.setOnItemClickListener(this);

        inflater = context.getLayoutInflater();
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        ViewHolder holder;

        if (view != null) {
            holder = (ViewHolder) view.getTag();
        } else {
            view = inflater.inflate(R.layout.simple_list_item_single_choice, parent, false);
            TextView text1 = (TextView) view.findViewById(R.id.text1);
            TextView text2 = (TextView) view.findViewById(R.id.text2);
            CheckBox cbChecked = (CheckBox) view.findViewById(R.id.checkbox);
            holder = new ViewHolder(text1, text2, cbChecked);

            view.setTag(holder);
        }

        holder.text1.setText(getItem(position).type);
        holder.text2.setText(getItem(position).url);
        holder.cbChecked.setChecked(getItem(position).checked);


        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int id, long l) {
        for (int i = 0; i < getCount(); i++) {
            getItem(i).checked = false;
        }
        ((CheckableLinearLayout)view).toggle();


        view.findViewById(R.id.text1);

        notifyDataSetChanged();
    }



    static class ViewHolder {
        TextView text1;
        TextView text2;
        CheckBox cbChecked;

        public ViewHolder(TextView text1, TextView text2,CheckBox cbChecked) {
            this.text1 = text1;
            this.text2 = text2;
            this.cbChecked = cbChecked;
        }
    }





    public static class SingleAccount {

        public SingleAccount(String type, String url, Boolean checked) {
            this.type = type;
            this.url = url;
            this.checked = checked;
        }

        public String type;
        public String url;

        public boolean checked;
    }

}
