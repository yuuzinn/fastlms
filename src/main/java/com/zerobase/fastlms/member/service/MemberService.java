package com.zerobase.fastlms.member.service;

import com.zerobase.fastlms.admin.dto.LoginHistoryDto;
import com.zerobase.fastlms.admin.dto.MemberDto;
import com.zerobase.fastlms.admin.model.MemberParam;
import com.zerobase.fastlms.member.entity.LoginHistory;
import com.zerobase.fastlms.member.entity.Member;
import com.zerobase.fastlms.member.model.MemberInput;
import com.zerobase.fastlms.member.model.ResetPasswordInput;
import org.springframework.security.core.userdetails.UserDetailsService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface MemberService extends UserDetailsService {

//    boolean login(HttpServletRequest request, MemberInput parameter);

    boolean login(String userId, String userAgent, String ipAddress);
    boolean register(MemberInput parameter);

    /**
     * uuid에 해당하는 계정을 활성화함.
     * @param uuid
     */
    boolean emailAuth(String uuid);

    /**
     * 입력한 이메일로 비밀번호 초기화 정보를 전송
     */
    boolean sendResetPassword(ResetPasswordInput parameter);

    /**
     *입력받은 uuid에 대해서 password로 초기화 함
     */
    boolean resetPassword(String uuid, String password);

    /**
     *입력받은 uuid값이 유효한지 확인
     */
    boolean checkResetPassword(String uuid);

    /**
     * 회원 목록 리턴(관리자에서만 사용가능)
     */
    List<MemberDto> list(MemberParam parameter);


    /**
     * 회원 상세 정보
     */
    MemberDto detail(String userId);

//    List<LoginHistoryDto> logList();

}
