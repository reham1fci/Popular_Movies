package com.tree.popular_movies;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.tree.popular_movies.DataBase.MoviesContract;
import com.tree.popular_movies.DataBase.movieDBHelper;

import java.util.ArrayList;
import java.util.HashMap;

import static com.tree.popular_movies.R.color.grey;

/**
 * Created by thy on 04/10/2015.
 */
public class DetailFragment extends Fragment  implements View.OnClickListener {
    ArrayAdapter<String> arrayAdapterReviews;
    ArrayAdapter<String> arrayAdapterTrailers;
    ListView LvReview;
    ListView LvTrailers;
    ProgressBar progressbar;
    String original_title;
    String vote_average;
    String release_date;
    String overview;
    String movieId;
    String poster_path;
    String full_poster_path;
    final String TAG = "#Popular Movies APP";
    private movieDBHelper OpenHelper;
    ArrayList<HashMap<String, String>> Reviews = new ArrayList<HashMap<String, String>>();
    ArrayList<String> Trailers_Key = new ArrayList<String>();
    ArrayList<HashMap<String, String>> MovieTrailers = new ArrayList<HashMap<String, String>>();


    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OpenHelper = new movieDBHelper(getActivity());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movie_details, container, false);
        TextView title = (TextView) rootView.findViewById(R.id.title);
        TextView rate = (TextView) rootView.findViewById(R.id.tvRate);
        TextView Date = (TextView) rootView.findViewById(R.id.tvDate);
        TextView Overview = (TextView) rootView.findViewById(R.id.tvOverview);
        TextView tvReviews = (TextView) rootView.findViewById(R.id.TvReviews);
        TextView tvTrailers = (TextView) rootView.findViewById(R.id.TvTrailers);
        ImageView poster = (ImageView) rootView.findViewById(R.id.poster);
        progressbar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        Button add_favorite = (Button) rootView.findViewById(R.id.btnFavorite);
        add_favorite.setOnClickListener(this);
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
       // String sort_by = prefs.getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_Default));


        Bundle arguments = getArguments();
        if (arguments == null) {
            return null;
        }
        HashMap<String, String> movieDetails = new HashMap<String, String>();
        movieDetails = (HashMap<String, String>) arguments.getSerializable(movieFragment.MOVIE_DETAILS);
        original_title = movieDetails.get("original_title");
        vote_average = movieDetails.get("vote_average");
        release_date = movieDetails.get("release_date");
        overview = movieDetails.get("overview");
        poster_path = movieDetails.get("poster_path");
        movieId = movieDetails.get(movieFragment.MOVIE_ID);
        title.setText(original_title);
        rate.setText(vote_average);
        Date.setText(release_date);
        Overview.setText(overview);
        String BASE_URL = "http://image.tmdb.org/t/p/";
        String POSTER_SIZE = getString(R.string.poster_size);
        full_poster_path = BASE_URL + POSTER_SIZE + poster_path;
        Log.d("full poster", full_poster_path);
        Picasso.with(getActivity())
                .load(full_poster_path)
                .into(poster);
        // Trailers ListView
        LvTrailers = (ListView) rootView.findViewById(R.id.LvTrailers);
        arrayAdapterTrailers = new ArrayAdapter<String>(getActivity(), R.layout.trailer_iteam, R.id.tvTrailerName);
        MovieTrailers = (ArrayList<HashMap<String, String>>) arguments.getSerializable(movieFragment.MOVIE_TRAILERS);
        if (MovieTrailers.size() <= 0) {
            tvTrailers.setVisibility(View.GONE);
        } else {
            for (int i = 0; i < MovieTrailers.size(); i++) {
                HashMap<String, String> Trailer = new HashMap<String, String>();
                Trailer = MovieTrailers.get(i);
                String TrailerKey = Trailer.get(movieFragment.MOVIE_TRAILERS_KEY);
                String TrailerName = Trailer.get(movieFragment.MOVIE_TRAILERS_NAME);
                arrayAdapterTrailers.add(TrailerName);
                Trailers_Key.add(i, TrailerKey);
                Log.d("TrailerName", TrailerName);
            }
            LvTrailers.setAdapter(arrayAdapterTrailers);
            getTotalHeightListView(LvTrailers);

        }
        LvTrailers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String KEY = Trailers_Key.get(position);
                showVideo(KEY);
            }
        });
        // Reviews ListView
        arrayAdapterReviews = new ArrayAdapter<String>(getActivity(), R.layout.review_iteam_textview, R.id.tvReview);
        LvReview = (ListView) rootView.findViewById(R.id.LvReviews);
        LvReview.setOnTouchListener(new View.OnTouchListener() {
            // Setting on Touch Listener for handling the touch inside ScrollView
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Disallow the touch request for parent scroll on touch of child view
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });
        Reviews = (ArrayList<HashMap<String, String>>) arguments.getSerializable(movieFragment.MOVIE_REVIEWS);
        if (Reviews.size() <= 0) {
            tvReviews.setVisibility(View.GONE);
        } else {
            for (int i = 0; i < Reviews.size(); i++) {
                HashMap<String, String> MovieReview = Reviews.get(i);
                String ReviewAuthor = MovieReview.get(movieFragment.MOVIE_REVIEWS_AUTHOR);
                String ReviewContent = MovieReview.get(movieFragment.MOVIE_REVIEWS_CONTENT);
                String Review = "By " + ReviewAuthor + System.getProperty("line.separator") + System.getProperty("line.separator") + ReviewContent;
                arrayAdapterReviews.add(Review);
            }
            LvReview.setAdapter(arrayAdapterReviews);
            getTotalHeightListView(LvReview);

        }
        if (isFavorite(movieId)) {
            add_favorite.setClickable(false);
            add_favorite.setBackgroundColor(grey);
            add_favorite.setText("Favorite \n Movie");

        }

        return rootView;


    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.details_fragment, menu);
        MenuItem menuItem = menu.findItem(R.id.menu_item_share);
        ShareActionProvider mShareActionProvider =
                (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        if (Trailers_Key.size() > 0) {
            mShareActionProvider.setShareIntent(createShareTrailerIntent());
        } else {
            Log.d("share", "Share Action Provider is null?");

        }
    }

    // show video in youtube and browsers
    public void showVideo(String key) {
        String parse_url = "https://www.youtube.com/watch?";
        String Video_key = "v";
        Uri uri = Uri.parse(parse_url).buildUpon().appendQueryParameter(Video_key, key).build();
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        v.setBackgroundColor(grey);
        v.setClickable(false);
        /*final SQLiteDatabase dbRead = OpenHelper.getReadableDatabase();
        String selection = MoviesContract.FavoriteMovieEntry.COLUMN_MOVIE_ID + "=?";
        Cursor cursor = dbRead.query(MoviesContract.FavoriteMovieEntry.TABLE_NAME,
                null, selection, new String[]{movieId}, null, null, null, null);
        if (!(isFavorite())) {*/
            ContentValues movieValues = new ContentValues();
            ContentValues movieReviewsValues = new ContentValues();
            ContentValues movieTrailersValues = new ContentValues();
            final SQLiteDatabase db = OpenHelper.getWritableDatabase();

            //insert favorite movie info

            movieValues.put(MoviesContract.FavoriteMovieEntry.COLUMN_MOVIE_ID, movieId);
            movieValues.put(MoviesContract.FavoriteMovieEntry.COLUMN_ORIGINAL_Title, original_title);
            movieValues.put(MoviesContract.FavoriteMovieEntry.COLUMN_OVERVIEW, overview);
            movieValues.put(MoviesContract.FavoriteMovieEntry.COLUMN_POSTER_PATH, poster_path);
            movieValues.put(MoviesContract.FavoriteMovieEntry.COLUMN_RELEASE_DATE, release_date);
            movieValues.put(MoviesContract.FavoriteMovieEntry.COLUMN_VOTE_AVERAGE, vote_average);
            long id = db.insert(MoviesContract.FavoriteMovieEntry.TABLE_NAME, null, movieValues);
            // insert reviews
            if (Reviews != null) {
                for (int i = 0; i < Reviews.size(); i++) {
                    HashMap<String, String> ReviewsHash = new HashMap<String, String>();
                    ReviewsHash = Reviews.get(i);
                    String ReviewsAuthor = ReviewsHash.get(movieFragment.MOVIE_REVIEWS_AUTHOR);
                    String ReviewsContent = ReviewsHash.get(movieFragment.MOVIE_REVIEWS_CONTENT);
                    movieReviewsValues.put(MoviesContract.ReviewsEntry.COLUMN_FAVOURITE_KEY, movieId);
                    movieReviewsValues.put(MoviesContract.ReviewsEntry.COLUMN_AUTHOR, ReviewsAuthor);
                    movieReviewsValues.put(MoviesContract.ReviewsEntry.COLUMN_CONTENT, ReviewsContent);
                    long id_reviews = db.insert(MoviesContract.ReviewsEntry.TABLE_NAME, null, movieReviewsValues);
                    Log.d("ID DATA reviews", String.valueOf(id_reviews));
                }
            }
            // insert trailers
            if (MovieTrailers != null) {

                for (int i = 0; i < MovieTrailers.size(); i++) {
                    HashMap<String, String> Trailer = new HashMap<String, String>();
                    Trailer = MovieTrailers.get(i);
                    String TrailerKey = Trailer.get(movieFragment.MOVIE_TRAILERS_KEY);
                    String TrailerName = Trailer.get(movieFragment.MOVIE_TRAILERS_NAME);
                    movieTrailersValues.put(MoviesContract.TrailersEntry.COLUMN_FAVOURITE_KEY, movieId);
                    movieTrailersValues.put(MoviesContract.TrailersEntry.COLUMN_TRAILERS_KEY, TrailerKey);
                    movieTrailersValues.put(MoviesContract.TrailersEntry.COLUMN_TRAILERS_NAME, TrailerName);
                    long id_trailers = db.insert(MoviesContract.TrailersEntry.TABLE_NAME, null, movieTrailersValues);
                    Log.d("ID DATA reviews", String.valueOf(id_trailers));
                }
            }
            db.close();
            Toast.makeText(getActivity(), "Movie added to your Favorite", Toast.LENGTH_LONG).show();

        //    Log.d("ID DATA BASE", String.valueOf(id));
       /* else {
            Toast.makeText(getActivity(), "Movie already exist in your Favorite", Toast.LENGTH_LONG).show();
        }
        dbRead.close();*/
        OpenHelper.close();
    }

    private Intent createShareTrailerIntent() {
        String parse_url = "https://www.youtube.com/watch?";
        String Video_key = "v";
        String KEY = Trailers_Key.get(0);
        Uri uri = Uri.parse(parse_url).buildUpon().appendQueryParameter(Video_key, KEY).build();
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, TAG);
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                uri.toString());
        return shareIntent;
    }

    public void getTotalHeightListView(ListView listView) {

        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = listView.getPaddingTop() + listView.getPaddingBottom();
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight
                + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);

    }

    public boolean isFavorite(String movie_id) {
        final SQLiteDatabase dbRead = OpenHelper.getReadableDatabase();
        String selection = MoviesContract.FavoriteMovieEntry.COLUMN_MOVIE_ID + "=?";
        Cursor cursor = dbRead.query(MoviesContract.FavoriteMovieEntry.TABLE_NAME,
                null, selection, new String[]{movie_id}, null, null, null, null);
        if (cursor.getCount()>0) {
           return true;
        }

         return false;


}
}
