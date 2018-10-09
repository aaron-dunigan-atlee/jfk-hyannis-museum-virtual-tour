package com.example.duniganatlee.jfkhyannismuseumvirtualtour;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import com.example.duniganatlee.jfkhyannismuseumvirtualtour.model.Exhibit;
import com.example.duniganatlee.jfkhyannismuseumvirtualtour.model.ExhibitResource;

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
public class FragmentSharedViewModel extends ViewModel {
    private final static String LOG_TAG = "FragmentSharedViewModel";
    private final MutableLiveData<ExhibitResource> resourceHolder = new MutableLiveData<ExhibitResource>();

    public void setResource(ExhibitResource resource) {
        Log.d(LOG_TAG, "Setting resource.");
        resourceHolder.setValue(resource);
    }

    public LiveData<ExhibitResource> getResource() {
        Log.d(LOG_TAG, "Getting resource.");
        return resourceHolder;
    }
}
