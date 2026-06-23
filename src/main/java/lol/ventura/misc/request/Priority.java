package lol.ventura.misc.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Priority {
    VERY_IMPORTANT(30),
    IMPORTANT(20),
    NORMAL(0),
    NOT_IMPORTANT(-20);

    private final int priority;
}