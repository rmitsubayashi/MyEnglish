package com.linnca.pelicann.questions;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.linnca.pelicann.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pelicann.linnca.com.corefunctionality.lessonquestions.MisspelledWordGenerator;

public class Question_ChooseCorrectSpelling extends QuestionFragmentInterface {
    private TextView questionTextView;
    private LinearLayout choicesLayout;
    private int choiceCt;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_question_choose_correct_spelling, container, false);
        parentViewGroupForFeedback = view.findViewById(R.id.fragment_question_choose_correct_spelling);
        siblingViewGroupForFeedback = view.findViewById(R.id.question_choose_correct_spelling_main_layout);

        questionTextView = view.findViewById(R.id.question_choose_correct_spelling_question);
        choicesLayout = view.findViewById(R.id.question_choose_correct_spelling_choices_layout);

        setChoiceCt();
        populateQuestion();
        populateButtons(inflater);
        inflateFeedback(inflater);
        return view;
    }

    @Override
    protected String getResponse(View clickedView){
        return (String)clickedView.getTag();
    }

    @Override
    protected void doSomethingAfterWrongAnswer(View clickedView){
        clickedView.setEnabled(false);
    }

    private void populateQuestion(){
        String question = questionData.getQuestion();
        questionTextView.setText(question);
    }

    private void setChoiceCt(){
        //the longer the word, the more variations allowed.
        //we might have more difficulty generating misspelled words of shorter length
        int answerLength = questionData.getAnswer().length();
        if (answerLength == 1) //just in case
            choiceCt = 1;
        else if (answerLength == 2) //just in case
            choiceCt = 2;
        else if (answerLength < 5)
            choiceCt = 3;
        else
            choiceCt = 4;
    }

    private void populateButtons(LayoutInflater inflater){
        //dynamically create misspelled items
        List<String> choices = new ArrayList<>(choiceCt);
        String answer = questionData.getAnswer();
        choices.add(answer);
        //odds can be based on teh length of the string.
        //long strings are more likely to change at least one character since
        // there are more characters in total, so the odds should be high.
        List<String> misspelledChoices = MisspelledWordGenerator.createMisspelledWords(
                answer, answer.length()/2, choiceCt-1
        );
        choices.addAll(misspelledChoices);
        Collections.shuffle(choices);

        //update the question data since we
        //reference the question data to see how many
        // choices a user has left
        questionData.setChoices(choices);

        for (String choice : choices){
            Button choiceButton = (Button)inflater.
                    inflate(R.layout.inflatable_question_multiple_choice_button, choicesLayout, false);
            choiceButton.setText(choice.toLowerCase());
            //for checking answer
            choiceButton.setTag(choice);
            choiceButton.setOnClickListener(getResponseListener());
            choicesLayout.addView(choiceButton);

        }
    }
}
