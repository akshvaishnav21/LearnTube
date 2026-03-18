package org.schabi.newpipe.fragments.detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.schabi.newpipe.NewPipeDatabase;
import org.schabi.newpipe.R;
import org.schabi.newpipe.database.notes.dao.TimestampNotesDAO;
import org.schabi.newpipe.database.notes.model.TimestampNoteEntity;
import org.schabi.newpipe.database.stream.dao.StreamDAO;
import org.schabi.newpipe.database.stream.model.StreamEntity;
import org.schabi.newpipe.extractor.stream.StreamInfo;
import org.schabi.newpipe.player.TimestampChangeData;
import org.schabi.newpipe.util.NavigationHelper;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Fragment that shows timestamped notes for a video, accessible as a tab in VideoDetailFragment.
 */
public class TimestampNotesFragment extends Fragment {

    private static final String ARG_STREAM_URL = "stream_url";
    private static final String ARG_SERVICE_ID = "service_id";

    private String streamUrl;
    private int serviceId;

    private RecyclerView recyclerView;
    private TextView emptyView;
    private NotesAdapter adapter;
    private final CompositeDisposable disposables = new CompositeDisposable();

    public static TimestampNotesFragment getInstance(final StreamInfo info) {
        final TimestampNotesFragment fragment = new TimestampNotesFragment();
        final Bundle args = new Bundle();
        args.putString(ARG_STREAM_URL, info.getUrl());
        args.putInt(ARG_SERVICE_ID, info.getServiceId());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            streamUrl = getArguments().getString(ARG_STREAM_URL);
            serviceId = getArguments().getInt(ARG_SERVICE_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        final LinearLayout root = new LinearLayout(requireContext());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(0, 0, 0, 0);

        emptyView = new TextView(requireContext());
        emptyView.setText(R.string.timestamp_notes_section_title);
        final int padding = (int) (16 * getResources().getDisplayMetrics().density);
        emptyView.setPadding(padding, padding, padding, padding);
        emptyView.setVisibility(View.GONE);

        recyclerView = new RecyclerView(requireContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new NotesAdapter();
        recyclerView.setAdapter(adapter);

        root.addView(emptyView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        root.addView(recyclerView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadNotes();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disposables.clear();
    }

    private void loadNotes() {
        if (streamUrl == null) {
            return;
        }
        disposables.add(
            io.reactivex.rxjava3.core.Single.fromCallable(() -> {
                final var db = NewPipeDatabase.getInstance(requireContext());
                final StreamDAO streamDao = db.streamDAO();
                final TimestampNotesDAO notesDao = db.timestampNotesDAO();
                final List<StreamEntity> streams = streamDao.getStream((long) serviceId, streamUrl)
                        .blockingFirst(new ArrayList<>());
                if (streams.isEmpty()) {
                    return new ArrayList<TimestampNoteEntity>();
                }
                final long streamUid = streams.get(0).getUid();
                return notesDao.getNotesForStream(streamUid).blockingFirst(new ArrayList<>());
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                notes -> {
                    adapter.setNotes(notes);
                    emptyView.setVisibility(notes.isEmpty() ? View.VISIBLE : View.GONE);
                    emptyView.setText(notes.isEmpty()
                            ? R.string.timestamp_notes_section_title : 0);
                },
                throwable -> { /* silently ignore */ }
            )
        );
    }

    private void deleteNote(final TimestampNoteEntity note) {
        disposables.add(
            io.reactivex.rxjava3.core.Single.fromCallable(() ->
                NewPipeDatabase.getInstance(requireContext()).timestampNotesDAO()
                        .deleteById(note.getUid())
            )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                count -> loadNotes(),
                throwable -> { /* silently ignore */ }
            )
        );
    }

    private void seekToNote(final TimestampNoteEntity note) {
        if (streamUrl == null) {
            return;
        }
        final TimestampChangeData data = new TimestampChangeData(
                serviceId, streamUrl, (int) (note.getTimestampMs() / 1000));
        requireContext().startService(
                NavigationHelper.getPlayerTimestampIntent(requireContext(), data));
    }

    private final class NotesAdapter extends RecyclerView.Adapter<NoteViewHolder> {
        private final List<TimestampNoteEntity> notes = new ArrayList<>();

        void setNotes(final List<TimestampNoteEntity> newNotes) {
            notes.clear();
            notes.addAll(newNotes);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public NoteViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int type) {
            final View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_timestamp_note, parent, false);
            return new NoteViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final NoteViewHolder holder, final int position) {
            final TimestampNoteEntity note = notes.get(position);
            final long totalSeconds = note.getTimestampMs() / 1000;
            final long hours = totalSeconds / 3600;
            final long minutes = (totalSeconds % 3600) / 60;
            final long seconds = totalSeconds % 60;
            final String timeLabel = hours > 0
                    ? String.format("%d:%02d:%02d", hours, minutes, seconds)
                    : String.format("%d:%02d", minutes, seconds);

            holder.timestampView.setText(timeLabel);
            holder.noteTextView.setText(note.getNote());
            holder.itemView.setOnClickListener(v -> seekToNote(note));
            holder.deleteButton.setOnClickListener(v ->
                new MaterialAlertDialogBuilder(requireContext())
                    .setMessage(R.string.timestamp_note_delete_confirm)
                    .setPositiveButton(R.string.delete, (d, w) -> deleteNote(note))
                    .setNegativeButton(R.string.cancel, null)
                    .show()
            );
        }

        @Override
        public int getItemCount() {
            return notes.size();
        }
    }

    private static class NoteViewHolder extends RecyclerView.ViewHolder {
        final TextView timestampView;
        final TextView noteTextView;
        final View deleteButton;

        NoteViewHolder(@NonNull final View itemView) {
            super(itemView);
            timestampView = itemView.findViewById(R.id.itemTimestampView);
            noteTextView = itemView.findViewById(R.id.itemNoteTextView);
            deleteButton = itemView.findViewById(R.id.itemNoteDeleteButton);
        }
    }
}
