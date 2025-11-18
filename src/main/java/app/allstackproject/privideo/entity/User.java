package app.allstackproject.privideo.entity;

import app.allstackproject.privideo.common.annotation.AgeTypeConstraint;
import app.allstackproject.privideo.common.enumStatus.GenderType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

@Entity
@Getter
@Table(name = "Users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {
    @Id
    @GeneratedValue
    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    @Email
    @Column(unique = true)
    private String email;

    @NotBlank
    @JsonProperty(access = Access.WRITE_ONLY)
    private String password;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(length = 100)
    private GenderType gender;

    @NotBlank
    @Column(length = 100)
    private String phoneNumber;

    @AgeTypeConstraint
    private int age;

    @Builder(access = AccessLevel.PRIVATE)
    private User(String name, String email, String password, GenderType gender,
                 String phoneNumber, int age) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
        this.age = age;
    }

    public static User create(String name, String email, String password, GenderType gender,
                              String phoneNumber, int age) {
        return User.builder()
                .name(name)
                .email(email)
                .password(password)
                .gender(gender)
                .phoneNumber(phoneNumber)
                .age(age)
                .build();
    }

    public User hashPassword(PasswordEncoder passwordEncoder) {
        this.password = passwordEncoder.encode(this.password);
        return this;
    }

    public boolean matchPassword(String rawPassword, PasswordEncoder passwordEncoder) {
        return passwordEncoder.matches(rawPassword, this.password);
    }

    public void changePassword(String newPassword, PasswordEncoder passwordEncoder) {
        this.password = passwordEncoder.encode(newPassword);
    }

    public void updateInfo(String NewPhoneNumber, GenderType newGender, int newAge) {
        this.phoneNumber = NewPhoneNumber;
        this.gender = newGender;
        this.age = newAge;
    }
}
