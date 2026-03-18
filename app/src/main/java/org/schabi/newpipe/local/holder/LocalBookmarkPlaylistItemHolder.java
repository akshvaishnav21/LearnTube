package org.schabi.newpipe.local.holder;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import org.schabi.newpipe.R;
import org.schabi.newpipe.database.LocalItem;
import org.schabi.newpipe.database.playlist.PlaylistMetadataEntry;
import org.schabi.newpipe.local.LocalItemBuilder;
import org.schabi.newpipe.local.history.HistoryRecordManager;

import java.time.format.DateTimeFormatter;
import java.util.Map;

public class LocalBookmarkPlaylistItemHolder extends LocalPlaylistItemHolder {
    private final View itemHandleView;
    private final ProgressBar itemPlaylistProgress;

    /** A map of playlistId -> (watchedCount, totalCount) injected by BookmarkFragment. */
    public static Map<Long, int[]> sPlaylistProgressMap = null;

    public LocalBookmarkPlaylistItemHolder(final LocalItemBuilder infoItemBuilder,
                                           final ViewGroup parent) {
        this(infoItemBuilder, R.layout.list_playlist_bookmark_item, parent);
    }

    LocalBookmarkPlaylistItemHolder(final LocalItemBuilder infoItemBuilder, final int layoutId,
                                    final ViewGroup parent) {
        super(infoItemBuilder, layoutId, parent);
        itemHandleView = itemView.findViewById(R.id.itemHandle);
        itemPlaylistProgress = itemView.findViewById(R.id.itemPlaylistProgress);
    }

    @Override
    public void updateFromItem(final LocalItem localItem,
                               final HistoryRecordManager historyRecordManager,
                               final DateTimeFormatter dateTimeFormatter) {
        if (!(localItem instanceof PlaylistMetadataEntry)) {
            return;
        }
        final PlaylistMetadataEntry item = (PlaylistMetadataEntry) localItem;

        itemHandleView.setOnTouchListener(getOnTouchListener(item));

        // Bind playlist progress bar
        if (itemPlaylistProgress != null) {
            final Map<Long, int[]> progressMap = sPlaylistProgressMap;
            final int[] counts = progressMap != null ? progressMap.get(item.getUid()) : null;
            if (counts != null && counts[1] > 0) {
                final int progress = (int) (counts[0] * 100L / counts[1]);
                itemPlaylistProgress.setProgress(progress);
                itemPlaylistProgress.setVisibility(View.VISIBLE);
            } else {
                itemPlaylistProgress.setVisibility(View.GONE);
            }
        }

        super.updateFromItem(localItem, historyRecordManager, dateTimeFormatter);
    }

    private View.OnTouchListener getOnTouchListener(final PlaylistMetadataEntry item) {
        return (view, motionEvent) -> {
            view.performClick();
            if (itemBuilder != null && itemBuilder.getOnItemSelectedListener() != null
                    && motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {
                itemBuilder.getOnItemSelectedListener().drag(item,
                        LocalBookmarkPlaylistItemHolder.this);
            }
            return false;
        };
    }
}
