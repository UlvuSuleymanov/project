package com.camaat.first.repository;

 import com.camaat.first.entity.university.Speciality;
 import org.springframework.data.jpa.repository.JpaRepository;
 import org.springframework.data.jpa.repository.Query;
 import org.springframework.stereotype.Repository;

 import java.util.List;
 import java.util.Optional;


@Repository
public interface SpecialityRepository extends JpaRepository<Speciality,Long> {
   @Query("SELECT s FROM Speciality s WHERE s.university.id = :id and s.specialityGroup = :group")
   List<Speciality> getSpeciality(Long id, Long group);

   Optional<Speciality> findBySpecialityCode(Long code);
}
