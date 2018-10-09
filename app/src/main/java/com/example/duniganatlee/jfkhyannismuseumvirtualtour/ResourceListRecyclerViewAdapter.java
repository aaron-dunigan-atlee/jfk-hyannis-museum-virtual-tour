package com.example.duniganatlee.jfkhyannismuseumvirtualtour;

import android.arch.lifecycle.ViewModelProviders;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.duniganatlee.jfkhyannismuseumvirtualtour.model.ExhibitPiece;
import com.example.duniganatlee.jfkhyannismuseumvirtualtour.model.ExhibitResource;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link ExhibitResource} title and uses a ViewModel
 * to set the appropriate ExhibitResource in the MediaPlayerFragment.
 */
public class ResourceListRecyclerViewAdapter extends RecyclerView.Adapter<ResourceListRecyclerViewAdapter.ViewHolder> {

    private final ExhibitPiece mExhibitPiece;
    private final FragmentActivity mActivity;

    public ResourceListRecyclerViewAdapter(ExhibitPiece piece, FragmentActivity activity) {
        mExhibitPiece = piece;
        mActivity = activity;
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_resource_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        ExhibitResource resource = mExhibitPiece.getResources().get(position);
        holder.mResource = resource;
        // holder.mIconView.setDrawable...;
        holder.mContentView.setText(resource.getTitle());
        final FragmentSharedViewModel model = ViewModelProviders.of(mActivity).get(FragmentSharedViewModel.class);
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                model.setResource(holder.mResource);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mExhibitPiece.getResources().size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView mIconView;
        public final TextView mContentView;
        public ExhibitResource mResource;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIconView = (ImageView) view.findViewById(R.id.resource_type_icon);
            mContentView = (TextView) view.findViewById(R.id.resource_title);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
