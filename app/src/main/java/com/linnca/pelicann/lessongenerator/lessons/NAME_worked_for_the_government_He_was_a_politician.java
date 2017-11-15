package com.linnca.pelicann.lessongenerator.lessons;

import com.linnca.pelicann.connectors.EndpointConnectorReturnsXML;
import com.linnca.pelicann.connectors.SPARQLDocumentParserHelper;
import com.linnca.pelicann.connectors.WikiBaseEndpointConnector;
import com.linnca.pelicann.connectors.WikiDataSPARQLConnector;
import com.linnca.pelicann.db.Database;
import com.linnca.pelicann.lessongenerator.FeedbackPair;
import com.linnca.pelicann.lessongenerator.GrammarRules;
import com.linnca.pelicann.lessongenerator.Lesson;
import com.linnca.pelicann.lessongenerator.LessonGeneratorUtils;
import com.linnca.pelicann.questions.QuestionData;
import com.linnca.pelicann.questions.QuestionDataWrapper;
import com.linnca.pelicann.questions.Question_ChooseCorrectSpelling;
import com.linnca.pelicann.questions.Question_FillInBlank_MultipleChoice;
import com.linnca.pelicann.questions.Question_Spelling;
import com.linnca.pelicann.questions.Question_TranslateWord;
import com.linnca.pelicann.userinterests.WikiDataEntryData;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class NAME_worked_for_the_government_He_was_a_politician extends Lesson {
    public static final String KEY = "NAME_worked_for_the government_He_was_a_politician";

    private final List<QueryResult> queryResults = new ArrayList<>();

    private class QueryResult {
        private final String personID;
        private final String personEN;
        private final String personJP;
        private final String genderEN;
        private final String genderJP;

        private QueryResult(
                String personID,
                String personEN,
                String personJP,
                boolean isMale
        )
        {
            this.personID = personID;
            this.personEN = personEN;
            this.personJP = personJP;
            this.genderEN = isMale ? "he" : "she";
            this.genderJP = isMale ? "彼" : "彼女";
        }
    }

    public NAME_worked_for_the_government_He_was_a_politician(EndpointConnectorReturnsXML connector, Database db, LessonListener listener){
        super(connector, db, listener);
        super.questionSetsToPopulate = 3;
        super.categoryOfQuestion = WikiDataEntryData.CLASSIFICATION_PERSON;
        super.lessonKey = KEY;

    }

    @Override
    protected String getSPARQLQuery(){
        return "SELECT ?person ?personLabel ?personEN " +
                " ?gender " +
                "WHERE " +
                "{" +
                "    {?person wdt:P31 wd:Q5} UNION " + //is human
                "    {?person wdt:P31 wd:Q15632617} . " + //or fictional human
                "    ?person wdt:P21 ?gender . " + //has gender
                "    ?person wdt:P106 wd:Q82955 . " + //is a politician
                "    ?person wdt:P570 ?dead . " + //is dead
                "    ?person rdfs:label ?personEN . " + //English label
                "    FILTER (LANG(?personEN) = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " +
                "    SERVICE wikibase:label {bd:serviceParam wikibase:language '" +
                WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + "','" +
                WikiBaseEndpointConnector.ENGLISH + "' } " +
                "    BIND (wd:%s as ?person) . " + //binding the ID of entity as ?person
                "} ";

    }

    @Override
    protected void processResultsIntoClassWrappers(Document document) {
        NodeList allResults = document.getElementsByTagName(
                WikiDataSPARQLConnector.RESULT_TAG
        );
        int resultLength = allResults.getLength();
        for (int i=0; i<resultLength; i++){
            Node head = allResults.item(i);
            String personID = SPARQLDocumentParserHelper.findValueByNodeName(head, "person");
            personID = LessonGeneratorUtils.stripWikidataID(personID);
            String personEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "personEN");
            String personJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "personLabel");
            String genderID = SPARQLDocumentParserHelper.findValueByNodeName(head, "gender");
            genderID = LessonGeneratorUtils.stripWikidataID(genderID);
            boolean isMale;
            switch (genderID){
                case "Q6581097":
                    isMale = true;
                    break;
                case "Q6581072":
                    isMale = false;
                    break;
                default:
                    isMale = true;
            }

            QueryResult qr = new QueryResult(personID, personEN, personJP, isMale);
            queryResults.add(qr);
        }
    }

    @Override
    protected int getQueryResultCt(){ return queryResults.size(); }

    @Override
    protected void createQuestionsFromResults(){
        for (QueryResult qr : queryResults){
            List<List<QuestionData>> questionSet = new ArrayList<>();
            List<QuestionData> fillInBlankMultipleChoiceQuestion = createFillInBlankMultipleChoiceQuestion(qr);
            questionSet.add(fillInBlankMultipleChoiceQuestion);

            List<QuestionData> fillInBlankQuestion = createFillInBlankMultipleChoiceQuestion2(qr);
            questionSet.add(fillInBlankQuestion);

            super.newQuestions.add(new QuestionDataWrapper(questionSet, qr.personID, qr.personJP, null));
        }

    }

    private String formatSentenceJP(QueryResult qr){
        return qr.personJP + "は政府で働いていました。" + qr.genderJP + "は政治家でした。";
    }

    private String fillInBlankMultipleChoiceQuestion(QueryResult qr){
        String sentence = qr.personEN + " worked for the government.";
        String sentence2 = qr.genderEN + " " + Question_FillInBlank_MultipleChoice.FILL_IN_BLANK_MULTIPLE_CHOICE + " a politician.";
        sentence2 = GrammarRules.uppercaseFirstLetterOfSentence(sentence2);
        return sentence + "\n" + sentence2;
    }


    private String fillInBlankMultipleChoiceAnswer(){
        return "was";
    }

    private List<String> fillInBlankMultipleChoiceChoices(){
        List<String> choices = new ArrayList<>(2);
        choices.add("was");
        choices.add("is");
        return choices;
    }

    private List<QuestionData> createFillInBlankMultipleChoiceQuestion(QueryResult qr){
        String question = this.fillInBlankMultipleChoiceQuestion(qr);
        String answer = fillInBlankMultipleChoiceAnswer();
        List<QuestionData> questionDataList = new ArrayList<>();
        List<String> choices = fillInBlankMultipleChoiceChoices();
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(qr.personJP);
        data.setQuestionType(Question_FillInBlank_MultipleChoice.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);


        questionDataList.add(data);

        return questionDataList;
    }

    private String fillInBlankMultipleChoiceQuestion2(QueryResult qr){
        String sentence = qr.personEN + " worked " + Question_FillInBlank_MultipleChoice.FILL_IN_BLANK_MULTIPLE_CHOICE + ".";
        String sentence2 = qr.genderEN + " was a politician.";
        sentence = GrammarRules.uppercaseFirstLetterOfSentence(sentence);
        sentence2 = GrammarRules.uppercaseFirstLetterOfSentence(sentence2);
        return sentence + "\n" + sentence2;
    }

    private String fillInBlankMultipleChoiceAnswer2(){
        return "for the government";
    }

    private List<String> fillInBlankMultipleChoiceChoices2(){
        List<String> choices = new ArrayList<>(3);
        choices.add("at the government");
        choices.add("for the government");
        choices.add("from the government");

        return choices;
    }

    private FeedbackPair fillInBlankMultipleChoiceFeedback2(){
        String response = "at the government";
        List<String> responses = new ArrayList<>(1);
        responses.add(response);
        String feedback = "atは特定な場所を指すときに使います。政府（government）は具体的な政府機関ではないので、atは使いません。";
        return new FeedbackPair(responses, feedback, FeedbackPair.EXPLICIT);
    }

    private List<QuestionData> createFillInBlankMultipleChoiceQuestion2(QueryResult qr){
        String question = this.fillInBlankMultipleChoiceQuestion2(qr);
        String answer = fillInBlankMultipleChoiceAnswer2();
        List<String> choices = fillInBlankMultipleChoiceChoices2();
        List<FeedbackPair> allFeedback = new ArrayList<>(1);
        allFeedback.add(fillInBlankMultipleChoiceFeedback2());
        List<QuestionData> questionDataList = new ArrayList<>();
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(qr.personJP);
        data.setQuestionType(Question_FillInBlank_MultipleChoice.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(choices);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);
        data.setFeedback(allFeedback);

        questionDataList.add(data);

        return questionDataList;
    }

    private List<QuestionData> createTranslateQuestionGeneric(){
        String question = "政府";
        String answer = "government";
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(TOPIC_GENERIC_QUESTION);
        data.setQuestionType(Question_TranslateWord.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);

        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);

        return dataList;
    }

    private List<QuestionData> createSpellingQuestionGeneric(){
        String question = "政治家";
        String answer = "politician";
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(TOPIC_GENERIC_QUESTION);
        data.setQuestionType(Question_ChooseCorrectSpelling.QUESTION_TYPE);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(null);

        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);

        return dataList;
    }

    private List<QuestionData> createSpellingQuestionGeneric2(){
        String question = "政治家";
        String answer = "politician";
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(TOPIC_GENERIC_QUESTION);
        data.setQuestionType(Question_Spelling.QUESTION_TYPE);
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
        List<List<String>> set = new ArrayList<>(3);
        for (int index=1; index<=3; index++){
            List<String> questionIDs = new ArrayList<>(1);
            questionIDs.add(LessonGeneratorUtils.formatGenericQuestionID(KEY, index));
            set.add(questionIDs);
        }
        return set;
    }

    @Override
    protected List<QuestionData> getGenericQuestions(){
        List<QuestionData> toSaveSet1 = createTranslateQuestionGeneric();
        List<QuestionData> toSaveSet2 = createSpellingQuestionGeneric();
        List<QuestionData> toSaveSet3 = createSpellingQuestionGeneric2();
        List<QuestionData> questions = new ArrayList<>(3);
        questions.addAll(toSaveSet1);
        questions.addAll(toSaveSet2);
        questions.addAll(toSaveSet3);
        int questionSize = questions.size();
        for (int i=1; i<= questionSize; i++){
            String id = LessonGeneratorUtils.formatGenericQuestionID(KEY, i);
            questions.get(i-1).setId(id);
        }

        return questions;

    }
}
