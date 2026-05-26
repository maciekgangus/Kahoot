package pl.agh.zti.quiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.agh.zti.quiz.domain.Host;

import java.util.Optional;

public interface HostRepository extends JpaRepository<Host, Long> {
    Optional<Host> findByUsername(String username);
    boolean existsByUsername(String username);
}
