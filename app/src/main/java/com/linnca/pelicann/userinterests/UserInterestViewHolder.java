package com.linnca.pelicann.userinterests;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.linnca.pelicann.R;

import pelicann.linnca.com.corefunctionality.userinterests.WikiDataEntity;

//holder for user interest list cells
class UserInterestViewHolder  extends RecyclerView.ViewHolder {
    private WikiDataEntity wikiDataEntity;
    private final TextView label;
    private final TextView description;
    private final ImageView icon;
    /*
    * Ideally we wouldn't have a button to delete but instead use a context action mode
    * and allow multiple selections.
    * But doesn't allowing multiple selections conflict with the nature of
    * real time databases??
    * */

    UserInterestViewHolder(View itemView) {
        super(itemView);
        label = itemView.findViewById(R.id.user_interests_list_item_label);
        description = itemView.findViewById(R.id.user_interests_list_item_description);
        icon = itemView.findViewById(R.id.user_interests_list_item_icon);
    }


    public void setLabel(String label) {
        this.label.setText(label);
    }

    public void setDescription(String description) {
        this.description.setText(description);
    }

    void setSelected(boolean selected){
        if (selected){
            icon.setImageResource(R.drawable.ic_check_circle);
            itemView.setBackgroundResource(R.drawable.gray_button);
        } else {
            icon.setImageDrawable(null);
            itemView.setBackgroundResource(R.drawable.transparent_button);
        }
    }

    void setWikiDataEntity(WikiDataEntity data){
        this.wikiDataEntity = data;
    }

    public WikiDataEntity getWikiDataEntity(){
        return this.wikiDataEntity;
    }

}