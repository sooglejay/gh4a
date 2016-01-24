package com.gh4a.adapter;

import org.eclipse.egit.github.core.Release;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gh4a.R;
import com.gh4a.utils.StringUtils;

public class ReleaseAdapter extends RootAdapter<Release, ReleaseAdapter.ViewHolder> {
    public ReleaseAdapter(Context context) {
        super(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(LayoutInflater inflater, ViewGroup parent) {
        View v = inflater.inflate(R.layout.row_release, null);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, Release release) {
        holder.tvTitle.setText(release.getName());
        holder.tvType.setText(formatReleaseType(release));
        holder.tvCreatedAt.setText(mContext.getString(R.string.download_created,
                StringUtils.formatRelativeTime(mContext, release.getCreatedAt(), true)));
    }

    private String formatReleaseType(Release release) {
        if (release.isDraft()) {
            return mContext.getString(R.string.release_type_draft);
        }
        if (release.isPreRelease()) {
            return mContext.getString(R.string.release_type_prerelease);
        }
        return mContext.getString(R.string.release_type_final);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ViewHolder(View view) {
            super(view);
            tvTitle = (TextView) view.findViewById(R.id.tv_title);
            tvType = (TextView) view.findViewById(R.id.tv_type);
            tvCreatedAt  = (TextView) view.findViewById(R.id.tv_created_at);
        }

        private TextView tvTitle;
        private TextView tvType;
        private TextView tvCreatedAt;
    }
}