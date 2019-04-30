package com.future.apix.entity.teamdetail;

import com.future.apix.entity.Mappable;
import lombok.Data;

@Data
public class Member implements Mappable {

    String username;
    Boolean grant; 
    // if team 'public': grant 'YES'; if team 'private': grant 'NO' until accepted by team creator
    // REVISION -> if grant (false) => invitation by teamCreator not yet accepted by member
}
