package com.linnca.pelicann.lessongenerator.lessons;

import com.linnca.pelicann.connectors.WikiBaseEndpointConnector;
import com.linnca.pelicann.lessongenerator.Lesson;
import com.linnca.pelicann.lessongenerator.LessonGeneratorUtils;
import com.linnca.pelicann.questions.ChatQuestionItem;
import com.linnca.pelicann.questions.QuestionData;
import com.linnca.pelicann.questions.QuestionTypeMappings;
import com.linnca.pelicann.questions.QuestionUtils;
import com.linnca.pelicann.questions.Question_Actions;
import com.linnca.pelicann.questions.Question_FillInBlank_Input;
import com.linnca.pelicann.questions.Question_FillInBlank_MultipleChoice;
import com.linnca.pelicann.questions.Question_General;
import com.linnca.pelicann.questions.Question_MultipleChoice;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class I_turned_left_right extends Lesson {
    public static final String KEY = "I_turned_left_right";

    public I_turned_left_right(WikiBaseEndpointConnector connector, LessonListener listener){
        super(connector, listener);
        super.lessonKey = KEY;
    }
    @Override
    protected int getQueryResultCt(){return 0;}
    @Override
    protected String getSPARQLQuery(){
        return "";
    }
    @Override
    protected void createQuestionsFromResults(){}
    @Override
    protected void processResultsIntoClassWrappers(Document document){}

    @Override
    protected List<QuestionData> getGenericQuestions(){
        List<QuestionData> questions = new ArrayList<>(6);
        List<QuestionData> multipleChoiceQuestion1 = multipleChoiceQuestion1();
        questions.addAll(multipleChoiceQuestion1);
        List<QuestionData> multipleChoiceQuestion2 = multipleChoiceQuestion2();
        questions.addAll(multipleChoiceQuestion2);
        List<QuestionData> actionQuestion1 = actionQuestion1();
        questions.addAll(actionQuestion1);
        List<QuestionData> multipleChoiceQuestion3 = multipleChoiceQuestion3();
        questions.addAll(multipleChoiceQuestion3);
        List<QuestionData> actionQuestion2 = actionQuestion2();
        questions.addAll(actionQuestion2);
        List<QuestionData> multipleChoiceQuestion4 = multipleChoiceQuestion4();
        questions.addAll(multipleChoiceQuestion4);
        for (int i=0; i<6; i++){
            QuestionData data = questions.get(i);
            data.setId(LessonGeneratorUtils.formatGenericQuestionID(KEY, i+1));
        }

        return questions;

    }

    @Override
    protected List<List<String>> getGenericQuestionIDSets(){
        List<List<String>> questionSet = new ArrayList<>(6);
        for (int i=1; i<=6; i++) {
            List<String> questions = new ArrayList<>();
            questions.add(LessonGeneratorUtils.formatGenericQuestionID(KEY, i));
            questionSet.add(questions);
        }

        return questionSet;
    }

    private String multipleChoiceQuestionQuestion1(){
        String sentence1 = "左折してください";
        String sentence2 = "turn " + Question_FillInBlank_MultipleChoice.FILL_IN_BLANK_MULTIPLE_CHOICE;
        return sentence1 + "\n\n" + sentence2;
    }

    private List<String> multipleChoiceQuestionChoices(){
        List<String> choices = new ArrayList<>(2);
        choices.add("left");
        choices.add("right");
        return choices;
    }

    private List<QuestionData> multipleChoiceQuestion1(){
        String question = multipleChoiceQuestionQuestion1();
        List<String> choices = multipleChoiceQuestionChoices();
        String answer = "left";
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(super.lessonKey);
        data.setTopic(TOPIC_GENERIC_QUESTION);
        data.setQuestionType(QuestionTypeMappings.FILL_IN_BLANK_MULTIPLE_CHOICE);
        data.setQuestion(question);
        data.setChoices(choices);
        //for suggestive, we don't need to lowercase everything
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);

        data.setFeedback(null);

        List<QuestionData> questionVariations = new ArrayList<>();
        questionVariations.add(data);
        return questionVariations;
    }

    private String multipleChoiceQuestionQuestion2(){
        String sentence1 = "右折してください";
        String sentence2 = "turn " + Question_FillInBlank_MultipleChoice.FILL_IN_BLANK_MULTIPLE_CHOICE;
        return sentence1 + "\n\n" + sentence2;    }

    private List<QuestionData> multipleChoiceQuestion2(){
        String question = multipleChoiceQuestionQuestion2();
        List<String> choices = multipleChoiceQuestionChoices();
        String answer = "right";
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(super.lessonKey);
        data.setTopic(TOPIC_GENERIC_QUESTION);
        data.setQuestionType(QuestionTypeMappings.FILL_IN_BLANK_MULTIPLE_CHOICE);
        data.setQuestion(question);
        data.setChoices(choices);
        //for suggestive, we don't need to lowercase everything
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);

        data.setFeedback(null);

        List<QuestionData> questionVariations = new ArrayList<>();
        questionVariations.add(data);
        return questionVariations;
    }

    private List<String> getActions1(){
        List<String> actions = new ArrayList<>(1);
        actions.add("turn left");
        return actions;
    }

    private List<QuestionData> actionQuestion1(){
        String question = "";
        List<String> actions = getActions1();
        String answer = Question_Actions.ANSWER_FINISHED;
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(super.lessonKey);
        data.setTopic(TOPIC_GENERIC_QUESTION);
        data.setQuestionType(QuestionTypeMappings.ACTIONS);
        data.setQuestion(question);
        data.setChoices(actions);
        //for suggestive, we don't need to lowercase everything
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);

        data.setFeedback(null);

        List<QuestionData> questionVariations = new ArrayList<>();
        questionVariations.add(data);
        return questionVariations;
    }

    private String multipleChoiceQuestionActionQuestion(){
        return "I turned...";
    }

    private List<String> multipleChoiceQuestionActionChoices(){
        List<String> choices = new ArrayList<>(2);
        choices.add("right");
        choices.add("left");

        return choices;
    }

    private List<QuestionData> multipleChoiceQuestion3(){
        String question = multipleChoiceQuestionActionQuestion();
        List<String> choices = multipleChoiceQuestionActionChoices();
        String answer = "left";
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(super.lessonKey);
        data.setTopic(TOPIC_GENERIC_QUESTION);
        data.setQuestionType(QuestionTypeMappings.MULTIPLE_CHOICE);
        data.setQuestion(question);
        data.setChoices(choices);
        //for suggestive, we don't need to lowercase everything
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);

        data.setFeedback(null);

        List<QuestionData> questionVariations = new ArrayList<>();
        questionVariations.add(data);
        return questionVariations;
    }

    private List<String> getActions2(){
        List<String> actions = new ArrayList<>(1);
        actions.add("turn right");
        return actions;
    }

    private List<QuestionData> actionQuestion2(){
        String question = "";
        List<String> actions = getActions2();
        String answer = Question_Actions.ANSWER_FINISHED;
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(super.lessonKey);
        data.setTopic(TOPIC_GENERIC_QUESTION);
        data.setQuestionType(QuestionTypeMappings.ACTIONS);
        data.setQuestion(question);
        data.setChoices(actions);
        //for suggestive, we don't need to lowercase everything
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);

        data.setFeedback(null);

        List<QuestionData> questionVariations = new ArrayList<>();
        questionVariations.add(data);
        return questionVariations;
    }
    private List<QuestionData> multipleChoiceQuestion4(){
        String question = multipleChoiceQuestionActionQuestion();
        List<String> choices = multipleChoiceQuestionActionChoices();
        String answer = "right";
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(super.lessonKey);
        data.setTopic(TOPIC_GENERIC_QUESTION);
        data.setQuestionType(QuestionTypeMappings.MULTIPLE_CHOICE);
        data.setQuestion(question);
        data.setChoices(choices);
        //for suggestive, we don't need to lowercase everything
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);

        data.setFeedback(null);

        List<QuestionData> questionVariations = new ArrayList<>();
        questionVariations.add(data);
        return questionVariations;
    }


}