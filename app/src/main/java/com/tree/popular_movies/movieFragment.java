package com.tree.popular_movies;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.tree.popular_movies.DataBase.MoviesContract.FavoriteMovieEntry;
import com.tree.popular_movies.DataBase.MoviesContract.ReviewsEntry;
import com.tree.popular_movies.DataBase.MoviesContract.TrailersEntry;
import com.tree.popular_movies.DataBase.movieDBHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;



public class movieFragment extends Fragment {
    GridView gridView;
    ArrayList<HashMap<String,String>>movieDetails=new ArrayList<HashMap<String,String>>();
    HashMap<String,String>movie_details=new HashMap<String, String>();
    ArrayList<HashMap<String,String>>movie_reviews=new ArrayList<HashMap<String,String>>();
    ArrayList<HashMap<String,String>>movie_trailers=new ArrayList<HashMap<String,String>>();

    gridviewAdaptar  adapter;
    String sort_by;
    final String PREF_POSTER_PATHS="sharedPreference_poserPaths";
    final static String ORIGINAL_TITLE="original_title";
    final static String MOVIE_POSTER="poster_path";
    final static String APLOT_SYNOPSIS="overview";
    final static String USER_RATING="vote_average";
    final static String RELEASE_DATE="release_date";
    final static String MOVIE_ID="id";
    final static String MOVIE_TRAILERS="MovieTrailers";
    final static String MOVIE_TRAILERS_KEY="TrailersKey";
    final static String MOVIE_TRAILERS_NAME="TrailersName";

    private movieDBHelper OpenHelper;
    final static String MOVIE_REVIEWS_AUTHOR="author";
    final static String MOVIE_REVIEWS_CONTENT="content";
  final static String MOVIE_REVIEWS="MovieReviews";
    final static String MOVIE_DETAILS="MovieDetail";



    ProgressBar progressbar;


    public movieFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OpenHelper = new movieDBHelper(getActivity());
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        gridView = (GridView)rootView.findViewById(R.id.gridView);
        progressbar=(ProgressBar)rootView.findViewById(R.id.progress);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sort_by = prefs.getString(getString(R.string.pref_sort_key),getString(R.string.pref_sort_Default));
        updateMovie();
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    movie_details= movieDetails.get(position);
                    String movieId = movieDetails.get(position).get(MOVIE_ID);
                if(!sort_by.equals(getString(R.string.pref_sort_by_Favorite))){
                    getReviews(movieId);

                }
                else{
                  movie_reviews=  getFavoriteMovieReviews(movieId);
                  movie_trailers=  getFavoriteMovieTrailers(movieId);
                    onItem_selected(movie_details,movie_reviews,movie_trailers);

                }


            }
        });

        return rootView;
    }
    public void getReviews( String movieId){
        FetchReviews ReviewsTask=new FetchReviews();
        ReviewsTask.execute(movieId);
        progressbar.setVisibility(View.VISIBLE);
    }
    public void updateMovie() {
        if(sort_by.equals(getString(R.string.pref_sort_by_Favorite))){
            ArrayList<String>movies_posters_path=new ArrayList<String>();
            String BASE_URL="http://image.tmdb.org/t/p/";
            String POSTER_SIZE=getString(R.string.poster_size);
            movieDetails= getFavoriteMovieDetails();
            for(int i=0;i<movieDetails.size();i++) {
                HashMap<String, String> movie_Details = movieDetails.get(i);
                String poster_path = movie_Details.get(MOVIE_POSTER);
                String full_poster_path=BASE_URL+POSTER_SIZE+poster_path;
                movies_posters_path.add(i,full_poster_path);
            }
            adapter = new gridviewAdaptar(getActivity(),movies_posters_path);
            gridView.setAdapter(adapter);
        }
        else {
            if (isConnectedToInternet()) {
                FetchMoviesTask MoviesTask = new FetchMoviesTask();
                MoviesTask.execute(sort_by);
                progressbar.setVisibility(View.VISIBLE);
            } else {
                ArrayList<String> Poster_Path_Cache = new ArrayList<String>();
                SharedPreferences pref = getActivity().getSharedPreferences(PREF_POSTER_PATHS, 0);
                if (pref.contains("posters_size")) {
                    int size = pref.getInt("posters_size", 0);
                    for (int i = 0; i < size; i++) {
                        Poster_Path_Cache.add(pref.getString("poster" + i, null));
                    }
                    adapter = new gridviewAdaptar(getActivity(), Poster_Path_Cache);
                    gridView.setAdapter(adapter);
                }
                Toast.makeText(getActivity(), "no internet connection", Toast.LENGTH_LONG).show();

            }
        }
    }
    public void onItem_selected(HashMap<String,String>movie_detail, ArrayList<HashMap<String,String>>movie_reviews,
    ArrayList<HashMap<String,String>>movie_trailers) {
        if(MainActivity.mTwoPane) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(MOVIE_DETAILS, movie_detail);
            bundle.putSerializable(MOVIE_REVIEWS, movie_reviews);
            bundle.putSerializable(MOVIE_TRAILERS, movie_trailers);
            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(bundle);
            getFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, fragment, MainActivity.DETAIL_FRAGMENT_TAG)
                    .commit();
        }
        else {
            Intent intent = new Intent(getActivity(), movieDetails.class);
            intent.putExtra(MOVIE_DETAILS, movie_detail);
            intent.putExtra(MOVIE_REVIEWS, movie_reviews);
            intent.putExtra(MOVIE_TRAILERS, movie_trailers);
            startActivity(intent);

        }
    }
    @Override
    public void onStart() {
        super.onStart();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
       String sort_change= prefs.getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_Default));
        Log.d("sort_change",sort_change);

        if(!sort_by.equals(sort_change)) {
            sort_by=sort_change;
            updateMovie();
        }
    }
     /*////////////////////////////////////////////////////////////////////////////////////////////
    // fetch movies request
    ////////////////////////////////////////////////////////////////////////////////////////////*/

