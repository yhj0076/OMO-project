package com.OmObe.OmO.member.service;

import com.OmObe.OmO.auth.jwt.JwtTokenizer;
import com.OmObe.OmO.auth.utils.MemberAuthorityUtils;
import com.OmObe.OmO.exception.BusinessLogicException;
import com.OmObe.OmO.exception.ExceptionCode;
import com.OmObe.OmO.member.dto.MemberDto;
import com.OmObe.OmO.member.entity.Member;
import com.OmObe.OmO.member.mapper.MemberMapper;
import com.OmObe.OmO.member.repository.MemberRepository;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@Transactional
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final MemberAuthorityUtils authorityUtils;
    private final MemberMapper mapper;
    private final JwtTokenizer jwtTokenizer;

    public MemberService(MemberRepository memberRepository,
                         PasswordEncoder passwordEncoder,
                         MemberAuthorityUtils authorityUtils,
                         MemberMapper mapper,
                         JwtTokenizer jwtTokenizer) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.authorityUtils = authorityUtils;
        this.mapper = mapper;
        this.jwtTokenizer = jwtTokenizer;
    }

    /**
     * <회원 가입>
     * Spring Security와 jwt 구현 후 수정 예정
     * 1. 이메일 중복 확인
     * 2. 닉네임 중복 확인
     * 3. 회원 상태를 활동 중으로 설정
     * 4. 입력받은 생년월일 저장
     * 5. 패스워드 확인
     * 6. 패스워드 암호화
     * 7. 권한 db에 저장
     * 8. 1~7번의 절차가 모두 완료되면 회원 데이터 저장
     */
