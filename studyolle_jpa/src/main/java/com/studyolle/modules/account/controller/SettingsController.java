package com.studyolle.modules.account.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyolle.modules.account.service.AccountService;
import com.studyolle.modules.account.security.CurrentAccount;
import com.studyolle.modules.account.form.NicknameForm;
import com.studyolle.modules.account.form.Notifications;
import com.studyolle.modules.account.form.PasswordForm;
import com.studyolle.modules.account.form.Profile;
import com.studyolle.entity.Account;
import com.studyolle.entity.Tag;
import com.studyolle.entity.Zone;
import com.studyolle.modules.account.validator.NicknameValidator;
import com.studyolle.modules.account.validator.PasswordFormValidator;
import com.studyolle.modules.tag.form.TagForm;
import com.studyolle.modules.tag.repository.TagRepository;
import com.studyolle.modules.tag.service.TagService;
import com.studyolle.modules.zone.form.ZoneForm;
import com.studyolle.modules.zone.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.studyolle.modules.account.controller.SettingsController.ROOT;
import static com.studyolle.modules.account.controller.SettingsController.SETTINGS;

@Controller
@RequestMapping(ROOT + SETTINGS)
@RequiredArgsConstructor
public class SettingsController {
    static final String ROOT = "/";
    static final String SETTINGS = "settings";
    static final String PROFILE = "/profile";
    static final String PASSWORD = "/password";
    static final String NOTIFICATIONS = "/notifications";
    static final String ACCOUNT = "/account";
    static final String TAGS = "/tags";
    static final String ZONES = "/zones";

    private final AccountService accountService;
    private final ModelMapper modelMapper;
    private final NicknameValidator nicknameValidator;
    private final TagService tagService;
    private final TagRepository tagRepository;
    private final ZoneRepository zoneRepository;
    private final ObjectMapper objectMapper;

