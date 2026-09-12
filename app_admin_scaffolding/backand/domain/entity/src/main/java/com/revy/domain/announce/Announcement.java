package com.revy.domain.announce;


import com.revy.domain.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Announcement")
@NoArgsConstructor
//@Audited
public class Announcement extends BaseEntity {
    @Column(nullable = false, length = 200)
    private String title;

    @Lob
    @Column(nullable = false)
    private String body;

    public Announcement(String title, String body) {
        this.title = title;
        this.body = body;
    }
}
