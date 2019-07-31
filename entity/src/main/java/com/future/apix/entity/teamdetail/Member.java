package com.future.apix.entity.teamdetail;

import com.future.apix.entity.Mappable;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Member implements Mappable {

    String username;
    Boolean grant; 
    // if team 'public': grant 'YES'; if team 'private': grant 'NO' until accepted by team creator
    // REVISION -> if grant (false) => invitation by teamCreator not yet accepted by member

    @Override
    public boolean equals (Object obj){
        if (this == obj) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        if (!(obj instanceof Member)) return false;

        Member other = (Member) obj;
//        return this.getUsername() == ((Member) obj).getUsername();
//        return username == other.username && grant == other.grant;
//        return other.getUsername() == this.getUsername()
//                && other.getGrant() == this.getGrant();
//        System.out.println("FROM MEMBER username EQUAL: " + username.equals(other.username));
        return grant == other.grant && username.equals(other.username); // Jika pakai String harus pakai .equals bukan tanda "=="
    }

//  https://stackoverflow.com/questions/2265503/why-do-i-need-to-override-the-equals-and-hashcode-methods-in-java
    @Override
    public int hashCode(){
        final int prime = 31;
        int result = 17;
        result = prime * result + ((username == null) ? 0 : username.hashCode());
        return result;
    }
}