    /**
     * PasswordFormValidator
     * @param webDataBinder
     */
    @InitBinder("passwordForm")
    public void passwordFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(new PasswordFormValidator());
    }

    /**
     * NicknameValidator
     * @param webDataBinder
     */
    @InitBinder("nicknameForm")
    public void nicknameFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(nicknameValidator);
    }

    /**
     * ????????? ?????? ??????
     * @param account
     * @param model
     * @return
     */
    @GetMapping(PROFILE)
    public String updateProfileForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, Profile.class));

        return SETTINGS + PROFILE;
    }

    /**
     * ????????? ??????
     * @param account
     * @param profile
     * @param errors
     * @param model
     * @param attributes
     * @return
     */
    @PostMapping(PROFILE)
    public String updateProfile(@CurrentAccount Account account, @Valid Profile profile, Errors errors,
                                Model model, RedirectAttributes attributes) {
        /* error check */
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return SETTINGS + PROFILE;
        }

        /* ????????? ?????? */
        accountService.updateProfile(account, profile);

        attributes.addFlashAttribute("message", "???????????? ??????????????????.");
        return "redirect:/" + SETTINGS + PROFILE;
    }

    /**
     * ???????????? ?????? ??????
     * @param account
     * @param model
     * @return
     */
    @GetMapping(PASSWORD)
    public String updatePasswordForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(new PasswordForm());

        return SETTINGS + PASSWORD;
    }

    /**
     * ???????????? ??????
     * @param account
     * @param passwordForm
     * @param errors
     * @param model
     * @param attributes
     * @return
     */
    @PostMapping(PASSWORD)
    public String updatePassword(@CurrentAccount Account account, @Valid PasswordForm passwordForm, Errors errors,
                                 Model model, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return SETTINGS + PASSWORD;
        }

        /* ???????????? ?????? */
        accountService.updatePassword(account, passwordForm.getNewPassword());

        attributes.addFlashAttribute("message", "??????????????? ??????????????????.");
        return "redirect:/" + SETTINGS + PASSWORD;
    }

    /**
     * ?????? ?????? ??????
     * @param account
     * @param model
     * @return
     */
    @GetMapping(NOTIFICATIONS)
    public String updateNotificationsForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, Notifications.class));

        return SETTINGS + NOTIFICATIONS;
    }

    /**
     * ?????? ????????????
     * @param account
     * @param notifications
     * @param errors
     * @param model
     * @param attributes
     * @return
     */
    @PostMapping(NOTIFICATIONS)
    public String updateNotifications(@CurrentAccount Account account, @Valid Notifications notifications, Errors errors,
                                      Model model, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return SETTINGS + NOTIFICATIONS;
        }

        /* ?????? ?????? ???????????? */
        accountService.updateNotifications(account, notifications);

        attributes.addFlashAttribute("message", "?????? ????????? ??????????????????.");
        return "redirect:/" + SETTINGS + NOTIFICATIONS;
    }

    /**
     * ?????? ?????? ??????
     * @param account
     * @param model
     * @return
     * @throws JsonProcessingException
     */
    @GetMapping(TAGS)
    public String updateTags(@CurrentAccount Account account, Model model) throws JsonProcessingException {
        model.addAttribute(account);

        /* tag ??? title String List */
        Set<Tag> tags = accountService.getTags(account);
        model.addAttribute("tags", tags.stream().map(Tag::getTitle).collect(Collectors.toList()));

        /* selectBox ???????????? ?????? allTags */
        List<String> allTags = tagRepository.findAll().stream().map(Tag::getTitle).collect(Collectors.toList());
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allTags));

        return SETTINGS + TAGS;
    }

    /**
     * ?????? ??????
     * @param account
     * @param tagForm
     * @return
     */
    @PostMapping(TAGS + "/add")
    @ResponseBody
    public ResponseEntity addTag(@CurrentAccount Account account, @RequestBody TagForm tagForm) {
        /* ?????? ?????? ?????? ??? ?????? */
        Tag tag = tagService.findOrCreateNew(tagForm.getTagTitle());

        /* ????????? ?????? ?????? */
        accountService.addTag(account, tag);

        return ResponseEntity.ok().build();
    }

    /**
     * ?????? ??????
     * @param account
     * @param tagForm
     * @return
     */
    @PostMapping(TAGS + "/remove")
    @ResponseBody
    public ResponseEntity removeTag(@CurrentAccount Account account, @RequestBody TagForm tagForm) {
        /* ?????? ?????? */
        String title = tagForm.getTagTitle();
        Tag tag = tagRepository.findByTitle(title);

        if (tag == null) {
            return ResponseEntity.badRequest().build();
        }

        /* ?????? ?????? */
        accountService.removeTag(account, tag);

        return ResponseEntity.ok().build();
    }

    /**
     * ???????????? ?????? ??????
     * @param account
     * @param model
     * @return
     * @throws JsonProcessingException
     */
    @GetMapping(ZONES)
    public String updateZonesForm(@CurrentAccount Account account, Model model) throws JsonProcessingException {
        model.addAttribute(account);

        /* ????????? ???????????? ?????? */
        Set<Zone> zones = accountService.getZones(account);
        model.addAttribute("zones", zones.stream().map(Zone::toString).collect(Collectors.toList()));

        /* ?????? ?????? ????????? ?????? */
        List<String> allZones = zoneRepository.findAll().stream().map(Zone::toString).collect(Collectors.toList());
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allZones));

        return SETTINGS + ZONES;
    }

    /**
     * ?????? ??????
     * @param account
     * @param zoneForm
     * @return
     */
    @PostMapping(ZONES + "/add")
    @ResponseBody
    public ResponseEntity addZone(@CurrentAccount Account account, @RequestBody ZoneForm zoneForm) {
        /* ???????????? ?????? ?????? */
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());

        if (zone == null) {
            return ResponseEntity.badRequest().build();
        }

        /* ?????? ?????? */
        accountService.addZone(account, zone);

        return ResponseEntity.ok().build();
    }

    /**
     * ?????? ??????
     * @param account
     * @param zoneForm
     * @return
     */
    @PostMapping(ZONES + "/remove")
    @ResponseBody
    public ResponseEntity removeZone(@CurrentAccount Account account, @RequestBody ZoneForm zoneForm) {
        /* ???????????? ?????? ?????? */
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());

        if (zone == null) {
            return ResponseEntity.badRequest().build();
        }

        /* ?????? ?????? */
        accountService.removeZone(account, zone);

        return ResponseEntity.ok().build();
    }

    /**
     * ?????? ?????? ?????? ??????
     * @param account
     * @param model
     * @return
     */
    @GetMapping(ACCOUNT)
    public String updateAccountForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, NicknameForm.class));

        return SETTINGS + ACCOUNT;
    }

    /**
     * ?????? ?????? ??????
     * @param account
     * @param nicknameForm
     * @param errors
     * @param model
     * @param attributes
     * @return
     */
    @PostMapping(ACCOUNT)
    public String updateAccount(@CurrentAccount Account account, @Valid NicknameForm nicknameForm, Errors errors,
                                Model model, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return SETTINGS + ACCOUNT;
        }

        /* ????????? ???????????? */
        accountService.updateNickname(account, nicknameForm.getNickname());

        attributes.addFlashAttribute("message", "???????????? ??????????????????.");

        return "redirect:/" + SETTINGS + ACCOUNT;
    }

}
