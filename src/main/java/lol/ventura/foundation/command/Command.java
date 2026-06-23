package lol.ventura.foundation.command;

import lombok.Getter;

public abstract class Command{
    @Getter
    private String name;
    @Getter
    private String[] usages;

    public Command(String name, String... usages) {
        this.name = name;
        this.usages = usages;
    }

    public abstract void execute(String... args);
}
