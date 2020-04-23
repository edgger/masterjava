package ru.javaops.masterjava.persist.model;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class City {

    @NonNull
    private String id;

    @NonNull
    private String name;
}
