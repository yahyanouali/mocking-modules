package io.reflectoring.booking.data;

import io.reflectoring.customer.data.Customer;
import lombok.Builder;
import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Builder
public class Booking {

  @Id
  @GeneratedValue
  private Long id;

  @ManyToOne
  private Customer customer;

  @Column
  private String flightNumber;

}
