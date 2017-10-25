package com.linnca.pelicann.lessongenerator.lessons;


import com.linnca.pelicann.connectors.SPARQLDocumentParserHelper;
import com.linnca.pelicann.connectors.WikiBaseEndpointConnector;
import com.linnca.pelicann.connectors.WikiDataSPARQLConnector;
import com.linnca.pelicann.lessongenerator.FeedbackPair;
import com.linnca.pelicann.lessongenerator.GrammarRules;
import com.linnca.pelicann.lessongenerator.Lesson;
import com.linnca.pelicann.lessongenerator.LessonGeneratorUtils;
import com.linnca.pelicann.questions.QuestionData;
import com.linnca.pelicann.questions.QuestionDataWrapper;
import com.linnca.pelicann.questions.QuestionTypeMappings;
import com.linnca.pelicann.questions.Question_FillInBlank_Input;
import com.linnca.pelicann.questions.Question_FillInBlank_MultipleChoice;
import com.linnca.pelicann.userinterests.WikiDataEntryData;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;



public class PLACE_is_a_COUNTRY_CITY extends Lesson {
    public static final String KEY = "PLACE_is_a_COUNTRY_CITY";
    private final List<QueryResult> queryResults = new ArrayList<>();
    private class QueryResult {

        private final String placeID;
        private final String placeNameEN;
        private final String placeNameJP;
        private final String countryCityEN;
        private final String countryCityJP;
        private final boolean isCountry;

        private QueryResult(
                String placeID,
                String placeNameEN,
                String placeNameJP,
                boolean isCountry
        ) {

            this.placeID = placeID;
            this.placeNameEN = placeNameEN;
            this.placeNameJP = placeNameJP;
            this.countryCityEN = isCountry ? "country" : "city";
            this.countryCityJP = isCountry ? "国" : "都市";
            this.isCountry = isCountry;
        }
    }



    public PLACE_is_a_COUNTRY_CITY(WikiBaseEndpointConnector connector, LessonListener listener){

        super(connector, listener);
        super.categoryOfQuestion = WikiDataEntryData.CLASSIFICATION_PLACE;
        super.questionSetsLeftToPopulate = 5;
        super.lessonKey = KEY;

    }

    @Override
    protected String getSPARQLQuery(){
        return "SELECT DISTINCT ?placeName ?placeNameEN ?placeNameLabel ?instance " +
                " WHERE " +
                "{" +
                "    {?placeName wdt:P31 wd:Q6256 . " +
                "    BIND ('country' as ?instance)} UNION " + //is a country
                "    {?placeName wdt:P31/wdt:P279* wd:Q515 . " +
                "    BIND ('city' as ?instance)} . " + //or city
                "    ?placeName rdfs:label ?placeNameEN . " +
                "    FILTER (LANG(?placeNameEN) = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " +
                "    SERVICE wikibase:label { bd:serviceParam wikibase:language '" +
                WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + "', " + //JP label if possible
                "    '" + WikiBaseEndpointConnector.ENGLISH + "'} . " + //fallback language is English

                "    BIND (wd:%s as ?placeName) . " + //binding the ID of entity as place

                "} ";



    }

    @Override

    protected void processResultsIntoClassWrappers(Document document) {

        NodeList allResults = document.getElementsByTagName(
                WikiDataSPARQLConnector.RESULT_TAG
        );

        int resultLength = allResults.getLength();

        for (int i=0; i<resultLength; i++) {
            Node head = allResults.item(i);
            String placeID = SPARQLDocumentParserHelper.findValueByNodeName(head, "placeName");

            placeID = LessonGeneratorUtils.stripWikidataID(placeID);
            String placeNameEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "placeNameEN");
            String placeNameJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "placeNameLabel");
            String cityOrCountryString = SPARQLDocumentParserHelper.findValueByNodeName(head, "instance");
            boolean isCountry = cityOrCountryString.equals("country");
            QueryResult qr = new QueryResult(placeID, placeNameEN, placeNameJP, isCountry);

