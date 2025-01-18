package com.ruslooob.fxcontrols;

import javafx.beans.property.*;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Class for demonstration purposes
 */
@ToString
@EqualsAndHashCode
public class Person {
    private final LongProperty id;
    private final StringProperty firstName;
    private final StringProperty lastName;
    private final IntegerProperty height;
    private final ObjectProperty<LocalDate> dateOfBirth;
    private final BooleanProperty isEmployed;
    private final StringProperty gender;
    private final ObjectProperty<LocalTime> createdAt;
    private final StringProperty infoCol;

    public Person(Long id,
                  String firstName,
                  String lastName,
                  Integer height,
                  LocalDate dateOfBirth,
                  boolean isEmployed,
                  String gender,
                  LocalTime createdAt,
                  String info) {
        this.id = new SimpleLongProperty(id);
        this.firstName = new SimpleStringProperty(firstName);
        this.lastName = new SimpleStringProperty(lastName);
        this.height = new SimpleIntegerProperty(height);
        this.dateOfBirth = new SimpleObjectProperty<>(dateOfBirth);
        this.isEmployed = new SimpleBooleanProperty(isEmployed);
        this.gender = new SimpleStringProperty(gender);
        this.createdAt = new SimpleObjectProperty<>(createdAt);
        this.infoCol = new SimpleStringProperty(info);
    }

    public long getId() {
        return id.get();
    }

    public LongProperty idProperty() {
        return id;
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

    public String getInfoCol() {
        return infoCol.get();
    }

    public StringProperty infoColProperty() {
        return infoCol;
    }
}