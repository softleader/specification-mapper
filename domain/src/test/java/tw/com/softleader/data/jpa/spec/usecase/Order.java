package tw.com.softleader.data.jpa.spec.usecase;

import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "orders")
public class Order {

  @Id
  @GeneratedValue
  Long id;

  String itemName;

  @Singular
  @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  Set<Tag> tags;
}
