package com.example.ryomi.myenglish.gui.widgets;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.ryomi.myenglish.R;

//holder for user interest list cells
public class UserInterestViewHolder  extends RecyclerView.ViewHolder {
    private final TextView label;
    private final TextView description;
    /*
    * Ideally we wouldn't have a button to delete but instead use a context action mode
    * and allow multiple selections.
    * But doesn't allowing multiple selections conflict with the nature of
    * real time databases??
    * */
    private final Button deleteButton;

    public UserInterestViewHolder(View itemView) {
        super(itemView);
        label = (TextView) itemView.findViewById(R.id.user_interests_list_item_label);
        description = (TextView) itemView.findViewById(R.id.user_interests_list_item_description);
        deleteButton = (Button) itemView.findViewById(R.id.user_interests_list_item_delete);
    }


    public void setLabel(String label) {
        this.label.setText(label);
    }

    public void setDescription(String description) {
        this.description.setText(description);
    }

    public void setButtonListener(View.OnClickListener listener){
        deleteButton.setOnClickListener(listener);
    }

}