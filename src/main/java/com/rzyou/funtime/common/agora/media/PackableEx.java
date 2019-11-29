package com.rzyou.funtime.common.agora.media;

public interface PackableEx extends Packable {
    void unmarshal(ByteBuf in);
}
