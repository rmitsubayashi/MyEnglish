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
import com.linnca.pelicann.questions.QuestionUtils;
import com.linnca.pelicann.questions.Question_FillInBlank_Input;
import com.linnca.pelicann.questions.Question_TrueFalse;
import com.linnca.pelicann.userinterests.WikiDataEntryData;

import org.joda.time.LocalDate;
import org.joda.time.Years;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class NAME_is_AGE_years_old_NAME_is_a_GENDER extends Lesson {
    public static final String KEY = "NAME_is_AGE_years_old_NAME_is_a_GENDER";

    //since this is before learning numbers,
    //use digits instead of words

    private final List<QueryResult> queryResults = new ArrayList<>();
    private class QueryResult {
        private final String personID;
        private final String personNameEN;
        private final String personNameJP;
        private final String genderEN;
        private final String genderJP;
        private final boolean isMale;
        private final int age;
        private boolean singular;
        private final String birthday;

        private QueryResult(
                String personID,
                String personNameEN,
                String personNameJP,
                String gender,
                String birthdayString)
        {
            this.personID = personID;
            this.personNameEN = personNameEN;
            this.personNameJP = personNameJP;
            this.age = getAge(birthdayString);
            this.birthday = getBirthday(birthdayString);
            this.genderEN = getGenderEN(gender);
            this.genderJP = getGenderJP(gender);
            this.isMale = getMale(gender);
        }

        private int getAge(String birthdayString){
            birthdayString = birthdayString.substring(0, 10);
            LocalDate birthday = LocalDate.parse(birthdayString);
            LocalDate now = new LocalDate();
            Years age = Years.yearsBetween(birthday, now);
            int ageInt = age.getYears();
            if (ageInt == 1){
                singular = true;
            }
            return ageInt;
        }

        private String getBirthday(String birthdayString){
            birthdayString = birthdayString.substring(0, 10);
            LocalDate birthday = LocalDate.parse(birthdayString);
            DateTimeFormatter birthdayFormat = DateTimeFormat.forPattern("yyyy年M月d日");
            return birthdayFormat.print(birthday);
        }

        private boolean getMale(String genderID){
            switch (genderID){
                case "Q6581097":
                    return true;
                case "Q6581072":
                    return false;
                default:
                    return true;
            }
        }

        private String getGenderEN(String genderID){
            switch (genderID){
                case "Q6581097":
                    if (age >= 18) {
                        return "man";
                    } else {
                        return  "boy";
                    }
                case "Q6581072":
                    if (age >= 18) {
                        return "woman";
                    } else {
                        return "girl";
                    }
                default:
                    if (age >= 18) {
                        return "man/woman";
                    } else {
                        return "boy/girl";
                    }
            }
        }

        private String getGenderJP(String genderID){
            switch (genderID){
                case "Q6581097":
                    if (age >= 18) {
                        return "大人の男";
                    } else {
                        return "男の子";
                    }
                case "Q6581072":
                    if (age >= 18) {
                        return "大人の女";
                    } else {
                        return "女の子";
                    }
                default:
                    if (age >= 18) {
                        return "大人の男/大人の女";
                    } else {
                        return "男の子/女の子";
                    }
            }
        }
    }

    public NAME_is_AGE_years_old_NAME_is_a_GENDER(WikiBaseEndpointConnector connector, LessonListener listener){
        super(connector, listener);
        super.questionSetsLeftToPopulate = 2;
        super.categoryOfQuestion = WikiDataEntryData.CLASSIFICATION_PERSON;
        super.lessonKey = KEY;

    }

    @Override
    protected String getSPARQLQuery(){
        //find person with birthday and is alive
        return "SELECT ?personName ?personNameLabel ?personNameEN " +
                " ?gender ?birthday " +
                "WHERE " +
                "{" +
                "    {?personName wdt:P31 wd:Q5} UNION " + //is human
                "    {?personName wdt:P31 wd:Q15632617} ." + //or fictional human
                "    ?personName wdt:P569 ?birthday . " + //has a birthday
                "    ?personName wdt:P21 ?gender . " + //has an gender
                "    FILTER NOT EXISTS { ?personName wdt:P570 ?dateDeath } . " + //but not a death date
                "    ?personName rdfs:label ?personNameEN . " +
                "    FILTER (LANG(?personNameEN) = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " +
                "    SERVICE wikibase:label { bd:serviceParam wikibase:language '" +
                WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + "', '" + //JP label if possible
                WikiBaseEndpointConnector.ENGLISH + "'} . " + //fallback language is English
                "    BIND (wd:%s as ?personName) . " + //binding the ID of entity as ?person
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
            String personID = SPARQLDocumentParserHelper.findValueByNodeName(head, "personName");
            personID = LessonGeneratorUtils.stripWikidataID(personID);
            String personNameEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "personNameEN");
            String personNameJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "personNameLabel");
            String birthday = SPARQLDocumentParserHelper.findValueByNodeName(head, "birthday");
            String gender = SPARQLDocumentParserHelper.findValueByNodeName(head, "gender");
            gender = LessonGeneratorUtils.stripWikidataID(gender);
            QueryResult qr = new QueryResult(personID, personNameEN, personNameJP,gender, birthday);
            queryResults.add(qr);
        }
    }

    @Override
    protected int getQueryResultCt(){ return queryResults.size(); }

    @Override
    protected void createQuestionsFromResults(){
        for (QueryResult qr : queryResults){
            List<List<QuestionData>> questionSet = new ArrayList<>();

            List<QuestionData> fillInBlankQuestion = createFillInBlankQuestion(qr);
            questionSet.add(fillInBlankQuestion);

            List<QuestionData> fillInBlankQuestion2 = createFillInBlankQuestion2(qr);
            questionSet.add(fillInBlankQuestion2);

            List<QuestionData> trueFalseQuestion = createTrueFalseQuestion(qr);
            questionSet.add(trueFalseQuestion);

            super.newQuestions.add(new QuestionDataWrapper(questionSet, qr.personID, qr.personNameJP, null));
        }

    }

    private String fillInBlankQuestion(QueryResult qr){
        //one year old vs two years old
        String yearString = qr.singular ? "year" : "years";
        String sentence = qr.personNameEN + " is " +
                Question_FillInBlank_Input.FILL_IN_BLANK_NUMBER + " " + yearString + " old.";
        sentence = GrammarRules.uppercaseFirstLetterOfSentence(sentence);
        String sentence2 = "ヒント：" + qr.personNameJP + "の誕生日は" + qr.birthday + "です";

        return sentence + "\n\n" + sentence2;
    }

    private String fillInBlankAnswer(QueryResult qr){
        return Integer.toString(qr.age);
    }

    //allow a leeway
    private List<String> fillInBlankAlternateAnswer(QueryResult qr){
        List<String> leeway = new ArrayList<>(2);
        leeway.add(Integer.toString(qr.age + 1));
        if (qr.age != 0)
            leeway.add(Integer.toString(qr.age-1));
        return leeway;
    }

    private FeedbackPair fillInBlankFeedback(QueryResult qr){
        List<String> responses = fillInBlankAlternateAnswer(qr);
        String feedback = "正確には" + Integer.toString(qr.age) + "歳";
        return new FeedbackPair(responses, feedback, FeedbackPair.IMPLICIT);
    }

    private List<QuestionData> createFillInBlankQuestion(QueryResult qr){
        String question = this.fillInBlankQuestion(qr);
        String answer = fillInBlankAnswer(qr);
        List<String> acceptableAnswers = fillInBlankAlternateAnswer(qr);
        FeedbackPair feedbackPair = fillInBlankFeedback(qr);
        List<FeedbackPair> feedbackPairs = new ArrayList<>();
        feedbackPairs.add(feedbackPair);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(qr.personNameJP);
        data.setQuestionType(QuestionTypeMappings.FILL_IN_BLANK_INPUT);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(acceptableAnswers);

        data.setFeedback(feedbackPairs);

        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);

        return dataList;
    }

    private String fillInBlankQuestion2(QueryResult qr){
        String sentence = qr.personNameJP + "は" + Integer.toString(qr.age) + "歳です。";
        String sentence2 = qr.personNameEN + " is " + Integer.toString(qr.age) + " " +
                Question_FillInBlank_Input.FILL_IN_BLANK_TEXT + ".";
        sentence2 = GrammarRules.uppercaseFirstLetterOfSentence(sentence2);
        return sentence + "\n\n" + sentence2;
    }

    private String fillInBlankAnswer2(QueryResult qr){
        String yearString = qr.singular ? "year" : "years";
        return yearString + " old";
    }

    //allow either plural/singular
    private List<String> fillInBlankAlternateAnswer2(QueryResult qr){
        List<String> alternateAnswers = new ArrayList<>(1);
        String yearString = qr.singular ? "years" : "year";
        String answer = yearString + " old";
        alternateAnswers.add(answer);
        return alternateAnswers;
    }

    private List<QuestionData> createFillInBlankQuestion2(QueryResult qr){
        String question = this.fillInBlankQuestion2(qr);
        String answer = fillInBlankAnswer2(qr);
        List<String> alternateAnswers = fillInBlankAlternateAnswer2(qr);
        QuestionData data = new QuestionData();
        data.setId("");
        data.setLessonId(lessonKey);
        data.setTopic(qr.personNameJP);
        data.setQuestionType(QuestionTypeMappings.FILL_IN_BLANK_INPUT);
        data.setQuestion(question);
        data.setChoices(null);
        data.setAnswer(answer);
        data.setAcceptableAnswers(alternateAnswers);


        List<QuestionData> dataList = new ArrayList<>();
        dataList.add(data);

        return dataList;
    }

    private String getTrueFalseLabel(boolean isAdult, boolean isMale){
        if (isAdult){
            if (isMale){
                return "man";
            } else {
                return "woman";
            }
        } else {
            if (isMale){
                return "boy";
            } else {
                return "girl";
            }
        }
    }

    private FeedbackPair getTrueFalseFeedback(String response, QueryResult qr, boolean isAdult, boolean isMale){
        String isAdultString = isAdult ? "大人" : "子供";
        String isMaleString = isMale ? "男性" : "女性";
        String feedback = isAdultString + "の" + isMaleString + "なので" + qr.genderJP + "です";
        List<String> responses = new ArrayList<>(1);
        responses.add(response);
        return new FeedbackPair(responses, feedback, FeedbackPair.EXPLICIT);
    }


    private String trueFalseQuestion(QueryResult qr, boolean isAdult, boolean isMale){
        //one year old vs two years old
        String yearString = qr.singular ? "year" : "years";
        String sentence = qr.personNameEN + " is " + Integer.toString(qr.age) + " " + yearString + " old.";
        sentence = GrammarRules.uppercaseFirstLetterOfSentence(sentence);
        String sentence2 = qr.personNameEN + " is a " +
                getTrueFalseLabel(isAdult, isMale) + ".";
        sentence2 = GrammarRules.uppercaseFirstLetterOfSentence(sentence2);
        return sentence + "\n" + sentence2;
    }

    private List<String> trueFalseAlternateAnswers(QueryResult qr){
        if (qr.genderEN.equals("man/woman") || qr.genderJP.equals("boy/girl")){
            List<String> answers = new ArrayList<>(2);
            answers.add(Question_TrueFalse.TRUE_FALSE_QUESTION_TRUE);
            answers.add(Question_TrueFalse.TRUE_FALSE_QUESTION_FALSE);
            return answers;
        }
        return null;
    }

    private List<QuestionData> createTrueFalseQuestion(QueryResult qr){
        List<QuestionData> dataList = new ArrayList<>();
        for (int i=0; i<4; i++) {
            boolean isMale = i > 2;
            boolean isAdult = i % 2 == 0;
            String question = this.trueFalseQuestion(qr, isAdult, isMale);
            boolean isTrue = true;
            if (isMale != qr.isMale)
                isTrue = false;
            if (isAdult != qr.age >= 18){
                isTrue = false;
            }
            String answer = Question_TrueFalse.getTrueFalseString(isTrue);
            FeedbackPair feedbackPair = getTrueFalseFeedback(
                    Question_TrueFalse.getTrueFalseString(!isTrue), qr,
                    isAdult, isMale
            );
            List<FeedbackPair> feedbackPairs = new ArrayList<>(1);
            feedbackPairs.add(feedbackPair);
            List<String> acceptableAnswers = trueFalseAlternateAnswers(qr);
            QuestionData data = new QuestionData();
            data.setId("");
            data.setLessonId(lessonKey);
            data.setTopic(qr.personNameJP);
            data.setQuestionType(QuestionTypeMappings.TRUE_FALSE);
            data.setQuestion(question);
            data.setChoices(null);
            data.setAnswer(answer);
            data.setAcceptableAnswers(acceptableAnswers);
            data.setFeedback(feedbackPairs);

            dataList.add(data);
        }


        return dataList;
    }

}