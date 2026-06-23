package lol.ventura.misc.render;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class GLObject {
    @Getter @Setter(value = AccessLevel.PROTECTED)
    private int handle;
}
