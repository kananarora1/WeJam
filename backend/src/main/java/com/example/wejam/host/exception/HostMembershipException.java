package com.example.wejam.host.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

/** Every membership failure, with user-facing detail. */
public class HostMembershipException extends ErrorResponseException {

    private HostMembershipException(HttpStatus status, String detail) {
        super(status, ProblemDetail.forStatusAndDetail(status, detail), null);
    }

    public static HostMembershipException notAGroup() {
        return new HostMembershipException(HttpStatus.CONFLICT, "Only group profiles have members");
    }

    public static HostMembershipException noUserWithPhone() {
        return new HostMembershipException(HttpStatus.NOT_FOUND,
                "No WeJam user with this number. Ask them to sign up first.");
    }

    public static HostMembershipException cannotInviteYourself() {
        return new HostMembershipException(HttpStatus.BAD_REQUEST, "You're already the group's admin");
    }

    public static HostMembershipException groupFull(int maxPeople) {
        return new HostMembershipException(HttpStatus.CONFLICT,
                "A group can have at most " + maxPeople + " people, including pending invites");
    }

    public static HostMembershipException alreadyInvited() {
        return new HostMembershipException(HttpStatus.CONFLICT, "Already invited or already a member");
    }

    public static HostMembershipException inviteNotFound() {
        return new HostMembershipException(HttpStatus.NOT_FOUND, "Invite not found");
    }

    public static HostMembershipException memberNotFound() {
        return new HostMembershipException(HttpStatus.NOT_FOUND, "Member not found");
    }

    public static HostMembershipException membersExist() {
        return new HostMembershipException(HttpStatus.CONFLICT,
                "Remove all members and pending invites before switching to an individual profile");
    }
}
