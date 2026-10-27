package com.dailystudy.backend.repository;
 
import com.dailystudy.backend.model.Curtida;
import org.springframework.data.mongodb.repository.MongoRepository;
 
import java.util.List;
import java.util.Optional;
 
public interface CurtidaRepository extends MongoRepository<Curtida, String>, CurtidaRepositoryCustom {
 
    long countByPostId(String postId);
 
    Optional<Curtida> findByPostIdAndAutorId(String postId, Long autorId);
 
    // ✅ NOVO MÉTODO - Busca curtidas do usuário em vários posts
    List<Curtida> findAllByPostIdInAndAutorId(List<String> postIds, Long autorId);
}
 