//    public Member createMember(MemberDto.Post post){
//        Member member = mapper.memberPostDtoToMember(post);
//        // 1. 이메일 중복 확인
//        verifyExistsEmail(member.getEmail());
//
//        // 2. 닉네임 중복 확인
//        verifyExistsNickname(member.getNickname());
//
//        // 3. 회원 상태를 활동 중으로 설정
//        member.setMemberStatus(Member.MemberStatus.MEMBER_ACTIVE);
//
//        // 4. 입력받은 생년월일 저장
//        member.setBirth(LocalDate.of(member.getBirthYear(), member.getBirthMonth(), member.getBirthDay()));
//
//        // 5. 패스워드 확인
//        String checkPassword = post.getCheckPassword();
//        verifyPassword(member.getPassword(), checkPassword);
////        log.info("password : {}", member.getPassword());
////        log.info("checkPassword : {}", post.getCheckPassword());
//
//        // 6. 패스워드 암호화
//        String encryptedPassword = passwordEncoder.encode(member.getPassword());
//        member.setPassword(encryptedPassword);
//
//        // 7. 권한 db에 저장
//        List<String> roles = authorityUtils.createRoles(member.getEmail());
//        member.setRoles(roles);
//
//        // 8. 1~7번의 절차가 모두 완료되면 회원 데이터 저장
//        Member savedMember = memberRepository.save(member);
//
//        return savedMember;
//    }

    /**
     * <소셜 로그인 후 추가 정보 입력>
     * 1. 존재하는 회원인지 확인
     * 2. 사용자의 로그인 인증 상태 검증
     * 3. 닉네임 중복 확인
     * 4. 사용자 생년월일, 닉네임, mbti, 성별 저장
     */
    public Member addInfo(Long memberId, MemberDto.Post post) {
        Member member = mapper.memberPostDtoToMember(post);

        // 1. 존재하는 회원인지 확인
        Member findMember = findVerifiedMember(memberId);

        // 2. 사용자의 로그인 인증 상태 검증
        verifiedAuthenticatedMember(memberId);

        // 3. 닉네임 중복 확인
        verifyExistsNickname(member.getNickname());

        // 4. 사용자 생년월일, 닉네임, mbti, 성별 저장
        // 4-1. 생년월일 저장
        findMember.setBirthYear(member.getBirthYear());
        findMember.setBirthMonth(member.getBirthMonth());
        findMember.setBirthDay(member.getBirthDay());
        findMember.setBirth(LocalDate.of(findMember.getBirthYear(), findMember.getBirthMonth(), findMember.getBirthDay()));

        // 4-2. 닉네임 저장
        findMember.setNickname(member.getNickname());

        // 3-3 mbti 저장
        findMember.setMbit(member.getMbit());

        // 3-4 성별 저장
        findMember.setGender(member.getGender());

        return memberRepository.save(findMember);
    }

    /**
     * <회원 탈퇴>
     * 1. 탈퇴하려는 회원이 존재하는 회원인지 검증
     * 2. 사용자의 로그인 인증 상태 검증
     * 3. 회원 정보 삭제
     */
    public void quitMember(Long memberId){
        // 1. 탈퇴하려는 회원이 존재하는 회원인지 검증
        Member findMember = findVerifiedMember(memberId);

        // 2. 사용자의 로그인 인증 상태 검증
        verifiedAuthenticatedMember(findMember.getMemberId());

        // 3. 회원 정보 삭제
        memberRepository.delete(findMember);
    }

    // 이메일 중복 검증 메서드
    public void verifyExistsEmail(String email){
        Optional<Member> member = memberRepository.findByEmail(email);

        if (member.isPresent()) {
            throw new BusinessLogicException(ExceptionCode.EMAIL_ALREADY_EXIST);
        }
    }

    // 닉네임 중복 검증 메서드
    public void verifyExistsNickname(String nickname){
        Optional<Member> member = memberRepository.findByNickname(nickname);

        if (member.isPresent()) {
            throw new BusinessLogicException(ExceptionCode.NICKNAME_ALREADY_EXIST);
        }
    }

    // 회원 존재 여부 검증 메서드 - memberId로 검증
    public Member findVerifiedMember(Long memberId){
        Optional<Member> optionalMember = memberRepository.findById(memberId);
        Member findMember = optionalMember.orElseThrow(() ->
                new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));

        return findMember;
    }

    // 회원 존재 여부 검증 메서드 - email로 검증
    public Member findVerifiedMemberByEmail(String email) {
        Optional<Member> optionalMember = memberRepository.findByEmail(email);
        Member findMember = optionalMember.orElseThrow(() ->
                new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));

        return findMember;
    }

    // 패스워드 확인 메서드
    public void verifyPassword(String password, String checkPassword) {
        if (!password.equals(checkPassword)) { // 기존 입력한 패스워드와 확인용 패스워드가 일치 하지 않을 경우 예외처리
            throw new BusinessLogicException(ExceptionCode.PASSWORD_NOT_CORRECT);
        }
    }

    // 사용자 로그인 인증 상태 검증 메서드
    public void verifiedAuthenticatedMember(Long memberId) {
        log.info("# Verified member's token");
        if (getHeader("Authorization") == null) { // Authorization의 헤더 값(액세스 토큰)이 없으면 예외처리
            throw new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND);
        }

        String jws = getHeader("Authorization").substring(7);
        log.info("jws : {}", jws);

        Long memberIdFromJws = getMemberIdFromJws(jws);
        log.info("memberIdFromJWs : {}", memberIdFromJws);
        Long memberIdFromRequest = memberId;
        log.info("memberIdFromRequest : {}", memberIdFromRequest);

        if (memberIdFromJws != memberIdFromRequest) { // 토큰을 통해 얻은 memberId의 값이 인증하고자 하는 회원의 memberId와 다른 경우 예외처리
            log.info("# INVALID TOKEN");
            throw new BusinessLogicException(ExceptionCode.INVALID_TOKEN);
        }
    }

    // 헤더 값 추출 메서드
    private String getHeader(String header) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        log.info("request.getHeader(header) : {}", request.getHeader(header));

        return request.getHeader(header);
    }

    // jws에서 memberId 추출하는 메서드
    private Long getMemberIdFromJws(String jws) {
        String base64EncodedSecretKey = jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey());
        Map<String, Object> claims = jwtTokenizer.getClaims(jws, base64EncodedSecretKey).getBody();

        log.info("memberId : {}", claims.get("memberId"));
        return Long.parseLong(claims.get("memberId").toString());
    }

    // 현재 로그인 중인 사용자 객체를 리턴하는 메서드
    public Member findLoggedInMember(String authorizationToken) {
        String accessToken = authorizationToken.substring(7);

        Long loggedInMemberId = getMemberIdFromJws(accessToken);

        return findVerifiedMember(loggedInMemberId);
    }
}