public class FetchMoviesTask extends AsyncTask<String,Void,ArrayList<HashMap<String,String>>>{

        @Override
        protected ArrayList<HashMap<String,String>> doInBackground(String...params) {

           String api_key = getString(R.string.api_key);
           String responseStr=null;
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            try {

            String parse_url = " http://api.themoviedb.org/3/discover/movie?";
            String sort_by_params="sort_by";
            String api_key_params="api_key";
            Uri uri=Uri.parse(parse_url).buildUpon().appendQueryParameter(sort_by_params,params[0])
                    .appendQueryParameter(api_key_params,api_key).build();
                URL url=new URL(uri.toString());
                Log.d("url",url.toString());
                urlConnection=(HttpURLConnection)url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                InputStream inputStream=urlConnection.getInputStream();
                StringBuffer buffer=new StringBuffer();
                if(inputStream==null){
                    responseStr=null;
                }
                reader=new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }
                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    responseStr = null;
                }
                responseStr = buffer.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }
            }
            try {
                movieDetails=getMovieInfoFromJson(responseStr);
            } catch (JSONException e) {
                e.printStackTrace();
            }
     //   Log.d("RESPONSE",responseStr);
          //  Log.d("urls_posters", movieDetails.toString());

            return movieDetails;

        }

        @Override
        protected void onPostExecute(ArrayList<HashMap<String,String>>strings) {
            String BASE_URL="http://image.tmdb.org/t/p/";
            String POSTER_SIZE=getString(R.string.poster_size);
           ArrayList<String>movies_posters_path=new ArrayList<String>();
            if( strings!=null){
                for(int i=0;i<strings.size();i++){
                    HashMap<String,String> movie_Details=strings.get(i);
                    String poster_path=movie_Details.get(MOVIE_POSTER);
                    movies_posters_path.add(i,BASE_URL + POSTER_SIZE + poster_path);
                }

                // add images posters paths in shared SharedPreferences
                SharedPreferences pref = getActivity().getSharedPreferences(PREF_POSTER_PATHS, 0);
                SharedPreferences.Editor editor = pref.edit();
                editor.putInt("posters_size", movies_posters_path.size());
                for(int i=0;i<movies_posters_path.size();i++)
                {
                    editor.remove("poster" + i);
                    editor.putString("poster" + i, movies_posters_path.get(i));
                }
                editor.commit();
                // stop progressbar
                progressbar.setVisibility(View.GONE);
                // set adapter for grid view
                adapter = new gridviewAdaptar(getActivity(),movies_posters_path);
                gridView.setAdapter(adapter);
            }
        }

        public ArrayList<HashMap<String,String>> getMovieInfoFromJson(String responseStr) throws JSONException {
            JSONObject MoviesJson = new JSONObject(responseStr);
            JSONArray results=MoviesJson.getJSONArray("results");
            ArrayList<HashMap<String,String>>movie_details=new ArrayList<HashMap<String,String>>();
             for(int i=0;i<results.length();i++){
                 HashMap<String,String>movieHash=new HashMap<String,String>();
                 String original_title;
                 String movie_poster;
                 String aplot_synopsis;
                 String user_rating;
                 String release_date;
                 String movie_id;
                 JSONObject movieInfoJson=results.getJSONObject(i);
                 original_title=movieInfoJson.getString(ORIGINAL_TITLE);
                 movie_poster=  movieInfoJson.getString(MOVIE_POSTER);
                 aplot_synopsis= movieInfoJson.getString(APLOT_SYNOPSIS);
                 user_rating= movieInfoJson.getString(USER_RATING);
                 release_date= movieInfoJson.getString(RELEASE_DATE);
                 movie_id= movieInfoJson.getString(MOVIE_ID);
                 movieHash.put(ORIGINAL_TITLE,original_title);
                 movieHash.put(MOVIE_POSTER,movie_poster);
                 movieHash.put(APLOT_SYNOPSIS,aplot_synopsis);
                 movieHash.put(USER_RATING,user_rating);
                 movieHash.put(RELEASE_DATE,release_date);
                 movieHash.put(MOVIE_ID,movie_id);
                 movie_details.add(i,movieHash);
             }
            return movie_details;
        }
    }

    /*////////////////////////////////////////////////////////////////////////////////////////////
    // fetch Reviews request
    ////////////////////////////////////////////////////////////////////////////////////////////*/
