package com.example.duniganatlee.jfkhyannismuseumvirtualtour;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.duniganatlee.jfkhyannismuseumvirtualtour.model.ExhibitPiece;
import com.example.duniganatlee.jfkhyannismuseumvirtualtour.model.ExhibitResource;

/**
 * A fragment representing a list of Items.
*/
public class ResourceListFragment extends Fragment {

    private ExhibitPiece mExhibitPiece;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ResourceListFragment() {
    }

    public void setPiece(ExhibitPiece piece) { mExhibitPiece = piece;};

    public static ResourceListFragment newInstance(ExhibitPiece piece) {
        ResourceListFragment fragment = new ResourceListFragment();
        fragment.setPiece(piece);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_resource_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            // Pass the parent activity to the adapter, to allow fragment-to-fragment communication
            // via a ViewModel.  See
            // https://developer.android.com/topic/libraries/architecture/viewmodel#sharing
            recyclerView.setAdapter(new ResourceListRecyclerViewAdapter(mExhibitPiece, getActivity()));
        }
        return view;
    }

}
