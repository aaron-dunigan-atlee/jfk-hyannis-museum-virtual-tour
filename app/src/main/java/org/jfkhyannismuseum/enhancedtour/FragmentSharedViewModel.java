package org.jfkhyannismuseum.enhancedtour;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import org.jfkhyannismuseum.enhancedtour.model.ExhibitResource;

import java.util.Hashtable;

/*
A ViewModel to faciliate communication between parent (ViewPagerFragment)
and child fragments (MediaPlayerFragment and ResourceListFragment).
the problem is that to use an OnListFragmentInteractionListener , this interface of the child fragment
must be implemented by the Activity, not by the parent fragment.  Therefore to communicate up
from child to parent fragment, you have to go child -> activity -> parent.
To communicate between fragments without involving the Activity, we use a ViewModel,
as explained here:
https://developer.android.com/training/basics/fragments/communicating
https://developer.android.com/topic/libraries/architecture/viewmodel#sharing
 */
class FragmentSharedViewModel extends ViewModel {
    private final static String LOG_TAG = "FragmentSharedViewModel";
    /* This ViewModel will hold a LiveData that contains a Hashtable of
     * <pieceId, resource> to indicate what ExhibitResource has been selected for each pieceId
     * currently in the history.
     */
    private final MutableLiveData<Hashtable<Integer,ExhibitResource>> resourceHolder = new MutableLiveData<Hashtable<Integer,ExhibitResource>>();
    public void setResource(int pieceId, ExhibitResource resource) {
        Log.d(LOG_TAG, "Setting resource.");
        Hashtable<Integer, ExhibitResource> exhibitResourceHashtable = resourceHolder.getValue();
        if (exhibitResourceHashtable == null) { exhibitResourceHashtable = new Hashtable<Integer, ExhibitResource>(); }
        if (exhibitResourceHashtable.containsKey(pieceId)) {
            exhibitResourceHashtable.remove(pieceId);
        }
        exhibitResourceHashtable.put(pieceId, resource);
        resourceHolder.setValue(exhibitResourceHashtable);
    }

    public LiveData<Hashtable<Integer,ExhibitResource>> getResources() {
        Log.d(LOG_TAG, "Getting resource.");
        return resourceHolder;
    }
}
