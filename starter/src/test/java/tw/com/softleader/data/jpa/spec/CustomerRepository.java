package tw.com.softleader.data.jpa.spec;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tw.com.softleader.data.jpa.spec.repository.QueryBySpecExecutor;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long>,
    QueryBySpecExecutor<Customer> {

}
