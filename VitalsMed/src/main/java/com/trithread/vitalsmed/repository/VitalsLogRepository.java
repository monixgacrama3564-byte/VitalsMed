public interface VitalsLogRepository extends JpaRepository<VitalsLog, Long> {
    List<VitalsLog> findByPatientId(Long patientId);
}