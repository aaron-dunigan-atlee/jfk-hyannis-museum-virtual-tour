package com.example.duniganatlee.jfkhyannismuseumvirtualtour;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

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
    private final MutableLiveData<Integer> resourcePosition = new MutableLiveData<Integer>();

    public void setResourcePosition(int position) {
        resourcePosition.setValue(position);
    }

    public LiveData<Integer> getResourcePosition() {
        return resourcePosition;
    }
}
