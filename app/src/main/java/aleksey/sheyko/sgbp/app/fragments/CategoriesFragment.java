package aleksey.sheyko.sgbp.app.fragments;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import aleksey.sheyko.sgbp.R;
import aleksey.sheyko.sgbp.app.helpers.Constants;

public class CategoriesFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_categories, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.categories_fragment, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getActivity().getComponentName()));

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_coupons) {
            Fragment fragment = new StoreListFragment();
            PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
                    .putInt("view_mode", Constants.VIEW_COUPONS).apply();
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
            ActionBar actionBar = getActivity().getActionBar();
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(getResources()
                    .getString(R.string.action_coupons));
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        }
        return super.onOptionsItemSelected(item);
    }
}
