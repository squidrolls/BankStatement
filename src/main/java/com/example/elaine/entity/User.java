package com.example.elaine.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.GenerationType.SEQUENCE;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "User")
@Table(name = "_user", uniqueConstraints = {@UniqueConstraint(name = "email_unique", columnNames = "email")})
public class User {
    @Id
    @SequenceGenerator(name = "user_id_sequence", sequenceName = "user_id_sequence", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "user_id_sequence")
    @Column(name = "id", updatable = false)
    private Long id;

    @NotBlank(message = "first name must be not empty")
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @NotBlank(message = "last name must be not empty")
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Email
    @NotBlank(message = "email must be not empty")
    @Column(name = "email", nullable = false)
    private String email;

    //could access the password but could not read the password
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotBlank(message = "password must be not empty")
    @Column(name = "password", nullable = false)
    private String password;  // todo:Consider hashing this

    @NotBlank(message = "address must be not empty")
    @Column(name = "address", nullable = false, columnDefinition = "TEXT")
    private String address;

    //when remove the user, keep its accounts
    @OneToMany(mappedBy = "user", cascade = {CascadeType.PERSIST}, fetch= FetchType.LAZY)
    private List<Account> accounts = new ArrayList<>();

    public User(String firstName, String lastName, String email, String password, String address) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.address = address;
    }

//    public void addAccounts(Account account){
//        if(!accounts.contains(account)){
//            accounts.add(account);
//            account.setUser(this);
//        }
//    }
//
//    public void removeAccounts(Account account){
//        if(accounts.contains(account)){
//            accounts.remove(account);
//            account.setUser(null);
//        }
//    }
}
