package com.example.UnityTrustBank.ServiceImple;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.UnityTrustBank.Entity.*;
import com.example.UnityTrustBank.Enum.*;
import com.example.UnityTrustBank.Repository.*;
import com.example.UnityTrustBank.Service.*;
import com.example.UnityTrustBank.dto.*;

@Service
public class AccountApplicationServiceImpl
        implements AccountApplicationService {

    @Autowired private AccountApplicationRepo appRepo;
    @Autowired private BranchRepo branchRepo;
    @Autowired private UserRepo userRepo;
    @Autowired private RoleRepo roleRepo;
    @Autowired private CustomerProfileRepo customerProfileRepo;
    @Autowired private AccountRepo accountRepo;
    @Autowired private AccountSequenceService accountSequenceService;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private FileService fileService;
    @Autowired private EmailService emaiService;

    @Value("${file.kyc.path}")
    private String kycPath;

    @Override
    public AccountApplicationResponseDto apply(AccountApplicationCreateDto dto) {
    	if (!OtpStore.isVerified(dto.getEmail())) {
            throw new RuntimeException("Email not verified");
        }
        // Don't clear verified status - it's needed for application processing
        // OtpStore.clear(dto.getEmail()); // REMOVED

        if (dto.getDob() == null ||
            Period.between(dto.getDob(), LocalDate.now()).getYears() < 18) {
            throw new RuntimeException("Applicant must be 18+");
        }

        if (appRepo.existsByMobile(dto.getMobile()))
            throw new RuntimeException("Mobile already exists");

        if (appRepo.existsByEmail(dto.getEmail()))
            throw new RuntimeException("Email already exists");

        if (appRepo.existsByAadhaar(dto.getAadhaar()))
            throw new RuntimeException("Aadhaar already exists");

        if (appRepo.existsByPan(dto.getPan()))
            throw new RuntimeException("PAN already exists");
         if (appRepo.existsByAadhaarAndStatusIn(dto.getAadhaar(), 
                List.of(ApplicationStatus.SUBMITTED, ApplicationStatus.APPROVED))) {
            throw new RuntimeException("An active application or account already exists with this Aadhaar.");
        }

        if (appRepo.existsByMobileAndStatusIn(dto.getMobile(), 
                List.of(ApplicationStatus.SUBMITTED, ApplicationStatus.APPROVED))) {
            throw new RuntimeException("Mobile number already in use for an active application.");
        }

        Branch branch = branchRepo.findById(dto.getBranchId())
                .filter(Branch::isActive)
                .orElseThrow(() -> new RuntimeException("Invalid or inactive branch"));

        if (appRepo.existsByEmailAndStatus(
                dto.getEmail(), ApplicationStatus.SUBMITTED)) {
            throw new RuntimeException("Application already pending");
        }

        AccountApplication app = new AccountApplication();
        app.setFullName(dto.getFullName());
        app.setFatherName(dto.getFatherName());
        app.setEmail(dto.getEmail());
        app.setMobile(dto.getMobile());
        app.setDob(dto.getDob());
        app.setAadhaar(dto.getAadhaar());
        app.setPan(dto.getPan());
        app.setAddress(dto.getAddress());
        app.setAccountType(dto.getAccountType());
        app.setBranch(branch);
        app.setStatus(ApplicationStatus.SUBMITTED);
        app.setAppliedAt(LocalDateTime.now());

        AccountApplication saved = appRepo.save(app);

        emaiService.send(
            saved.getEmail(),
            "UnityTrust Bank – Application Received",
            "Dear " + saved.getFullName() + ",\n\n" +
            "Your account opening request has been successfully submitted.\n\n" +
            "Application ID: " + saved.getId() + "\n" +
            "Status: Under Review\n\n" +
            "Regards,\nUnityTrust Bank"
        );

        return toDto(saved);
    }

    @Override
    @Transactional
    public void uploadKycImages(
            Long applicationId,
            MultipartFile profileImage,
            MultipartFile aadhaarImage,
            MultipartFile panImage) {

        AccountApplication app = appRepo.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (app.getStatus() != ApplicationStatus.SUBMITTED) {
            throw new RuntimeException("Cannot upload KYC after approval");
        }

        String profilePath =
                fileService.uploadfile(profileImage, kycPath + "/profile");

        String aadhaarPath =
                fileService.uploadfile(aadhaarImage, kycPath + "/aadhaar");

        String panPath =
                fileService.uploadfile(panImage, kycPath + "/pan");

        app.setProfileImagePath(profilePath);
        app.setAadhaarImagePath(aadhaarPath);
        app.setPanImagePath(panPath);

        appRepo.save(app);
    }

    @Override
    @Transactional
    public void approve(Long applicationId) {

        User admin = getCurrentAdmin();

        AccountApplication app = appRepo.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (app.getStatus() != ApplicationStatus.SUBMITTED)
            throw new RuntimeException("Application already processed");

        // Admin can only approve applications from their own branch
        // Manager can approve applications from any branch
        if (admin.getRole().getRoleName() == AppRole.ROLE_ADMIN) {
            if (!admin.getBranch().getId().equals(app.getBranch().getId())) {
                throw new RuntimeException("Unauthorized branch access");
            }
        }
        // Check if user with same mobile already exists
        if (userRepo.existsByMobile(app.getMobile())) {
            throw new RuntimeException("A user with mobile number " + app.getMobile() + " already exists");
        }
        
        String tempPassword = "UTB@" + (int)(Math.random()*9000 + 1000);
        
        
        
        User user = userRepo.findByEmail(app.getEmail())
                .orElseGet(() -> {
                    User u = new User();
                    u.setEmail(app.getEmail());
                    u.setMobile(app.getMobile());
                    u.setActive(true);
                    u.setRole(
                        roleRepo.findByRoleName(AppRole.ROLE_USER)
                                .orElseThrow());
                    u.setBranch(app.getBranch());
                    u.setPassword(passwordEncoder.encode(tempPassword));
                    return userRepo.save(u);
                });

        if (accountRepo.existsByUser_Id(user.getId()))
            throw new RuntimeException("User already has an account");

        CustomerProfile profile =
                customerProfileRepo.findByUser_Id(user.getId())
                        .orElseGet(() -> {
                            CustomerProfile cp = new CustomerProfile();
                            cp.setUser(user);
                            cp.setFullName(app.getFullName());
                            cp.setFatherName(app.getFatherName());
                            cp.setDob(app.getDob());
                            cp.setAddress(app.getAddress());
                            cp.setAadhaar(app.getAadhaar());
                            cp.setPan(app.getPan());
                            return cp;
                        });

        profile.setProfileImagePath(app.getProfileImagePath());
        profile.setAadhaarImagePath(app.getAadhaarImagePath());
        profile.setPanImagePath(app.getPanImagePath());
        profile.setAadhaarVerified(false);
        profile.setPanVerified(false);
        profile.setBranchVisitRequired(false);

        customerProfileRepo.save(profile);

        String accountNumber =
                accountSequenceService.generateAccountNumber(
                        app.getBranch().getId());

        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setBalance(BigDecimal.ZERO);
        account.setAccountType(app.getAccountType()); 
        account.setStatus(AccountStatus.PARTIAL_KYC_PENDING); 
        account.setOpenedAt(LocalDateTime.now());
        account.setUser(user);
        account.setBranch(app.getBranch());

        accountRepo.save(account);

        app.setStatus(ApplicationStatus.APPROVED);
        app.setDecisionAt(LocalDateTime.now());
        app.setDecidedBy(admin);

        appRepo.save(app);
        emaiService.send(
                user.getEmail(),
                "UnityTrust Bank – Account Opened Successfully",
                "Dear " + app.getFullName() + ",\n\n" +
                "Congratulations! Your bank account has been successfully opened.\n\n" +
                "Account Number: " + accountNumber + "\n" +
                "IFSC Code: UTBK0001\n" +
                "Customer ID: " + user.getId() + "\n\n" +
                "Account ID: " + account.getId() + "\n\n" +
                "Temporary Password: " + tempPassword + "\n" +
                "Reset Password Link:\n" +
                "http://localhost:3000/reset-password\n\n" +
                "Regards,\nUnityTrust Bank"
                );
    }

    // ================= REJECT =================
    @Override
    @Transactional
    public void reject(Long applicationId, String reason) {

        if (reason == null || reason.isBlank())
            throw new RuntimeException("Rejection reason required");

        User admin = getCurrentAdmin();

        AccountApplication app = appRepo.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (app.getStatus() != ApplicationStatus.SUBMITTED)
            throw new RuntimeException("Application already processed");

        // Admin can only reject applications from their own branch
        // Manager can reject applications from any branch
        if (admin.getRole().getRoleName() == AppRole.ROLE_ADMIN) {
            if (!admin.getBranch().getId().equals(app.getBranch().getId())) {
                throw new RuntimeException("Unauthorized branch access");
            }
        }

        app.setStatus(ApplicationStatus.REJECTED);
        app.setRejectionReason(reason);
        app.setDecisionAt(LocalDateTime.now());
        app.setDecidedBy(admin);

        appRepo.save(app);
        emaiService.send(
        	    app.getEmail(),
        	    "UnityTrust Bank – Application Rejected",
        	    "Dear " + app.getFullName() + ",\n\n" +
        	    "We regret to inform you that your application has been rejected.\n\n" +
        	    "Reason:\n" + reason + "\n\n" +
        	    "You may reapply after correcting the issue.\n\n" +
        	    "Regards,\nUnityTrust Bank"
        	);

    }

    // ================= HELPERS =================
    private User getCurrentAdmin() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole().getRoleName() != AppRole.ROLE_ADMIN && user.getRole().getRoleName() != AppRole.ROLE_MANAGER)
            throw new RuntimeException("Admin access required");

        return user;
    }


    public AccountApplicationResponseDto toDto(AccountApplication a) {
        return new AccountApplicationResponseDto(
                a.getId(),
                a.getFullName(),
                a.getEmail(),
                a.getMobile(),
                a.getAadhaar(),
                a.getPan(),
                a.getAddress(),
                a.getAccountType(),
                a.getProfileImagePath(),
                a.getAadhaarImagePath(),
                a.getPanImagePath(),
                a.getStatus(),
                a.getAppliedAt(),
                a.getDecisionAt());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountApplicationResponseDto> pendingForManager() {

        User admin = getCurrentAdmin();

        return appRepo
                .findByBranch_IdAndStatus(
                        admin.getBranch().getId(),
                        ApplicationStatus.SUBMITTED)
                .stream()
                .map(this::toDto)
                .toList();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AccountApplicationResponseDto> pendingForBranch(Long branchId) {

        return appRepo
                .findByBranch_IdAndStatus(
                        branchId,
                        ApplicationStatus.SUBMITTED)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public AccountApplicationResponseDto getApplicationById(Long id) {
        User currentUser = getCurrentAdmin();
        
        AccountApplication app = appRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        
        // Admin can only access applications from their own branch
        // Manager can access all applications
        if (currentUser.getRole().getRoleName() == AppRole.ROLE_ADMIN) {
            if (!currentUser.getBranch().getId().equals(app.getBranch().getId())) {
                throw new RuntimeException("Unauthorized access to this application");
            }
        }
        
        return toDto(app);
    }
    @Override
    @Transactional(readOnly = true)
    public List<AccountApplicationResponseDto> getAllApplicationsForManager() {

        User currentUser = getCurrentAdmin();

        // Manager → All branches
        if (currentUser.getRole().getRoleName() == AppRole.ROLE_MANAGER) {

            return appRepo.findAll(
                    Sort.by(Sort.Direction.DESC, "appliedAt")
            )
            .stream()
            .map(this::toDto)
            .collect(Collectors.toList());

        } 
        // Admin → Own branch only
        else {

            return appRepo
                    .findByBranch_IdOrderByAppliedAtDesc(
                            currentUser.getBranch().getId()
                    )
                    .stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
        }
    }


    @Override
    @Transactional(readOnly = true)
    public List<AccountApplicationResponseDto> getAllApplicationsForBranch(Long branchId) {

        return appRepo
                .findByBranch_IdOrderByAppliedAtDesc(branchId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
