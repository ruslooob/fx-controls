package com.ruslooob.fxcontrols.controls;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdvancedDateTimeFilter extends AdvancedFilter<LocalDateTime> {
    //todo make stepper for time control
    // separate date and separate time
}
