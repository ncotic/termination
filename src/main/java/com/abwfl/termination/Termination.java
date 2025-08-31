package com.abwfl.termination;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Mod(Termination.MODID)
public class Termination {
    public static TerminationList terminationList;

    public static final File TERMINATION_FILE = new File("termination.json");

    public static final String MODID = "termination";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Termination() {
        terminationList = new TerminationList(TERMINATION_FILE);

        MinecraftForge.EVENT_BUS.register(TerminationHandler.class);

        MinecraftForge.EVENT_BUS.register(this);
    }

    public record TerminationListEntry(String player, List<String> terminatedEntities) {}

    public static class TerminationList {
        private final File file;
        private final Gson gson;
        private List<TerminationListEntry> entries;

        public TerminationList(File file) {
            this.file = file;
            this.gson = new GsonBuilder().setPrettyPrinting().create();
            this.entries = new ArrayList<>();
            load();
        }

        public void load() {
            if (!file.exists()) {
                save();
                return;
            }

            try (FileReader reader = new FileReader(file)) {
                Type listType = new TypeToken<List<TerminationListEntry>>(){}.getType();
                List<TerminationListEntry> loaded = gson.fromJson(reader, listType);
                this.entries = loaded != null ? loaded : new ArrayList<>();
            } catch (IOException e) {
                LOGGER.error("Failed to load termination list!", e);
            }
        }

        public void save() {
            try {
                try (FileWriter writer = new FileWriter(file)) {
                    gson.toJson(entries, writer);
                }
            } catch (IOException e) {
                LOGGER.error("Failed to save termination list!", e);
            }
        }

        public void addEntry(String playerName, EntityType<?> entityType) {
            String entityString = Objects.requireNonNull(ForgeRegistries.ENTITY_TYPES.getKey(entityType)).toString();

            TerminationListEntry entry = entries.stream()
                    .filter(e -> e.player().equals(playerName))
                    .findFirst()
                    .orElse(null);

            if (entry == null) {
                entry = new TerminationListEntry(playerName, new ArrayList<>());
                entries.add(entry);
            }

            if (!entry.terminatedEntities().contains(entityString)) {
                entry.terminatedEntities().add(entityString);
                save();
            }
        }

        public void removeEntry(String playerName, EntityType<?> entityType) {
            String entityString = Objects.requireNonNull(ForgeRegistries.ENTITY_TYPES.getKey(entityType)).toString();

            TerminationListEntry entry = entries.stream()
                    .filter(e -> e.player().equals(playerName))
                    .findFirst()
                    .orElse(null);

            if (entry != null) {
                if (entry.terminatedEntities().contains(entityString)) {
                    entry.terminatedEntities().remove(entityString);
                    if (entry.terminatedEntities().isEmpty()) {
                        entries.remove(entry);
                    }
                    save();
                }
            }
        }

        public boolean shouldHideEntity(String playerName, EntityType<?> entityType) {
            String entityString = Objects.requireNonNull(ForgeRegistries.ENTITY_TYPES.getKey(entityType)).toString();
            return entries.stream()
                    .filter(e -> e.player().equals(playerName))
                    .anyMatch(e -> e.terminatedEntities().contains(entityString));
        }
    }
}
