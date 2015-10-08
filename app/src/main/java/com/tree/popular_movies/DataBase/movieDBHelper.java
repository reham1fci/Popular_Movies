package com.tree.popular_movies.DataBase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.tree.popular_movies.DataBase.MoviesContract.FavoriteMovieEntry;
import com.tree.popular_movies.DataBase.MoviesContract.ReviewsEntry;
import com.tree.popular_movies.DataBase.MoviesContract.TrailersEntry;

/**
 * Created by thy on 01/10/2015.
 */
public class movieDBHelper extends SQLiteOpenHelper{
    private static final int DATABASE_VERSION = 2;
    static final String DATABASE_NAME = "movies.db";
    public movieDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        //CREATE FAVORITE MOVIES TABLE
        final String SQL_CREATE_FAVORITE_TABLE = "CREATE TABLE " + FavoriteMovieEntry.TABLE_NAME + " (" +
                FavoriteMovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                FavoriteMovieEntry.COLUMN_MOVIE_ID+ " TEXT UNIQUE NOT NULL, " +
                FavoriteMovieEntry.COLUMN_POSTER_PATH + " TEXT UNIQUE NOT NULL, " +
                FavoriteMovieEntry.COLUMN_ORIGINAL_Title + " TEXT NOT NULL, " +
                FavoriteMovieEntry.COLUMN_OVERVIEW + " TEXT NOT NULL, " +
                FavoriteMovieEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL, "+
                FavoriteMovieEntry.COLUMN_VOTE_AVERAGE + " TEXT NOT NULL "+");";
                   db.execSQL(SQL_CREATE_FAVORITE_TABLE);

        //CREATE FAVORITE MOVIES  REVIEWS TABLE
        final String SQL_CREATE_REVIEWS_TABLE = "CREATE TABLE " +ReviewsEntry.TABLE_NAME + " (" +
                ReviewsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                ReviewsEntry.COLUMN_AUTHOR+ " TEXT NOT NULL, " +
                ReviewsEntry.COLUMN_CONTENT + " TEXT NOT NULL, " +
                ReviewsEntry.COLUMN_FAVOURITE_KEY + " TEXT NOT NULL, " +
                " FOREIGN KEY (" +ReviewsEntry.COLUMN_FAVOURITE_KEY + ") REFERENCES " +
                FavoriteMovieEntry.TABLE_NAME + " (" + FavoriteMovieEntry.COLUMN_MOVIE_ID+ ") "+");";
                  db.execSQL(SQL_CREATE_REVIEWS_TABLE);

        //CREATE FAVORITE MOVIES TRAILERS TABLE
        final String SQL_CREATE_TRAILERS_TABLE = "CREATE TABLE " +TrailersEntry.TABLE_NAME + " (" +
                TrailersEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                TrailersEntry.COLUMN_TRAILERS_KEY+ " TEXT NOT NULL, " +
                TrailersEntry.COLUMN_TRAILERS_NAME + " TEXT NOT NULL, " +
                TrailersEntry.COLUMN_FAVOURITE_KEY+ " TEXT NOT NULL, " +
                " FOREIGN KEY (" +TrailersEntry.COLUMN_FAVOURITE_KEY + ") REFERENCES " +
                FavoriteMovieEntry.TABLE_NAME + " (" + FavoriteMovieEntry.COLUMN_MOVIE_ID+ ") "+");";
        db.execSQL(SQL_CREATE_TRAILERS_TABLE);

    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + FavoriteMovieEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TrailersEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ReviewsEntry.TABLE_NAME);
        onCreate(db);
    }
}
