package za.ac.richfield.smartpantry.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import za.ac.richfield.smartpantry.R;
import za.ac.richfield.smartpantry.model.PantryItem;
import za.ac.richfield.smartpantry.util.DateUtils;
import za.ac.richfield.smartpantry.util.DisplayFormatters;

/**
 * Binds pantry domain objects to reusable RecyclerView rows and exposes row actions to the Activity.
 */
public final class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {
    public interface PantryActions {
        void onEdit(PantryItem item);

        void onDelete(PantryItem item);
    }

    private final List<PantryItem> items = new ArrayList<>();
    private final PantryActions actions;
    private boolean showExpiryWarnings;

    public PantryAdapter(PantryActions actions) {
        this.actions = actions;
    }

    public void submitList(List<PantryItem> newItems, boolean showWarnings) {
        items.clear();
        items.addAll(newItems);
        showExpiryWarnings = showWarnings;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);
        return new PantryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PantryViewHolder holder, int position) {
        PantryItem item = items.get(position);
        holder.name.setText(item.getName());
        holder.quantity.setText(holder.itemView.getContext().getString(
                R.string.quantity_unit_format,
                DisplayFormatters.quantity(item.getQuantity()),
                item.getUnit()));

        if (item.getExpiryDate() == null || item.getExpiryDate().trim().isEmpty()) {
            holder.expiry.setText(R.string.no_expiry);
        } else {
            holder.expiry.setText(holder.itemView.getContext().getString(
                    R.string.expires_on,
                    item.getExpiryDate()));
        }

        bindExpiryBadge(holder, item);
        holder.editButton.setOnClickListener(view -> actions.onEdit(item));
        holder.deleteButton.setOnClickListener(view -> actions.onDelete(item));
        holder.itemView.setOnClickListener(view -> actions.onEdit(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private void bindExpiryBadge(PantryViewHolder holder, PantryItem item) {
        holder.expiryBadge.setVisibility(View.GONE);
        if (!showExpiryWarnings) {
            return;
        }

        DateUtils.ExpiryStatus status = DateUtils.getExpiryStatus(item.getExpiryDate());
        if (status == DateUtils.ExpiryStatus.EXPIRED) {
            holder.expiryBadge.setText(R.string.expired);
            holder.expiryBadge.setBackgroundResource(R.drawable.expired_badge_background);
            holder.expiryBadge.setVisibility(View.VISIBLE);
        } else if (status == DateUtils.ExpiryStatus.SOON) {
            holder.expiryBadge.setText(R.string.expires_soon);
            holder.expiryBadge.setBackgroundResource(R.drawable.expiry_badge_background);
            holder.expiryBadge.setVisibility(View.VISIBLE);
        }
    }

    static final class PantryViewHolder extends RecyclerView.ViewHolder {
        private final TextView name;
        private final TextView quantity;
        private final TextView expiry;
        private final TextView expiryBadge;
        private final MaterialButton editButton;
        private final MaterialButton deleteButton;

        private PantryViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.pantry_item_name);
            quantity = itemView.findViewById(R.id.pantry_item_quantity);
            expiry = itemView.findViewById(R.id.pantry_item_expiry);
            expiryBadge = itemView.findViewById(R.id.expiry_badge);
            editButton = itemView.findViewById(R.id.edit_item_button);
            deleteButton = itemView.findViewById(R.id.delete_item_button);
        }
    }
}
