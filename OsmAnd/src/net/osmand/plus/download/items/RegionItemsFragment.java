package net.osmand.plus.download.items;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;

import net.osmand.PlatformUtil;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.OsmandPlugin;
import net.osmand.plus.R;
import net.osmand.plus.WorldRegion;
import net.osmand.plus.activities.OsmandBaseExpandableListAdapter;
import net.osmand.plus.activities.OsmandExpandableListFragment;
import net.osmand.plus.download.DownloadActivity;
import net.osmand.plus.srtmplugin.SRTMPlugin;

import org.apache.commons.logging.Log;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RegionItemsFragment extends OsmandExpandableListFragment {
	public static final String TAG = "RegionItemsFragment";
	private static final Log LOG = PlatformUtil.getLog(RegionItemsFragment.class);
	private static final MessageFormat formatGb = new MessageFormat("{0, number,<b>#.##</b>} GB", Locale.US);

	private ItemsListBuilder builder;
	private RegionsItemsAdapter listAdapter;

	private static final String REGION_KEY = "world_region_key";
	private WorldRegion region;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		WorldRegion region = null;
		if (savedInstanceState != null) {
			Object regionObj = savedInstanceState.getSerializable(REGION_KEY);
			if (regionObj != null) {
				region = (WorldRegion)regionObj;
			}
		}
		if (region == null) {
			Object regionObj = getArguments().getSerializable(REGION_KEY);
			if (regionObj != null) {
				region = (WorldRegion)regionObj;
			}
		}

		this.region = region;

		View view = inflater.inflate(R.layout.download_items_fragment, container, false);

		builder = new ItemsListBuilder(getMyApplication(), this.region);

		ExpandableListView listView = (ExpandableListView)view.findViewById(android.R.id.list);
		listAdapter = new RegionsItemsAdapter(getActivity());
		listView.setAdapter(listAdapter);
		setListView(listView);

		if (builder.build()) {
			fillRegionItemsAdapter();
			listAdapter.notifyDataSetChanged();
			expandAllGroups();
		}

		return view;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putSerializable(REGION_KEY, region);
		super.onSaveInstanceState(outState);
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
		Object obj = listAdapter.getChild(groupPosition, childPosition);
		if (obj instanceof WorldRegion) {
			WorldRegion region = (WorldRegion) obj;
			((RegionDialogFragment) getParentFragment())
					.onRegionSelected(region);
			return true;
		} else {
			return false;
		}
	}

	private void expandAllGroups() {
		for (int i = 0; i < listAdapter.getGroupCount(); i++) {
			getExpandableListView().expandGroup(i);
		}
	}

	public OsmandApplication getMyApplication() {
		return (OsmandApplication)getActivity().getApplication();
	}

	private void fillRegionItemsAdapter() {
		if (listAdapter != null) {
			listAdapter.clear();
			if (builder.getRegionMapItems().size() > 0) {
				listAdapter.add("Region maps".toUpperCase(), builder.getRegionMapItems());
			}
			if (builder.getAllResourceItems().size() > 0) {
				listAdapter.add("Additional maps".toUpperCase(), builder.getAllResourceItems());
			}
		}
	}

	private DownloadActivity getDownloadActivity() {
		return (DownloadActivity) getActivity();
	}

	public static RegionItemsFragment createInstance(WorldRegion region) {
		Bundle bundle = new Bundle();
		bundle.putSerializable(REGION_KEY, region);
		RegionItemsFragment fragment = new RegionItemsFragment();
		fragment.setArguments(bundle);
		return fragment;
	}

	private class RegionsItemsAdapter extends OsmandBaseExpandableListAdapter {

		private Map<String, List> data = new LinkedHashMap<>();
		private List<String> sections = new LinkedList<>();
		private Context ctx;
		private boolean srtmDisabled;

		public RegionsItemsAdapter(Context ctx) {
			this.ctx = ctx;
			srtmDisabled = OsmandPlugin.getEnabledPlugin(SRTMPlugin.class) == null;
			TypedArray ta = ctx.getTheme().obtainStyledAttributes(new int[]{android.R.attr.textColorPrimary});
			ta.recycle();
		}

		public void clear() {
			data.clear();
			sections.clear();
			notifyDataSetChanged();
		}

		public void add(String section, List list) {
			if (!sections.contains(section)) {
				sections.add(section);
			}
			if (!data.containsKey(section)) {
				data.put(section, new ArrayList());
			}
			data.get(section).addAll(list);
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			String section = sections.get(groupPosition);
			return data.get(section).get(childPosition);
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return groupPosition * 10000 + childPosition;
		}

		@Override
		public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

			final Object child = getChild(groupPosition, childPosition);

			if (child instanceof ItemsListBuilder.ResourceItem && groupPosition == 0 && getGroupCount() > 1) {
				ItemViewHolder viewHolder;
				if (convertView == null) {
					convertView = LayoutInflater.from(parent.getContext())
							.inflate(R.layout.two_line_with_images_list_item, parent, false);
					viewHolder = new ItemViewHolder(convertView);
					convertView.setTag(viewHolder);
				} else {
					viewHolder = (ItemViewHolder) convertView.getTag();
				}
				viewHolder.setSrtmDisabled(srtmDisabled);

				ItemsListBuilder.ResourceItem item = (ItemsListBuilder.ResourceItem)child;
				viewHolder.bindIndexItem(item.getIndexItem(), getDownloadActivity(), true, false);
			} else {
				ItemViewHolder viewHolder;
				if (convertView == null) {
					convertView = LayoutInflater.from(parent.getContext())
							.inflate(R.layout.two_line_with_images_list_item, parent, false);
					viewHolder = new ItemViewHolder(convertView);
					convertView.setTag(viewHolder);
				} else {
					viewHolder = (ItemViewHolder) convertView.getTag();
				}
				viewHolder.setSrtmDisabled(srtmDisabled);

				if (child instanceof WorldRegion) {
					viewHolder.bindRegion((WorldRegion) child, getDownloadActivity());
				} else if (child instanceof ItemsListBuilder.ResourceItem) {
					viewHolder.bindIndexItem(((ItemsListBuilder.ResourceItem) child).getIndexItem(),
							getDownloadActivity(), false, true);
				} else {
					throw new IllegalArgumentException("Item must be of type WorldRegion or " +
							"IndexItem but is of type:" + child.getClass());
				}
			}

			convertView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					onChildClick(null, v, groupPosition, childPosition, 0);
				}
			});

			return convertView;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
			View v = convertView;
			String section = getGroup(groupPosition);
			if (v == null) {
				LayoutInflater inflater = (LayoutInflater) getDownloadActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = inflater.inflate(R.layout.download_item_list_section, parent, false);
			}
			TextView nameView = ((TextView) v.findViewById(R.id.section_name));
			nameView.setText(section);

			v.setOnClickListener(null);
			return v;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			String section = sections.get(groupPosition);
			return data.get(section).size();
		}

		@Override
		public String getGroup(int groupPosition) {
			return sections.get(groupPosition);
		}

		@Override
		public int getGroupCount() {
			return sections.size();
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}
	}
}
