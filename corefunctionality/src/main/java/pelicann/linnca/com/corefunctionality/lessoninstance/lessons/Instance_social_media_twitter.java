package pelicann.linnca.com.corefunctionality.lessoninstance.lessons;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

import pelicann.linnca.com.corefunctionality.connectors.SPARQLDocumentParserHelper;
import pelicann.linnca.com.corefunctionality.connectors.WikiBaseEndpointConnector;
import pelicann.linnca.com.corefunctionality.connectors.WikiDataSPARQLConnector;
import pelicann.linnca.com.corefunctionality.lesson.lessons.Social_media_twitter;
import pelicann.linnca.com.corefunctionality.lessoninstance.EntityPropertyData;
import pelicann.linnca.com.corefunctionality.lessoninstance.InstanceGenerator;
import pelicann.linnca.com.corefunctionality.lessoninstance.Translation;
import pelicann.linnca.com.corefunctionality.userinterests.WikiDataEntity;

public class Instance_social_media_twitter extends InstanceGenerator {
    public Instance_social_media_twitter(){
        super();
        super.uniqueEntities = 1;
        super.lessonKey = Social_media_twitter.KEY;
    }

    @Override
    protected String getSPARQLQuery(){
        return "SELECT DISTINCT ?person ?personLabel ?personENLabel " +
                " ?twitterUsername " +
                " ?picLabel " +
                "WHERE " +
                "{" +
                "    ?person   wdt:P2002   ?twitterUsername; " +
                "              rdfs:label ?personENLabel . " +
                "    OPTIONAL { ?person    wdt:P18    ?pic } . " + //image if possible
                "    FILTER (LANG(?personENLabel) = '" +
                WikiBaseEndpointConnector.ENGLISH + "') . " +
                "    SERVICE wikibase:label { bd:serviceParam wikibase:language '" +
                WikiBaseEndpointConnector.LANGUAGE_PLACEHOLDER + "', '" + //JP label if possible
                WikiBaseEndpointConnector.ENGLISH + "'} . " + //fallback language is English
                "    BIND (wd:%s as ?person) . " + //binding the ID of entity as ?company
                "} ";

    }

    @Override
    protected synchronized void processResultsIntoEntityPropertyData(Document document){
        NodeList allResults = document.getElementsByTagName(
                WikiDataSPARQLConnector.RESULT_TAG
        );
        int resultLength = allResults.getLength();
        for (int i=0; i<resultLength; i++){
            Node head = allResults.item(i);
            String personID = SPARQLDocumentParserHelper.findValueByNodeName(head, "person");
            personID = WikiDataEntity.getWikiDataIDFromReturnedResult(personID);
            String personEN = SPARQLDocumentParserHelper.findValueByNodeName(head, "personENLabel");
            String personJP = SPARQLDocumentParserHelper.findValueByNodeName(head, "personLabel");
            Translation personTranslation = new Translation(personEN, personJP);
            String twitterUsername = SPARQLDocumentParserHelper.findValueByNodeName(head, "twitterUsername");
            Translation twitterTranslation = new Translation(twitterUsername, twitterUsername);
            List<Translation> properties = new ArrayList<>();
            properties.add(personTranslation);
            properties.add(twitterTranslation);
            EntityPropertyData entityPropertyData = new EntityPropertyData();
            entityPropertyData.setLessonKey(lessonKey);
            entityPropertyData.setWikidataID(personID);
            entityPropertyData.setProperties(properties);

            String pic = SPARQLDocumentParserHelper.findValueByNodeName(head, "picLabel");
            if (pic != null && !pic.equals("")){
                pic = WikiDataSPARQLConnector.cleanImageURL(pic);
            }
            entityPropertyData.setImageURL(pic);

            newEntityPropertyData.add(entityPropertyData);
        }
    }
}
