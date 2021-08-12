package net.Abdymazhit.SkyWarsRanked.game.events;

import net.Abdymazhit.SkyWarsRanked.Config;
import net.Abdymazhit.SkyWarsRanked.SkyWarsRanked;
import net.Abdymazhit.SkyWarsRanked.enums.GameStage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Отвечает за событие взаимодействия игрока с объектом
 *
 * @version   12.08.2021
 * @author    Islam Abdymazhit
 */
public class PlayerInteractListener implements Listener {

    /**
     * Событие взаимодействия игрока с объектом
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // Проверка, является ли игрок зрителем
        if(SkyWarsRanked.getGameManager().getSpectators().contains(player)) {
            // Проверка клика на правый
            if(event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) {
                return;
            }

            // Проверка существует ли предмет
            if(event.getItem() == null) {
                return;
            }

            // Использовать предмет игроку
            SkyWarsRanked.getGameManager().getGameItems().useItem(player, event.getItem());
        }
        // Проверка стадии игры на WAITING или STARTING
        else if(SkyWarsRanked.getGameManager().getGameStage().equals(GameStage.WAITING) || SkyWarsRanked.getGameManager().getGameStage().equals(GameStage.STARTING)) {
            // Отменить событие, чтобы игрок не мог взаимодействовать с блоками в острове лобби
            event.setCancelled(true);

            // Проверка клика на правый
            if(event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) {
                return;
            }

            // Проверка существует ли предмет
            if(event.getItem() == null) {
                return;
            }

            // Использовать предмет игроку
            SkyWarsRanked.getGameManager().getGameItems().useItem(player, event.getItem());
        }
        // Проверка стадии игры на GAME
        else if(SkyWarsRanked.getGameManager().getGameStage().equals(GameStage.GAME)) {
            // Проверка клика на правый
            if(event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) {
                return;
            }

            // Проверка на взаимодействие с эндер сундуком
            if (event.hasBlock() && event.getClickedBlock().getTypeId() == Material.ENDER_CHEST.getId()) {
                event.setCancelled(true);

                // Проверка на взаимодействие с мистическим сундуком
                if (Config.mysteryChest.equals(event.getClickedBlock().getLocation())) {
                    // Отправить сообщение, что мистический сундук закрыт
                    if (!SkyWarsRanked.getGameManager().getChestManager().getMysteryChestManager().isOpen()) {
                        player.sendMessage("§cСундук в данный момент закрыт. Приходите позже.");
                    }
                    // Открыть мистический сундук игроку
                    else {
                        player.openInventory(SkyWarsRanked.getGameManager().getChestManager().getMysteryChestManager().getInventory());
                    }
                }
            }
        }
    }
}