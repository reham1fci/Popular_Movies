package com.tree.popular_movies;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;


public class movieDetails extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        if (savedInstanceState == null) {
            Bundle arguments = new Bundle();
            arguments.putSerializable(movieFragment.MOVIE_DETAILS,getIntent().getSerializableExtra(movieFragment.MOVIE_DETAILS));
            arguments.putSerializable(movieFragment.MOVIE_REVIEWS,getIntent().getSerializableExtra(movieFragment.MOVIE_REVIEWS));
            arguments.putSerializable(movieFragment.MOVIE_TRAILERS,getIntent().getSerializableExtra(movieFragment.MOVIE_TRAILERS));
            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.movie_detail_container, fragment
                    )
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
