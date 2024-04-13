package com.groupproject.music;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    //member variables
    Context context;
    List<Song> songs;

    //constructor
    public SongAdapter(Context context, List<Song> songs) {
        this.context = context;
        this.songs = songs;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate song row
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_row_item,parent,false);

        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        //current Song
        Song song = songs.get(position);
        SongViewHolder viewHolder = (SongViewHolder) holder;

        //Set values to view
        viewHolder.titleHolder.setText(song.getTitle());
        viewHolder.durationHolder.setText(song.getDuration());
        viewHolder.sizeHolder.setText(song.getSize());

        //artwork
        Uri artworkUri = song.getArtworkUri();

        if (artworkUri!=null){
            //set uri to image view
            viewHolder.artworkHolder.setImageURI(artworkUri);

            //kiem tra uri
            if(viewHolder.artworkHolder.getDrawable() == null){
                viewHolder.artworkHolder.setImageResource(R.drawable.artwork);
            }
        }

        //on click
        viewHolder.itemView.setOnClickListener(view -> Toast.makeText(context,song.getTitle(),Toast.LENGTH_SHORT).show());

    }

    //View Holder
    public static class SongViewHolder extends RecyclerView.ViewHolder {
        //member variables
        ImageView artworkHolder;
        TextView titleHolder,durationHolder,sizeHolder;
        public SongViewHolder(@NonNull View itemView) {
            super(itemView);

            artworkHolder = itemView.findViewById(R.id.artworkView);
            titleHolder = itemView.findViewById(R.id.titleView);
            durationHolder = itemView.findViewById(R.id.durationView);
            sizeHolder = itemView.findViewById(R.id.sizeView);
        }
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    //filter Song/Search
    public void filterSongs(List<Song> filteredList){
        songs = filteredList;
        notifyDataSetChanged();
    }
}
