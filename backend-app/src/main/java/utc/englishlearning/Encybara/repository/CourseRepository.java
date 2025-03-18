package utc.englishlearning.Encybara.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import utc.englishlearning.Encybara.domain.Course;
import utc.englishlearning.Encybara.util.constant.CourseStatusEnum;
import utc.englishlearning.Encybara.util.constant.CourseTypeEnum;
import utc.englishlearning.Encybara.util.constant.SpecialFieldEnum;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    Course findByName(String name);

    Page<Course> findByGroup(String group, Pageable pageable); // Support pagination for group query

    @Query("SELECT c FROM Course c WHERE " +
            "(:name IS NULL OR c.name LIKE %:name%) AND " +
            "(:diffLevel IS NULL OR c.diffLevel = :diffLevel) AND " +
            "(:recomLevel IS NULL OR c.recomLevel = :recomLevel) AND " +
            "(:courseType IS NULL OR c.courseType = :courseType) AND " +
            "(:speciField IS NULL OR c.speciField = :speciField) AND " +
            "(:group IS NULL OR c.group = :group) AND " +
            "(:courseStatus IS NULL OR c.courseStatus = :courseStatus)")
    Page<Course> findCoursesWithFilters(
            @Param("name") String name,
            @Param("diffLevel") Double diffLevel,
            @Param("recomLevel") Double recomLevel,
            @Param("courseType") CourseTypeEnum courseType,
            @Param("speciField") SpecialFieldEnum speciField,
            @Param("group") String group,
            @Param("courseStatus") CourseStatusEnum courseStatus,
            Pageable pageable);
}