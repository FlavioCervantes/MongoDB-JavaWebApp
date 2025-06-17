package application.model;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PrescriptionRepository extends MongoRepository<Prescription, Integer> {
  List<Prescription> findByPatient_id(int patientID);
}
