package com.bignerdranch.android.photogallery;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by james on 12/01/2016.
 */
public class PhotoGalleryFragment extends Fragment {

    private static final String TAG = "PhotoGalleryFragment";

    private RecyclerView mPhotoRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<GalleryItem> mItems = new ArrayList<>();

    private int mPreviousTotal = 0;
    private boolean mLoading = true;
    private int mVisibleThreshold = 5;
    private int mFirstVisibleItem, mVisibleItemCount, mTotalItemCount;

    public static PhotoGalleryFragment newInstance() {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        new FetchItemsTask().execute(1);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);

        mPhotoRecyclerView = (RecyclerView) v.findViewById(R.id.fragment_photo_gallery_recycler_view);
        mLayoutManager = new GridLayoutManager(getActivity(), 3);
        mPhotoRecyclerView.setLayoutManager(mLayoutManager);

        mPhotoRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                mVisibleItemCount = mPhotoRecyclerView.getChildCount();
                mTotalItemCount = mLayoutManager.getItemCount();
                mFirstVisibleItem = ((GridLayoutManager) mLayoutManager).findFirstVisibleItemPosition();

                if (mLoading) {
                    if (mTotalItemCount > mPreviousTotal) {
                        mLoading = false;
                        mPreviousTotal = mTotalItemCount;
                    }
                }
                if (!mLoading && (mTotalItemCount - mVisibleItemCount) <= (mFirstVisibleItem + mVisibleThreshold)) {
                    //End has been reached

                    Toast.makeText(getActivity(), "Loading more...", Toast.LENGTH_SHORT).show();
                    int nextPage = (int)(mTotalItemCount * 0.01) + 1;

                    new FetchItemsTask().execute(nextPage);

                    mLoading = true;
                }
            }
        });

        setupAdapter();

        return v;
    }

    private void setupAdapter() {
        if (isAdded()) {
            mPhotoRecyclerView.swapAdapter(new PhotoAdapter(mItems), false);
        }
    }

    private class PhotoHolder extends RecyclerView.ViewHolder {

        private TextView mTitleTextView;

        public PhotoHolder(View itemView) {
            super(itemView);

            mTitleTextView = (TextView) itemView;
        }

        public void bindGalleryItem(GalleryItem item) {
            mTitleTextView.setText(item.toString());
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {

        private List<GalleryItem> mGalleryItems;

        public PhotoAdapter(List<GalleryItem> galleryItems) {
            mGalleryItems = galleryItems;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            TextView textView = new TextView(getActivity());
            return new PhotoHolder(textView);
        }

        @Override
        public void onBindViewHolder(PhotoHolder holder, int position) {
            GalleryItem galleryItem = mGalleryItems.get(position);
            holder.bindGalleryItem(galleryItem);
        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }
    }

    private class FetchItemsTask extends AsyncTask<Integer, Void, List<GalleryItem>> {

        @Override
        protected List<GalleryItem> doInBackground(Integer... params) {
            return new FlickrFetchr().fetchItems(params[0]);
        }

        @Override
        protected void onPostExecute(List<GalleryItem> galleryItems) {
            mItems.addAll(galleryItems);
            setupAdapter();
        }
    }
}
