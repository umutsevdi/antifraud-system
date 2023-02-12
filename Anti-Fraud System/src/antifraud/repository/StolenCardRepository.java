package antifraud.repository;

import antifraud.repository.entity.StolenCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StolenCardRepository extends JpaRepository<StolenCard, Long> {

    boolean existsById(Long id);

    boolean existsByNumber(String number);

    void deleteByNumber(String number);
}
