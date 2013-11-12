package net.i2p.android.router.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import net.i2p.android.router.R;
import net.i2p.android.router.activity.AddressbookSettingsActivity;
import net.i2p.android.router.activity.HelpActivity;
import net.i2p.android.router.adapter.AddressEntryAdapter;
import net.i2p.android.router.fragment.I2PFragmentBase.RouterContextProvider;
import net.i2p.android.router.loader.AddressEntry;
import net.i2p.android.router.loader.AddressEntryLoader;
import net.i2p.router.RouterContext;

public class AddressbookFragment extends ListFragment implements
        LoaderManager.LoaderCallbacks<List<AddressEntry>> {
    public static final String BOOK_NAME = "book_name";

    private static final int ROUTER_LOADER_ID = 1;
    private static final int PRIVATE_LOADER_ID = 2;

    RouterContextProvider mRouterContextProvider;
    OnAddressSelectedListener mCallback;
    private AddressEntryAdapter mAdapter;
    private String mBook;
    private String mCurFilter;

    // Container Activity must implement this interface
    public interface OnAddressSelectedListener {
        public void onAddressSelected(CharSequence host);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mRouterContextProvider = (RouterContextProvider) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement RouterContextProvider");
        }

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnAddressSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnAddressSelectedListener");
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAdapter = new AddressEntryAdapter(getActivity());
        mBook = getArguments().getString(BOOK_NAME);

        setListAdapter(mAdapter);

        LoaderManager lm = getLoaderManager();
        // If the Router is running, or there is an existing Loader
        if (getRouterContext() != null || lm.getLoader("private".equals(mBook) ?
                PRIVATE_LOADER_ID : ROUTER_LOADER_ID) != null) {
            setEmptyText("No hosts in address book " + mBook);

            setListShown(false);
            lm.initLoader("private".equals(mBook) ?
                    PRIVATE_LOADER_ID : ROUTER_LOADER_ID, null, this);
        } else {
            setEmptyText(getResources().getString(
                    R.string.router_not_running));
        }
    }

    @Override
    public void onListItemClick(ListView parent, View view, int pos, long id) {
        CharSequence host = ((TextView) view).getText();
        mCallback.onAddressSelected(host);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    	inflater.inflate(R.menu.fragment_addressbook_actions, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        
        switch (item.getItemId()) {
            //case R.id.action_add_to_addressbook:
            //    return true;
        case R.id.action_addressbook_settings:
            Intent si = new Intent(getActivity(), AddressbookSettingsActivity.class);
            startActivity(si);
            return true;
        case R.id.action_addressbook_help:
            Intent hi = new Intent(getActivity(), HelpActivity.class);
            hi.putExtra(HelpActivity.REFERRER, "addressbook");
            startActivity(hi);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    public void filterAddresses(String query) {
        mCurFilter = !TextUtils.isEmpty(query) ? query : null;
        getLoaderManager().restartLoader("private".equals(mBook) ?
                PRIVATE_LOADER_ID : ROUTER_LOADER_ID, null, this);
    }

    // Duplicated from I2PFragmentBase because this extends ListFragment
    private RouterContext getRouterContext() {
        return mRouterContextProvider.getRouterContext();
    }

    // LoaderManager.LoaderCallbacks<List<AddressEntry>>

    public Loader<List<AddressEntry>> onCreateLoader(int id, Bundle args) {
        return new AddressEntryLoader(getActivity(),
                getRouterContext(), mBook, mCurFilter);
    }

    public void onLoadFinished(Loader<List<AddressEntry>> loader,
            List<AddressEntry> data) {
        if (loader.getId() == ("private".equals(mBook) ?
                PRIVATE_LOADER_ID : ROUTER_LOADER_ID)) {
            mAdapter.setData(data);

            if (isResumed()) {
                setListShown(true);
            } else {
                setListShownNoAnimation(true);
            }

            // Show Toast with addressbook size
            int sz = data.size();
            Context context = getActivity().getApplicationContext();
            CharSequence text = sz + " hosts in address book.";
            if (sz == 1)
                text = "1 host in address book.";
            Toast.makeText(context, text, Toast.LENGTH_LONG).show();
        }
    }

    public void onLoaderReset(Loader<List<AddressEntry>> loader) {
        if (loader.getId() == ("private".equals(mBook) ?
                PRIVATE_LOADER_ID : ROUTER_LOADER_ID)) {
            mAdapter.setData(null);
        }
    }
}
