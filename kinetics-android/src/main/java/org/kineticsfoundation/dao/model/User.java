package org.kineticsfoundation.dao.model;

import java.util.Date;

/**
 * User POJO
 * Created with IntelliJ IDEA.
 * User: akaverin
 * Date: 5/23/13
 * Time: 3:54 PM
 */

//TODO: check to skip unneeded values!
public class User implements UniqueEntity {

    private Integer id;
    private String email;
    private String firstName;
    private String secondName;
    private Date birthday;
    private String UID;
    private Gender gender;

    /**
     * For UT only
     */
    public User(Integer id, String email, String name, String UID) {
        this.id = id;
        this.email = email;
        this.firstName = name;
        this.secondName = name;
        this.UID = UID;
        this.gender = Gender.FEMALE;
        birthday = new Date();
    }

    public User() {
    }

    public User(String email, String firstName, String secondName) {
        this.email = email;
        this.firstName = firstName;
        this.secondName = secondName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSecondName() {
        return secondName;
    }

    public void setSecondName(String secondName) {
        this.secondName = secondName;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", secondName='" + secondName + '\'' +
                ", birthday=" + birthday +
                ", UID='" + UID + '\'' +
                ", gender=" + gender +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (!UID.equals(user.UID)) return false;
        if (birthday != null ? !birthday.equals(user.birthday) : user.birthday != null) return false;
        if (!email.equals(user.email)) return false;
        if (firstName != null ? !firstName.equals(user.firstName) : user.firstName != null) return false;
        if (gender != user.gender) return false;
        if (!id.equals(user.id)) return false;
        if (secondName != null ? !secondName.equals(user.secondName) : user.secondName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + email.hashCode();
        result = 31 * result + (firstName != null ? firstName.hashCode() : 0);
        result = 31 * result + (secondName != null ? secondName.hashCode() : 0);
        result = 31 * result + (birthday != null ? birthday.hashCode() : 0);
        result = 31 * result + UID.hashCode();
        result = 31 * result + gender.hashCode();
        return result;
    }
}