public class FetchReviews extends AsyncTask<String,Void,ArrayList<HashMap<String,String>>>{
    String movie_id;
    ArrayList<HashMap<String,String>>MovieReviews=new ArrayList<HashMap<String,String>>();
    @Override
    protected ArrayList<HashMap<String,String>> doInBackground(String...params) {

        String api_key = getString(R.string.api_key);
         movie_id=params[0];
        String responseStr=null;
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        try {
            String parse_url = "https://api.themoviedb.org/3/movie/"+movie_id+"/reviews?";
            String api_key_params="api_key";
            Uri uri=Uri.parse(parse_url).buildUpon().appendQueryParameter(api_key_params, api_key).build();
           URL url = new URL(uri.toString());
            Log.d("url",url.toString());
            urlConnection= (HttpURLConnection)url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
            InputStream inputStream=urlConnection.getInputStream();
            StringBuffer buffer=new StringBuffer();
            if(inputStream==null){
                responseStr=null;
            }
            reader=new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }
            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                responseStr = null;
            }
            responseStr = buffer.toString();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally{
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e("PlaceholderFragment", "Error closing stream", e);
                }
            }
        }
        try {
    MovieReviews= getReviewsFromJson(responseStr);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("response",MovieReviews.toString());
        return MovieReviews;
    }

    @Override
    protected void onPostExecute(ArrayList<HashMap<String,String>> strings) {
        movie_reviews=strings;
        //call async task to fetch trailers
        FetchTrailersTask trailersTask  = new FetchTrailersTask();
      trailersTask.execute(movie_id);
        Log.d("movie_id",movie_id);

    }

    public ArrayList<HashMap<String,String>> getReviewsFromJson(String responseStr) throws JSONException {
        JSONObject MovieReviewsJson = new JSONObject(responseStr);
        JSONArray results=MovieReviewsJson.getJSONArray("results");
        ArrayList<HashMap<String,String>>MovieReviews=new ArrayList<HashMap<String,String>>();

        for(int i=0;i<results.length();i++){
            HashMap<String, String> reviewsHash = new HashMap<String, String>();
            JSONObject ReviewJson=results.getJSONObject(i);
            String ReviewAuthor=ReviewJson.getString("author");
            String ReviewContent =ReviewJson.getString("content");
            reviewsHash.put(MOVIE_REVIEWS_AUTHOR,ReviewAuthor);
            reviewsHash.put(MOVIE_REVIEWS_CONTENT,ReviewContent);
            MovieReviews.add(i,reviewsHash);
        }
        return MovieReviews;
    }
}

    /*////////////////////////////////////////////////////////////////////////////////////////////
    // fetch trailers request
    ////////////////////////////////////////////////////////////////////////////////////////////*/
