package ru.javaops.masterjava.persist.model;

import com.bertoncelj.jdbi.entitymapper.Column;
import lombok.*;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class User extends BaseEntity {

    @NonNull
    @Column("full_name")
    private String fullName;

    @NonNull
    private String email;

    @NonNull
    private UserFlag flag;

    @Column("city_id")
    private String cityId;

    public User(Integer id, String fullName, String email, UserFlag flag, String cityId) {
        this(fullName, email, flag, cityId);
        this.id = id;
    }

    public User(String fullName, String email, UserFlag flag, String cityId) {
        this.fullName = fullName;
        this.email = email;
        this.flag = flag;
        this.cityId = cityId;
    }
}