package com.sadevolui.modules.category.entity;

import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import com.sadevolui.modules.user.entity.UserAccount;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "categories")
@Getter
@NoArgsConstructor
public class Category {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false, unique = true, length = 80)
    private String name;
    @Column(nullable = false, length = 500)
    private String description;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "owner_id")
    private UserAccount owner;

    public Category(UserAccount owner, String name, String description) {
        this.owner = owner;
        this.name = name.trim();
        this.description = description.trim();
    }

    public void update(String name, String description) { this.name = name.trim(); this.description = description.trim(); }
}