public class FetchTrailersTask  extends AsyncTask<String,Void,ArrayList<HashMap<String,String>>> {
        ArrayList<HashMap<String,String>>MovieTrailers=new ArrayList<HashMap<String,String>>();
        @Override
        protected ArrayList<HashMap<String,String>> doInBackground(String... params) {
            String api_key = getString(R.string.api_key);
            String movie_id = params[0];
            String responseStr = null;
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            try {
                String parse_url = "https://api.themoviedb.org/3/movie/"+ movie_id +"/videos?";
                String api_key_params = "api_key";
                Uri uri = Uri.parse(parse_url).buildUpon().appendQueryParameter(api_key_params, api_key).build();
                URL url = new URL(uri.toString());
                Log.d("url", url.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    responseStr = null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }
                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    responseStr = null;
                }
                responseStr = buffer.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }
            }
            try {
                MovieTrailers = getTrailersFromJson(responseStr);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d("response", MovieTrailers.toString());
            return MovieTrailers;
        }

        @Override
        protected void onPostExecute(ArrayList<HashMap<String, String>> MovieTrailers) {
            progressbar.setVisibility(View.GONE);
            super.onPostExecute(MovieTrailers);
            movie_trailers=MovieTrailers;
            Log.d("MOVIE_TRAILERS",MovieTrailers.toString());
            onItem_selected(movie_details,movie_reviews,movie_trailers);
        }

        //parse trailers json
        public ArrayList<HashMap<String,String>> getTrailersFromJson(String responseStr) throws JSONException {
            JSONObject MovieTrailersJson = new JSONObject(responseStr);
            JSONArray results=MovieTrailersJson.getJSONArray("results");
            ArrayList<HashMap<String,String>>MovieTrailers=new ArrayList<HashMap<String,String>>();
            for(int i=0;i<results.length();i++){
                JSONObject TrailerJson=results.getJSONObject(i);
                HashMap<String,String>MovieTrailerHash=new HashMap<String,String>();
                String TrailerKey=TrailerJson.getString("key");
                String TrailerName =TrailerJson.getString("name");
                MovieTrailerHash.put(MOVIE_TRAILERS_KEY,TrailerKey);
                MovieTrailerHash.put(MOVIE_TRAILERS_NAME,TrailerName);
                MovieTrailers.add(i,MovieTrailerHash);
            }
            return MovieTrailers;
        }

    }

    /*////////////////////////////////////////////////////////////////////////////////////////////
    // function to check internet connection
    ////////////////////////////////////////////////////////////////////////////////////////////*/
public boolean isConnectedToInternet(){
        ConnectivityManager connectivity = (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null)
        {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                    {
                        return true;
                    }

        }
        return false;
    }

     /*////////////////////////////////////////////////////////////////////////////////////////////
    // get Favorite movie details from data base
    ////////////////////////////////////////////////////////////////////////////////////////////*/
