package de.luhmer.owncloud.accountimporter.adapter;

import android.accounts.Account;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.luhmer.owncloud.accountimporter.R;
import de.luhmer.owncloud.accountimporter.helper.CheckableLinearLayout;

/**
 * Created by David on 29.05.2014.
 */
public class AccountImporterAdapter extends ArrayAdapter<Account> implements AdapterView.OnItemClickListener {

    private LayoutInflater inflater;
    private List<Integer> selectedIndexes = new ArrayList<>();

    public AccountImporterAdapter(Activity context, Account[] accounts, ListView listView) {
        super(context, R.layout.simple_list_item_single_choice, accounts);
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

        String username = getItem(position).name.split("@")[0];
        String server   = getItem(position).name.split("@")[1];

        holder.text1.setText(username);
        holder.text2.setText(server);
        holder.cbChecked.setChecked(selectedIndexes.contains(position));


        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        selectedIndexes.clear();
        selectedIndexes.add(position);
        ((CheckableLinearLayout)view).toggle();
        view.findViewById(R.id.text1);
        notifyDataSetChanged();
    }



    static class ViewHolder {
        TextView text1;
        TextView text2;
        CheckBox cbChecked;

        ViewHolder(TextView text1, TextView text2, CheckBox cbChecked) {
            this.text1 = text1;
            this.text2 = text2;
            this.cbChecked = cbChecked;
        }
    }

}
