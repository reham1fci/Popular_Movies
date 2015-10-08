package com.tree.popular_movies;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by thy on 04/09/2015.
 */
public class gridviewAdaptar extends BaseAdapter  {
    Activity activity;
  ArrayList<String> movies_posters_path;
    LayoutInflater  inflater;
    public gridviewAdaptar(Activity activity, ArrayList<String> movies_posters_path) {
        this.activity = activity;
        this.movies_posters_path = movies_posters_path;
          inflater = LayoutInflater.from(activity);

    }

    @Override
    public int getCount() {
        return movies_posters_path.size();
    }

    @Override
    public Object getItem(int position) {
        return movies_posters_path.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolderItem viewHolder;
        if(convertView == null) {
            convertView = inflater.inflate(R.layout.movie_iteam_imageview,null);
            ImageView imageView=(ImageView)convertView.findViewById(R.id.movie_imageView);
            viewHolder = new ViewHolderItem();
            viewHolder.imageView=imageView;
            convertView.setTag(viewHolder);

        }
       else {
            viewHolder = (ViewHolderItem) convertView.getTag();
        }

                 Picasso.with(activity)
                .load(movies_posters_path.get(position))
                .into(viewHolder.imageView);
        return  convertView;

    }
   public static class ViewHolderItem {
        ImageView imageView ;
    }
}
