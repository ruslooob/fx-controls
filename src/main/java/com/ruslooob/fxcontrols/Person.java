package com.ruslooob.fxcontrols;

import javafx.beans.property.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Class for demonstration purposes
 */
public class Person {
    private final StringProperty firstName;
    private final StringProperty lastName;
    private final IntegerProperty height;
    private final ObjectProperty<LocalDate> dateOfBirth;
    private final BooleanProperty isEmployed;
    private final StringProperty gender;
    private final ObjectProperty<LocalTime> createdAt;

    public Person(String firstName,
                  String lastName,
                  Integer height,
                  LocalDate dateOfBirth,
                  boolean isEmployed,
                  String gender,
                  LocalTime createdAt) {
        this.firstName = new SimpleStringProperty(firstName);
        this.lastName = new SimpleStringProperty(lastName);
        this.height = new SimpleIntegerProperty(height);
        this.dateOfBirth = new SimpleObjectProperty<>(dateOfBirth);
        this.isEmployed = new SimpleBooleanProperty(isEmployed);
        this.gender = new SimpleStringProperty(gender);
        this.createdAt = new SimpleObjectProperty<>(createdAt);
    }

    public StringProperty firstNameProperty() {
        return firstName;
    }

    public String getFirstName() {
        return firstName.get();
    }

    public StringProperty lastNameProperty() {
        return lastName;
    }

    public String getLastName() {
        return lastName.get();
    }

    public int getHeight() {
        return height.get();
    }

    public IntegerProperty heightProperty() {
        return height;
    }

    public ObjectProperty<LocalDate> dateOfBirthProperty() {
        return dateOfBirth;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth.get();
    }

    public boolean isEmployed() {
        return isEmployed.get();
    }

    public BooleanProperty isEmployedProperty() {
        return isEmployed;
    }

    public StringProperty genderProperty() {
        return gender;
    }

    public String getGender() {
        return gender.get();
    }

    public ObjectProperty<LocalTime> createdAtProperty() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Person person = (Person) o;
        return Objects.equals(firstName, person.firstName) && Objects.equals(lastName, person.lastName) && Objects.equals(dateOfBirth, person.dateOfBirth);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, dateOfBirth);
    }

    @Override
    public String toString() {
        return "Person{" +
                "firstName=" + firstName +
                ", lastName=" + lastName +
                ", dateOfBirth=" + dateOfBirth +
                '}';
    }
}