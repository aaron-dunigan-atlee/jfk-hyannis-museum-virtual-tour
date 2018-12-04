package org.jfkhyannismuseum.enhancedtour;

import android.arch.lifecycle.ViewModelProviders;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.jfkhyannismuseum.enhancedtour.model.ExhibitPiece;
import org.jfkhyannismuseum.enhancedtour.model.ExhibitResource;

/**
 * {@link RecyclerView.Adapter} that can display a {@link ExhibitResource} title and uses a ViewModel
 * to set the appropriate ExhibitResource in the MediaPlayerFragment.
 */
public class ResourceListRecyclerViewAdapter extends RecyclerView.Adapter<ResourceListRecyclerViewAdapter.ViewHolder> {
    // By default, load the resource at this position.
    private static final int DEFAULT_RESOURCE = 0;
    private final ExhibitPiece mExhibitPiece;
    private final FragmentActivity mActivity;
    // Keep track of the current selected resource so we don't reload on click.
    // Assume we start with resource 0 (the exhibit piece's description).
    // As described at https://stackoverflow.com/a/39139163/
    private int mSelectedPosition = DEFAULT_RESOURCE;

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
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        ExhibitResource resource = mExhibitPiece.getResources().get(position);
        holder.mResource = resource;
        String resourceType = resource.getType();
        int iconId;
        switch (resourceType) {
            case ExhibitResource.VIDEO: iconId = R.drawable.ic_videocam_black_24dp; break;
            case ExhibitResource.AUDIO: iconId = R.drawable.ic_volume_up_black_24dp; break;
            case ExhibitResource.IMAGE: iconId = R.drawable.ic_image_black_24dp; break;
            default: iconId = R.drawable.ic_play_circle_filled_black_24dp;
        }
        holder.mIconView.setImageResource(iconId);
        holder.mIconView.setContentDescription(resourceType);
        holder.mContentView.setText(resource.getTitle());
        final FragmentSharedViewModel model = ViewModelProviders.of(mActivity).get(FragmentSharedViewModel.class);
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int adapterPosition = holder.getAdapterPosition();
                // If user re-clicks the currently viewed resource, don't reload.
                if (adapterPosition != mSelectedPosition) {
                    mSelectedPosition = adapterPosition;
                    model.setResource(mExhibitPiece.getId(), holder.mResource);
                }
            }
        });
       if (holder.getAdapterPosition() == DEFAULT_RESOURCE) {
           holder.mView.requestFocus();
       }
    }

    @Override
    public int getItemCount() {
        return mExhibitPiece.getResources().size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final ImageView mIconView;
        final TextView mContentView;
        ExhibitResource mResource;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mIconView = view.findViewById(R.id.resource_type_icon);
            mContentView = view.findViewById(R.id.resource_title);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
