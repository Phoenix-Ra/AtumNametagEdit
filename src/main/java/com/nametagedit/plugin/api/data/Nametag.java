package com.nametagedit.plugin.api.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Nametag {
    private String prefix;
    private String suffix;
    @Getter
    @Setter
    private boolean visible = true;

    public Nametag(String prefix, String suffix) {
        this.prefix = prefix;
        this.suffix = suffix;
    }
}