package net.osmand.plus.mapmarkers.adapters;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;

import net.osmand.data.LatLon;
import net.osmand.plus.IconsCache;
import net.osmand.plus.MapMarkersHelper.MapMarker;
import net.osmand.plus.MapMarkersHelper.MapMarkersGroup;
import net.osmand.plus.MapMarkersHelper.GroupHeader;
import net.osmand.plus.MapMarkersHelper.ShowHideHistoryButton;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.OsmandSettings;
import net.osmand.plus.R;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.dashboard.DashLocationFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapMarkersGroupsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private static final int HEADER_TYPE = 1;
	private static final int MARKER_TYPE = 2;
	private static final int SHOW_HIDE_HISTORY_TYPE = 3;

	private static final int TODAY_HEADER = 56;
	private static final int YESTERDAY_HEADER = 57;
	private static final int LAST_SEVEN_DAYS_HEADER = 58;
	private static final int THIS_YEAR_HEADER = 59;

	private MapActivity mapActivity;
	private OsmandApplication app;
	private List<Object> items = new ArrayList<>();
	private boolean night;
	private int screenOrientation;
	private LatLon location;
	private Float heading;
	private boolean useCenter;
	private boolean showDirectionEnabled;
	private List<MapMarker> showDirectionMarkers;

	public MapMarkersGroupsAdapter(MapActivity mapActivity) {
		this.mapActivity = mapActivity;
		app = mapActivity.getMyApplication();
		night = !mapActivity.getMyApplication().getSettings().isLightContent();
		updateShowDirectionMarkers();
		createDisplayGroups();
	}

	public void updateShowDirectionMarkers() {
		showDirectionEnabled = app.getSettings().MAP_MARKERS_MODE.get() != OsmandSettings.MapMarkersMode.NONE;;
		List<MapMarker> mapMarkers = app.getMapMarkersHelper().getMapMarkers();
		int markersCount = mapMarkers.size();
		showDirectionMarkers = new ArrayList<>(mapMarkers.subList(0, markersCount > 0 ? markersCount > 1 ? 2 : 1 : 0));
	}

	public void createDisplayGroups() {
		items.clear();
		List<MapMarkersGroup> groups = app.getMapMarkersHelper().getMapMarkersGroups();
		for (int i = 0; i < groups.size(); i++) {
			MapMarkersGroup group = groups.get(i);
			String markerGroupName = group.getName();
			if (markerGroupName == null) {
				int previousDateHeader = -1;
				int monthsDisplayed = 0;

				Calendar currentDateCalendar = Calendar.getInstance();
				currentDateCalendar.setTimeInMillis(System.currentTimeMillis());
				int currentDay = currentDateCalendar.get(Calendar.DAY_OF_YEAR);
				int currentMonth = currentDateCalendar.get(Calendar.MONTH);
				int currentYear = currentDateCalendar.get(Calendar.YEAR);
				Calendar markerCalendar = Calendar.getInstance();
				List<MapMarker> groupMarkers = group.getActiveMarkers();
				for (int j = 0; j < groupMarkers.size(); j++) {
					MapMarker marker = groupMarkers.get(j);
					markerCalendar.setTimeInMillis(marker.creationDate);
					int markerDay = markerCalendar.get(Calendar.DAY_OF_YEAR);
					int markerMonth = markerCalendar.get(Calendar.MONTH);
					int markerYear = markerCalendar.get(Calendar.YEAR);
					if (markerYear == currentYear) {
						if (markerDay == currentDay && previousDateHeader != TODAY_HEADER) {
							items.add(TODAY_HEADER);
							previousDateHeader = TODAY_HEADER;
						} else if (markerDay == currentDay - 1 && previousDateHeader != YESTERDAY_HEADER) {
							items.add(YESTERDAY_HEADER);
							previousDateHeader = YESTERDAY_HEADER;
						} else if (currentDay - markerDay >= 2 && currentDay - markerDay <= 8 && previousDateHeader != LAST_SEVEN_DAYS_HEADER) {
							items.add(LAST_SEVEN_DAYS_HEADER);
							previousDateHeader = LAST_SEVEN_DAYS_HEADER;
						} else if (currentDay - markerDay > 8 && monthsDisplayed < 3 && previousDateHeader != markerMonth) {
							items.add(markerMonth);
							previousDateHeader = markerMonth;
							monthsDisplayed += 1;
						} else if (currentMonth - markerMonth >= 4 && previousDateHeader != THIS_YEAR_HEADER) {
							items.add(THIS_YEAR_HEADER);
							previousDateHeader = THIS_YEAR_HEADER;
						}
					} else if (previousDateHeader != markerYear) {
						items.add(markerYear);
						previousDateHeader = markerYear;
					}
					items.add(marker);
				}
			} else {
				GroupHeader header = group.getGroupHeader();
				items.add(header);
				ShowHideHistoryButton showHideHistoryButton = group.getShowHideHistoryButton();
				if (showHideHistoryButton != null && showHideHistoryButton.isShowHistory()) {
					items.addAll(group.getMarkers());
				} else {
					items.addAll(group.getActiveMarkers());
				}
				if (showHideHistoryButton != null) {
					items.add(showHideHistoryButton);
				}
			}
		}
	}

	public void setLocation(LatLon location) {
		this.location = location;
	}

	public void setHeading(Float heading) {
		this.heading = heading;
	}

	public void setUseCenter(boolean useCenter) {
		this.useCenter = useCenter;
	}

	public void setScreenOrientation(int screenOrientation) {
		this.screenOrientation = screenOrientation;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
		if (viewType == MARKER_TYPE) {
			View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.map_marker_item_new, viewGroup, false);
			return new MapMarkerItemViewHolder(view);
		} else if (viewType == HEADER_TYPE) {
			View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.map_marker_item_header, viewGroup, false);
			return new MapMarkerHeaderViewHolder(view);
		} else if (viewType == SHOW_HIDE_HISTORY_TYPE) {
			View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.map_marker_item_show_hide_history, viewGroup, false);
			return new MapMarkersShowHideHistoryViewHolder(view);
		} else {
			throw new IllegalArgumentException("Unsupported view type");
		}
	}

	@Override
	public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
		IconsCache iconsCache = app.getIconsCache();
		if (holder instanceof MapMarkerItemViewHolder) {
			final MapMarkerItemViewHolder itemViewHolder = (MapMarkerItemViewHolder) holder;
			final MapMarker marker = (MapMarker) getItem(position);

			final boolean markerInHistory = marker.history;

			int color = MapMarker.getColorId(marker.colorIndex);
			ImageView markerImageViewToUpdate;
			int drawableResToUpdate;
			final boolean markerToHighlight = showDirectionMarkers.contains(marker);
			if (showDirectionEnabled && markerToHighlight) {
				itemViewHolder.iconDirection.setVisibility(View.GONE);

				itemViewHolder.icon.setImageDrawable(iconsCache.getIcon(R.drawable.ic_arrow_marker_diretion, color));
				itemViewHolder.mainLayout.setBackgroundColor(ContextCompat.getColor(mapActivity, night ? R.color.list_divider_dark : R.color.markers_top_bar_background));
				itemViewHolder.title.setTextColor(ContextCompat.getColor(mapActivity, R.color.color_white));
				itemViewHolder.divider.setBackgroundColor(ContextCompat.getColor(mapActivity, R.color.map_markers_on_map_divider_color));
				itemViewHolder.optionsBtn.setBackgroundDrawable(mapActivity.getResources().getDrawable(R.drawable.marker_circle_background_on_map_with_inset));
				itemViewHolder.optionsBtn.setImageDrawable(iconsCache.getIcon(markerInHistory ? R.drawable.ic_action_reset_to_default_dark : R.drawable.ic_action_marker_passed, R.color.color_white));
				itemViewHolder.description.setTextColor(ContextCompat.getColor(mapActivity, R.color.map_markers_on_map_color));

				drawableResToUpdate = R.drawable.ic_arrow_marker_diretion;
				markerImageViewToUpdate = itemViewHolder.icon;
			} else {
				itemViewHolder.iconDirection.setVisibility(View.VISIBLE);

				itemViewHolder.icon.setImageDrawable(iconsCache.getIcon(R.drawable.ic_action_flag_dark, color));
				itemViewHolder.mainLayout.setBackgroundColor(ContextCompat.getColor(mapActivity, night ? R.color.bg_color_dark : R.color.bg_color_light));
				itemViewHolder.title.setTextColor(ContextCompat.getColor(mapActivity, night ? R.color.color_white : R.color.color_black));
				itemViewHolder.divider.setBackgroundColor(ContextCompat.getColor(mapActivity, night ? R.color.dashboard_divider_dark : R.color.dashboard_divider_light));
				itemViewHolder.optionsBtn.setBackgroundDrawable(mapActivity.getResources().getDrawable(R.drawable.marker_circle_background_light_with_inset));
				itemViewHolder.optionsBtn.setImageDrawable(iconsCache.getThemedIcon(markerInHistory ? R.drawable.ic_action_reset_to_default_dark : R.drawable.ic_action_marker_passed));
				itemViewHolder.description.setTextColor(ContextCompat.getColor(mapActivity, night ? R.color.dash_search_icon_dark : R.color.icon_color));

				drawableResToUpdate = R.drawable.ic_direction_arrow;
				markerImageViewToUpdate = itemViewHolder.iconDirection;
			}

			itemViewHolder.title.setText(marker.getName(app));

			if (markerInHistory) {
				itemViewHolder.point.setVisibility(View.VISIBLE);
				itemViewHolder.description.setVisibility(View.VISIBLE);
				itemViewHolder.description.setText(app.getString(R.string.passed, new SimpleDateFormat("MMM dd", Locale.getDefault()).format(new Date(marker.visitedDate))));
			} else {
				itemViewHolder.point.setVisibility(View.GONE);
				itemViewHolder.description.setVisibility(View.GONE);
			}

			String markerGroupName = marker.groupName;
			final MapMarkersGroup group = app.getMapMarkersHelper().getMapMarkerGroupByName(markerGroupName);
			itemViewHolder.optionsBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					int position = itemViewHolder.getAdapterPosition();
					if (position < 0) {
						return;
					}
					if (markerInHistory) {
						app.getMapMarkersHelper().restoreMarkerFromHistory(marker, 0);
						if (group != null) {
							ShowHideHistoryButton showHideHistoryButton = group.getShowHideHistoryButton();
							if (showHideHistoryButton != null) {
								if (group.getHistoryMarkers().size() == 0) {
									items.remove(showHideHistoryButton);
									group.setShowHideHistoryButton(null);
								}
							}
						}
					} else {
						app.getMapMarkersHelper().moveMapMarkerToHistory(marker);
						if (group != null) {
							ShowHideHistoryButton showHideHistoryButton = group.getShowHideHistoryButton();
							if (showHideHistoryButton == null) {
								items.remove(marker);
								showHideHistoryButton = new ShowHideHistoryButton();
								showHideHistoryButton.setShowHistory(false);
								showHideHistoryButton.setMarkerGroup(group);
								int index = getLastDisplayItemIndexOfGroup(group);
								if (index != -1) {
									items.add(index + 1, showHideHistoryButton);
									group.setShowHideHistoryButton(showHideHistoryButton);
								}
							} else if (!showHideHistoryButton.isShowHistory()) {
								items.remove(marker);
							}
						}
					}
					updateShowDirectionMarkers();
					notifyDataSetChanged();
				}
			});
			itemViewHolder.iconReorder.setVisibility(View.GONE);
			itemViewHolder.flagIconLeftSpace.setVisibility(View.VISIBLE);
			boolean lastItem = position == getItemCount() - 1;
			if ((getItemCount() > position + 1 && getItemViewType(position + 1) == HEADER_TYPE) || lastItem) {
				itemViewHolder.divider.setVisibility(View.GONE);
			} else {
				itemViewHolder.divider.setVisibility(View.VISIBLE);
			}
			itemViewHolder.bottomShadow.setVisibility(lastItem ? View.VISIBLE : View.GONE);

			LatLon markerLatLon = new LatLon(marker.getLatitude(), marker.getLongitude());
			DashLocationFragment.updateLocationView(useCenter, location,
					heading, markerImageViewToUpdate, drawableResToUpdate, markerToHighlight ? color : 0,
					itemViewHolder.distance, markerLatLon,
					screenOrientation, app, mapActivity, true);
		} else if (holder instanceof MapMarkerHeaderViewHolder) {
			final MapMarkerHeaderViewHolder headerViewHolder = (MapMarkerHeaderViewHolder) holder;
			final Object header = getItem(position);
			String headerString;
			if (header instanceof Integer) {
				headerViewHolder.icon.setVisibility(View.GONE);
				headerViewHolder.iconSpace.setVisibility(View.VISIBLE);
				Integer dateHeader = (Integer) header;
				if (dateHeader == TODAY_HEADER) {
					headerString = app.getString(R.string.today);
				} else if (dateHeader == YESTERDAY_HEADER) {
					headerString = app.getString(R.string.yesterday);
				} else if (dateHeader == LAST_SEVEN_DAYS_HEADER) {
					headerString = app.getString(R.string.last_seven_days);
				} else if (dateHeader == THIS_YEAR_HEADER) {
					headerString = app.getString(R.string.this_year);
				} else if (dateHeader / 100 == 0) {
					headerString = getMonth(dateHeader);
				} else {
					headerString = String.valueOf(dateHeader);
				}
				headerViewHolder.disableGroupSwitch.setVisibility(View.GONE);
			} else if (header instanceof GroupHeader) {
				final GroupHeader groupHeader = (GroupHeader) header;
				String groupName = groupHeader.getGroup().getName();
				if (groupName.equals("")) {
					groupName = app.getString(R.string.shared_string_favorites);
				}
				headerString = groupName + " \u2014 "
						+ groupHeader.getGroup().getActiveMarkers().size()
						+ "/" + groupHeader.getGroup().getMarkers().size();
				headerViewHolder.icon.setVisibility(View.VISIBLE);
				headerViewHolder.iconSpace.setVisibility(View.GONE);
				headerViewHolder.icon.setImageDrawable(iconsCache.getIcon(groupHeader.getIconRes(), R.color.divider_color));
				boolean groupIsDisabled = groupHeader.getGroup().isDisabled();
				headerViewHolder.disableGroupSwitch.setChecked(!groupIsDisabled);
				headerViewHolder.disableGroupSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
						groupHeader.getGroup().setDisabled(!b);
						notifyDataSetChanged();
					}
				});
			} else {
				throw new IllegalArgumentException("Unsupported header");
			}
			headerViewHolder.title.setText(headerString);
		} else if (holder instanceof MapMarkersShowHideHistoryViewHolder) {
			final MapMarkersShowHideHistoryViewHolder showHideHistoryViewHolder = (MapMarkersShowHideHistoryViewHolder) holder;
			final ShowHideHistoryButton showHideHistoryButton = (ShowHideHistoryButton) getItem(position);
			final boolean showHistory = showHideHistoryButton.isShowHistory();
			showHideHistoryViewHolder.title.setText(app.getString(showHistory ? R.string.hide_passed : R.string.show_passed));
			showHideHistoryViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					List<MapMarker> historyMarkers = showHideHistoryButton.getMapMarkerGroup().getHistoryMarkers();
					int pos = holder.getAdapterPosition();
					if (showHistory) {
						showHideHistoryButton.setShowHistory(false);
						items.removeAll(historyMarkers);
					} else {
						showHideHistoryButton.setShowHistory(true);
						items.addAll(pos, historyMarkers);
					}
					notifyDataSetChanged();
				}
			});
		}
	}

	@Override
	public int getItemViewType(int position) {
		Object item = items.get(position);
		if (item instanceof MapMarker) {
			return MARKER_TYPE;
		} else if (item instanceof GroupHeader || item instanceof Integer) {
			return HEADER_TYPE;
		} else if (item instanceof ShowHideHistoryButton) {
			return SHOW_HIDE_HISTORY_TYPE;
		} else {
			throw new IllegalArgumentException("Unsupported view type");
		}
	}

	@Override
	public int getItemCount() {
		return items.size();
	}

	public Object getItem(int position) {
		return items.get(position);
	}

	private String getMonth(int month) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("LLLL", Locale.getDefault());
		Date date = new Date();
		date.setMonth(month);
		return dateFormat.format(date);
	}

	private int getLastDisplayItemIndexOfGroup(MapMarkersGroup group) {
		List<MapMarker> markers = group.getActiveMarkers();
		int index = -1;
		for (MapMarker marker : markers) {
			int markerIndex = items.indexOf(marker);
			if (markerIndex > index) {
				index = markerIndex;
			}
		}
		if (index == -1) {
			GroupHeader header = group.getGroupHeader();
			if (header != null) {
				index = items.indexOf(group.getGroupHeader());
			}
		}
		return index;
	}
}
