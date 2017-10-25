package com.linnca.pelicann.lessongenerator.lessons;


import android.util.Log;

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
import com.linnca.pelicann.questions.QuestionUtils;
import com.linnca.pelicann.questions.Question_FillInBlank_Input;
import com.linnca.pelicann.questions.Question_FillInBlank_MultipleChoice;
import com.linnca.pelicann.userinterests.WikiDataEntryData;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;



public class NAME_possessive_mother_father_is_NAME2 extends Lesson {
    public static final String KEY = "NAME_possessive_mother_father_is_NAME2";
    private final List<QueryResult> queryResults = new ArrayList<>();
    private class QueryResult {

        private final String personID;
        private final String personNameEN;
        private final String personNameJP;
        private final String parentNameEN;
        private final String parentNameJP;
        private final String parentTypeEN;
        private final String parentTypeJP;

        private QueryResult(
                String personID,
                String personNameEN,
                String personNameJP,
                String parentNameEN,
                String parentNameJP,
                boolean isFather
        ) {

            this.personID = personID;
            this.personNameEN = personNameEN;
            this.personNameJP = personNameJP;
            this.parentNameEN = parentNameEN;
            this.parentNameJP = parentNameJP;
            this.parentTypeEN = isFather ? "father" : "mother";
            this.parentTypeJP = isFather ? "父" : "母";
        }
    }



    public NAME_possessive_mother_father_is_NAME2(WikiBaseEndpointConnector connector, LessonListener listener){
        super(connector, listener);
        super.categoryOfQuestion = WikiDataEntryData.CLASSIFICATION_PERSON;
        super.questionSetsLeftToPopulate = 3;
        super.lessonKey = KEY;

    }

    @Override
    protected String getSPARQLQuery(){
        return "SELECT ?personName ?personNameEN ?personNameLabel " +
                " ?parentEN ?parentLabel ?instance " +
                " WHERE " +
                "{" +
                "    {?personName wdt:P31 wd:Q5} UNION " + //is human
                "    {?personName wdt:P31 wd:Q15632617} ." + //or fictional human
                "    {?personName wdt:P22 ?parent . " +
                "    BIND ('father' as ?instance) ." +
                "    ?parent rdfs:label ?parentEN " + //this NEEDS to be in the union
                "    } UNION " + //is a father
                "    {?personName wdt:P25 ?parent . " +
                "    BIND ('mother' as ?instance) . " +
                "    ?parent rdfs:label ?parentEN " + //this NEEDS to be in the union
                "    } . " + //or mother
                "    ?personName rdfs:label ?personNameEN . " +
                "    FILTER (LANG(?personNameEN) = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " +
                "    FILTER (LANG(?parentEN) = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " +
                "    SERVICE wikibase:label { bd:serviceParam wikibase:language '" +
                WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + "', " + //JP label if possible
                "    '" + WikiBaseEndpointConnector.ENGLISH + "'} . " + //fallback language is English

                "    BIND (wd:%s as ?personName) . " + //binding the ID of entity as person

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
            String personID = SPARQLDocumentParserHelper.findValueByNodeName(head, "personName");

            personID = LessonGeneratorUtils.stripWikidataID(personID);
            String personNameEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "personNameEN");
            String personNameJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "personNameLabel");
            String parentNameEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "parentEN");
            String parentNameJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "parentLabel");
            String fatherOrMotherString = SPARQLDocumentParserHelper.findValueByNodeName(head, "instance");
            boolean isFather = fatherOrMotherString.equals("father");
            QueryResult qr = new QueryResult(personID, personNameEN, personNameJP, parentNameEN, parentNameJP, isFather);

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

            List<QuestionData> sentencePuzzleQuestion = createSentencePuzzleQuestion(qr);
            questionSet.add(sentencePuzzleQuestion);

            super.newQuestions.add(new QuestionDataWrapper(questionSet, qr.personID, qr.personNameJP, null));
        }



    }

    private String fillInBlankMultipleChoiceQuestion(QueryResult qr){
        return  Question_FillInBlank_MultipleChoice.FILL_IN_BLANK_MULTIPLE_CHOICE +
                " " + qr.parentTypeEN + " is " + qr.parentNameEN + ".";
    }

    private List<String> fillInBlankMultipleChoiceChoices(QueryResult qr){
        List<String> choices = new ArrayList<>(4);
        choices.add(qr.personNameEN + "'s");
        choices.add(qr.personNameEN);
        choices.add(qr.personNameEN + "s'");
        choices.add(qr.personNameEN + "s");
        return choices;
    }

    private String fillInBlankMultipleChoiceAnswer(QueryResult qr){
        return qr.personNameEN + "'s";
    }

    private List<QuestionData> createFillInBlankMultipleChoiceQuestion(QueryResult qr){
        String question = this.fillInBlankMultipleChoiceQuestion(qr);
        List<String> choices = fillInBlankMultipleChoiceChoices(qr);
        String answer = fillInBlankMultipleChoiceAnswer(qr);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(super.lessonKey);
        data.setTopic(qr.personNameJP);
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

    private String formatSentenceJP(QueryResult qr){
        return qr.parentNameJP + "の" + qr.parentTypeJP + "は" +
                qr.parentNameJP + "です。";
    }

    //puzzle pieces for sentence puzzle question
    private List<String> puzzlePieces(QueryResult qr){
        List<String> pieces = new ArrayList<>();
        pieces.add(qr.personNameEN);
        pieces.add("'s");
        pieces.add(qr.parentTypeEN);
        pieces.add("is");
        pieces.add(qr.parentNameEN);
        return pieces;
    }

    private String puzzlePiecesAnswer(QueryResult qr){
        return QuestionUtils.formatPuzzlePieceAnswer(puzzlePieces(qr));
    }

    private List<String> puzzlePiecesAcceptableAnswers(QueryResult qr){
        List<String> pieces = new ArrayList<>();
        pieces.add(qr.parentNameEN);
        pieces.add("is");
        pieces.add(qr.personNameEN);
        pieces.add("'s");
        pieces.add(qr.parentTypeEN);
        String answer = QuestionUtils.formatPuzzlePieceAnswer(pieces);
        List<String> answers = new ArrayList<>(1);
        answers.add(answer);
        return answers;
    }

    private List<QuestionData> createSentencePuzzleQuestion(QueryResult qr){
        String question = this.formatSentenceJP(qr);
        List<String> choices = this.puzzlePieces(qr);
        String answer = puzzlePiecesAnswer(qr);
        List<String> acceptableAnswers = puzzlePiecesAcceptableAnswers(qr);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(qr.personNameJP);
        data.setQuestionType(QuestionTypeMappings.SENTENCE_PUZZLE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(acceptableAnswers);


        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);
        return dataList;
    }

    private List<QuestionData> createTranslateQuestionGeneric(){
        String question = "father";
        String answer = "父";
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(TOPIC_GENERIC_QUESTION);
        data.setQuestionType(QuestionTypeMappings.TRANSLATE_WORD);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);


        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);

        return dataList;
    }

    private List<QuestionData> createTranslateQuestionGeneric2(){
        String question = "mother";
        String answer = "母";
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(TOPIC_GENERIC_QUESTION);
        data.setQuestionType(QuestionTypeMappings.TRANSLATE_WORD);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);


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