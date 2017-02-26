package com.gh4a.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.gh4a.R;
import com.gh4a.loader.NotificationHolder;
import com.gh4a.utils.StringUtils;
import com.gh4a.utils.UiUtils;

import org.eclipse.egit.github.core.Notification;
import org.eclipse.egit.github.core.NotificationSubject;
import org.eclipse.egit.github.core.Repository;

public class NotificationAdapter extends
        RootAdapter<NotificationHolder, NotificationAdapter.ViewHolder> {
    private static final int VIEW_TYPE_NOTIFICATION_HEADER = RootAdapter.CUSTOM_VIEW_TYPE_START + 1;
    private static final String SUBJECT_ISSUE = "Issue";
    private static final String SUBJECT_PULL_REQUEST = "PullRequest";
    private static final String SUBJECT_COMMIT = "Commit";

    public interface OnNotificationActionCallback {
        void markAsRead(NotificationHolder notificationHolder);

        void unsubscribe(NotificationHolder notificationHolder);
    }

    private final int mTopBottomMargin;
    private final int mBottomPadding;
    private final Context mContext;
    private final OnNotificationActionCallback mActionCallback;

    public NotificationAdapter(Context context, OnNotificationActionCallback actionCallback) {
        super(context);
        mContext = context;
        mActionCallback = actionCallback;

        Resources resources = context.getResources();
        mTopBottomMargin = resources.getDimensionPixelSize(R.dimen.card_top_bottom_margin);
        mBottomPadding = resources.getDimensionPixelSize(R.dimen.notification_card_padding_bottom);
    }

    public void markAsRead(@Nullable Repository repository, @Nullable Notification notification) {
        for (int i = 0; i < getCount(); i++) {
            NotificationHolder item = getItem(i);
            // Passing both repository and notification as null will mark everything as read
            if ((repository == null && notification == null) ||
                    (repository != null && item.repository.equals(repository)) ||
                    (item.notification != null && item.notification.equals(notification))) {
                item.setIsRead(true);
            }
        }

        notifyDataSetChanged();
    }

    @Override
    protected ViewHolder onCreateViewHolder(LayoutInflater inflater, ViewGroup parent,
            int viewType) {
        int layoutResId = viewType == VIEW_TYPE_NOTIFICATION_HEADER
                ? R.layout.row_notification_header
                : R.layout.row_notification;
        View v = inflater.inflate(layoutResId, parent, false);
        return new ViewHolder(v, mActionCallback);
    }

    @Override
    protected int getItemViewType(NotificationHolder item) {
        if (item.notification == null) {
            return VIEW_TYPE_NOTIFICATION_HEADER;
        }
        return super.getItemViewType(item);
    }

    @Override
    protected void onBindViewHolder(ViewHolder holder, NotificationHolder item) {
        holder.ivAction.setTag(item);

        float alpha = item.isRead() ? 0.5f : 1f;
        holder.tvTitle.setAlpha(alpha);

        if (item.notification == null) {
            holder.ivAction.setVisibility(item.isRead() ? View.GONE : View.VISIBLE);

            Repository repository = item.repository;
            holder.tvTitle.setText(repository.getOwner().getLogin() + "/" + repository.getName());
            return;
        }

        holder.ivIcon.setAlpha(alpha);
        holder.tvTimestamp.setAlpha(alpha);
        holder.mPopupMenu.getMenu().findItem(R.id.mark_as_read).setVisible(!item.isRead());

        NotificationSubject subject = item.notification.getSubject();
        int iconResId = getIconResId(subject.getType());
        if (iconResId > 0) {
            holder.ivIcon.setImageResource(iconResId);
            holder.ivIcon.setVisibility(View.VISIBLE);
        } else {
            holder.ivIcon.setVisibility(View.INVISIBLE);
        }

        holder.tvTitle.setText(subject.getTitle());
        holder.tvTimestamp.setText(StringUtils.formatRelativeTime(mContext,
                item.notification.getUpdatedAt(), true));

        int bottomPadding = item.isLastRepositoryNotification() ? mBottomPadding : 0;
        holder.cvCard.setContentPadding(0, 0, 0, bottomPadding);

        ViewGroup.MarginLayoutParams layoutParams =
                (ViewGroup.MarginLayoutParams) holder.cvCard.getLayoutParams();
        int bottomMargin = item.isLastRepositoryNotification() ? mTopBottomMargin : 0;
        layoutParams.setMargins(0, 0, 0, bottomMargin);
        holder.cvCard.setLayoutParams(layoutParams);
    }

    private int getIconResId(String subjectType) {
        if (SUBJECT_ISSUE.equals(subjectType)) {
            return UiUtils.resolveDrawable(mContext, R.attr.issueIcon);
        }
        if (SUBJECT_PULL_REQUEST.equals(subjectType)) {
            return UiUtils.resolveDrawable(mContext, R.attr.pullRequestIcon);
        }
        if (SUBJECT_COMMIT.equals(subjectType)) {
            return UiUtils.resolveDrawable(mContext, R.attr.commitIcon);
        }

        return -1;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            PopupMenu.OnMenuItemClickListener {
        public ViewHolder(View view, OnNotificationActionCallback actionCallback) {
            super(view);
            mActionCallback = actionCallback;

            ivAction = (ImageView) view.findViewById(R.id.iv_action);
            ivAction.setOnClickListener(this);
            ivIcon = (ImageView) view.findViewById(R.id.iv_icon);
            tvTitle = (TextView) view.findViewById(R.id.tv_title);
            cvCard = (CardView) view.findViewById(R.id.cv_card);
            tvTimestamp = (TextView) view.findViewById(R.id.tv_timestamp);

            mPopupMenu = new PopupMenu(view.getContext(), ivAction);
            mPopupMenu.getMenuInflater().inflate(R.menu.notification_menu, mPopupMenu.getMenu());
            mPopupMenu.setOnMenuItemClickListener(this);
        }

        private final ImageView ivIcon;
        private final ImageView ivAction;
        private final TextView tvTitle;
        private final CardView cvCard;
        private final TextView tvTimestamp;
        private final PopupMenu mPopupMenu;
        private final OnNotificationActionCallback mActionCallback;

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.iv_action) {
                NotificationHolder notificationHolder = (NotificationHolder) v.getTag();

                if (notificationHolder.notification == null) {
                    mActionCallback.markAsRead(notificationHolder);
                } else {
                    mPopupMenu.show();
                }
            }
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            NotificationHolder notificationHolder = (NotificationHolder) ivAction.getTag();

            switch (item.getItemId()) {
                case R.id.mark_as_read:
                    mActionCallback.markAsRead(notificationHolder);
                    return true;
                case R.id.unsubscribe:
                    mActionCallback.unsubscribe(notificationHolder);
                    return true;
            }

            return false;
        }
    }
}
