package dev.nxkorasu.nikostera.event;

import com.cobblemon.mod.common.api.events.battles.BattleFledEvent;
import com.cobblemon.mod.common.api.events.battles.BattleVictoryEvent;
import com.cobblemon.mod.common.api.events.battles.instruction.TerastallizationEvent;
import com.cobblemon.mod.common.api.events.pokemon.PokemonCapturedEvent;
import com.cobblemon.mod.common.api.events.pokemon.PokemonSentEvent;
import com.cobblemon.mod.common.api.pokemon.feature.FlagSpeciesFeature;
import com.cobblemon.mod.common.api.pokemon.feature.StringSpeciesFeature;
import com.cobblemon.mod.common.api.types.tera.TeraType;
import com.cobblemon.mod.common.api.types.tera.TeraTypes;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import kotlin.Unit;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.UUID;

public class TeraGlowHandler {
    public static Unit terrastallizationUsed(TerastallizationEvent event) {
        LivingEntity pokemon = event.getPokemon().getEffectedPokemon().getEntity();
        Pokemon pk = event.getPokemon().getEffectedPokemon();
        if (pk.getSpecies().getName().equals("Terapagos"))
            new StringSpeciesFeature("tera_form", "stellar").apply(pk);
        if (pk.getSpecies().getName().equals("Ogerpon"))
            new FlagSpeciesFeature("embody_aspect", true).apply(pk);
        if (pokemon.getWorld() instanceof ServerWorld serverLevel) {
            ServerScoreboard scoreboard = serverLevel.getScoreboard();
            String teamName = "glow_" + UUID.randomUUID().toString().substring(0, 8);
            Team team = scoreboard.getTeam(teamName);
            Formatting color = getGlowColorForTeraType(pk.getTeraType());
            if (team == null) {
                team = scoreboard.addTeam(teamName);
                team.setColor(color);
            }
            scoreboard.addScoreHolderToTeam(pokemon.getUuid().toString(), team);
            pk.setLastFlowerFed(new ItemStack(Items.BARRIER, 1));
            pokemon.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, Integer.MAX_VALUE, 0));
        }
        return Unit.INSTANCE;
    }

    public static Formatting getGlowColorForTeraType(TeraType type) {
        return switch (type.getDisplayName().getString().toLowerCase()) {
            case "fire" -> Formatting.RED;
            case "water" -> Formatting.BLUE;
            case "electric" -> Formatting.YELLOW;
            case "grass" -> Formatting.GREEN;
            case "ice", "flying" -> Formatting.AQUA;
            case "fighting" -> Formatting.DARK_RED;
            case "poison" -> Formatting.DARK_PURPLE;
            case "ground" -> Formatting.GOLD;
            case "psychic", "fairy" -> Formatting.LIGHT_PURPLE;
            case "bug" -> Formatting.DARK_GREEN;
            case "rock" -> Formatting.GRAY;
            case "ghost" -> Formatting.DARK_GRAY;
            case "dragon" -> Formatting.DARK_BLUE;
            case "dark" -> Formatting.BLACK;
            case "steel" -> Formatting.DARK_AQUA;
            default -> Formatting.WHITE;
        };
    }

    public static Unit switchIn(PokemonSentEvent.Post event) {
        Pokemon pk = event.getPokemon();
        PokemonEntity pokemonEntity = event.getPokemonEntity();
        LivingEntity pokemon = pokemonEntity;
        if (pk.getLastFlowerFed().getItem().equals(Items.BARRIER)) {
            if (pokemon.getWorld() instanceof ServerWorld serverLevel) {
                ServerScoreboard scoreboard = serverLevel.getScoreboard();
                String teamName = "glow_" + UUID.randomUUID().toString().substring(0, 8);
                Team team = scoreboard.getTeam(teamName);
                Formatting color = getGlowColorForTeraType(pk.getTeraType());
                if (team == null) {
                    team = scoreboard.addTeam(teamName);
                    team.setColor(color);
                }
                scoreboard.addScoreHolderToTeam(pokemon.getUuid().toString(), team);
                if (pk.getSpecies().getName().equalsIgnoreCase("terapagos"))
                    new StringSpeciesFeature("tera_form", "terastal").apply(pk);
                if (pk.getSpecies().getName().equalsIgnoreCase("ogerpon"))
                    new FlagSpeciesFeature("embody_aspect", false).apply(pk);
                pokemon.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, Integer.MAX_VALUE, 0));
            }
        }
        return Unit.INSTANCE;
    }

    public static Unit leaveBattle(BattleFledEvent event) {
        clearFlowers(event.getPlayer().getPokemonList());
        return Unit.INSTANCE;
    }

    public static void clearFlowers(List<BattlePokemon> team) {
        for (BattlePokemon bp : team) {
            bp.getEffectedPokemon().setLastFlowerFed(new ItemStack(Items.AIR, 1));
            if (bp.getEffectedPokemon().getSpecies().getName().equals("Terapagos"))
                new StringSpeciesFeature("tera_form", "terastal").apply(bp.getOriginalPokemon());
            if (bp.getEffectedPokemon().getSpecies().getName().equals("Ogerpon"))
                new FlagSpeciesFeature("embody_aspect", false).apply(bp.getOriginalPokemon());
        }
    }

    public static Unit winBattle(BattleVictoryEvent event) {
        clearFlowers(event.getWinners().getFirst().getPokemonList());
        clearFlowers(event.getLosers().getFirst().getPokemonList());
        return Unit.INSTANCE;
    }

    public static Unit capturedPokemon(PokemonCapturedEvent event) {
        if (event.getPokemon().getSpecies().getName().equalsIgnoreCase("ogerpon"))
            event.getPokemon().setTeraType(TeraTypes.getGRASS());
        if (event.getPokemon().getSpecies().getName().equalsIgnoreCase("terapagos"))
            event.getPokemon().setTeraType(TeraTypes.getSTELLAR());
        return Unit.INSTANCE;
    }
}
