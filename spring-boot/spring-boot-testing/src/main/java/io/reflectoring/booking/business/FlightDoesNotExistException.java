package io.reflectoring.booking.business;

class FlightDoesNotExistException extends RuntimeException {

  FlightDoesNotExistException(String flightNumber) {
    super(String.format("A flight with ID '%s' doesn't exist!", flightNumber));
  }

}
