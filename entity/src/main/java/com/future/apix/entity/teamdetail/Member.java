package com.future.apix.entity.teamdetail;

import com.future.apix.entity.Mappable;
import lombok.Data;

@Data
public class Member implements Mappable {

    String username;
    String grant; // if team 'public': grant 'YES'; if team 'private': grant 'NO' until accepted by team creator
}
