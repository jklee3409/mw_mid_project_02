package com.example.photoviewer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private Context context;
    private List<Post> postList;
    private List<Post> filteredList;
    private SharedPreferences prefs;

    public PostAdapter(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList;
        this.filteredList = new ArrayList<>(postList);
        prefs = context.getSharedPreferences("favorites", Context.MODE_PRIVATE);
    }

    @Override
    public PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PostViewHolder holder, int position) {
        Post post = filteredList.get(position);
        holder.title.setText(post.getTitle());
        holder.text.setText(post.getText());
        Picasso.get().load(post.getImageUrl()).into(holder.imageView);
        // 즐겨찾기 상태 표시
        boolean fav = prefs.getBoolean(post.getImageUrl(), false);
        holder.btnFavorite.setImageResource(fav ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);

        holder.btnFavorite.setOnClickListener(v -> {
            boolean current = prefs.getBoolean(post.getImageUrl(), false);
            prefs.edit().putBoolean(post.getImageUrl(), !current).apply();
            notifyItemChanged(position);
        });

        // 아이템 클릭 시 상세 페이지로 이동
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PostDetailActivity.class);
            intent.putExtra("title", post.getTitle());
            intent.putExtra("text", post.getText());
            intent.putExtra("image", post.getImageUrl());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView title;
        TextView text;
        ImageButton btnFavorite;
        public PostViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            title = itemView.findViewById(R.id.titleText);
            text = itemView.findViewById(R.id.contentText);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
        }
    }

    /**
     * 검색어에 따라 리스트 필터링. 대소문자 구분 없음.
     */
    public void filter(String query) {
        filteredList.clear();
        if (query == null || query.trim().isEmpty()) {
            filteredList.addAll(postList);
        } else {
            String lower = query.toLowerCase();
            for (Post p : postList) {
                if (p.getTitle().toLowerCase().contains(lower) || p.getText().toLowerCase().contains(lower)) {
                    filteredList.add(p);
                }
            }
        }
        notifyDataSetChanged();
    }

    /**
     * 날짜 기준 또는 제목 기준으로 정렬.
     * @param ascending true면 오름차순, false면 내림차순
     * @param byDate true면 createdDate, false면 title 기준
     */
    public void sort(boolean ascending, boolean byDate) {
        filteredList.sort((p1, p2) -> {
            int cmp;
            if (byDate) {
                cmp = p1.getCreatedDate().compareTo(p2.getCreatedDate());
            } else {
                cmp = p1.getTitle().compareToIgnoreCase(p2.getTitle());
            }
            return ascending ? cmp : -cmp;
        });
        notifyDataSetChanged();
    }
}
