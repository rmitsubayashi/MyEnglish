package com.linnca.pelicann.gui.widgets;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linnca.pelicann.R;
import com.linnca.pelicann.db.datawrappers.WikiDataEntryData;
import com.linnca.pelicann.userinterestcontrols.UserInterestRemover;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class UserInterestAdapter
        extends RecyclerView.Adapter<UserInterestViewHolder>
{
    private String userID;
    private UserInterestAdapterListener listener;
    private List<WikiDataEntryData> originalList;
    //default to no filter
    private int filter = ToolbarSpinnerAdapter.FILTER_ALL;
    private List<WikiDataEntryData> filteredList = new ArrayList<>();

    public interface UserInterestAdapterListener {
        //should allow undo-ing
        void onItemRemoved(WikiDataEntryData item);
    }

    //Query instead of reference so we can order the data alphabetically
    public UserInterestAdapter(String userID,
                               UserInterestAdapterListener listener){
        super();
        this.userID = userID;
        this.listener = listener;
    }

    @Override
    public int getItemCount(){
        return filteredList.size();
    }

    @Override
    public long getItemId(int position){
        return filteredList.get(position).hashCode();
    }

    @Override
    public UserInterestViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.inflatable_user_interests_list_item, parent, false);
        return new UserInterestViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(UserInterestViewHolder holder, int position){
        final WikiDataEntryData data = filteredList.get(position);
        holder.setLabel(data.getLabel());
        holder.setDescription(data.getDescription());
        holder.setWikiDataEntryData(data);
        holder.setButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserInterestRemover.removeUserInterest(data, userID);
                listener.onItemRemoved(data);
            }
        });

    }

    public void setInterests(List<WikiDataEntryData> updatedList){
        //initial call
        if (originalList == null){
            originalList = new ArrayList<>(updatedList);
            filteredList = new ArrayList<>(updatedList);
            notifyDataSetChanged();
            return;
        }

        originalList = new ArrayList<>(updatedList);
        List<WikiDataEntryData> updatedFilteredList = new ArrayList<>(updatedList);
        filter(updatedFilteredList);
        List<WikiDataEntryData> prevFilteredList = filteredList;
        filteredList = updatedFilteredList;

        int prevListSize = prevFilteredList.size();
        int updatedListSize = updatedFilteredList.size();
        //more than one item removed/added
        if (Math.abs(prevListSize - updatedListSize) > 1){
            notifyDataSetChanged();
            return;
        }

        //we should remove
        if (prevListSize > updatedListSize){
            //we don't want to go beyond the array range so loop until the smaller one
            for (int i=0; i<updatedListSize; i++){
                WikiDataEntryData prevListItem = prevFilteredList.get(i);
                WikiDataEntryData updatedListItem = updatedFilteredList.get(i);
                if (!prevListItem.equals(updatedListItem)){
                    notifyItemRemoved(i);
                    return;
                }
            }
            return;
        }

        //we should add
        if (prevListSize < updatedListSize){
            for (int i=0; i<prevListSize; i++){
                WikiDataEntryData prevListItem = prevFilteredList.get(i);
                WikiDataEntryData updatedListItem = updatedFilteredList.get(i);
                if (!prevListItem.equals(updatedListItem)){
                    notifyItemInserted(i);
                    return;
                }
            }
            return;
        }

        //this shouldn't happen as of this implementation (8/31/17)
        if (prevListSize == updatedListSize){
            notifyDataSetChanged();
        }

    }

    public void setFilter(int newFilter){
        //just making sure we have the right constant
        if (!ToolbarSpinnerAdapter.isSpinnerState(newFilter))
            return;

        //first instance.
        //should always initialize list before filtering it,
        //but just to make sure
        if (originalList == null){
            return;
        }

        if (this.filter == newFilter){
            //we don't need to do anything
            //because the filters are the same
            return;
        }

        int oldFilter = this.filter;
        this.filter = newFilter;
        List<WikiDataEntryData> newFilteredList = new ArrayList<>(originalList);
        filter(newFilteredList);
        List<WikiDataEntryData> oldFilteredList = filteredList;
        filteredList = newFilteredList;
        //should animate filtered list,
        //but only when it goes from all to something else or vice versa
        // since the rest is mutually exclusive??

        //we want to remove items
        if (oldFilter == ToolbarSpinnerAdapter.FILTER_ALL){
            //make the new list a st to make it easier to search
            Set<WikiDataEntryData> newFilteredSet = new HashSet<>(newFilteredList);
            int itemsRemoved = 0;
            for (int i=0; i<oldFilteredList.size(); i++){
                WikiDataEntryData oldItem = oldFilteredList.get(i);
                if (!newFilteredSet.contains(oldItem)){
                    notifyItemRemoved(i-itemsRemoved);
                    itemsRemoved++;
                }
            }
            return;
        }
        //we want to add items
        if (newFilter == ToolbarSpinnerAdapter.FILTER_ALL){
            Set<WikiDataEntryData> oldFilteredSet = new HashSet<>(oldFilteredList);
            int tempToAdd = 0;
            int oldFilteredListIndex = 0;
            int itemsAdded =0;
            for (int i=0; i<newFilteredList.size(); i++){
                WikiDataEntryData newData = newFilteredList.get(i);
                if (!oldFilteredSet.contains(newData)){
                    tempToAdd++;
                } else {
                    if (tempToAdd > 0){
                        notifyItemRangeInserted(oldFilteredListIndex+itemsAdded, tempToAdd);
                        itemsAdded += tempToAdd;
                        tempToAdd = 0;
                    }
                    oldFilteredListIndex++;
                }
            }
            //to handle when the last item should be added
            if (tempToAdd > 0){
                notifyItemRangeInserted(oldFilteredListIndex+itemsAdded, tempToAdd);
            }
            return;
        }

        //the rest
        notifyDataSetChanged();

    }

    private void filter(List<WikiDataEntryData> list){
        if (filter == ToolbarSpinnerAdapter.FILTER_ALL){
            return;
        }
        List<Integer> toMatchFilter = new ArrayList<>();
        if (filter == ToolbarSpinnerAdapter.FILTER_PERSON){
            toMatchFilter.add(WikiDataEntryData.CLASSIFICATION_PERSON);
        }
        if (filter == ToolbarSpinnerAdapter.FILTER_PLACE){
            toMatchFilter.add(WikiDataEntryData.CLASSIFICATION_PLACE);
        }
        if (filter == ToolbarSpinnerAdapter.FILTER_OTHER){
            toMatchFilter.add(WikiDataEntryData.CLASSIFICATION_OTHER);
            //also all unidentified
            toMatchFilter.add(WikiDataEntryData.CLASSIFICATION_NOT_SET);
        }
        for (Iterator<WikiDataEntryData> iterator = list.iterator(); iterator.hasNext();){
            WikiDataEntryData data = iterator.next();
            if (!toMatchFilter.contains(data.getClassification())){
                iterator.remove();
            }
        }
    }
}
