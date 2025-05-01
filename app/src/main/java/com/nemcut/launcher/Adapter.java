package com.nemcut.launcher;

import static androidx.core.util.TypedValueCompat.dpToPx;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class Adapter extends RecyclerView.Adapter<Adapter.AppViewHolder> {

    private List<AppInfo> appList;
    private Context context;
    private boolean isGrid;
    private boolean isPreview;
    private int iconSize;
    private int textSize;
    private int labelWidth;

    public Adapter(Context context, List<AppInfo> appList, boolean isGrid, boolean isPreview, int iconSize, int textSize, int gridColumns) {
        this.context = context;
        this.appList = appList;
        this.isGrid = isGrid;
        this.isPreview = isPreview;
        this.textSize = textSize;

        this.labelWidth = 1300/gridColumns;

        float density = context.getResources().getDisplayMetrics().density;
        this.iconSize = Math.round(iconSize * density);
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = isGrid ? R.layout.item_grid : R.layout.item;
        View view = LayoutInflater.from(context).inflate(layout, parent, false);
        return new AppViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
        AppInfo app = appList.get(position);
        holder.label.setText(app.label);
        holder.icon.setImageDrawable(app.icon);

        ViewGroup.LayoutParams params = holder.icon.getLayoutParams();

        params.width = iconSize;
        params.height = iconSize;
        holder.icon.setLayoutParams(params);

        if (isGrid) {
            TextView label = holder.itemView.findViewById(R.id.label);
            params = label.getLayoutParams();
            params.width = labelWidth;
            label.setLayoutParams(params);
        }
        holder.label.setTextSize(textSize);

        if (!isPreview) {
            holder.itemView.setOnClickListener(v -> {
                Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(app.packageName);
                if (launchIntent != null) {
                    context.startActivity(launchIntent);
                }
            });
        }

        holder.itemView.setOnLongClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + app.packageName));
            context.startActivity(intent);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    public static class AppViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView label;

        public AppViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
            label = itemView.findViewById(R.id.label);
        }
    }


    public void filterList(List<AppInfo> filteredList) {
        this.appList = filteredList;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {return position;}


}

