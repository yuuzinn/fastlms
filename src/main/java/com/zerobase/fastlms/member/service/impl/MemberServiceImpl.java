package com.zerobase.fastlms.member.service.impl;

import com.zerobase.fastlms.components.MailComponents;
import com.zerobase.fastlms.member.entity.Member;
import com.zerobase.fastlms.member.exception.MemberNotEmailAuthException;
import com.zerobase.fastlms.member.model.MemberInput;
import com.zerobase.fastlms.member.repository.MemberRepository;
import com.zerobase.fastlms.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final MailComponents mailComponents;

    /**
     * 화원 가입 함수
     */
    @Override
    public boolean register(MemberInput parameter) {

        Optional<Member> optionalMember = memberRepository.findById(parameter.getUserId());
        if (optionalMember.isPresent()) {
            // 현재 userId에 해당하는 데이터가 존재
            return false;
        }

        /**
         * 비밀번호가 1111인데 그대로 저장이 되지 않는다.
         * 왜 ?
         *
         *  @Bean
         *  PasswordEncoder getPasswordEncoder() {
         *      return new BCryptPasswordEncoder();
         *  }
         *
         *  회원 가입을 할 때 1111이 아닌 새로운 암호로 들어가버려서.. 그래서  encPassword 객체로 따로 빼서
         *  BCrypt.hashpw(parameter.getPassword(), BCrypt.gensalt()); 함수들로 묶어서 객체에 담고
         *  build 패턴에 password 부분을 encPassword 로 채워 넣으면 되는 것.
         *
         *  실제로 그렇게 하게 될 경우, DB에는 1111이 아니라 여러 난수로 보여지게 됨.
         *  ex)$2a$10$RpqziTOfa3d8VtroIZdKH.wSE4KuwzEmlQsuKzYAM0HOCw6feFeja 로.. 써짐..
         */
        String encPassword = BCrypt.hashpw(parameter.getPassword(), BCrypt.gensalt());
        String uuid = UUID.randomUUID().toString();

        Member member = Member.builder()
                .userId(parameter.getUserId())
                .userName(parameter.getUserName())
                .phone(parameter.getPhone())
                .password(encPassword)
                .regDt(LocalDateTime.now())
                .emailAuthYn(false)
                .emailAuthKey(uuid)
                .build(); // builder pattern.. 왜 씀? -> 밑의 로직을 지금처럼 더더욱 가독성 있게 쓰려고. (보기 편함)
        /*
        Member member = new Member();
        member.setUserId(parameter.getUserId());
        member.setUserName(parameter.getUserName());
        member.setPhone(parameter.getPhone());
        member.setPassword(parameter.getPassword());
        member.setRegDt(LocalDateTime.now());
        member.setEmailAuthYn(false);
        member.setEmailAuthKey(uuid);
         */
        memberRepository.save(member);


        /**
         * DB에 저장해서 필요한 읽어오는 부분, EMAIL 관리자 페이지에서 MAIL 수정이라는 부분을 통해 수정하는 부분이 있음.
         * 과제를 통해 올 가능성..농후
         */
        String email = parameter.getUserId();
        String subject = "fastlms 사이트에 가입을 축하드립니다.";
        String text = "<p>fastlms 사이트 가입을 축하드립니다.<p><p>아래 링크를 클릭하셔서 가입을 완료하세요.</p>"
                + "<div><a target='_blank' href='http://localhost:8080/member/email-auth?id=" + uuid + " '> 가입 완료 </a></div>";
        mailComponents.sendMail(email, subject, text);

        return true;
    }

    @Override
    public boolean emailAuth(String uuid) {

        Optional<Member> optionalMember = memberRepository.findByEmailAuthKey(uuid);
        if (!optionalMember.isPresent()) {
            return false;
        }

        Member member =optionalMember.get();
        member.setEmailAuthYn(true);
        member.setEmailAuthDt(LocalDateTime.now());
        memberRepository.save(member);
        return true;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException { //username == email

        Optional<Member> optionalMember = memberRepository.findById(username);
        if (!optionalMember.isPresent()) {
            throw new UsernameNotFoundException("회원 정보가 존재하지 않습니다.");
        }

        Member member =optionalMember.get();

        if (!member.isEmailAuthYn()) {
            throw new MemberNotEmailAuthException("이메일을 활성화 이후에 로그인을 해 주세요.");
        }

        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();

        grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        return new User(member.getUserId(), member.getPassword(), grantedAuthorities);
    }
}