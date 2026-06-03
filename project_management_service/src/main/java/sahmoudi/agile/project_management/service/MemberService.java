package sahmoudi.agile.project_management.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sahmoudi.agile.project_management.client.UserServiceClient;
import sahmoudi.agile.project_management.dto.request.InviteMemberRequest;
import sahmoudi.agile.project_management.dto.response.MemberResponse;
import sahmoudi.agile.project_management.dto.response.UserResponse;
import sahmoudi.agile.project_management.exception.*;
import sahmoudi.agile.project_management.model.Project;
import sahmoudi.agile.project_management.model.ProjectMember;
import sahmoudi.agile.project_management.model.ProjectMemberId;
import sahmoudi.agile.project_management.repository.ProjectMemberRepository;
import sahmoudi.agile.project_management.repository.ProjectRepository;

import java.util.*;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository memberRepository;
    private final UserServiceClient userServiceClient;
    private final sahmoudi.agile.project_management.event.publisher.EventPublisher eventPublisher;

    private void requireRole(String callerRole, String... allowedRoles) {
        if (!Set.of(allowedRoles).contains(callerRole)) {
            throw new AccessDeniedException();
        }
    }

    private void requireMembership(UUID projectId, UUID callerId, String callerRole) {
        if ("ADMIN".equals(callerRole)) return;
        if (!memberRepository.existsByProjectIdAndUserId(projectId, callerId)) {
            throw new AccessDeniedException();
        }
    }

    @Transactional
    public MemberResponse inviteMember(UUID projectId, InviteMemberRequest request, UUID callerId, String callerRole) {
        requireRole(callerRole, "ADMIN", "PO");

        projectRepository.findById(projectId)
            .orElseThrow(() -> new ProjectNotFoundException(projectId));
        requireMembership(projectId, callerId, callerRole);

        UserResponse user;
        if (request.isNew()) {
            if (request.password() == null || request.firstName() == null || request.lastName() == null) {
                throw new MemberRegistrationFailedException("Missing required fields for new user registration");
            }
            try {
                user = userServiceClient.registerUser(request.email(), request.password(), request.firstName(), request.lastName(), request.role());
            } catch (Exception e) {
                throw new MemberRegistrationFailedException("Could not register user: " + e.getMessage());
            }
        } else {
            try {
                user = userServiceClient.getUserByEmail(request.email(), callerId, callerRole);
            } catch (UserNotFoundException e) {
                throw new ExistingUserNotFoundException(request.email());
            }
        }

        // Check not already member
        if (memberRepository.existsByProjectIdAndUserId(projectId, user.id())) {
            throw new AlreadyMemberException(user.id(), projectId);
        }

        ProjectMember member = ProjectMember.builder()
            .projectId(projectId)
            .userId(user.id())
            .role(request.role())
            .build();
        member = memberRepository.save(member);

        eventPublisher.publishMemberInvited(new sahmoudi.agile.project_management.event.payload.MemberInvitedEvent(
            "MEMBER_INVITED", user.id(), projectId, request.role(), java.time.Instant.now()
        ));



        return new MemberResponse(
            user.id(), user.email(), user.firstName(), user.lastName(),
            request.role(), member.getJoinedAt()
        );
    }

    public List<MemberResponse> listMembers(UUID projectId, UUID callerId, String callerRole) {
        projectRepository.findById(projectId)
            .orElseThrow(() -> new ProjectNotFoundException(projectId));
        requireMembership(projectId, callerId, callerRole);

        List<ProjectMember> members = memberRepository.findByProjectId(projectId);
        return members.stream().map(m -> {
            UserResponse user = userServiceClient.getUserById(m.getUserId(), callerId, callerRole);
            return new MemberResponse(
                user.id(), user.email(), user.firstName(), user.lastName(),
                m.getRole(), m.getJoinedAt()
            );
        }).toList();
    }

    @Transactional
    public void removeMember(UUID projectId, UUID userId, UUID callerId, String callerRole) {
        requireRole(callerRole, "ADMIN", "PO");

        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new ProjectNotFoundException(projectId));
        requireMembership(projectId, callerId, callerRole);

        ProjectMemberId memberId = new ProjectMemberId(projectId, userId);
        if (!memberRepository.existsById(memberId)) {
            throw new MemberNotFoundException(userId, projectId);
        }

        // Cannot remove self if creator
        if (callerId.equals(userId) && project.getCreatedBy().equals(callerId)) {
            throw new IllegalArgumentException("Cannot remove the project creator");
        }

        memberRepository.deleteById(memberId);

        eventPublisher.publishMemberRemoved(new sahmoudi.agile.project_management.event.payload.MemberRemovedEvent(
            "MEMBER_REMOVED", userId, projectId, java.time.Instant.now()
        ));
    }
}
