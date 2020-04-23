package ru.javaops.masterjava.persist.model;

import com.bertoncelj.jdbi.entitymapper.Column;
import lombok.*;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Group extends BaseEntity {

    @NonNull
    private String name;

    @NonNull
    private GroupStatus status;

    @Column("project_id")
    private Integer projectId;

    public Group(Integer id, String name, GroupStatus status, Integer projectId) {
        this(name, status, projectId);
        this.id = id;
    }

    public Group(String name, GroupStatus status, Integer projectId) {
        this.name = name;
        this.status = status;
        this.projectId = projectId;
    }
}
