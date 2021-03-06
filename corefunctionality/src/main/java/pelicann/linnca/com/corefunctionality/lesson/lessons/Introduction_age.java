package pelicann.linnca.com.corefunctionality.lesson.lessons;

import pelicann.linnca.com.corefunctionality.lesson.Lesson;
import pelicann.linnca.com.corefunctionality.lessoninstance.lessons.Instance_introduction_age;
import pelicann.linnca.com.corefunctionality.lessonquestions.questions.Questions_introduction_age;
import pelicann.linnca.com.corefunctionality.lessonscript.scripts.Script_introduction_age;

/*
* 0. person
* 1. birthday //not age because age changes over time
* 2. pic
* 3. 1st name
* */

public class Introduction_age extends Lesson {
    public static final String KEY = "introductionAge";

    public Introduction_age(){
        this.instanceGenerator = new Instance_introduction_age();
        this.questionGenerator = new Questions_introduction_age();
        this.scriptGenerator = new Script_introduction_age();
    }
}
