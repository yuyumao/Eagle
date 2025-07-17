package com.eagle.pojo;

import jakarta.persistence.*;

@Table(name = "Eagle_User")
@Entity
public class User {

    @Id
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;
    private String userName;
    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                '}';
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
