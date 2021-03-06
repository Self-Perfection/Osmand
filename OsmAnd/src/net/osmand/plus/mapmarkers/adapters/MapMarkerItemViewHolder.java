package net.osmand.plus.mapmarkers.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import net.osmand.plus.R;

public class MapMarkerItemViewHolder extends RecyclerView.ViewHolder {

	final View mainLayout;
	final ImageView iconDirection;
	final ImageView iconReorder;
	final ImageView icon;
	final TextView title;
	final TextView distance;
	final View flagIconLeftSpace;
	final View leftPointSpace;
	final TextView point;
	final View rightPointSpace;
	final TextView description;
	public final ImageButton optionsBtn;
	final View divider;
	final View bottomShadow;

	public MapMarkerItemViewHolder(View view) {
		super(view);
		mainLayout = view.findViewById(R.id.main_layout);
		iconDirection = (ImageView) view.findViewById(R.id.map_marker_direction_icon);
		iconReorder = (ImageView) view.findViewById(R.id.map_marker_reorder_icon);
		icon = (ImageView) view.findViewById(R.id.map_marker_icon);
		title = (TextView) view.findViewById(R.id.map_marker_title);
		distance = (TextView) view.findViewById(R.id.map_marker_distance);
		flagIconLeftSpace = view.findViewById(R.id.flag_icon_left_space);
		leftPointSpace = view.findViewById(R.id.map_marker_left_point_space);
		point = (TextView) view.findViewById(R.id.map_marker_point_text_view);
		rightPointSpace = view.findViewById(R.id.map_marker_right_point_space);
		description = (TextView) view.findViewById(R.id.map_marker_description);
		optionsBtn = (ImageButton) view.findViewById(R.id.map_marker_options_button);
		divider = view.findViewById(R.id.divider);
		bottomShadow = view.findViewById(R.id.bottom_shadow);
	}
}
