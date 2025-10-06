package com.petd.tiktok_system_be.dto_v2.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CursorPageResponse<T> {
    List<T> data;
    boolean hasMore;
    String nextCursor;
    Long total;
}
