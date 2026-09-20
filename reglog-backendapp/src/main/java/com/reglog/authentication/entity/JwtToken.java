package com.reglog.authentication.entity;

import com.reglog.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "jwt_tokens")
public class JwtToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tid;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uid", nullable = false)
    private User user;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String token;

    @Column(name = "cat", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime cat;

    @Column(name = "eat", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime eat;

    protected JwtToken() {
    }

    public JwtToken(User user, String token, LocalDateTime cat, LocalDateTime eat) {
        this.user = user;
        this.token = token;
        this.cat = cat;
        this.eat = eat;
    }

    public Long getTid() {
        return tid;
    }

    public void setTid(Long tid) {
        this.tid = tid;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getCat() {
        return cat;
    }

    public void setCat(LocalDateTime cat) {
        this.cat = cat;
    }

    public LocalDateTime getEat() {
        return eat;
    }

    public void setEat(LocalDateTime eat) {
        this.eat = eat;
    }
}