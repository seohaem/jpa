package com.studyolle.modules.account.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyolle.infra.ContainerBaseTest;
import com.studyolle.infra.MockMvcTest;
import com.studyolle.modules.account.WithAccount;
import com.studyolle.modules.account.repository.AccountRepository;
import com.studyolle.modules.account.service.AccountService;
import com.studyolle.entity.Account;
import com.studyolle.entity.Tag;
import com.studyolle.entity.Zone;
import com.studyolle.modules.tag.form.TagForm;
import com.studyolle.modules.zone.form.ZoneForm;
import com.studyolle.modules.tag.repository.TagRepository;
import com.studyolle.modules.zone.repository.ZoneRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static com.studyolle.modules.account.controller.SettingsController.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
class SettingsControllerTest extends ContainerBaseTest {

    @Autowired MockMvc mockMvc;
    @Autowired
    AccountRepository accountRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired ObjectMapper objectMapper;
    @Autowired TagRepository tagRepository;
    @Autowired
    AccountService accountService;
    @Autowired ZoneRepository zoneRepository;

    private Zone testZone = Zone.builder().city("test").localNameOfCity("νμ€νΈμ").province("νμ€νΈμ£Ό").build();

    @BeforeEach
    void beforeEach() {
        zoneRepository.save(testZone);
    }

    @AfterEach
    void afterEach() {
        accountRepository.deleteAll();
        zoneRepository.deleteAll();
    }

    @WithAccount("keesun")
    @DisplayName("κ³μ μ μ§μ­ μ λ³΄ μμ  νΌ")
    @Test
    void updateZonesForm() throws Exception {
        mockMvc.perform(get(ROOT + SETTINGS + ZONES))
                .andExpect(view().name(SETTINGS + ZONES))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(model().attributeExists("zones"));
    }

    @WithAccount("keesun")
    @DisplayName("κ³μ μ μ§μ­ μ λ³΄ μΆκ°")
    @Test
    void addZone() throws Exception {
        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());

        mockMvc.perform(post(ROOT + SETTINGS + ZONES + "/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(zoneForm))
                .with(csrf()))
                .andExpect(status().isOk());

        Account keesun = accountRepository.findByNickname("keesun");
        Zone zone = zoneRepository.findByCityAndProvince(testZone.getCity(), testZone.getProvince());
        assertTrue(keesun.getZones().contains(zone));
    }

    @WithAccount("keesun")
    @DisplayName("κ³μ μ μ§μ­ μ λ³΄ μΆκ°")
    @Test
    void removeZone() throws Exception {
        Account keesun = accountRepository.findByNickname("keesun");
        Zone zone = zoneRepository.findByCityAndProvince(testZone.getCity(), testZone.getProvince());
        accountService.addZone(keesun, zone);

        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());

