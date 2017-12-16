package com.linnca.pelicann.lessondetails;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.linnca.pelicann.R;
import com.linnca.pelicann.connectors.WikiBaseEndpointConnector;
import com.linnca.pelicann.connectors.WikiDataSPARQLConnector;
import com.linnca.pelicann.db.Database;
import com.linnca.pelicann.db.FirebaseAnalyticsHeaders;
import com.linnca.pelicann.db.FirebaseDB;
import com.linnca.pelicann.db.OnDBResultListener;
import com.linnca.pelicann.lessongenerator.Lesson;
import com.linnca.pelicann.lessongenerator.LessonFactory;
import com.linnca.pelicann.mainactivity.ThemeColorChanger;
import com.linnca.pelicann.mainactivity.MainActivity;
import com.linnca.pelicann.mainactivity.ToolbarState;
import com.linnca.pelicann.questions.InstanceRecord;
import com.linnca.pelicann.questions.QuestionAttempt;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LessonDetails extends Fragment {
    public static final String TAG = "LessonDetails";
    private Database db;
    public static final String BUNDLE_LESSON_DATA = "lessonData";
    private LessonData lessonData;
    private RecyclerView list;
    private FloatingActionButton createButton;
    private ProgressBar createProgressBar;
    private TextView noItemAddTextView;
    private TextView noItemsDescriptionTextView;
    private ProgressBar loading;
    private ViewGroup mainLayout;

    private LessonDetailsAdapter adapter;
    private LessonDetailsListener lessonDetailsListener;

    public interface LessonDetailsListener {
        void lessonDetailsToQuestions(LessonInstanceData lessonInstanceData, String lessonKey);
        void setToolbarState(ToolbarState state);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        try {
            db = (Database) getArguments().getSerializable(MainActivity.BUNDLE_DATABASE);
        } catch (Exception e){
            e.printStackTrace();
            //hard code a new database instance
            db = new FirebaseDB();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_lesson_details, container, false);
        list = view.findViewById(R.id.lesson_details_instanceList);
        list.setLayoutManager(new LinearLayoutManager(getContext()));
        //so we can long click the list items to show the context menu options
        registerForContextMenu(list);
        noItemAddTextView = view.findViewById(R.id.lesson_details_no_items_add_guide);
        noItemsDescriptionTextView = view.findViewById(R.id.lesson_details_no_items_description_guide);
        loading = view.findViewById(R.id.lesson_details_loading);
        mainLayout = view.findViewById(R.id.fragment_lesson_details);
        createButton = view.findViewById(R.id.lesson_details_add);
        createProgressBar = view.findViewById(R.id.lesson_details_add_progress_bar);
        Bundle arguments = getArguments();
        if (arguments.getSerializable(BUNDLE_LESSON_DATA) != null) {
            //get data
            lessonData = (LessonData) arguments.getSerializable(BUNDLE_LESSON_DATA);

            addActionListeners();
            setLessonColor(lessonData.getColorID());
        }

        return view;
    }

    @Override
    public void onStart(){
        super.onStart();
        lessonDetailsListener.setToolbarState(
                new ToolbarState(lessonData.getTitle(), false, false, lessonData.getKey())
        );
        populateData();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        implementListeners(context);
    }

    //must implement to account for lower APIs
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        implementListeners(activity);
    }

    private void implementListeners(Context context){
        try {
            lessonDetailsListener = (LessonDetailsListener) context;
        } catch (ClassCastException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.lesson_details_item_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        LessonInstanceData longClickData = adapter.getLongClickPositionData();
        switch (item.getItemId()) {
            case R.id.lesson_details_item_menu_more_info:
                //open a dialog with details (access records)
                getInstanceDetails(longClickData);
                return true;
            case R.id.lesson_details_item_menu_delete:
                OnDBResultListener onDBResultListener = new OnDBResultListener() {
                    @Override
                    public void onLessonInstanceRemoved() {
                        super.onLessonInstanceRemoved();
                    }
                };
                db.removeLessonInstance(lessonData.getKey(), longClickData, onDBResultListener);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void populateData(){
        OnDBResultListener onDBResultListener = new OnDBResultListener() {
            @Override
            public void onLessonInstancesQueried(List<LessonInstanceData> lessonInstances) {
                if (adapter == null) {
                    adapter = new LessonDetailsAdapter(
                            getLessonDetailsAdapterListener(), lessonDetailsListener, lessonData.getKey()
                    );
                    list.setAdapter(adapter);
                }

                loading.setVisibility(View.GONE);
                adapter.setLessonInstances(lessonInstances);
            }

            @Override
            public void onNoConnection(){
                if (adapter == null) {
                    adapter = new LessonDetailsAdapter(
                            getLessonDetailsAdapterListener(), lessonDetailsListener, lessonData.getKey()
                    );
                    list.setAdapter(adapter);
                    adapter.setOffline();

                }
                loading.setVisibility(View.GONE);
            }
        };

        db.getLessonInstances(getContext(), lessonData.getKey(), true, onDBResultListener);
    }


    private void setLessonColor(int attrID){
        //background for whole screen
        mainLayout.setBackgroundColor(ThemeColorChanger.getColorFromAttribute(attrID, getContext()));
    }

    private LessonDetailsAdapter.LessonDetailsAdapterListener getLessonDetailsAdapterListener(){
        return new LessonDetailsAdapter.LessonDetailsAdapterListener() {
            @Override
            public void onItems(){
                noItemAddTextView.setVisibility(View.GONE);
                noItemsDescriptionTextView.setVisibility(View.GONE);
            }

            @Override
            public void onNoItems() {
                //we don't show this as a part of the list because we want
                // the 'add lessons here' text to line up above the FAB
                noItemAddTextView.setVisibility(View.VISIBLE);
                noItemsDescriptionTextView.setVisibility(View.VISIBLE);
            }
        };
    }

    private void addActionListeners(){
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewInstance();
            }
        });
    }

    private void createNewInstance(){
        disableCreateButtonForLoading();
        //load lesson class
        Lesson lesson = LessonFactory.parseLesson(lessonData.getKey(),
            new WikiDataSPARQLConnector(WikiBaseEndpointConnector.JAPANESE),
            db,
            new Lesson.LessonListener() {
                @Override
                public void onLessonCreated() {
                    enableCreateButtonAfterLoading();
                }
                @Override
                public void onNoConnection(){
                    //just sets it back to default state
                    enableCreateButtonAfterLoading();
                    //also let the user know we couldn't create a lesson because
                    // there was no connection
                    Toast.makeText(getContext(), R.string.no_connection, Toast.LENGTH_SHORT)
                            .show();
                }
            }
        );

        if (lesson != null) {
            lesson.createInstance(getContext());
        }
    }

    private void disableCreateButtonForLoading(){
        createButton.setEnabled(false);
        createButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.gray500)));

        createProgressBar.setVisibility(View.VISIBLE);
    }

    private void enableCreateButtonAfterLoading(){
        //since these will be called from a separate thread, we want to make sure
        // these run on the UI thread
        //( not sure if this achieves it though. These are called even though we destroy the fragment)
        createButton.post(new Runnable() {
            @Override
            public void run() {
                if (LessonDetails.this.isVisible()) {
                    createButton.setBackgroundTintList(ColorStateList.valueOf(
                            ThemeColorChanger.getColorFromAttribute(R.attr.colorAccent500, getContext())));
                    createButton.setEnabled(true);
                }
            }
        });
        createProgressBar.post(new Runnable() {
            @Override
            public void run() {
                if (LessonDetails.this.isVisible()) {
                    Animation fadeoutAnimation = new AlphaAnimation(1f,0f);
                    fadeoutAnimation.setDuration(500);
                    fadeoutAnimation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            createProgressBar.setVisibility(View.INVISIBLE);
                            //reset alpha so the progress bar shows the next time around
                            createProgressBar.setAlpha(1f);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    createProgressBar.startAnimation(fadeoutAnimation);

                }
            }
        });
    }

    private void getInstanceDetails(final LessonInstanceData instanceData){
        OnDBResultListener onDBResultListener = new OnDBResultListener() {
            @Override
            public void onLessonInstanceDetailsQueried(List<InstanceRecord> records) {
                showInstanceDetailDialog(instanceData, records);
            }
        };
        db.getLessonInstanceDetails(lessonData.getKey(), instanceData.getId(), onDBResultListener);
    }

    private void showInstanceDetailDialog(LessonInstanceData instanceData, List<InstanceRecord> allRecords){
        View dialogView = getLayoutInflater().inflate(R.layout.inflatable_lesson_details_instance_details_dialog, null);
        TextView lastPlayedTextView = dialogView.findViewById(R.id.lesson_details_instance_details_last_played);
        TextView lastPlayedScoreTextView = dialogView.findViewById(R.id.lesson_details_instance_details_last_played_score);
        TextView playedNumberTextView = dialogView.findViewById(R.id.lesson_details_instance_details_played_number);
        TextView averageScoreTextView = dialogView.findViewById(R.id.lesson_details_instance_details_average_score);
        if (allRecords == null || allRecords.size() == 0){
            playedNumberTextView.setText(Integer.toString(0));
            averageScoreTextView.setText("0%");
            lastPlayedTextView.setText(R.string.lesson_details_instance_details_last_played_never);
            lastPlayedScoreTextView.setText("0%");
        } else {
            //the records are already in date order by default
            InstanceRecord lastRecord = allRecords.get(allRecords.size() - 1);
            List<QuestionAttempt> lastRecordQuestions = lastRecord.getAttempts();
            //should this be the time completed or time started??
            long lastPlayedTimestamp = lastRecordQuestions.get(0).getStartTime();
            DateFormat dateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT, Locale.JAPAN);
            String dateString = dateFormat.format(new Date(lastPlayedTimestamp));
            lastPlayedTextView.setText(dateString);
            int correctCt = 0;
            int totalCt = 0;
            String tempQuestionID = "";
            for (QuestionAttempt attempt : lastRecordQuestions) {
                String questionID = attempt.getQuestionID();
                //there can only be one correct answer per question.
                // (there can be multiple incorrect answers)
                if (attempt.getCorrect())
                    correctCt++;
                if (!tempQuestionID.equals(questionID)) {
                    totalCt++;
                    tempQuestionID = questionID;
                }
            }
            //out of 100%
            int lastPlayedPercentage = correctCt * 100 / totalCt;
            String lastPlayedScore = lastPlayedPercentage + "%";
            lastPlayedScoreTextView.setText(lastPlayedScore);

            playedNumberTextView.setText(Integer.toString(allRecords.size()));

            //reset
            correctCt = 0;
            totalCt = 0;
            for (InstanceRecord instanceRecord : allRecords) {
                //reset
                tempQuestionID = "";
                List<QuestionAttempt> attempts = instanceRecord.getAttempts();
                for (QuestionAttempt attempt : attempts) {
                    String questionID = attempt.getQuestionID();
                    //there can only be one correct answer per question.
                    // (there can be multiple incorrect answers)
                    if (attempt.getCorrect())
                        correctCt++;
                    if (!tempQuestionID.equals(questionID)) {
                        totalCt++;
                        tempQuestionID = questionID;
                    }
                }
            }
            int totalAveragePercentage = correctCt * 100 / totalCt;
            String totalAverageScore = totalAveragePercentage + "%";
            averageScoreTextView.setText(totalAverageScore);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(dialogView);
        builder.create().show();

    }

    @Override
    public void onStop(){
        super.onStop();
        db.cleanup();
        adapter = null;
    }

}
