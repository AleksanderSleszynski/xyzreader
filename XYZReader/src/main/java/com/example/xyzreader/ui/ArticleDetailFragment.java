package com.example.xyzreader.ui;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;

import java.util.Date;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    public static final String ARG_ITEM_ID = "item_id";
    private static final String KEY_LARGE_TEXT = "large_text";
    private static final String TAG = "ArticleDetailFragment";

    ActionBar mActionBar;
    private CollapsingToolbarLayout mCollapsingToolbar;
    private Cursor mCursor;
    private ImageView mPhotoView;
    private long mItemId;
    private SharedPreferences mPrefs;
    private TextView mTextBody;
    private View mRootView;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {
    }

    public static ArticleDetailFragment newInstance(long itemId) {
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItemId = getArguments().getLong(ARG_ITEM_ID);
        }
        mPrefs = getActivity().getSharedPreferences("BACON", Context.MODE_PRIVATE);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detail, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menu_large){
            if(Build.VERSION.SDK_INT < 23){
                mTextBody.setTextAppearance(getActivity(), android.R.style.TextAppearance_Large);
            } else {
                mTextBody.setTextAppearance(android.R.style.TextAppearance_Large);
            }
            mPrefs.edit().putBoolean(KEY_LARGE_TEXT, true).apply();
        } else if(item.getItemId() == R.id.menu_normal) {
            if(Build.VERSION.SDK_INT < 23){
                mTextBody.setTextAppearance(getActivity(), android.R.style.TextAppearance_Medium);
            } else {
                mTextBody.setTextAppearance(android.R.style.TextAppearance_Medium);
            }
            mPrefs.edit().putBoolean(KEY_LARGE_TEXT, false).apply();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // In support library r8, calling initLoader for a fragment in a FragmentPagerAdapter in
        // the fragment's onCreate may cause the same LoaderManager to be dealt to multiple
        // fragments because their mIndex is -1 (haven't been added to the activity yet). Thus,
        // we do this in onActivityCreated.
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);
        mPhotoView = (ImageView) mRootView.findViewById(R.id.photo_image_view);

        mCollapsingToolbar = (CollapsingToolbarLayout) mRootView.findViewById(R.id.collapsing_toolbar);

        mRootView.findViewById(R.id.share_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setText("Some sample text")
                        .getIntent(), getString(R.string.action_share)));
            }
        });

        AppCompatActivity activity = ((AppCompatActivity) getActivity());
        activity.setSupportActionBar((Toolbar) mRootView.findViewById(R.id.toolbar));
        mActionBar = activity.getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
        }
        mTextBody = (TextView) mRootView.findViewById(R.id.body_text_view);
        if (mPrefs.getBoolean(KEY_LARGE_TEXT, false)) {
            mTextBody.setTextAppearance(getActivity(), android.R.style.TextAppearance_Large);
        }

        bindViews();
        return mRootView;
    }


    private void bindViews() {
        if (mRootView == null) {
            return;
        }

        if (mCursor != null) {
            mTextBody.setText(String.format("%s\n\n%tc by %s",
                    Html.fromHtml(mCursor.getString(ArticleLoader.Query.BODY)),
                    new Date(mCursor.getLong(ArticleLoader.Query.PUBLISHED_DATE)),
                    mCursor.getString(ArticleLoader.Query.AUTHOR)));
            mCollapsingToolbar.setTitle(mCursor.getString(ArticleLoader.Query.TITLE));
            Glide.with(getActivity())
                    .load(mCursor.getString(ArticleLoader.Query.PHOTO_URL))
                    .centerCrop()
                    .into(mPhotoView);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(getActivity(), mItemId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (!isAdded()) {
            if (cursor != null) {
                cursor.close();
            }
            return;
        }

        mCursor = cursor;
        if (mCursor != null && !mCursor.moveToFirst()) {
            Log.e(TAG, "Error reading item detail cursor");
            mCursor.close();
            mCursor = null;
        }

        bindViews();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        bindViews();
    }

}
