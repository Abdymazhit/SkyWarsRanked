package net.Abdymazhit.SkyWarsRanked.game;

import net.Abdymazhit.SkyWarsRanked.Config;
import net.Abdymazhit.SkyWarsRanked.SkyWarsRanked;
import net.Abdymazhit.SkyWarsRanked.customs.Island;
import net.Abdymazhit.SkyWarsRanked.enums.GameStage;
import net.Abdymazhit.SkyWarsRanked.kits.Kit;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Менеджер {@link GameStage стадии игры}, отвечает за изменение {@link GameStage стадии игры}
 *
 * @version   13.08.2021
 * @author    Islam Abdymazhit
 */
public class GameStageManager extends GameEventsManager {

    /** Время до начала игры */
    private static final int TIME_BEFORE_STARTING_GAME = 15;

    /** Время до завершения игры */
    private static final int TIME_BEFORE_ENDING_GAME = 15;

    /**
     * Начинает {@link GameStage стадию игры} WAITING
     */
    private void startWaitingStage() {
        // Установить стадию игры на WAITING
        SkyWarsRanked.getGameManager().setGameStage(GameStage.WAITING);

        // Установить статус scoreboard'а лобби на WAITING
        SkyWarsRanked.getGameManager().getLobbyBoard().setWaitingStatus();
    }

    /**
     * Попытается начать {@link GameStage стадию игры} STARTING
     */
    public void tryStartStartingStage() {
        // Начать стадию STARTING, если набрано достаточное количество игроков
        if (SkyWarsRanked.getGameManager().getPlayers().size() == 2) {
            startStartingStage();
        }
    }

    /**
     * Начинает {@link GameStage стадию игры} STARTING
     */
    private void startStartingStage() {
        // Установить стадию игры на STARTING
        SkyWarsRanked.getGameManager().setGameStage(GameStage.STARTING);

        // Начать обратный отсчет начала игры
        task = new BukkitRunnable() {
            int time = TIME_BEFORE_STARTING_GAME; // Время до начала игры

            @Override
            public void run() {
                // Проверить, не вышли ли игроки с игры
                if (SkyWarsRanked.getGameManager().getPlayers().size() < 2) {
                    // Установить игру на WAITING, так как игроки вышли из игры
                    startWaitingStage();
                    cancel();
                } else {
                    // Изменить таймер начала игры в scoreboard'е лобби
                    SkyWarsRanked.getGameManager().getLobbyBoard().setStartingStatus("До начала: §a" + timeToString(time));

                    // Начать стадию игры GAME
                    if (time-- <= 0) {
                        startGameStage();
                        cancel();
                    }
                }
            }
        }.runTaskTimer(SkyWarsRanked.getInstance(), 0L, 20L);
    }

    /**
     * Начинает {@link GameStage стадию игры} GAME
     */
    private void startGameStage() {
        // Установить стадию игры на GAME
        SkyWarsRanked.getGameManager().setGameStage(GameStage.GAME);

        // Установить для игрока остров, если игрок не выбрал остров
        for(Player player : SkyWarsRanked.getGameManager().getPlayers()) {
            boolean hasIsland = false;

            for(Island island : Config.islands) {
                if(!hasIsland) {
                    if(island.getPlayer() != null) {
                        if(island.getPlayer().equals(player)) {
                            hasIsland = true;
                        }
                    }
                }
            }

            if(!hasIsland) {
                boolean islandSelected = false;

                for (Island island : Config.islands) {
                    if (!islandSelected) {
                        if (island.getPlayer() == null) {
                            island.setPlayer(player);
                            islandSelected = true;
                        }
                    }
                }
            }
        }

        // Перекинуть игроков в игру
        for(Island island : Config.islands) {
            if(island.getPlayer() != null) {
                Player player = island.getPlayer();

                // Очистить инвентарь игрока
                player.getInventory().setHelmet(null);
                player.getInventory().setChestplate(null);
                player.getInventory().setLeggings(null);
                player.getInventory().setBoots(null);
                player.getInventory().clear();

                // Установить игровой режим на выживание
                player.setGameMode(GameMode.SURVIVAL);

                // Установить игрокам scoreboard игры
                SkyWarsRanked.getGameManager().getGameBoard().setScoreboard(player);

                // Установить игроку количество его убийств в scoreboard'е игры
                SkyWarsRanked.getGameManager().getGameBoard().updateKillsCount(player);

                // Телепортировать игрока в спавн острова
                player.teleport(island.getSpawn());

                // Выдать игроку набор
                Kit.equip(player);
            }
        }

        // Перекинуть зрителей в игру
        for(Player player : SkyWarsRanked.getGameManager().getSpectators()) {
            // Установить зрителям scoreboard игры
            SkyWarsRanked.getGameManager().getGameBoard().setScoreboard(player);

            // Установить зрителям количество их убийств в scoreboard'е игры
            SkyWarsRanked.getGameManager().getGameBoard().updateKillsCount(player);
        }

        // Обновить меню телепортации к игрокам
        SkyWarsRanked.getGameManager().getGameItems().getTeleportMenu().update();

        // Обновить количество живых игроков в scoreboard'е игры
        SkyWarsRanked.getGameManager().getGameBoard().updateLivePlayersCount();

        // Обновить количество зрителей в scoreboard'е игры
        SkyWarsRanked.getGameManager().getGameBoard().updateSpectatorsCount();

        // Установить зону
        WorldBorder worldBorder = Config.world.getWorldBorder();
        worldBorder.setCenter(Config.mysteryChest);
        worldBorder.setSize(300);

        // Заполнить сундуки лутом
        SkyWarsRanked.getGameManager().getChestManager().refillIslandChests();
        SkyWarsRanked.getGameManager().getChestManager().refillBasicChests();
        SkyWarsRanked.getGameManager().getChestManager().refillMiddleChests();

        // Начать игровое событие начала битвы
        startBattleStartEvent();
    }

    /**
     * Начинает {@link GameStage стадию игры} ENDING
     */
    public void startEndingStage() {
        // Отменить таймер предыдущего события
        task.cancel();

        // Установить стадию игры на ENDING
        SkyWarsRanked.getGameManager().setGameStage(GameStage.ENDING);

        // Начать обратный отсчет конца игры
        task = new BukkitRunnable() {
            int time = TIME_BEFORE_ENDING_GAME; // Время до завершения игры

            @Override
            public void run() {
                // Обновить таймер конца игры в scoreboard'е игры
                SkyWarsRanked.getGameManager().getGameBoard().updateEvent("Конец игры " + timeToString(time));

                // Завершить игру
                if (time-- <= 0) {
                    for(Player player : Bukkit.getOnlinePlayers()) {
                        player.kickPlayer("Игра завершена");
                    }

                    Bukkit.getServer().unloadWorld(Config.world, true);
                    Bukkit.shutdown();

                    cancel();
                }
            }
        }.runTaskTimer(SkyWarsRanked.getInstance(), 0L, 20L);
    }

    /**
     * Получает время до заполнения сундуков
     * @return Время до заполнения сундуков
     */
    public int getTimeBeforeRefillingChests() {
        return timeBeforeRefillingChests;
    }
}