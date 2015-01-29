package aleksey.sheyko.sgbp.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import aleksey.sheyko.sgbp.R;
import aleksey.sheyko.sgbp.utils.helpers.Constants;

public class CategoriesAdapter extends BaseAdapter {
    private List<Item> items = new ArrayList<Item>();
    private LayoutInflater inflater;

    public CategoriesAdapter(Context context) {
        inflater = LayoutInflater.from(context);

                items.add(new Item(Constants.CATEGORY_FOOD, R.drawable.running));
                items.add(new Item(Constants.CATEGORY_AUTO, R.drawable.running));
                items.add(new Item(Constants.CATEGORY_SOUND, R.drawable.running));
                items.add(new Item(Constants.CATEGORY_BODY_CARE, R.drawable.running));
                items.add(new Item(Constants.CATEGORY_HOTELS, R.drawable.running));
                items.add(new Item("Masquerade", R.drawable.running));
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int i) {
        return items.get(i);
    }

    @Override
    public long getItemId(int i) {
        return items.get(i).drawableId;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View v = view;
        ImageView picture;
        TextView name;

        if (v == null) {
            v = inflater.inflate(R.layout.category_grid_item, viewGroup, false);
            v.setTag(R.id.picture, v.findViewById(R.id.picture));
            v.setTag(R.id.text, v.findViewById(R.id.text));
        }

        picture = (ImageView) v.getTag(R.id.picture);
        name = (TextView) v.getTag(R.id.text);

        Item item = (Item) getItem(i);

        picture.setImageResource(item.drawableId);
        name.setText(item.name);

        return v;
    }

    private class Item {
        final String name;
        final int drawableId;

        Item(String name, int drawableId) {
            this.name = name;
            this.drawableId = drawableId;
        }
    }
}
