package service;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import io.github.rysefoxx.PlayLegendPermission;
import io.github.rysefoxx.manager.LanguageManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LanguageManagerTest {

    private LanguageManager languageService;
    private PlayerMock player;

    @BeforeEach
    public void setUp() {
        ServerMock server = MockBukkit.mock();
        PlayLegendPermission plugin = MockBukkit.load(PlayLegendPermission.class);
        this.player = server.addPlayer();
        this.languageService = plugin.getLanguageManager();
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    /**
     * Tests that the message is sent correctly and in the correct language.
     */
    @Test
    public void sendTranslatedMessage() {
        this.languageService.sendTranslatedMessage(this.player, "group_weight_not_integer");
        this.player.assertSaid("The weight must be an integer.");
    }

    /**
     * Tests that the player gets the key back if it does not exist.
     */
    @Test
    public void sendTranslatedMessageWithKeyReturn() {
        this.languageService.sendTranslatedMessage(this.player, "this_key_does_not_exist");
        this.player.assertSaid("this_key_does_not_exist");
    }
}