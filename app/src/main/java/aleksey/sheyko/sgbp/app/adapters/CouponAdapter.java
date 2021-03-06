package aleksey.sheyko.sgbp.app.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import aleksey.sheyko.sgbp.R;
import aleksey.sheyko.sgbp.model.Coupon;

public class CouponAdapter extends ArrayAdapter<Coupon> {

    // declaring our ArrayList of items
    private ArrayList<Coupon> mCoupons;

    private SharedPreferences mSharedPrefs;

    /* here we must override the constructor for ArrayAdapter
    * the only variable we care about now is ArrayList<Item> objects,
    * because it is the list of objects we want to display.
    */
    public CouponAdapter(Context context, int textViewResourceId, ArrayList<Coupon> coupons) {
        super(context, textViewResourceId, coupons);
        mCoupons = coupons;
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /*
         * we are overriding the getView method here - this is what defines how each
         * list item will look.
         */
    public View getView(int position, View convertView, ViewGroup parent) {

        // assign the view we are converting to a local variable
        View view = convertView;

        // first check to see if the view is null. if so, we have to inflate it.
        // to inflate it basically means to render, or show, the view.
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.coupon_list_item, parent, false);
        }

		/*
         * Recall that the variable position is sent in as an argument to this method.
		 * The variable simply refers to the position of the current object in the list. (The ArrayAdapter
		 * iterates through the list we sent it)
		 *
		 * Therefore, store refers to the current Store object.
		 */
        Coupon coupon = mCoupons.get(position);
        if (coupon != null) {

            TextView nameTextView = (TextView) view.findViewById(R.id.name);
            TextView codeTextView = (TextView) view.findViewById(R.id.code);
            TextView dateTextView = (TextView) view.findViewById(R.id.date);
            TextView descTextView = (TextView) view.findViewById(R.id.desc);

            nameTextView.setText(coupon.getStoreName());
            codeTextView.setText("Code: " + coupon.getCode());
            dateTextView.setText("Use By " + coupon.getExpireDate());
            descTextView.setText(coupon.getDesc());
        }
        return view;
    }
}