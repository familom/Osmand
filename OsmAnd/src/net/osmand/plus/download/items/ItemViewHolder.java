package net.osmand.plus.download.items;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.osmand.plus.R;
import net.osmand.plus.WorldRegion;
import net.osmand.plus.download.BaseDownloadActivity;
import net.osmand.plus.download.DownloadActivity;
import net.osmand.plus.download.DownloadActivityType;
import net.osmand.plus.download.IndexItem;

public class ItemViewHolder {
	private final TextView nameTextView;
	private final TextView descrTextView;
	private final ImageView leftImageView;
	private final ImageView rightImageButton;
	private final ProgressBar progressBar;

	private boolean srtmDisabled;
	private int textColorPrimary;
	private int textColorSecondary;

	public ItemViewHolder(View convertView) {
		nameTextView = (TextView) convertView.findViewById(R.id.name);
		descrTextView = (TextView) convertView.findViewById(R.id.description);
		leftImageView = (ImageView) convertView.findViewById(R.id.leftImageView);
		rightImageButton = (ImageView) convertView.findViewById(R.id.rightImageButton);
		progressBar = (ProgressBar) convertView.findViewById(R.id.progressBar);

		TypedValue typedValue = new TypedValue();
		Resources.Theme theme = convertView.getContext().getTheme();
		theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, true);
		textColorPrimary = typedValue.data;
		theme.resolveAttribute(android.R.attr.textColorSecondary, typedValue, true);
		textColorSecondary = typedValue.data;
	}

	public void setSrtmDisabled(boolean srtmDisabled) {
		this.srtmDisabled = srtmDisabled;
	}

	public void bindIndexItem(final IndexItem indexItem, final DownloadActivity context, boolean showTypeInTitle, boolean showTypeInDesc) {
		boolean light = context.getMyApplication().getSettings().isLightContent();
		boolean disabled = false;
		if (indexItem.getType() == DownloadActivityType.VOICE_FILE) {
			nameTextView.setText(indexItem.getVisibleName(context,
					context.getMyApplication().getRegions()));
		} else {
			if (indexItem.getType() == DownloadActivityType.SRTM_COUNTRY_FILE && srtmDisabled) {
				nameTextView.setText(context.getString(R.string.srtm_plugin_disabled));
				disabled = true;
			} else if (showTypeInTitle) {
				nameTextView.setText(indexItem.getType().getString(context));
			} else {
				nameTextView.setText(indexItem.getVisibleName(context, context.getMyApplication().getRegions()));
			}
		}

		if (!showTypeInTitle && indexItem.getType() == DownloadActivityType.SRTM_COUNTRY_FILE && srtmDisabled) {
			descrTextView.setText(indexItem.getType().getString(context));
		} else if (showTypeInDesc) {
			descrTextView.setText(indexItem.getType().getString(context) + "  •  " + indexItem.getSizeDescription(context));
		} else {
			descrTextView.setText(indexItem.getSizeDescription(context));
		}
		rightImageButton.setVisibility(View.VISIBLE);
		rightImageButton.setImageDrawable(getContextIcon(context, R.drawable.ic_action_import));
		rightImageButton.setTag(R.id.index_item, indexItem);
		rightImageButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				((BaseDownloadActivity) v.getContext())
						.startDownload((IndexItem) v.getTag(R.id.index_item));
//				progressBar.setVisibility(View.VISIBLE);
//				rightImageButton.setImageDrawable(getContextIcon(context,
//						R.drawable.ic_action_remove_dark));
			}
		});
		progressBar.setVisibility(View.GONE);

		if (disabled) {
			leftImageView.setImageDrawable(getContextIcon(context, indexItem.getType().getIconResource(), textColorSecondary));
			nameTextView.setTextColor(textColorSecondary);
		} else {
			leftImageView.setImageDrawable(getContextIcon(context, indexItem.getType().getIconResource()));
			nameTextView.setTextColor(textColorPrimary);
		}
	}

	public void bindRegion(WorldRegion region, DownloadActivity context) {
		nameTextView.setText(region.getName());
		if (region.getResourceTypes().size() > 0) {
			StringBuilder stringBuilder = new StringBuilder();
			for (DownloadActivityType activityType : region.getResourceTypes()) {
				if (stringBuilder.length() > 0) {
					stringBuilder.append(", ");
				}
				stringBuilder.append(activityType.getString(context));
			}
			descrTextView.setText(stringBuilder.toString());
		} else {
			descrTextView.setText(R.string.shared_string_others);
		}
		leftImageView.setImageDrawable(getContextIcon(context, R.drawable.ic_map));
		rightImageButton.setVisibility(View.GONE);
		progressBar.setVisibility(View.GONE);
	}

	private Drawable getContextIcon(DownloadActivity context, int resourceId) {
		return context.getMyApplication().getIconsCache().getContentIcon(resourceId);
	}

	private Drawable getContextIcon(DownloadActivity context, int resourceId, int color) {
		return context.getMyApplication().getIconsCache().getPaintedContentIcon(resourceId, color);
	}
}
