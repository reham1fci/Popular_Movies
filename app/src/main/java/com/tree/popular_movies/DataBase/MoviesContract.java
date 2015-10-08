package com.tree.popular_movies.DataBase;

import android.provider.BaseColumns;

/**
 * Created by thy on 01/10/2015.
 */
public class MoviesContract {
    public static final class FavoriteMovieEntry implements BaseColumns {
        public static final String TABLE_NAME = "favorite_movies";
        public static final String COLUMN_ORIGINAL_Title = "original_title";
        public static final String COLUMN_POSTER_PATH= "poster_path";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_VOTE_AVERAGE = "vote_average";
        public static final String COLUMN_RELEASE_DATE="release_date";
        public static final String COLUMN_MOVIE_ID="id";

    }
    public static final class ReviewsEntry implements BaseColumns {
        public static final String TABLE_NAME = "reviews";
        public static final String COLUMN_AUTHOR = "author";
        public static final String COLUMN_CONTENT= "content";
        public static final String COLUMN_FAVOURITE_KEY="id";

    }
    public static final class TrailersEntry implements BaseColumns {
        public static final String TABLE_NAME = "trailers";
        public static final String COLUMN_TRAILERS_KEY= "key";
        public static final String COLUMN_TRAILERS_NAME= "name";
        public static final String COLUMN_FAVOURITE_KEY="id";


    }
}
