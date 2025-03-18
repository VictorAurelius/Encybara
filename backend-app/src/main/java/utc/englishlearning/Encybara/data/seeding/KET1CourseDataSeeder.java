package utc.englishlearning.Encybara.data.seeding;

import org.springframework.stereotype.Service;
import utc.englishlearning.Encybara.data.loader.JsonDataLoader;
import utc.englishlearning.Encybara.domain.*;
import utc.englishlearning.Encybara.repository.*;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class KET1CourseDataSeeder {
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final QuestionRepository questionRepository;
    private final CourseLessonRepository courseLessonRepository;
    private final QuestionChoiceRepository questionChoiceRepository;
    private final LessonQuestionRepository lessonQuestionRepository;
    private final JsonDataLoader jsonDataLoader;
    private final ObjectMapper objectMapper;

    public KET1CourseDataSeeder(
            CourseRepository courseRepository,
            LessonRepository lessonRepository,
            QuestionRepository questionRepository,
            CourseLessonRepository courseLessonRepository,
            QuestionChoiceRepository questionChoiceRepository,
            LessonQuestionRepository lessonQuestionRepository,
            JsonDataLoader jsonDataLoader,
            ObjectMapper objectMapper) {
        this.courseRepository = courseRepository;
        this.lessonRepository = lessonRepository;
        this.questionRepository = questionRepository;
        this.courseLessonRepository = courseLessonRepository;
        this.questionChoiceRepository = questionChoiceRepository;
        this.lessonQuestionRepository = lessonQuestionRepository;
        this.jsonDataLoader = jsonDataLoader;
        this.objectMapper = objectMapper;
    }

    @SuppressWarnings("unchecked")
    public void seedCourse() {
        try {
            // Load all data first
            List<Course> courses = jsonDataLoader.loadCourses();
            Map<String, Lesson> lessonsByName = jsonDataLoader.loadLessons();
            Map<String, Question> questionsByContent = jsonDataLoader.loadQuestions();

            for (Course course : courses) {
                // Check if course already exists
                Course existingCourse = courseRepository.findByName(course.getName());
                if (existingCourse != null) {
                    System.out.println(">>> SKIP: " + course.getName() + " already exists");
                    continue;
                }

                System.out.println(">>> START SEEDING: " + course.getName());

                // Save course
                course.setCreateAt(Instant.now());
                course = courseRepository.save(course);

                // Process each lesson using the lessonNames from Course entity
                Map<String, Object> courseData = objectMapper.convertValue(course, Map.class);
                List<String> lessonNames = (List<String>) courseData.get("lessonNames");

                if (lessonNames != null) {
                    course.setLessonNames(lessonNames); // Set the lessonNames to the Course entity

                    for (String lessonName : lessonNames) {
                        Lesson lesson = lessonsByName.get(lessonName);
                        if (lesson == null) {
                            System.out.println(">>> WARNING: Lesson not found: " + lessonName);
                            continue;
                        }

                        // Save lesson
                        lesson.setCreateAt(Instant.now());
                        lesson = lessonRepository.save(lesson);

                        // Create course-lesson relationship
                        Course_Lesson courseLesson = new Course_Lesson();
                        courseLesson.setCourse(course);
                        courseLesson.setLesson(lesson);
                        courseLessonRepository.save(courseLesson);

                        // Process questions from lesson's questionContents
                        List<String> questionContents = lesson.getQuestionContents();
                        if (questionContents != null && !questionContents.isEmpty()) {
                            // Process each question for this lesson
                            for (String quesContent : questionContents) {
                                Question question = questionsByContent.get(quesContent);
                                if (question == null) {
                                    System.out.println(">>> WARNING: Question not found: " + quesContent);
                                    continue;
                                }

                                // Save question and its choices
                                for (Question_Choice choice : question.getQuestionChoices()) {
                                    choice.setQuestion(question);
                                }
                                question = questionRepository.save(question);

                                // Create lesson-question relationship
                                Lesson_Question lessonQuestion = new Lesson_Question();
                                lessonQuestion.setLesson(lesson);
                                lessonQuestion.setQuestion(question);
                                lessonQuestionRepository.save(lessonQuestion);

                                // Save question choices
                                for (Question_Choice choice : question.getQuestionChoices()) {
                                    questionChoiceRepository.save(choice);
                                }
                            }
                        }
                    }
                }

                System.out.println(">>> END SEEDING: " + course.getName());
            }
        } catch (IOException e) {
            System.err.println("Error seeding KET1 course data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}