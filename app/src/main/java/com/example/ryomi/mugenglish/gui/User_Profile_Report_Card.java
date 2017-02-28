package com.example.ryomi.mugenglish.gui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.ryomi.mugenglish.R;
import com.example.ryomi.mugenglish.db.datawrappers.InstanceRecord;
import com.example.ryomi.mugenglish.db.datawrappers.QuestionAttempt;
import com.example.ryomi.mugenglish.gui.widgets.UserProfileReportCardAdapter;
import com.example.ryomi.mugenglish.gui.widgets.UserProfileReportCardData;

import java.util.ArrayList;
import java.util.List;

public class User_Profile_Report_Card extends Fragment {
    private RecyclerView list;
    public User_Profile_Report_Card() {
    }

    @Override
    @SuppressWarnings("unchecked")
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_profile_report_card, container, false);
        Bundle dataBundle = getArguments();
        List<InstanceRecord> records = (List<InstanceRecord>)dataBundle.getSerializable(null);
        //if we fail to read the records
        if (records == null){
            return view;
        }

        list = (RecyclerView) view.findViewById(R.id.user_profile_report_card_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        list.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(list.getContext(),
                layoutManager.getOrientation());
        list.addItemDecoration(dividerItemDecoration);


        populateReportCard(records);


        return view;
    }

    private void populateReportCard(List<InstanceRecord> records){
        if (records.size() == 0)
            return;

        List<UserProfileReportCardData> dataList = new ArrayList<>();
        String tempThemeID = records.get(0).getThemeId();
        String tempInstanceID = "";
        UserProfileReportCardData tempData = new UserProfileReportCardData();
        tempData.setThemeID(tempThemeID);
        for (int i=0; i<records.size(); i++){
            InstanceRecord record = records.get(i);
            if (!record.getThemeId().equals(tempThemeID)){
                dataList.add(tempData);
                tempThemeID = record.getThemeId();
                tempData = new UserProfileReportCardData();
                tempData.setThemeID(tempThemeID);
            }

            tempData.incrementRecordCt();
            if (!record.getInstanceId().equals(tempInstanceID)){
                tempData.incrementInstanceCt();
                tempInstanceID = record.getInstanceId();
            }
            String tempQuestionID = "";
            for (QuestionAttempt attempt : record.getAttempts()){
                if (attempt.getCorrect())
                    tempData.incrementCorrectCt();
                if (!tempQuestionID.equals(attempt.getQuestionID())){
                    tempData.incrementTotalCt();
                    tempQuestionID = attempt.getQuestionID();
                }
            }
        }
        //add the last row
        dataList.add(tempData);
        list.setAdapter(new UserProfileReportCardAdapter(dataList, getContext()));
    }



}
