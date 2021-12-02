package tw.com.softleader.data.jpa.spec;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "badges")
public class Badge {

  @Id
  @GeneratedValue
  Long id;

  String badgeType;
}
