package io.example.payment;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Payments extends CrudRepository<Payment, Long> {
}
