package com.OmObe.OmO.MyCourse.service;

import com.OmObe.OmO.MyCourse.entity.MyCourse;
import com.OmObe.OmO.MyCourse.repository.MyCourseRepository;
import com.OmObe.OmO.exception.BusinessLogicException;
import com.OmObe.OmO.exception.ExceptionCode;
import com.OmObe.OmO.member.entity.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class MyCourseService {

    private final MyCourseRepository myCourseRepository;

    public MyCourseService(MyCourseRepository myCourseRepository) {
        this.myCourseRepository = myCourseRepository;
    }

    public MyCourse createCourse(List<MyCourse> course, Member writer){
        Collections.reverse(course);
        for(int i = 0; i<course.size(); i++){
            MyCourse part = course.get(i);
            part.setMember(writer);
            if(i<course.size()-1) {
                course.get(i + 1).setNextCourse(myCourseRepository.save(part));
            } else {
                myCourseRepository.save(part);
            }
        }
//        course.forEach(part -> part.setMember(writer));
        return course.get(course.size()-1);
    }


    public MyCourse updateCourse(List<MyCourse> course,long startId){
        log.info("enter 3-1");
        Collections.reverse(course);
        List<Long> courseIdList = new ArrayList<>();

        searchIdList(courseIdList,startId);
        Collections.reverse(courseIdList);
        courseIdList.forEach(id -> log.info("id : "+id));

        for(int i = 0; i<course.size(); i++){
            MyCourse part = findCourse(courseIdList.get(i));
            part.setPlaceName(course.get(i).getPlaceName());
            part.setPlaceId(course.get(i).getPlaceId());
            part.setTimes(course.get(i).getTimes());
            myCourseRepository.save(part);
        }
        log.info("passed 3-1");
        return findCourse(startId);
    }

    public MyCourse findCourse(long courseId){
        Optional<MyCourse> optionalCourse = myCourseRepository.findById(courseId);
        MyCourse course = optionalCourse.orElseThrow(() ->
                new BusinessLogicException(ExceptionCode.COURSE_NOT_FOUND));
        return course;
    }

    public void searchIdList(List<Long> courseIdList, long startId){
        MyCourse mc = findCourse(startId);
        if(mc.getNextCourse() != null){
            searchIdList(courseIdList,mc.getNextCourse().getCourseId());
        }
        courseIdList.add(startId);
    }
}
