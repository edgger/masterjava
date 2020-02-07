package ru.javaops.masterjava;

import java.util.Objects;

public class UserData {
    private String name;
    private String email;
    private boolean flag;

    public UserData(String name, String email, boolean flag) {
        this.name = name;
        this.email = email;
        this.flag = flag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserData)) return false;
        UserData that = (UserData) o;
        return flag == that.flag &&
                Objects.equals(name, that.name) &&
                Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, email, flag);
    }
}
