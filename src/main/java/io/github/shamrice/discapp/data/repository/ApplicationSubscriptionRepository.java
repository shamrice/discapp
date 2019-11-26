package io.github.shamrice.discapp.data.repository;

import io.github.shamrice.discapp.data.model.ApplicationSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationSubscriptionRepository extends JpaRepository<ApplicationSubscription, Long> {

    ApplicationSubscription findByApplicationIdAndSubscriberEmail(Long applicationId, String subscriberEmail);
}