        mockMvc.perform(post(ROOT + SETTINGS + ZONES + "/remove")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(zoneForm))
                .with(csrf()))
                .andExpect(status().isOk());

        assertFalse(keesun.getZones().contains(zone));
    }

    @WithAccount("keesun")
    @DisplayName("κ³μ μ νκ·Έ μμ  νΌ")
    @Test
    void updateTagsForm() throws Exception {
        mockMvc.perform(get(ROOT + SETTINGS + TAGS))
                .andExpect(view().name(SETTINGS + TAGS))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(model().attributeExists("tags"));
    }

    @WithAccount("keesun")
    @DisplayName("κ³μ μ νκ·Έ μΆκ°")
    @Test
    void addTag() throws Exception {
        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");

        mockMvc.perform(post(ROOT + SETTINGS + TAGS + "/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagForm))
                .with(csrf()))
                .andExpect(status().isOk());

        Tag newTag = tagRepository.findByTitle("newTag");
        assertNotNull(newTag);
        Account keesun = accountRepository.findByNickname("keesun");
        assertTrue(keesun.getTags().contains(newTag));
    }

    @WithAccount("keesun")
    @DisplayName("κ³μ μ νκ·Έ μ­μ ")
    @Test
    void removeTag() throws Exception {
        Account keesun = accountRepository.findByNickname("keesun");
        Tag newTag = tagRepository.save(Tag.builder().title("newTag").build());
        accountService.addTag(keesun, newTag);

        assertTrue(keesun.getTags().contains(newTag));

        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");

        mockMvc.perform(post(ROOT + SETTINGS + TAGS + "/remove")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagForm))
                .with(csrf()))
                .andExpect(status().isOk());

        assertFalse(keesun.getTags().contains(newTag));
    }

    @WithAccount("keesun")
    @DisplayName("λλ€μ μμ  νΌ")
    @Test
    void updateAccountForm() throws Exception {
        mockMvc.perform(get(ROOT + SETTINGS + ACCOUNT))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("nicknameForm"));
    }

    @WithAccount("keesun")
    @DisplayName("λλ€μ μμ νκΈ° - μλ ₯κ° μ μ")
    @Test
    void updateAccount_success() throws Exception {
        String newNickname = "whiteship";
        mockMvc.perform(post(ROOT + SETTINGS + ACCOUNT)
                .param("nickname", newNickname)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(ROOT + SETTINGS + ACCOUNT))
                .andExpect(flash().attributeExists("message"));

        assertNotNull(accountRepository.findByNickname("whiteship"));
    }

    @WithAccount("keesun")
    @DisplayName("λλ€μ μμ νκΈ° - μλ ₯κ° μλ¬")
    @Test
    void updateAccount_failure() throws Exception {
        String newNickname = "Β―\\_(γ)_/Β―";
        mockMvc.perform(post(ROOT + SETTINGS + ACCOUNT)
                .param("nickname", newNickname)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SETTINGS + ACCOUNT))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("nicknameForm"));
    }

    @WithAccount("keesun")
    @DisplayName("νλ‘ν μμ  νΌ")
    @Test
    void updateProfileForm() throws Exception {
        mockMvc.perform(get(ROOT + SETTINGS + PROFILE))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"));
    }

    @WithAccount("keesun")
    @DisplayName("νλ‘ν μμ νκΈ° - μλ ₯κ° μ μ")
    @Test
    void updateProfile() throws Exception {
        String bio = "μ§§μ μκ°λ₯Ό μμ νλ κ²½μ°.";
        mockMvc.perform(post(ROOT + SETTINGS + PROFILE)
                .param("bio", bio)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(ROOT + SETTINGS + PROFILE))
                .andExpect(flash().attributeExists("message"));

        Account keesun = accountRepository.findByNickname("keesun");
        assertEquals(bio, keesun.getBio());
    }

    @WithAccount("keesun")
    @DisplayName("νλ‘ν μμ νκΈ° - μλ ₯κ° μλ¬")
    @Test
    void updateProfile_error() throws Exception {
        String bio = "κΈΈκ² μκ°λ₯Ό μμ νλ κ²½μ°. κΈΈκ² μκ°λ₯Ό μμ νλ κ²½μ°. κΈΈκ² μκ°λ₯Ό μμ νλ κ²½μ°. λλ¬΄λλ κΈΈκ² μκ°λ₯Ό μμ νλ κ²½μ°. ";
        mockMvc.perform(post(ROOT + SETTINGS + PROFILE)
                .param("bio", bio)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SETTINGS + PROFILE))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("profile"))
                .andExpect(model().hasErrors());

        Account keesun = accountRepository.findByNickname("keesun");
        assertNull(keesun.getBio());
    }

    @WithAccount("keesun")
    @DisplayName("ν¨μ€μλ μμ  νΌ")
    @Test
    void updatePassword_form() throws Exception {
        mockMvc.perform(get(ROOT + SETTINGS + PASSWORD))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"));
    }

    @WithAccount("keesun")
    @DisplayName("ν¨μ€μλ μμ  - μλ ₯κ° μ μ")
    @Test
    void updatePassword_success() throws Exception {
        mockMvc.perform(post(ROOT + SETTINGS + PASSWORD)
                .param("newPassword", "12345678")
                .param("newPasswordConfirm", "12345678")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(ROOT + SETTINGS + PASSWORD))
                .andExpect(flash().attributeExists("message"));

        Account keesun = accountRepository.findByNickname("keesun");
        assertTrue(passwordEncoder.matches("12345678", keesun.getPassword()));
    }

    @WithAccount("keesun")
    @DisplayName("ν¨μ€μλ μμ  - μλ ₯κ° μλ¬ - ν¨μ€μλ λΆμΌμΉ")
    @Test
    void updatePassword_fail() throws Exception {
        mockMvc.perform(post(ROOT + SETTINGS + PASSWORD)
                .param("newPassword", "12345678")
                .param("newPasswordConfirm", "11111111")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SETTINGS + PASSWORD))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(model().attributeExists("account"));
    }

}