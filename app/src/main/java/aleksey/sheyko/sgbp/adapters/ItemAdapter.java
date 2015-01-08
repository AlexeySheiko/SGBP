package aleksey.sheyko.sgbp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import aleksey.sheyko.sgbp.R;
import aleksey.sheyko.sgbp.models.Store;

public class ItemAdapter extends ArrayAdapter<Store> {

    // declaring our ArrayList of items
    private ArrayList<Store> objects;

    /* here we must override the constructor for ArrayAdapter
    * the only variable we care about now is ArrayList<Item> objects,
    * because it is the list of objects we want to display.
    */
    public ItemAdapter(Context context, int textViewResourceId, ArrayList<Store> objects) {
        super(context, textViewResourceId, objects);
        this.objects = objects;
    }

    /*
     * we are overriding the getView method here - this is what defines how each
     * list item will look.
     */
    public View getView(int position, View convertView, ViewGroup parent) {

        // assign the view we are converting to a local variable
        View v = convertView;

        // first check to see if the view is null. if so, we have to inflate it.
        // to inflate it basically means to render, or show, the view.
        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.list_item_store, null);
        }

		/*
         * Recall that the variable position is sent in as an argument to this method.
		 * The variable simply refers to the position of the current object in the list. (The ArrayAdapter
		 * iterates through the list we sent it)
		 *
		 * Therefore, i refers to the current Item object.
		 */
        Store i = objects.get(position);


        if (i != null) {

            // This is how you obtain a reference to the TextViews.
            // These TextViews are created in the XML files we defined.

            TextView ttd = (TextView) v.findViewById(R.id.nameLabel);
            TextView mtd = (TextView) v.findViewById(R.id.phoneLabel);
            //            TextView btd = (TextView) v.findViewById(R.id.desctext);

            // check to see if each individual textview is null.
            // if not, assign some text!
            if (ttd != null) {
                ttd.setText(i.getName());
            }
            if (mtd != null) {
                mtd.setText("$" + i.getAddress());

                //            if (btd != null){
                //                btd.setText(i.getDetails());
                //            }
            }
        }
        // the view must be returned to our activity
        return v;
    }
}