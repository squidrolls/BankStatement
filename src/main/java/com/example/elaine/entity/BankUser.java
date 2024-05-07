package com.example.elaine.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.GenerationType.SEQUENCE;

@Entity(name = "BankUser")
@Table(
        name = "bank_user",
        uniqueConstraints = {@UniqueConstraint(name = "email_unique", columnNames = "email")}
)
public class BankUser {
    @Id
    @SequenceGenerator(
            name = "user_id_sequence",
            sequenceName = "user_id_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = SEQUENCE,
            generator = "user_id_sequence"
    )
    @Column(
            name = "id",
            updatable = false
    )
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

    @NotBlank(message = "password must be not empty")
    @Column(name = "password", nullable = false)
    private String password;  // Consider hashing this

    @NotBlank(message = "address must be not empty")
    @Column(name = "address", nullable = false, columnDefinition = "TEXT")
    private String address;

    //when remove the user, keep its accounts
    @OneToMany(mappedBy = "bankUser", cascade = {CascadeType.PERSIST}, fetch= FetchType.LAZY)
    private List<Account> accounts = new ArrayList<>();

    public void addAccounts(Account account){
        if(!accounts.contains(account)){
            accounts.add(account);
            account.setUser(this);
        }
    }

    public void removeAccounts(Account account){
        if(accounts.contains(account)){
            accounts.remove(account);
            account.setUser(null);
        }
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }



}
