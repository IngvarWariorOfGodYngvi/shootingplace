package com.shootingplace.shootingplace.domain;

import com.shootingplace.shootingplace.address.Address;
import com.shootingplace.shootingplace.member.Member;

public class MemberWithAddress {
    Member member;
    Address address;

    public Member getMember() {
        return member;
    }

    public Address getAddress() {
        return address;
    }
}