public  ArrayList<HashMap<String,String>> getFavoriteMovieDetails(){
         ArrayList<HashMap<String,String>>movie_Details=new ArrayList<HashMap<String,String>>();
         Cursor cursor=OpenHelper.getReadableDatabase().query(FavoriteMovieEntry.TABLE_NAME,
                 null,null,null,null,null,null,null);
         if (cursor.moveToFirst()) {
             do {
                 HashMap<String,String>movieHash=new HashMap<String,String>();
                 String movie_poster =cursor.getString(cursor.getColumnIndex(FavoriteMovieEntry.COLUMN_POSTER_PATH));
                String original_title=cursor.getString(cursor.getColumnIndex(FavoriteMovieEntry.COLUMN_ORIGINAL_Title));
                String overview=cursor.getString(cursor.getColumnIndex(FavoriteMovieEntry.COLUMN_OVERVIEW));
                String rate=cursor.getString(cursor.getColumnIndex(FavoriteMovieEntry.COLUMN_VOTE_AVERAGE));
                String release_Date=cursor.getString(cursor.getColumnIndex(FavoriteMovieEntry.COLUMN_RELEASE_DATE));
                String movie_id=cursor.getString(cursor.getColumnIndex(FavoriteMovieEntry.COLUMN_MOVIE_ID));
                 movieHash.put(ORIGINAL_TITLE,original_title);
                 movieHash.put(MOVIE_POSTER,movie_poster);
                 movieHash.put(APLOT_SYNOPSIS,overview);
                 movieHash.put(USER_RATING,rate);
                 movieHash.put(RELEASE_DATE,release_Date);
                 movieHash.put(MOVIE_ID,movie_id);
                 movie_Details.add(movieHash);
             } while (cursor.moveToNext());
         }
    else {
             Toast.makeText(getActivity(), "no Movies added to Favorite", Toast.LENGTH_LONG).show();

         }
         return movie_Details;
     }

    /*////////////////////////////////////////////////////////////////////////////////////////////
    // get Favorite movie reviews from data base
    ////////////////////////////////////////////////////////////////////////////////////////////*/
    public ArrayList<HashMap<String,String>>getFavoriteMovieReviews(String id){
        String selection=ReviewsEntry.COLUMN_FAVOURITE_KEY+"=?";
        Cursor cursor=OpenHelper.getReadableDatabase().query(ReviewsEntry.TABLE_NAME,
                null,selection,new String[]{id},null,null,null,null);
        ArrayList<HashMap<String,String>>MovieReviews=new ArrayList<HashMap<String,String>>();
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> reviewsHash = new HashMap<String, String>();
                String review_content=cursor.getString(cursor.getColumnIndex(ReviewsEntry.COLUMN_CONTENT));
                String review_author=cursor.getString(cursor.getColumnIndex(ReviewsEntry.COLUMN_AUTHOR));
                reviewsHash.put(MOVIE_REVIEWS_AUTHOR,review_author);
                reviewsHash.put(MOVIE_REVIEWS_CONTENT,review_content);
                MovieReviews.add(reviewsHash);
            } while (cursor.moveToNext());}
        return MovieReviews;
    }

     /*////////////////////////////////////////////////////////////////////////////////////////////
    // get Favorite movie trailers from data base
    ////////////////////////////////////////////////////////////////////////////////////////////*/
    public  ArrayList<HashMap<String,String>>getFavoriteMovieTrailers(String id){
        String selection=TrailersEntry.COLUMN_FAVOURITE_KEY+"=?";
        Cursor cursor=OpenHelper.getReadableDatabase().query(TrailersEntry.TABLE_NAME,
                null,selection,new String[]{id},null,null,null,null);
        ArrayList<HashMap<String,String>>MovieTrailers=new ArrayList<HashMap<String,String>>();

        if (cursor.moveToFirst()) {
            do {
                HashMap<String,String>MovieTrailerHash=new HashMap<String,String>();
                String TrailerKey=cursor.getString(cursor.getColumnIndex(TrailersEntry.COLUMN_TRAILERS_KEY));
                String TrailerName =cursor.getString(cursor.getColumnIndex(TrailersEntry.COLUMN_TRAILERS_NAME));
                MovieTrailerHash.put(MOVIE_TRAILERS_KEY,TrailerKey);
                MovieTrailerHash.put(MOVIE_TRAILERS_NAME,TrailerName);
                MovieTrailers.add(MovieTrailerHash);
            } while (cursor.moveToNext());}

        return MovieTrailers;
    }
}