            queryResults.add(qr);

        }

    }

    @Override

    protected int getQueryResultCt(){
        return queryResults.size();
    }

    protected void createQuestionsFromResults(){

        for (QueryResult qr : queryResults){
            List<List<QuestionData>> questionSet = new ArrayList<>();

            List<QuestionData> fillInBlankMultipleChoiceQuestion = createFillInBlankMultipleChoiceQuestion(qr);
            questionSet.add(fillInBlankMultipleChoiceQuestion);

            List<QuestionData> fillInBlankInputQuestion = createFillInBlankInputQuestion(qr);
            questionSet.add(fillInBlankInputQuestion);

            super.newQuestions.add(new QuestionDataWrapper(questionSet, qr.placeID, qr.placeNameJP, null));
        }



    }

    private String fillInBlankMultipleChoiceQuestion(QueryResult qr){
        String placeName = qr.placeNameEN;
        if (qr.isCountry){
            placeName = GrammarRules.definiteArticleBeforeCountry(placeName);
        }
        String sentence = placeName + " is a " + Question_FillInBlank_MultipleChoice.FILL_IN_BLANK_MULTIPLE_CHOICE + ".";
        return GrammarRules.uppercaseFirstLetterOfSentence(sentence);
    }

    private List<String> fillInBlankMultipleChoiceChoices(){
        List<String> choices = new ArrayList<>(2);
        choices.add("country");
        choices.add("city");
        return choices;
    }

    private List<QuestionData> createFillInBlankMultipleChoiceQuestion(QueryResult qr){
        String question = this.fillInBlankMultipleChoiceQuestion(qr);
        List<String> choices = fillInBlankMultipleChoiceChoices();
        String answer = fillInBlankAnswer(qr);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(super.lessonKey);
        data.setTopic(qr.placeNameJP);
        data.setQuestionType(QuestionTypeMappings.FILL_IN_BLANK_MULTIPLE_CHOICE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);

        data.setFeedback(null);
        List<QuestionData> questionDataList = new ArrayList<>();
        questionDataList.add(data);
        return questionDataList;

    }

    private String fillInBlankInputQuestion(QueryResult qr){
        String placeName = qr.placeNameEN;
        if (qr.isCountry){
            placeName = GrammarRules.definiteArticleBeforeCountry(placeName);
        }
        String sentence = placeName + " is a " + Question_FillInBlank_Input.FILL_IN_BLANK_TEXT + ".";
        return GrammarRules.uppercaseFirstLetterOfSentence(sentence);
    }

    private String fillInBlankAnswer(QueryResult qr){
        return qr.countryCityEN;
    }

    private List<QuestionData> createFillInBlankInputQuestion(QueryResult qr){
        String question = this.fillInBlankInputQuestion(qr);

        String answer = fillInBlankAnswer(qr);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(super.lessonKey);
        data.setTopic(qr.placeNameJP);
        data.setQuestionType(QuestionTypeMappings.FILL_IN_BLANK_INPUT);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);

        data.setFeedback(null);
        List<QuestionData> questionDataList = new ArrayList<>();
        questionDataList.add(data);
        return questionDataList;

    }

    private List<String> translateAcceptableAnswersGeneric(){
        List<String> acceptableAnswers = new ArrayList<>(1);
        acceptableAnswers.add("国家");
        return acceptableAnswers;
    }

    private List<QuestionData> createTranslateQuestionGeneric(){
        String question = "country";
        String answer = "国";
        List<String> acceptableAnswers = translateAcceptableAnswersGeneric();
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(TOPIC_GENERIC_QUESTION);
        data.setQuestionType(QuestionTypeMappings.TRANSLATE_WORD);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(acceptableAnswers);


        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);

        return dataList;
    }

    private List<String> translateAcceptableAnswersGeneric2(){
        List<String> acceptableAnswers = new ArrayList<>(1);
        acceptableAnswers.add("市");
        return acceptableAnswers;
    }

    private List<QuestionData> createTranslateQuestionGeneric2(){
        String question = "city";
        String answer = "都市";
        List<String> acceptableAnswers = translateAcceptableAnswersGeneric2();
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(TOPIC_GENERIC_QUESTION);
        data.setQuestionType(QuestionTypeMappings.TRANSLATE_WORD);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(acceptableAnswers);


        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);

        return dataList;
    }

    @Override
    protected List<List<String>> getGenericQuestionIDSets(){
        int index = 1;

        List<String> questionIDs = new ArrayList<>();
        List<QuestionData> toSave1 = createTranslateQuestionGeneric();
        int toSave1Size = toSave1.size();
        while (index <= toSave1Size){
            questionIDs.add(LessonGeneratorUtils.formatGenericQuestionID(KEY, index));
            index++;
        }
        List<List<String>> questionSets = new ArrayList<>();
        questionSets.add(questionIDs);

        questionIDs = new ArrayList<>();
        List<QuestionData> toSave2 = createTranslateQuestionGeneric2();
        int toSave2Size = toSave2.size() + index - 1;
        while (index <= toSave2Size){
            questionIDs.add(LessonGeneratorUtils.formatGenericQuestionID(KEY, index));
            index++;
        }
        questionSets.add(questionIDs);
        return questionSets;
    }

    @Override
    protected List<QuestionData> getGenericQuestions(){
        List<QuestionData> questions = new ArrayList<>(2);
        List<QuestionData> toSaveSet1 = createTranslateQuestionGeneric();
        int set1Size = toSaveSet1.size();
        for (int i=1; i<= set1Size; i++){
            String id = LessonGeneratorUtils.formatGenericQuestionID(KEY, i);
            toSaveSet1.get(i-1).setId(id);
            questions.add(toSaveSet1.get(i-1));
        }
        List<QuestionData> toSaveSet2 = createTranslateQuestionGeneric2();
        int set2Size = toSaveSet2.size();
        for (int i=1; i<=set2Size; i++){
            String id = LessonGeneratorUtils.formatGenericQuestionID(KEY, i+set1Size);
            toSaveSet2.get(i-1).setId(id);
            questions.add(toSaveSet2.get(i-1));
        }

        return questions;

    }

}